package dridco.jmx.status
import dridco.jmx.monitor.MonitorConnectionSpec
import scala.collection.mutable.ListBuffer

class ZookeeperStatus extends StatusReporter {
    val ZOO_DOMAIN_NAME = "org.apache.ZooKeeperService"
    val DATA_PROVIDER_OBJECT_NAME = "com.senseidb:indexing-manager=stream-data-provider"
        
    val ZOOKEEPER_NODE_NAME = "zookeeperNodeName"
    val ZOOKEEPER_QUORUM_SIZE = "zookeeperQuorumSize"
    val ZOOKEEPER_NODE_STATE = "zookeeperState"
        
    def report(request:StatusRequest):Map[String, Any] = {
        
        withConnection(request.connectionSpec) { conn =>
            
		    val keysMap = conn.lookupObjectNameKeys(ZOO_DOMAIN_NAME + ":*")
		    
		    val nameOpt = keysMap.get("name0")
		    val result = nameOpt.map { name =>
		        val objName = ZOO_DOMAIN_NAME + ":name0=" + name
		        val id = name.substring( name.indexOf("_id" ) + 3 )
		        val replicaState = { 
	            	val idInt = id.toInt
        			val replicaName = objName + ",name1=replica." + idInt
        			conn.getAttributeValue[String](replicaName, "State")
		        }	
		        
		        val nodeName = conn.getAttributeValue[String](objName, "Name")
		        val quorumSize = conn.getAttributeValue[Int](objName, "QuorumSize")
		        
		        Map(ZOOKEEPER_NODE_NAME -> nodeName,
					ZOOKEEPER_QUORUM_SIZE -> quorumSize,
					ZOOKEEPER_NODE_STATE -> replicaState
					)
	        		
		    }
		    
		    result.getOrElse(Map())
        }
        
    } 
    
	def reportResult(request:StatusRequest):StatusResult = {

	    checkResult(request) { values =>
		    var msg = new ListBuffer[String]
		    
		    val nodeName = values.getOrElse(ZOOKEEPER_NODE_NAME, "Unknown")
		    
		    val availableValue = values.getOrElse(ZOOKEEPER_NODE_STATE, "NOT_RUNNING")
		    val isAvailable = availableValue == "RUNNABLE" || availableValue == "TIMED_WAITING"
		        
		    if (!isAvailable) msg += ("Zookeeper Node " + nodeName + " is not AVAILABLE")

		    (isAvailable, msg.toList)
	    }
	    
	}
    
}