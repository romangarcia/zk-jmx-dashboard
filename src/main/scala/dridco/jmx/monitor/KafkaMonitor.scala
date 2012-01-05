package dridco.jmx.monitor


class KafkaMonitorLocator extends MonitorLocator {
	import KafkaMonitor._
	def locateMonitors(conn:MonitorConnector): Iterable[JmxMonitor] = {
        val keysMap = conn.lookupObjectNameKeys(KAFKA_DOMAIN_NAME)
        
        val nameOpt = keysMap.get("type")
        nameOpt.map { n =>
            val id = MonitorLocators.uniqueMonitorId(n, conn)
            new KafkaMonitor(id, conn)
        }
	}
}

/**
 * Kafka Monitor
 */
object KafkaMonitor {
    val KAFKA_DOMAIN_NAME = "kafka:type=kafka.SocketServerStats"
    val KAFKA_LOG_DOMAIN_PREFIX = "kafka:type=kafka.logs."
    val KAFKA_FLUSH_STATS_NAME = "kafka:type=kafka.LogFlushStats"
}

class KafkaMonitor(id:String, val connector:MonitorConnector) extends JmxMonitor(id) with JmxAccess {
    import KafkaMonitor._
    
	val name:String = "Kafka"
    
    def available: Boolean = safeAvailable(connector) && !connector.lookupObjectNames(KAFKA_DOMAIN_NAME).isEmpty
    
    def availableTopicsDomainNames:Seq[String] = connector.lookupObjectNames(KAFKA_LOG_DOMAIN_PREFIX + "*").toSeq
    def availableTopics:Seq[String] = availableTopicsDomainNames.map( connector.getAttributeValue[String](_, "Name") )
    
    def alarms = Seq[Alarm]()
    def information = Map("Available" -> available, "Topics" -> availableTopics.mkString(", "))
    
    override def stats: Seq[Stat] = Seq(
            new JmxStat("AvgFlushMs", "Average Flush (ms)", KAFKA_FLUSH_STATS_NAME, connector),
            new JmxStat("MaxFlushMs", "Maximum Flush (ms)", KAFKA_FLUSH_STATS_NAME, connector),
            new JmxStat("NumFlushes", "Flushes Counter", KAFKA_FLUSH_STATS_NAME, connector),
            new JmxStat("FlushesPerSecond", "Flushes per Second", KAFKA_FLUSH_STATS_NAME, connector)
        )
}


