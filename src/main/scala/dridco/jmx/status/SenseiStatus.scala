package dridco.jmx.status

import dridco.jmx.monitor.MonitorConnectionSpec

class SenseiStatus extends StatusReporter {

	val SENSEI_SERVER_OBJECT_NAME = "com.senseidb:name=sensei-server-1"
    val DATA_PROVIDER_OBJECT_NAME = "com.senseidb:indexing-manager=stream-data-provider"
        
    val SENSEI_AVAILABLE = "senseiAvailable"
    val DATA_PROVIDER_STATUS = "dataProviderStatus"
    val DATA_PROVIDER_EVENT_COUNT = "dataProviderEventCount"
        
    def report(connSpec:MonitorConnectionSpec):Map[String, Any] = {

        withConnection(connSpec) { conn => 
        	val senseiAvailable = conn.getAttributeValue[Boolean](SENSEI_SERVER_OBJECT_NAME, "Available")
        	var dataProviderStatus = conn.getAttributeValue[String](DATA_PROVIDER_OBJECT_NAME, "Status")
        	dataProviderStatus = dataProviderStatus.substring( 0, dataProviderStatus.indexOf(" :") )
        	val dataProviderEventCount = conn.getAttributeValue[Long](DATA_PROVIDER_OBJECT_NAME, "EventCount")
        	
        	Map(SENSEI_AVAILABLE -> senseiAvailable,
    	        DATA_PROVIDER_STATUS -> dataProviderStatus, 
    	        DATA_PROVIDER_EVENT_COUNT -> dataProviderEventCount
    	        ) 
        }
        
    } 
	
	def reportResult(connSpec:MonitorConnectionSpec):StatusResult = {

	    checkResult(connSpec) { values =>
	    	val msg = new StringBuilder
	    
		    val isAvailable = values.getOrElse(SENSEI_AVAILABLE, false) == true
		    if (!isAvailable) msg append "Sensei is not AVAILABLE\n"
	
		    val isRunning = values.getOrElse(DATA_PROVIDER_STATUS, "") == "running"
		    if (!isRunning) msg append "Sensei Data Provider is NOT RUNNING"
		    
		    (isAvailable && isRunning, msg.toString)
	    }
	    
	}
    
}