package dridco.jmx

import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Ignore
import dridco.jmx.monitor.MonitorLocators
import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec
import dridco.jmx.monitor.ZookeeperMonitor

/**
 * REQUIRES a Zookeeper Service running on 'localhost' with JMX enabled via RMI on port 1999
 * 
 * TODO: setup jmx host/port via ENV properties 
 */
@RunWith(classOf[JUnitRunner])
class AnIntegrationSpec extends WordSpec with ShouldMatchers with TypeMatchers {

    "A real MonitorLocator" ignore {		// TODO: IGNORED!! should be enabled on integration env
        val monLoc = MonitorLocators.DEFAULT
        "having a Zookeeper service online on localhost:18888" when {
            val conn = new JmxMonitorConnector(new MonitorConnectionSpec("localhost:1999", None, Seq()))
        	"asked about available monitors" should {
        	    val availMons = monLoc.listAvailable(conn)
        		"return a zookeeper monitor" in {
       				availMons should not be ('empty)
    				availMons.size should equal (2)
    				availMons(0) should be (anInstanceOf[ZookeeperMonitor])
        		}
        	}
        }
    }
}