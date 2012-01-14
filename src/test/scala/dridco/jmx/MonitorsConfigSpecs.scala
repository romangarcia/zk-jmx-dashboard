package dridco.jmx
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import scala.io.Source
import dridco.jmx.monitor.MonitorsConfig

@RunWith(classOf[JUnitRunner])
class MonitorsConfigSpecs extends WordSpec with ShouldMatchers with TypeMatchers {
	val propContent = 
			"""
<connectors>
	<property name="clusterName" value="testCluster" />
	
	<connector url="localhost:18888" type="zookeeper" />
	<connector url="localhost:18889" type="kafka" />
	<connector url="localhost:18890" type="sensei" />
</connectors>
	    """                

    "A MonitorsConfig" when {
        "given a configuration" when {
            "declares a single connector with two monitors" should {
        		val config = MonitorsConfig(getClass.getResourceAsStream("/connectors.xml"))
        		
				"create Not Empty connectors" in {
        			config.connectorSpecs should not be ('empty)
        		}
        		
				"create ONE valid Connector" in {
                	config.connectorSpecs should have size (1)
                }
                
                "configure THREE monitors" in {
                	config.connectorSpecs(0).monitors should have size (3)
                }
                
                "configure Sensei monitor" in {
                    config.connectorSpecs(0).monitors(0) should equal ("sensei")
                } 
                "configure Zookeeper monitor" in {
                	config.connectorSpecs(0).monitors(1) should equal ("zookeeper")
                } 
                "configure Kafka monitor" in {
                	config.connectorSpecs(0).monitors(2) should equal ("kafka")
                } 
            }
        }
        
    }
}