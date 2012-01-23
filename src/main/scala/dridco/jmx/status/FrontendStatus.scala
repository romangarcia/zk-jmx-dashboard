package dridco.jmx.status

import dridco.jmx.monitor.MonitorConnectionSpec
import scala.collection.mutable.ListBuffer

class FrontendStatus extends StatusReporter {

    val NORBERT_CLUSTER_QUERY = "com.linkedin.norbert:type=ClusterClient,serviceName=*"
        
    val CLUSTER_CONNECTED = "clusterConnected"
    val CLUSTER_NODES = "clusterNodes"
        
    val CLUSTER_AVAILABLE_NODES_PROPERTY = "minAvailableNodes"
        
    def report(request:StatusRequest):Map[String, Any] = {

        withConnection(request.connectionSpec) { conn => 
            
            // cluster available nodes
            val clusterClients = conn.lookupObjectNames(NORBERT_CLUSTER_QUERY)
            clusterClients.flatMap { name =>
                val clusterName = conn.lookupObjectNameKeys(name).get("serviceName").get
	            val clusterConnected = conn.getAttributeValue[Boolean](name, "Connected")
	            val clusterNodes = conn.getAttributeValue[Array[String]](name, "Nodes")
                
	            Map(
	            		CLUSTER_CONNECTED + "_" + clusterName -> (clusterName, clusterConnected),
	            		CLUSTER_NODES + "_" + clusterName -> (clusterName, clusterNodes)
            		) 
            }.toMap
            
        }
        
    } 
	
	def reportResult(request:StatusRequest):StatusResult = {
		def isConnected(value:Any) = value.asInstanceOf[Boolean]
		def isAvailableNodes(min:Int, value:Any) = value.asInstanceOf[Array[String]].length >= min
	    
	    val minNodes = request.properties.getOrElse(CLUSTER_AVAILABLE_NODES_PROPERTY, "0").toInt
	    
	    checkResult(request) { values =>
	    	var msgs = new ListBuffer[String]

	    	values.foreach { entry =>
	    	    val (key, (cluster, value)) = entry
	    	    
	    	    // cluster is connected
	    	    if (key.startsWith(CLUSTER_CONNECTED) && !isConnected(value)) {
	    	        msgs += "Sensei Cluster [" + cluster + "] is NOT CONNECTED"
	    	    }
	    	        
    	        // cluster has at least N nodes reachable
    	        if (key.startsWith(CLUSTER_NODES) && !isAvailableNodes(minNodes, value)) {
    	        	msgs += "Sensei Cluster [" + cluster + "] has NODES AVAILABLE < " + minNodes
    	        }
	    	    
	    	}
	    	
            val isValid = msgs.isEmpty
		    (isValid, msgs.toList)
	    }
	    
	}
    
}