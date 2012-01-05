package dridco.jmx
import scala.io.Source
import java.io.File
import dridco.jmx.monitor.MonitorLocators
import dridco.jmx.monitor.MonitorsConfig
import dridco.jmx.monitor.JmxMonitorConnector


object DashboardSystem extends Logging {

    val userManager = new UserManager

    val connectionManager = new ConnectionManager
    
}

object DashboardWebSystem {
    val AUTHENTICATION_REDIRECT_QUERY_PARAM = "redir"
    
    val homePage = "/"
	val loginPage = "/dashboard.zul"
}