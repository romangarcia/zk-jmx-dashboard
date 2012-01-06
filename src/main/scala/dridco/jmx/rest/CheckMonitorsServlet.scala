package dridco.jmx.rest
import dridco.jmx.monitor.JmxCredentials
import javax.servlet.http.HttpServletRequest
import dridco.jmx.status.MonitorStatusException
import dridco.jmx.status.MonitorStatusApp
import javax.servlet.http.HttpServletResponse
import dridco.jmx.DashboardSystem
import dridco.jmx.monitor.MonitorConnectionSpec

class CheckMonitorsServlet extends BaseMonitorServlet {

    override def doGet(req:HttpServletRequest, resp:HttpServletResponse) {
        implicit val request = req
        implicit val response = resp
        
        val monStat = new MonitorStatusApp()
        val out = resp.getWriter()

        var status = "OK"
        val errorMsg = new StringBuilder
        
        val nameOpt = param("monitor")
        val specs = filterSpecs(nameOpt)

        specs.foreach { spec =>
        	try {
        		status = monStat.reportStatus(spec)
        	} catch {
	        	case mse:MonitorStatusException => 
	        	    errorMsg append mse.getMessage() append '\n'
	        	case e => throw e
        	}
        }
        
        if (errorMsg.isEmpty) 
            out.print(status)
        else {
        	val errorMsgStr = errorMsg.toString
            out.print(errorMsgStr)
            resp.addHeader("MONITOR-FAILURE-MESSAGES", errorMsgStr)
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND)
//            resp.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg.toString)
        }
    }
    
    private def filterSpecs(nameOpt:Option[String]) = {
    	import DashboardSystem._
    	
        if (nameOpt.isDefined) { 
            val nameFilter = nameOpt.get
            config.connectorSpecs.filter( _.monitors.contains( nameFilter ))
        } else {
        	config.connectorSpecs
        } 
    
    }

}