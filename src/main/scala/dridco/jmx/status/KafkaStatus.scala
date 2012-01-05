package dridco.jmx.status

import scala.collection.mutable.MapBuilder
import dridco.jmx.monitor.MonitorConnectionSpec

class KafkaStatus extends StatusReporter {
    val KAFKA_FLUSH_OBJ_NAME = "kafka:type=kafka.LogFlushStats"
	val KAFKA_OBJECT_QUERY = "kafka:*"
    val KAFKA_LOGS_CLASSNAME = "kafka.log.LogStats"
        
    val KAFKA_FLUSHES_PER_SECOND = "kafkaFlushesPerSecond"

    def report(connSpec:MonitorConnectionSpec):Map[String, Any] = {
        
        withConnection(connSpec) { conn =>
            val logNames = conn.lookupObjectNamesForClass(KAFKA_OBJECT_QUERY, KAFKA_LOGS_CLASSNAME)
	        var offsets = Map[String, String]()
            for (logName <- logNames) {
                val name = conn.getAttributeValue[String](logName, "Name")
            	val offset = conn.getAttributeValue[Long](logName, "CurrentOffset")
            	
            	offsets += (name -> offset.toString)
            }
            
            val flushesPerSecond = conn.getAttributeValue[Double](KAFKA_FLUSH_OBJ_NAME, "FlushesPerSecond")

            // return a Map with flushes x second + offset for every topic
	        Map(KAFKA_FLUSHES_PER_SECOND -> flushesPerSecond) ++ offsets
        }
        
    }
    
	def reportResult(connSpec:MonitorConnectionSpec):StatusResult = {

	    checkResult(connSpec) { values =>
	        
	    	val msg = new StringBuilder
	    	val isFlushing = values.getOrElse(KAFKA_FLUSHES_PER_SECOND, 0.0).asInstanceOf[Double] > 0.0 
	    	if (!isFlushing) msg append "Kafka Node is not FLUSHING\n"
	    
	    	(isFlushing, msg.toString)
		    
	    }
	    
	}    
}