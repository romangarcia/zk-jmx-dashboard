package dridco.jmx
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import scala.io.Source
import dridco.jmx.monitor.MonitorsConfig
import dridco.jmx.monitor.XmlMonitorsConfig
import java.io.InputStream
import java.io.ByteArrayInputStream

@RunWith(classOf[JUnitRunner])
class XmlMonitorsConfigSpecs extends WordSpec with ShouldMatchers with TypeMatchers {
	val propContent = 
			"""
<connectors username="monitorRole" password="testing">
	<connector url="weba111:9001" type="sensei" />
	<connector url="weba111:9002" type="kafka" username="controlRole" />
	<connector url="weba111:9003" type="zookeeper" password="p4ssw0rd" />
	<connector url="weba111:9004" type="zookeeper" enabled="false" />
</connectors>
			"""                

	"A MonitorsConfig" that {
        "declares a single connector with two monitors" should {
    		val config = XmlMonitorsConfig(new ByteArrayInputStream(propContent.getBytes))
    		
//			"create Not Empty connectors" in {
//    			
//    		}
//    		
//			"create ONE valid Connector" in {
//            	config.connectorSpecs should have size (1)
//            }
//            
//            "configure THREE monitors" in {
//            	config.connectorSpecs(0).monitors should have size (3)
//            }
//            
//            "configure Sensei monitor" in {
//                config.connectorSpecs(0).monitors(0) should equal ("sensei")
//            } 
//            "configure Zookeeper monitor" in {
//            	config.connectorSpecs(0).monitors(1) should equal ("zookeeper")
//            } 
//            "configure Kafka monitor" in {
//            	config.connectorSpecs(0).monitors(2) should equal ("kafka")
//            } 
        }
        
    }
}