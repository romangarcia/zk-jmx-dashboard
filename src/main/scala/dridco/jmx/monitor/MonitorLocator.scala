package dridco.jmx.monitor

object MonitorLocators {
    val DEFAULT = new MonitorLocators(Seq[MonitorLocator](
            new ZookeeperMonitorLocator(), 
            new KafkaMonitorLocator(),
            new SenseiMonitorLocator(),
            new FrontendMonitorLocator()
        ))
    
    def uniqueMonitorId(name:String, conn:MonitorConnector) = {
        val auth = conn.connectionSpec.credentials
        val posfix = if (auth.isDefined) "@" + auth.get.username else ""
        name + posfix + "@" + conn.connectionSpec.url
    }
}
class MonitorLocators(locators:Seq[MonitorLocator]) {
    def listAvailable(conn: MonitorConnector) = locators.map( _.locateMonitors(conn) ) flatten
}

trait MonitorLocator {
    def locateMonitors(conn: MonitorConnector): Iterable[JmxMonitor]
}