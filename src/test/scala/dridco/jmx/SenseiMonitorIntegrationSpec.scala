package dridco.jmx
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import monitor.SenseiMonitor
import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec
import scala.util.Properties

/**
 * REQUIRES a Sensei Service running on 'localhost' with JMX enabled via RMI on port 1999
 * 
 * TODO: setup jmx host/port via ENV properties 
 */
@RunWith(classOf[JUnitRunner])
class SenseiMonitorIntegrationSpec extends WordSpec with ShouldMatchers {
    
    val HOST = Properties.envOrElse("jmx.host", "localhost")
	val PORT = Properties.envOrElse("jmx.port", "18888").toInt
	
    "A real Sensei Monitor" when {
    	val sensei = new SenseiMonitor("anId", new JmxMonitorConnector(new MonitorConnectionSpec(HOST + ":" + PORT, None, Seq())))
        "queried about stats" should {
            val stats = sensei.stats
            "return 2 stats" in {stats should have size (2)}
            "have events-per-minute stat" in {stats.find( _.id == "EventsPerMinute" ) should be ('defined)}
            "have events-count stat" in {stats.find( _.id == "EventCount" ) should be ('defined)}
        }
    }
    
}