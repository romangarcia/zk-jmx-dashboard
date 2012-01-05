package dridco.jmx.monitor

trait JmxAccess {

    def safeAttributeValue[T](objectName:String, name:String)(implicit conn:MonitorConnector, m:Manifest[T]) : Option[T] = {
    	if (conn.isAvailable) {
    	    Some(conn.getAttributeValue[T](objectName, name))
    	} else {
    	    None
    	}
    }

    def safeAvailable(conn:MonitorConnector): Boolean = {
        conn.isAvailable
    }
}