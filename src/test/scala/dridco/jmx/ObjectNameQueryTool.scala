package dridco.jmx

import System._
import dridco.jmx.monitor.JmxCredentials
import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec

object ObjectNameQueryTool extends App {

    val host = getProperty("jmx.host", "localhost")
    val port = getProperty("jmx.port", "9999").toInt
    
    val username = Option(getProperty("jmx.username"))
    val password = Option(getProperty("jmx.password"))
    val auth = (username, password) match {
        case (Some(u), Some(p)) 
        	if (u.length > 0 && p.length > 0) => Some(JmxCredentials(u, p))
        case _ => None
    }
    
    val conn = new JmxMonitorConnector(new MonitorConnectionSpec(host + ":" + port, auth, Seq()))
    
//    val result = conn.lookupObjectNames(args(0))
    val result = conn.lookupObjectNameKeys(args(0))
    
    result.foreach( println )
}