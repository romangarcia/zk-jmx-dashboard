package dridco.jmx.status

import dridco.jmx.monitor.MonitorConnectionSpec

class SenseiStatus extends StatusReporter {

	val SENSEI_SERVER_OBJECT_NAME_PREFIX = "com.senseidb:name="
    val DATA_PROVIDER_OBJECT_NAME = "com.senseidb:indexing-manager=stream-data-provider"
        
    val SENSEI_AVAILABLE = "senseiAvailable"
    val DATA_PROVIDER_STATUS = "dataProviderStatus"
    val DATA_PROVIDER_EVENT_COUNT = "dataProviderEventCount"
        
    def report(connSpec:MonitorConnectionSpec):Map[String, Any] = {

        withConnection(connSpec) { conn => 
            val nodes = conn.lookupObjectNameKeys(SENSEI_SERVER_OBJECT_NAME_PREFIX + "*")
            
            val nodeName = nodes.get("name").get
            
    		val senseiAvailable = conn.getAttributeValue[Boolean](SENSEI_SERVER_OBJECT_NAME_PREFIX + nodeName, "Available")
    		var dataProviderStatus = conn.getAttributeValue[String](DATA_PROVIDER_OBJECT_NAME, "Status")
    		dataProviderStatus = dataProviderStatus substring( dataProviderStatus.indexOf(" : ") + 3 )
            val dataProviderEventCount = conn.getAttributeValue[Long](DATA_PROVIDER_OBJECT_NAME, "EventCount")
            
            Map(SENSEI_AVAILABLE -> senseiAvailable,
	    		DATA_PROVIDER_STATUS -> dataProviderStatus, 
	    		DATA_PROVIDER_EVENT_COUNT -> dataProviderEventCount
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
	
	def reportResult(connSpec:MonitorConnectionSpec):StatusResult = {

	    checkResult(connSpec) { values =>
	    	val msg = new StringBuilder
	    
		    val isAvailable = values.getOrElse(SENSEI_AVAILABLE, false) == true
		    if (!isAvailable) msg append "Sensei is not AVAILABLE\n"
	
		    var isRunning = false
		    val dataProvStatusOpt = values.get(DATA_PROVIDER_STATUS)
		    if (dataProvStatusOpt.isDefined) {
		        isRunning = checkRunning(dataProvStatusOpt.get.toString)
		    }
		    if (!isRunning) msg append "Sensei Data Provider is NOT RUNNING"
		    
		    (isAvailable && isRunning, msg.toString)
	    }
	    
	}
    
}