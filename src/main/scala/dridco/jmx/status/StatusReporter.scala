package dridco.jmx.status

import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec

case class StatusResult(valid:Boolean, message:Option[String])

trait StatusReporter {

    def report(connSpec:MonitorConnectionSpec):Map[String, Any]
    
    def reportResult(connSpec:MonitorConnectionSpec):StatusResult
    
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
    
	def checkResult(connSpec:MonitorConnectionSpec)(check:Map[String, Any] => (Boolean, String)) = {
	    
	    try {
	        val values = report(connSpec)
	        val (valid, msg) = check(values)
	        val msgOpt = if (msg.isEmpty) None else Some(msg.toString)
	        StatusResult(valid, msgOpt)
	    } catch {
	        case ise:IllegalStateException => 
		        StatusResult(false, Some("Connection error: " + ise.getMessage))
	        case e => 
	        	StatusResult(false, Some("Unknown error: " + e.getMessage))
	    }
	}    
}