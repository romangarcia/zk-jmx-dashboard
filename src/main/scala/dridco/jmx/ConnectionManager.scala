package dridco.jmx
import dridco.jmx.monitor.MonitorLocators
import dridco.jmx.monitor.MonitorsConfig
import scala.io.Source
import dridco.jmx.monitor.JmxMonitorConnector
import monitor.JmxMonitor

class ConnectionManager(config:MonitorsConfig) extends Logging {

    private val locators = MonitorLocators.DEFAULT
    
    def monitors():Seq[JmxMonitor] = {
        
        val monitors = 
            for (spec <- config.connectorSpecs) yield {
                try {
                	val conn = new JmxMonitorConnector(spec)    
                	locators.listAvailable(conn)
                } catch {
                	case e => warn("Couldn't attach connector while locating monitors: " + spec, e)
                	List()
                }
            	
            }
    		
        // remove option and seq/seq...REFACTOR!
    	monitors.flatten
        
    }

    def monitors(monitorIdentifiers:Seq[String]):Seq[JmxMonitor] = {
        
        val all = monitors()
        
        all.filter( m => monitorIdentifiers.contains( m.id ))
        
    }
    
    def monitors(monitorName:String):Seq[JmxMonitor] = {
        
        val monitorSpecs = config.connectorSpecs.filter( _.monitors.contains(monitorName) )
        
        val monitorConnectors = 
            for (spec <- monitorSpecs) 
            	yield new JmxMonitorConnector(spec)
        
        monitorConnectors.map( locators.listAvailable(_) ).flatten
    }
        

}