package dridco.jmx.monitor

import java.lang.Number
import System._
import javax.management.ObjectName

case class MonitorSpec(name:String, id:String, info:String)

class Group(val name:String, val description:Option[String] = None)

abstract class Stat(val id:String, val label:String, 
        			val description:Option[String] = None, val group:Option[Group] = None) {
    def getValue(): StatValue
}

class JmxStat(
        id:String, label:String, description:Option[String], group:Option[Group], 
        objectName:String, conn: MonitorConnector) 
		extends Stat(id, label, description, group) {
    
    def this(id:String, label:String, objectName:String, conn:MonitorConnector) =
    	this(id, label, None, None, objectName, conn)
    	
	def getValue(): StatValue = {
	    new StatValue(id, currentTimeMillis(), conn.getAttributeValue(objectName, id))
	}
}


class StatValue(val statId:String, val time:Long, val value:Number)

abstract class Action(val id:String, val label:String, 
    				val description:Option[String] = None, val group:Option[Group] = None) {
	def invoke()
}

class JmxAction(
        id:String, label:String, description:Option[String], group:Option[Group], 
        objectName:String, conn: MonitorConnector) 
        extends Action(id, label, description, group) {
    
    def this(id:String, label:String, objectName:String, conn:MonitorConnector) = 
        this(id, label, None, None, objectName, conn)
        
    def invoke() = {
        conn.invoke(objectName, id)
    }
}

abstract class Setting[T](val id:String, val label:String, 
        val description:Option[String] = None, 
        val group:Option[Group] = None) {
    def setValue(value:String)
    def currentValue():T
    
    def defaultValue:Option[T]
    def recommendedValue:Option[T]
}

class JmxSetting[T](id:String, label:String, 
        objectName:String, objectKey:String, conn: MonitorConnector, 
        val defaultValue:Option[T], val recommendedValue:Option[T], 
        description:Option[String], group:Option[Group]) 
        extends Setting[T](id, label, description, group) {
    
    def this(id:String, 
    		label:String, 
    		objectName:String, 
    		conn: MonitorConnector, 
    		defaultValue:Option[T], 
    		recommendedValue:Option[T]) =
            this(id, label, objectName, id, conn, defaultValue, 
                    recommendedValue, None, None)

    def this(id:String, 
            label:String, 
            objectName:String, 
            conn: MonitorConnector, 
            defaultValue:Option[T], 
            recommendedValue:Option[T], 
            description:Option[String], 
            group:Option[Group]) =
    		this(id, label, objectName, id, conn, defaultValue, 
    				recommendedValue, None, None)
                    
    def setValue(value:String) {
        conn.setPropertyFromString(objectName, objectKey, value)
    }
    
    def currentValue():T = {
        conn.getAttributeValue(objectName, objectKey)
    }
    
}


