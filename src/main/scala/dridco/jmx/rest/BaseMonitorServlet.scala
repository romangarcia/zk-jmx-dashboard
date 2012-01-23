package dridco.jmx.rest
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import dridco.jmx.monitor.JmxCredentials
import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.MonitorConnectionSpec
import dridco.jmx.status.MonitorStatusReader
import dridco.jmx.status.MonitorStatusException

class BaseMonitorServlet extends HttpServlet {
   
    def param(name:String)(implicit req:HttpServletRequest):Option[String] = {
        Option(req.getParameter(name))
    }
    
    def fail(msg:String)(implicit resp:HttpServletResponse) = {
        throw new IllegalStateException(msg)
    }
}