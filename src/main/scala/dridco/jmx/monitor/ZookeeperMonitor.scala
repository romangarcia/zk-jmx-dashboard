package dridco.jmx.monitor

import ZookeeperMonitor._
class ZookeeperMonitorLocator extends MonitorLocator {
    
    def locateMonitors(conn:MonitorConnector): Iterable[JmxMonitor] = {
        val keysMap = conn.lookupObjectNameKeys(ZOO_DOMAIN_NAME + ":*")
        
        val nameOpt = keysMap.get("name0")
        nameOpt.map { name =>
            val id = MonitorLocators.uniqueMonitorId(name, conn)
            new ZookeeperMonitor(id, name, conn)
        }
    }
}

object ZookeeperMonitor {
	val ZOO_DOMAIN_NAME = "org.apache.ZooKeeperService"
}

class ZookeeperMonitor(id:String, nodeName:String, val connector:MonitorConnector) extends JmxMonitor(id) with JmxAccess {
    
	val name:String = "Zookeeper"
    
	val zookeeperNodeName = ZOO_DOMAIN_NAME + ":name0=" + nodeName
    
	
	def available: Boolean = safeAvailable(connector)
    def alarms = Seq[Alarm]()
    def information = 
        if (nodeName.startsWith("Replicated")) {
            // Replicated Mode
            Map(
            "Available" -> available,
            "Node Name" -> connector.getAttributeValue(zookeeperNodeName, "Name"),
            "Quorum Size" -> connector.getAttributeValue(zookeeperNodeName, "QuorumSize")
    		)
        } else {
            // Development Mode
            Map(
	            "Available" -> available,
	            "Client Port" -> connector.getAttributeValue(zookeeperNodeName, "ClientPort"),
	            "Version" -> connector.getAttributeValue(zookeeperNodeName, "Version"),
	            "Start Time" -> connector.getAttributeValue(zookeeperNodeName, "StartTime")
    		)
        }

}