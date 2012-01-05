package dridco.jmx.zk

import javax.servlet.Filter
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import dridco.jmx.Logging
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import dridco.jmx.DashboardSystem
import org.zkoss.zk.ui.sys.ComponentsCtrl
import org.zkoss.zk.ui.sys.ComponentCtrl
import org.zkoss.zk.ui.Execution
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.Sessions
import org.zkoss.zk.ui.sys.SessionsCtrl
import org.zkoss.zk.ui.Components.RequestScope
import dridco.jmx.DashboardWebSystem
import java.net.URLEncoder

class AuthenticationFilter extends Filter with Logging {
    
    def init(config:FilterConfig) {
        info("Initializing Authentication Filter...")
    }
    
    def destroy() {
    	info("Destroying Authentication Filter...")
    }
    
    def doFilter(req:ServletRequest, resp:ServletResponse, chain:FilterChain) {

        if (req.isInstanceOf[HttpServletRequest]) {
           doHttpFilter(req.asInstanceOf[HttpServletRequest], 
                   resp.asInstanceOf[HttpServletResponse], chain) 
        }
    }
    
    def doHttpFilter(req:HttpServletRequest, resp:HttpServletResponse, chain:FilterChain) {
        
    	import DashboardSystem._
        import DashboardWebSystem._

		if (!userManager.isAuthenticated && this.requiresAuthentication) {
		    val redir = loginPage + appendCurrentRequestLocation(req)
    	    Executions.getCurrent().sendRedirect(redir)
		} else {
		    chain.doFilter(req, resp)
		}

    }
    
    private def appendCurrentRequestLocation(req:HttpServletRequest):String = {
        val queryString = req.getQueryString()
        if (queryString != null && queryString.length() == 0) {
        	"?"
        } else {
        	"&"
        } + "redir=" + URLEncoder.encode(req.getRequestURL().toString, "UTF-8")
    }
    
    protected def requiresAuthentication = true
}