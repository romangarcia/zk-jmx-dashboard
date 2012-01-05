package dridco.jmx
import scala.reflect.Manifest

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import org.scalatest.WordSpec

import dridco.jmx.monitor.KafkaMonitorLocator
import dridco.jmx.monitor.MonitorLocators
import dridco.jmx.monitor.SenseiMonitorLocator
import dridco.jmx.monitor.ZookeeperMonitorLocator
import monitor.SenseiMonitor
import monitor.ZookeeperMonitor
import monitor.ZookeeperMonitorLocator

@RunWith(classOf[JUnitRunner])
class MonitorLocatorsSpecs extends WordSpec with ShouldMatchers with TypeMatchers with MonitorLocatorBehaviors{   
    
    "A MonitorLocators" that {
        
        // zookeeper locator
    	"is aware of Zookeeper Locator" when {
	    	val locators = new MonitorLocators(Array(new ZookeeperMonitorLocator()))
	
	    	"queried availables for a valid connection that holds ONE Zookeeper MBean" should {
	    	    behave like singleMonitorAvailable[ZookeeperMonitor](locators, 
	    	            Seq("org.apache.ZooKeeperService:name0=QuorumPeerServer"))
	        }
	
	    	"queried availables for a valid connection that holds TWO Zookeeper MBean" should {
	    	    behave like twoMonitorsAvailable[ZookeeperMonitor](locators, 
	    	            Seq("org.apache.ZooKeeperService:name0=QuorumPeerServer", 
	    	                "org.apache.ZooKeeperService:name1=QuorumPeerServer"))
	    	}

	    	"queried availables for a valid connection that holds NO Zookeeper MBean" should {
	    	    behave like noMonitorsAvailable(locators, Seq())
	    	}

	    	"queried availables for a valid connection that holds OTHER MBeans" ignore {		// TODO: figure a way to stub this one (without stubbing the names lookup)
	    		behave like noMonitorsAvailable(locators, Seq("com.senseidb:sensei-server-1"))
	    	}
	    }
    	
    	// Sensei locator
    	"is aware of Sensei Locator" when {
	    	val locators = new MonitorLocators(Array(new SenseiMonitorLocator()))
	
	    	"queried availables for a valid connection that holds ONE Sensei MBean" should {
	    		behave like singleMonitorAvailable[SenseiMonitor](locators, Seq("com.senseidb:sensei-server-1"))
	    	}
	
	    	"queried availables for a valid connection that holds TWO Sensei MBean" should {
	    		behave like twoMonitorsAvailable[SenseiMonitor](locators, 
	    		        Seq("com.senseidb:sensei-server-1", "com.senseidb:sensei-server-2"))    	    
	    	}
	    	
	    	"queried availables for a valid connection that holds NO Sensei MBean" should {
	    		behave like noMonitorsAvailable(locators, Seq())
	    	}
	    }    	

    	// Kafka locator
    	"is aware of Kafka Locator" when {
    		val locators = new MonitorLocators(Array(new KafkaMonitorLocator()))
    		
    		"queried availables for a valid connection that holds ONE Kafka MBean" should {
    			behave like singleMonitorAvailable[SenseiMonitor](locators, Seq("com.senseidb:sensei-server-1"))
    		}
    		
    		"queried availables for a valid connection that holds TWO Kafka MBean" should {
    			behave like twoMonitorsAvailable[SenseiMonitor](locators, 
    			        Seq("com.senseidb:sensei-server-1", "com.senseidb:sensei-server-2"))    	    
    		}
    		
    		"queried availables for a valid connection that holds NO Kafka MBean" should {
    			behave like noMonitorsAvailable(locators, Seq())
    		}
    	}    	
    }

}

trait MonitorLocatorBehaviors { this: WordSpec with ShouldMatchers with TypeMatchers =>

	def listMonitors(locators:MonitorLocators, stubbedNames:Seq[String]) = {
		val conn = new StubMonitorConnector(objectNames = stubbedNames)
		locators.listAvailable(conn)
	}
    
    def singleMonitorAvailable[T](locators:MonitorLocators, stubbedNames:Seq[String])(implicit manifest:Manifest[T]) {

        val avail = listMonitors(locators, stubbedNames)
        
		"not be an empty list" in {
            avail should not be ('empty) 
        }

	    "list ONE monitor" in {
	        avail should have size (1)
	    }
		    	
	    "return first a Zookeeper Monitor" in { 
	        avail(0) should be (anInstanceOf(manifest)) 
        }

    }
    
    def twoMonitorsAvailable[T](locators:MonitorLocators, stubbedNames:Seq[String])(implicit manifest:Manifest[T]) {
        
    	val avail = listMonitors(locators, stubbedNames)

    	"not be an empty result" in {
    	    avail should not be ('empty)
    	}
			
    	"return two monitors" in {
    	    avail should have size (2)
    	}
	    
    	"return first a valid Monitor" in {
    	    avail(0) should be (anInstanceOf(manifest)) 
	    }
	    
    	"return second a valid Monitor" in {
    	    avail(1) should be (anInstanceOf(manifest)) 
	    }    	    
    }
    
    def noMonitorsAvailable(locators:MonitorLocators, stubbedNames:Seq[String]) {
 
        val avail = listMonitors(locators, stubbedNames)
	    		
		"be an empty result" in {
            avail should be ('empty) 
        }
    }
}