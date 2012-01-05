package dridco.jmx
import org.apache.commons.logging.LogFactory

trait Logging {
    @transient private lazy val log = LogFactory.getLog(getClass)

	def trace(msg: => String) {
    	if (log.isTraceEnabled()) 
    		log trace msg
    }

    def debug(msg: => String) {
        if (log.isDebugEnabled) 
            log debug msg
    }
    
    def info(msg: => String) {
    	if (log.isInfoEnabled()) 
    		log info msg
    }
    
    def warn(msg: => String) {
    	if (log.isWarnEnabled()) 
    		log warn msg
    }

    def warn(msg: => String, e:Throwable) {
    	if (log.isWarnEnabled()) 
    		log warn (msg, e)
    }
}