package dridco.jmx.monitor

import javax.management.remote.JMXConnectorFactory
import javax.management.JMX
import javax.management.remote.JMXServiceURL
import javax.management.ObjectName
import javax.management.remote.JMXConnector
import scala.collection.JavaConversions
import javax.management.Attribute
import com.sun.java.util.jar.pack.PropMap
import javax.management.MBeanServerConnection
import javax.management.Query

case class MonitorConnectionSpec(val url:String, credentials:Option[JmxCredentials], monitors:Seq[String], enabled:Boolean = true) {
    override def toString() = {
        val username = if (credentials.isDefined) " [" + credentials.get.username + "]" else ""
        url + username
    }
}

trait MonitorConnector {
//	def createProxy[T](objectName: String)(implicit manifest:Manifest[T])
	
    def isAvailable: Boolean
    
	def getAttributeValue[T](objectName: String, name: String)(implicit m:Manifest[T]): T
	
	def lookupObjectNames(query:String): Iterable[String]

	def lookupObjectNameKeys(baseQuery:String): Map[String, String]
    
    def lookupObjectNamesForClass(objectName:String, className:String): Iterable[String]
	
	def setPropertyFromString(objectName: String, propName: String, value:String)
	
	def setProperty[T](objectName: String, propName: String, value:T)
	
	def invoke(objectName:String, action:String)
	
	def connectionSpec: MonitorConnectionSpec
	
	def close()
	
}

class JmxMonitorConnector(val connectionSpec:MonitorConnectionSpec) extends MonitorConnector {
    
	private val jmxConnector = attemptConnect()
	
	private val jmxConnection = jmxConnector.getMBeanServerConnection()

	private def jmxURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + connectionSpec.url + "/jmxrmi")

    def attemptConnect() = {
		var env = new java.util.HashMap[String, Any]()
		connectionSpec.credentials.foreach { auth =>
			env.put(JMXConnector.CREDENTIALS, Array(auth.username, auth.password))
		}
		
		try {
			JMXConnectorFactory.connect(jmxURL, env)
		} catch {
		    case e => throw new IllegalStateException("Unexpected state. Error connecting to monitor: " + connectionSpec, e)
		}
    }
	
	def isAvailable: Boolean = {
	    // FIXME: hack! No other way to check socket?
	    try {
	    	jmxConnection.getDomains()
	    	true
	    } catch {
	        case e => false
	    }
	}

	def close() {
	    jmxConnector.close()
	}
	
//	def createProxy[T](objectName: String)(implicit manifest:Manifest[T]) = {
//	    val oName = new ObjectName(objectName)
//		JMX.newMBeanProxy(jmxConnection, oName, manifest.erasure).asInstanceOf[T]
//	}
	
	def getAttributeValue[T](objectName: String, name: String)(implicit m:Manifest[T]): T = {
	    val attr = jmxConnection.getAttribute(new ObjectName(objectName), name)
	    
	    attr.asInstanceOf[T]
	}

	// lookup all object names given a query 
    def lookupObjectNames(query:String): Iterable[String] = {
	    val names = jmxConnection.queryNames(new ObjectName(query), null)
	    val namesIterable = JavaConversions.collectionAsScalaIterable(names)

	    namesIterable.map( _.getCanonicalName() )
	}
    
	def lookupObjectNamesForClass(objectName:String, className:String): Iterable[String] = {
	    var result = List[String]()
        for (name <- lookupObjectNames(objectName)) {
        	val beanInfo = jmxConnection.getMBeanInfo(new ObjectName(name))
			if (beanInfo.getClassName() == className) {
			    result = name :: result
			}
        }
	    
	    result
	}    
    
    def lookupObjectNameKeys(baseQuery:String): Map[String, String] = {
        val names = jmxConnection.queryNames(new ObjectName(baseQuery), null)
        val namesIterable = JavaConversions.collectionAsScalaIterable(names)
        
        namesIterable.flatMap { name =>
            val keyList = name.getKeyPropertyList()
            val entries = keyList.entrySet()
            val sEntries = JavaConversions.asScalaSet(entries)
            for (entry <- sEntries) yield (entry.getKey() -> entry.getValue())
        }.toMap
        
    }

    def setProperty[T](objectName: String, propName: String, value:T) {
    	jmxConnection.setAttribute(new ObjectName(objectName), new Attribute(propName, value))
    }
    
    def setPropertyFromString(objectName: String, propName: String, value:String) {
        val mbeanInfo = jmxConnection.getMBeanInfo(new ObjectName(objectName))
        val attr = mbeanInfo.getAttributes().find( _.getName() == propName )
        
        val converted = attr match {
            case Some(attrInfo) if (attrInfo.isWritable()) =>
                attemptConvert(value, attrInfo.getType())
            case _ =>
                throw new IllegalArgumentException("Property not available on bean: " + propName)
        }
        
        setProperty(objectName, propName, converted)
    }

    def invoke(objectName:String, action:String) {
        jmxConnection.invoke(new ObjectName(objectName), action, Array(), Array())
    }
    
    private def attemptConvert(value:String, destType:String):Any = {
        destType match {
            case "int" => new java.lang.Integer(value)
            case "long" => new java.lang.Long(value)
            case "char" => new java.lang.Character(value.charAt(0))
            case "float" => new java.lang.Float(value)
            case "double" => new java.lang.Double(value)
            case "boolean" => new java.lang.Boolean(value)
            case _ => {
            	val destClass = Class.forName(destType)
    			if (destClass == classOf[String]) value
    			else if (destClass == classOf[Int]) value.toInt
    			else if (destClass == classOf[Long]) value.toLong
    			else if (destClass == classOf[Char]) value(0)
    			else if (destClass == classOf[Double]) value.toDouble
    			else if (destClass == classOf[Float]) value.toFloat
    			else if (destClass == classOf[Boolean]) value.toBoolean
    			else {
    				throw new IllegalArgumentException("Invalid value for property. " + destClass + " required")
    			}
            }
        }
    }
    
}