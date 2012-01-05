package dridco.jmx.rest
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import dridco.jmx.monitor.JmxCredentials
import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec
import dridco.jmx.status.MonitorStatusApp
import dridco.jmx.status.MonitorStatusException

class JmxMonitorServlet extends HttpServlet {

    override def doGet(req:HttpServletRequest, resp:HttpServletResponse) {
        implicit val request = req
        implicit val response = resp
        
        val monitorName = param("monitor").getOrElse( fail("No monitor requested") )
        val url = param("url").getOrElse( fail("No url requested") )
        
        val user = param("user")
        val pass = param("password")
        
        val auth =
            if (user.isDefined && pass.isDefined) 
                Some(JmxCredentials(user.get, pass.get))
            else 
                None              
        
        try {
        	val monitorStatus = new MonitorStatusApp()
        	val status = monitorStatus.reportStatus(url, auth, monitorName)
        	
        	val out = resp.getWriter()
        	out.print(status)
        	out.flush()
        } catch {
            case mse:MonitorStatusException =>
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, mse.getMessage())
            case e => throw e
        }
        
    }
    
    def param(name:String)(implicit req:HttpServletRequest):Option[String] = {
        Option(req.getParameter(name))
    }
    
    def fail(msg:String)(implicit resp:HttpServletResponse) = {
        throw new IllegalStateException(msg)
    }
}