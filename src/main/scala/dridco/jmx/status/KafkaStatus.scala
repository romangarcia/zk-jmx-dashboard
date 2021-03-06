package dridco.jmx.status

import scala.collection.mutable.MapBuilder
import dridco.jmx.monitor.MonitorConnectionSpec
import scala.collection.mutable.ListBuffer

class KafkaStatus extends StatusReporter {
    val KAFKA_FLUSH_OBJ_NAME = "kafka:type=kafka.LogFlushStats"
	val KAFKA_OBJECT_QUERY = "kafka:*"
    val KAFKA_LOGS_CLASSNAME = "kafka.log.LogStats"
        
    val KAFKA_FLUSHES_PER_SECOND = "kafkaFlushesPerSecond"

    def report(request:StatusRequest):Map[String, Any] = {
        
        withConnection(request.connectionSpec) { conn =>
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
    
	def reportResult(request:StatusRequest):StatusResult = {

	    checkResult(request) { values =>
	    	var msgs = new ListBuffer[String]
	    	
	    	val isFlushing = values.getOrElse(KAFKA_FLUSHES_PER_SECOND, 0.0).asInstanceOf[Double] > 0.0 
	    	if (!isFlushing) msgs += "Kafka Node is not FLUSHING"
	    
	    	(isFlushing, msgs.toList)
		    
	    }
	    
	}    
}