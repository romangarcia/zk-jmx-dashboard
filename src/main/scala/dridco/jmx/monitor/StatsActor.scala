package dridco.jmx.monitor

import java.lang.System._
import scala.actors.Actor
import dridco.jmx.Logging

trait StatsListener {
    def statProduced(stat:StatValue)
}

class StatsActor[T](monitor:JmxMonitor) extends Actor with Logging {
    
	private val MILLIS_IN_SECOND = 1000L
    
	private val validSchedules = Set(0.5, 1.0, 2.0, 5.0, 30.0, 60.0, 300.0, 600.0)

	var scheduleMillis:Long = calculateMillis(validSchedules.last)

	var countdownToQueryStats = scheduleMillis
	
	val monitorStats = monitor.stats
	
	var statListeners = Seq[StatsListener]()

	def act() {
	    
	    while (true) {
	        produceStats
        }
	    
    }

    def produceStats {
        
    	// while schedule time hasn't elapsed
    	checkMessages
    	
    	if (!statListeners.isEmpty) {
    		// produce stats
    		val stats = monitorStats.map { s=>
    		s.getValue()
    		}
    		
    		// notify listeners about recent stats
    		notifyStats(stats)
    		
    		// adjust timing
    		countdownToQueryStats = scheduleMillis
    	}

    }
    
    def notifyStats(stats:Seq[StatValue]) {
        stats.foreach ( s =>
        	statListeners.foreach { l =>
        	    l.statProduced(s)
        	}
        )
    }
    
    // check if there are messages to process
    def checkMessages {

        while (countdownToQueryStats > 0) {

            var startTime = currentTimeMillis
            try {
                receiveWithin(scheduleMillis / 2) {
                    // change schedule
                    case freq: Double if validSchedules.contains(freq) =>
                        scheduleMillis = calculateMillis(freq)
                    // subscribe a listener
                    case ('subscribe, listener: StatsListener) =>
                        statListeners :+ listener
                    // unsubscribe a listener
                    case ('unsubscribe, listener: StatsListener) =>
                        // FIXME: do something!
                }

            } catch {
                case _ =>
                    trace("Timeout waiting for command within Statistics Actor")
            }

            val elapsed = currentTimeMillis - startTime
            countdownToQueryStats = countdownToQueryStats - elapsed
        }

    }

    def calculateMillis(secs: Double) = {
        if (!validSchedules.contains(secs)) {
            throw new IllegalArgumentException("Invalid schedule for Stats")
        }

        (secs * MILLIS_IN_SECOND).toLong
    }


}