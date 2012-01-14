package dridco.jmx.monitor

import java.util.Properties
import java.io.FileInputStream
import java.io.File
import scala.collection.mutable.ListBuffer
import scala.io.Source
import java.io.ByteArrayInputStream
import scala.collection.JavaConversions
import scala.xml.XML
import scala.xml.{Source => XmlSource}
import scala.xml.NodeSeq
import scala.xml.Node
import java.io.InputStream

case class MonitorsConfig(properties:Map[String, String], 
        connectorSpecs:Seq[MonitorConnectionSpec])
        
object MonitorsConfig {
        
    def apply(src:InputStream):MonitorsConfig = {
    	parse(src)
    }
    
    def parse(src:InputStream) = {
        val xml = XML.load(src)
		
        val defaultUsernameOpt = xml.attribute("username")
		val defaultPasswordOpt = xml.attribute("password")

		val connectors = xml \ "connector"
        
        val specs = connectors.map { c =>
        	val enabledOpt = c.attribute("enabled")

        	val name = c \ "@type" text
			val url = c \ "@url" text
			val userOpt = c.attribute("username") 
			val passOpt = c.attribute("password")
			
			// enabled by default
			val enabled = 
			    if (enabledOpt.isDefined)
			    	(enabledOpt.get text).toBoolean
			    else
			    	true
			        
			val auth = determineCredentials(userOpt, passOpt, 
			defaultUsernameOpt, defaultPasswordOpt)
					
			new MonitorConnectionSpec(url, auth, Seq(name))
        }
        
        val properties = xml \ "property"
        
        val props = properties.map { p =>
            val name = p \ "@name" text
            val value = p \ "@value" text
            
            (name -> value)
        }.toMap
        
        MonitorsConfig(props, specs)

    }
    def determineCredentials(userOpt:Option[Seq[Node]], passOpt:Option[Seq[Node]], 
            defUserOpt:Option[Seq[Node]], defPassOpt:Option[Seq[Node]]) = {
        
            val username = 
                if (userOpt.isDefined) 
                    Some(userOpt.get text) 
                else if (defUserOpt.isDefined)
                    Some(defUserOpt.get text)
                else
                    None
                    
    		val password = 
    		    if (passOpt.isDefined) 
    		        Some(passOpt.get text) 
		        else if (defPassOpt.isDefined)
		            Some(defPassOpt.get text)
	            else
	                None

            (username, password) match {
                case (Some(u:String), Some(p:String)) => 
                    Some(new JmxCredentials(u, p))
                case _ => None
            }
    
    }
}