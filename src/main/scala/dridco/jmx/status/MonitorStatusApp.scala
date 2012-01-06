package dridco.jmx.status

import javax.management.remote.JMXConnector
import dridco.jmx.monitor.JmxCredentials
import dridco.jmx.monitor.MonitorConnectionSpec

object MonitorStatusApp extends App {
    val BEAN_NAME_POS = 0
    val JMX_URL_POS = 1
    val JMX_USERNAME_POS = 2
    val JMX_PASSWORD_POS = 3
    val CLI_ARGS_COUNT = 4
    
    val EXIT_STATUS_UNAVAILABLE = 1
    val EXIT_STATUS_INVALID_ARGS = 2
    
    if (args.length < CLI_ARGS_COUNT - 2) {
        printUsage()
        sys.exit(EXIT_STATUS_INVALID_ARGS)
    }
    
    val auth = 
        if (args.length == CLI_ARGS_COUNT) 
            Some(new JmxCredentials(args(JMX_USERNAME_POS), args(JMX_PASSWORD_POS)))
        else 
            None
            
    val mbean = args(BEAN_NAME_POS)

    def start() {
    	var exitCode = 0
		var status = ""
		try {
			val statusApp = new MonitorStatusApp()
			val connSpec = MonitorConnectionSpec(args(JMX_URL_POS), auth, Seq(mbean), true)
			status = statusApp.reportStatus( connSpec )
		} catch {
		    case e =>
		        status = "FAILURE: " + e.getMessage()
	            exitCode = EXIT_STATUS_UNAVAILABLE
		}
    	
    	println(status)
    	
    	sys.exit(exitCode)
            
    }

    
    private def printUsage() {
        
    	val usage = """
 * Usage:
 * 
 * SenseiMonitor <system> <jmx-url> [<username> <password>]
 * 
 * <system> means either zookeeper|kafka|sensei
 * <jmx-url> means host:port
 * <username>, <password> mean jmx credentials (optional)
 * 
 * exit code:
 * 0: success
 * 1: invalid status
 * 2: invalid arguments"""
    	    
    	println(usage)
    }
    
}

class MonitorStatusApp {
    import MonitorStatusApp._
    
    val SUCCESS = "OK"

    def reportStatus(connSpec:MonitorConnectionSpec) = {
        
        val mbean = connSpec.monitors(0)

        val statusReporter = mbean match {
	    	case "sensei" => new SenseiStatus()
	    	case "zookeeper" => new ZookeeperStatus()
	    	case "kafka" => new KafkaStatus()
    	}
    	
    	val status = statusReporter.reportResult(connSpec)
    			
		if (status.valid) {
			SUCCESS
		} else {
			throw new MonitorStatusException(status.message.get)
		}
            
    }
}

class MonitorStatusException(msg:String) extends Exception(msg)