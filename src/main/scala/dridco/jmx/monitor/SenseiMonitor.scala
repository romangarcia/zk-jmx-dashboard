package dridco.jmx.monitor

import SenseiMonitor._

object SenseiMonitor {
	val SENSEI_DOMAIN_NAME = "com.senseidb"
	val SENSEI_SERVERS_QUERY = SENSEI_DOMAIN_NAME + ":name=*"
	val DATA_PROVIDER_OBJECT_NAME = SENSEI_DOMAIN_NAME + ":indexing-manager=stream-data-provider"
	val ZOIE_OBJECT_NAME = SENSEI_DOMAIN_NAME + ":zoie-name=zoie-admin-1-0"
	
	def locateServers(conn:MonitorConnector) = {
	    val keysMap = conn.lookupObjectNameKeys(SENSEI_SERVERS_QUERY)
        keysMap.get("name")
	}
}

class SenseiMonitor(id:String, val connector:MonitorConnector) extends JmxMonitor(id) {
    
    val name:String = "Sensei"
	
    var alarmStates = Seq()
    
    private val serverName = locateServers(connector)
    private val serverDomainName = SENSEI_DOMAIN_NAME + ":name=" + serverName.getOrElse("*")
    
    private val dataProviderGroup = new Group("Data Provider")
	private val zoieAdminGroup = new Group("Index Admin")

    override def actions = Seq(
        new JmxAction("start", "Start", 
                Some("Start Data Provider Streaming"), Some(dataProviderGroup), 
                DATA_PROVIDER_OBJECT_NAME, connector),
        new JmxAction("stop", "Stop",
        		Some("Stop Data Provider Streaming"), Some(dataProviderGroup), 
                DATA_PROVIDER_OBJECT_NAME, connector),
        new JmxAction("pause", "Pause", 
                Some("Pause Data Provider Streaming"), Some(dataProviderGroup), 
                DATA_PROVIDER_OBJECT_NAME, connector),
        new JmxAction("resume", "Resume", 
                Some("Resume Data Provider Streaming"), Some(dataProviderGroup), 
                DATA_PROVIDER_OBJECT_NAME, connector)
        )
    
    override def stats = Seq(
        new JmxStat("EventsPerMinute", "Events per minute", DATA_PROVIDER_OBJECT_NAME, connector),
        new JmxStat("EventCount", "Total events count", DATA_PROVIDER_OBJECT_NAME, connector)
    )
        
    override def settings = Seq(
		new JmxSetting("zoieBatchDelay", "Indexing Batch Delay", ZOIE_OBJECT_NAME, "BatchDelay", connector, None, None, Some("Batch indexing delay (ms)"), Some(zoieAdminGroup)),
		new JmxSetting("zoieBatchSize", "Indexing Batch Size", ZOIE_OBJECT_NAME, "BatchSize", connector, Some(1L), Some(10L), Some("Batch indexing size"), Some(zoieAdminGroup)),
		new JmxSetting("zoieFreshness", "Realtime Freshness", ZOIE_OBJECT_NAME, "Freshness", connector, Some(50L), Some(100L), Some("Realtime indexing refreshing times (ms)"), Some(zoieAdminGroup)),
		new JmxSetting("dataProviderBatchSize", "Data Provider Batch Size", DATA_PROVIDER_OBJECT_NAME, "BatchSize", connector, Some(1L), Some(10L), Some("Data Provider batch size"), Some(dataProviderGroup)),
		new JmxSetting("dataProviderMaxEventsPerMinute", "Max Indexing Events per Minute", DATA_PROVIDER_OBJECT_NAME, "MaxEventsPerMinute", connector, Some(40000L), Some(20000L), Some("Maximum events to attend per minute"), Some(dataProviderGroup))
	)

    def available: Boolean = {
        if (serverName.isDefined && connector.isAvailable) {
        	connector.getAttributeValue(serverDomainName, "Available")
        } else false
	}
	
    def alarms = alarmStates
    
	def information = 
	    	Map("ID" -> connector.getAttributeValue(serverDomainName, "Id"),
    	        "Available" -> available,
    	        "Partitions" -> connector.getAttributeValue(serverDomainName, "Partitions"),
	    		"Port" -> connector.getAttributeValue(serverDomainName, "Port"),
	    		"Index Path" -> connector.getAttributeValue(ZOIE_OBJECT_NAME, "IndexDir"),
	    		"Realtime" -> connector.getAttributeValue(ZOIE_OBJECT_NAME, "Realtime")
    		)
}

class SenseiMonitorLocator extends MonitorLocator {

    def locateMonitors(conn:MonitorConnector): Iterable[JmxMonitor] = {
        
        val nameOpt = SenseiMonitor.locateServers(conn)
        nameOpt.map { n =>
            val id = MonitorLocators.uniqueMonitorId(n, conn)
            new SenseiMonitor(id, conn)
        }
	}

}
