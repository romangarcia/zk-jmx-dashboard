package dridco.jmx.monitor

/**
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * TODO: Pending implementation!!!!
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
class FrontendMonitorLocator extends MonitorLocator {
	def locateMonitors(conn:MonitorConnector): Iterable[JmxMonitor] = {
		Seq[JmxMonitor]()
	}
}
class FrontendMonitor(id:String, val connector:MonitorConnector) extends JmxMonitor(id) {
	val name:String = "Frontend Webapp"
    def available: Boolean = true
    def alarms = Seq[Alarm]()
    def information = Map("Available" -> available)
}