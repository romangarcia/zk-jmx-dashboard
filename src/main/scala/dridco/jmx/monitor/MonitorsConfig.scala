package dridco.jmx.monitor

import java.util.Properties
import java.io.FileInputStream
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.io.Source
import java.io.ByteArrayInputStream
import scala.collection.JavaConversions

object MonitorsConfig {
    val CONNECTOR_KEY = "connectors"
	val CONNECTOR_PREFIX = "connector."
    val CONNECTOR_URL_POSFIX = ".url"
    val CONNECTOR_USER_POSFIX = ".username"
    val CONNECTOR_PASSWORD_POSFIX = ".password"
    val CONNECTOR_MONITORS_POSFIX = ".monitors"
    val DEFAULT_CONNECTOR_PORT = 1099
        
    def apply(srcPath:String):MonitorsConfig = {
        val sourceInput = Source.fromInputStream(getClass().getResourceAsStream(srcPath))
        apply(sourceInput)
    }
	def apply(src:Source):MonitorsConfig = {
		val props = new Properties
		props.load(new ByteArrayInputStream(src.mkString.getBytes()))
				
		val connectorNames = new ListBuffer[String]
		
		val connectorsListString = props.getProperty(CONNECTOR_KEY, "")
		if (connectorsListString != "") {
		    val names = connectorsListString.split(",")
			connectorNames ++= names.toList
		}
		
		val connectors = for (connName <- connectorNames.toList) yield {
		    parseConnectorSpec(props, connName)
		}
		
		new MonitorsConfig(connectors)
	}
    
    def parseConnectorSpec(props:Properties, connName:String) = {
	    val prefix = CONNECTOR_PREFIX + connName
	    
	    // url
	    val url = props.getProperty(prefix + CONNECTOR_URL_POSFIX)
	    
	    // auth
		val username = Option(props.getProperty(prefix + CONNECTOR_USER_POSFIX))
		val password = Option(props.getProperty(prefix + CONNECTOR_PASSWORD_POSFIX))
		val auth = (username, password) match {
	        case (Some(u), Some(p)) if (u.length > 0 && p.length > 0) => Some(new JmxCredentials(u, p))
	        case _ => None
	    }

	    // monitors
	    val monitorsArray = props.getProperty(prefix + CONNECTOR_MONITORS_POSFIX).split("[\\s]*,[\\s]*")

	    MonitorConnectionSpec(url, auth, monitorsArray)
    }
}

case class MonitorsConfig(connectorSpecs:Seq[MonitorConnectionSpec])