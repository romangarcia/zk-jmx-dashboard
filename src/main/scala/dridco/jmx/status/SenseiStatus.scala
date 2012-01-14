package dridco.jmx.status

import dridco.jmx.monitor.MonitorConnectionSpec
import scala.collection.mutable.ListBuffer

class SenseiStatus extends StatusReporter {

	val SENSEI_SERVER_OBJECT_NAME_PREFIX = "com.senseidb:name="
	val ZOIE_ADMIN_OBJECT_NAME_PREFIX = "com.senseidb:zoie-name="
    val DATA_PROVIDER_OBJECT_NAME = "com.senseidb:indexing-manager=stream-data-provider"
    val NORBERT_CLUSTER_OBJECT_NAME = "com.linkedin.norbert:type=ClusterClient"
        
        
    val SENSEI_AVAILABLE = "senseiAvailable"
    val DATA_PROVIDER_STATUS = "dataProviderStatus"
    val DATA_PROVIDER_EVENT_COUNT = "dataProviderEventCount"
    val CLUSTER_CONNECTED = "clusterConnected"
    val CLUSTER_NODES = "clusterNodes"
    val HEALTH_STATUS = "healthStatus"
        
    val CLUSTER_NAME_PROPERTY = "clusterName"
        
    def report(request:StatusRequest):Map[String, Any] = {

        withConnection(request.connectionSpec) { conn => 
            val nodes = conn.lookupObjectNameKeys(SENSEI_SERVER_OBJECT_NAME_PREFIX + "*")
            
            val nodeName = nodes.get("name").get

            // available
    		val senseiAvailable = conn.getAttributeValue[Boolean](SENSEI_SERVER_OBJECT_NAME_PREFIX + nodeName, "Available")
    		
    		// data provider status
    		var dataProviderStatus = conn.getAttributeValue[String](DATA_PROVIDER_OBJECT_NAME, "Status")
    		dataProviderStatus = dataProviderStatus substring( dataProviderStatus.indexOf(" : ") + 3 )
            
            // data provider events
            val dataProviderEventCount = conn.getAttributeValue[Long](DATA_PROVIDER_OBJECT_NAME, "EventCount")
            
            // cluster available nodes
    	    val clusterName = request.properties.getOrElse(CLUSTER_NAME_PROPERTY, 
	            throw new IllegalStateException("No " + CLUSTER_NAME_PROPERTY + " defined in monitor configuration!"))
            val clusterClientName = NORBERT_CLUSTER_OBJECT_NAME + ",serviceName=" + clusterName
            val clusterConnected = conn.getAttributeValue[Boolean](clusterClientName, "Connected")
            val clusterNodes = conn.getAttributeValue[Array[String]](clusterClientName, "Nodes")
            
            // zoie admin health
            val zoieNames = conn.lookupObjectNames(ZOIE_ADMIN_OBJECT_NAME_PREFIX + "*")
            val zoieName = zoieNames.filter( _.contains("zoie-admin") ).head
            
            val healthLevel = conn.getAttributeValue[Long](zoieName, "Health")
            
            Map(SENSEI_AVAILABLE -> senseiAvailable,
	    		DATA_PROVIDER_STATUS -> dataProviderStatus, 
	    		DATA_PROVIDER_EVENT_COUNT -> dataProviderEventCount,
	    		CLUSTER_CONNECTED -> clusterConnected,
	    		CLUSTER_NODES -> clusterNodes,
	    		HEALTH_STATUS -> healthLevel
    		) 
        }
        
    } 
	
	def checkRunning(stateString:String) = {
	    import Thread.State._
	    val state = Thread.State.valueOf(stateString)
	    state match {
	        case TERMINATED | NEW => false
	        case _ => true
	    }
	}
	
	def reportResult(request:StatusRequest):StatusResult = {

	    checkResult(request) { values =>
	    	var msgs = new ListBuffer[String]
	    
	    	// available
		    val isAvailable = values.getOrElse(SENSEI_AVAILABLE, false) == true
		    if (!isAvailable) msgs += "Sensei is not AVAILABLE"
	
		    // data provider running!
		    var isRunning = false
		    val dataProvStatusOpt = values.get(DATA_PROVIDER_STATUS)
		    if (dataProvStatusOpt.isDefined) {
		        isRunning = checkRunning(dataProvStatusOpt.get.toString)
		    }
		    if (!isRunning) msgs += "Sensei Data Provider is NOT RUNNING"
		    
		    // cluster is connected
		    val isConnected  = values.getOrElse(CLUSTER_CONNECTED, false) == true
		    if (!isConnected) msgs += "Sensei Cluster is NOT CONNECTED"
		    
		    // cluster has at least 1 node reachable
		    val nodesAvailable = values.getOrElse(CLUSTER_NODES, 
		            Array[String]()).asInstanceOf[Array[String]].length > 0
            if (!nodesAvailable) msgs += "Sensei Cluster has NO NODES AVAILABLE"
            
            val isHealthy = values.getOrElse(HEALTH_STATUS, 1) == 0
            if (!isHealthy) msgs += "Sensei Node is NOT HEALTHY"
		    
		    (isAvailable & isRunning & isConnected & nodesAvailable & isHealthy, msgs.toList)
	    }
	    
	}
    
}