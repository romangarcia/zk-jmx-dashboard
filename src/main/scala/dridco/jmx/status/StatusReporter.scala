package dridco.jmx.status

import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec

case class StatusRequest(connectionSpec:MonitorConnectionSpec, properties:Map[String, String] = Map())

case class StatusResult(valid:Boolean, messages:List[String])

trait StatusReporter {

    def report(request:StatusRequest):Map[String, Any]
    
    def reportResult(request:StatusRequest):StatusResult
    
    def withConnection(connSpec:MonitorConnectionSpec)(f: JmxMonitorConnector => Map[String, Any]) = {
        
        var conn:JmxMonitorConnector = null
        try {
        	conn = new JmxMonitorConnector(connSpec)
            f(conn)
        } finally {
            if (conn != null) {
                conn.close()
            }
        }
    } 
    
	def checkResult(request:StatusRequest)(check:Map[String, Any] => (Boolean, List[String])) = {
	    
	    try {
	        val values = report(request)
	        val (valid, msgs) = check(values)
	        StatusResult(valid, msgs)
	    } catch {
	        case ise:IllegalStateException => 
		        StatusResult(false, List("Connection error: " + ise.getMessage))
	        case e => 
	        	StatusResult(false, List("Unknown error: " + e.getMessage))
	    }
	}    
}