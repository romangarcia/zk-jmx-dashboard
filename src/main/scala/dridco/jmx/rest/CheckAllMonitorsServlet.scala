package dridco.jmx.rest
import dridco.jmx.monitor.JmxCredentials
import javax.servlet.http.HttpServletRequest
import dridco.jmx.status.MonitorStatusException
import dridco.jmx.status.MonitorStatusApp
import javax.servlet.http.HttpServletResponse
import dridco.jmx.DashboardSystem

class CheckAllMonitorsServlet extends BaseMonitorServlet {

    override def doGet(req:HttpServletRequest, resp:HttpServletResponse) {
        implicit val request = req
        implicit val response = resp
        import DashboardSystem._
        
        val monStat = new MonitorStatusApp()
        val out = resp.getWriter()

        var status = "OK"
        val errorMsg = new StringBuilder
        
        config.connectorSpecs.foreach { spec =>
            spec.monitors.foreach { monitorName =>
            	try {
	        		status = monStat.reportStatus(spec.url, spec.credentials, monitorName)
	        	} catch {
		        	case mse:MonitorStatusException => 
		        	    errorMsg append mse.getMessage() append '\n'
		        	case e => throw e
	        	}
            }
        }
        
        if (errorMsg.isEmpty) 
            out.print("OK")
        else 
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, errorMsg.toString)
        
    }

}