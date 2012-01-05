package dridco.jmx

import scala.reflect.Manifest
import dridco.jmx.monitor.MonitorConnectionSpec
import dridco.jmx.monitor.MonitorConnector

class StubMonitorConnector(var failNext:Boolean = true, objectNames: Iterable[String] = Seq()) extends MonitorConnector {

    val connectionSpec:MonitorConnectionSpec = new MonitorConnectionSpec("www.somehost.com:1987", None, Seq())
    
    val isAvailable: Boolean = true
    
	def getAttributeValue[T](objectName: String, name: String)(implicit m:Manifest[T]): T = {
	    throw new UnsupportedOperationException("NOT IMPLEMENTED")
	}
	
	def lookupObjectNames(query:String): Iterable[String] = {
	    objectNames
	}
	
	def lookupObjectNameKeys(baseQuery:String): Map[String, String] = {
	    Map()
	}
	
	def lookupObjectNamesForClass(objectName:String, className:String): Iterable[String] = {
	    Seq()
	}
	
	def setPropertyFromString(objectName: String, propName: String, value:String) = {
	    if (failNext) throw new Exception("Fail!")
	}
	
	def setProperty[T](objectName: String, propName: String, value:T) = {
		if (failNext) throw new Exception("Fail!")
	}
	
	def invoke(objectName:String, action:String) = {
		if (failNext) throw new Exception("Fail!")
	}

	def close() {
	    failNext = true
	}
	
	def failNotFound = throw new IllegalStateException("No attribute found for domain/attribute")
}