package dridco.jmx
import scala.io.Source
import java.io.File
import dridco.jmx.monitor.MonitorLocators
import dridco.jmx.monitor.MonitorsConfig
import dridco.jmx.monitor.JmxMonitorConnector
import dridco.jmx.monitor.XmlMonitorsConfig


object DashboardSystem extends Logging {
    val userManager = new UserManager
    val config = XmlMonitorsConfig(getClass.getResourceAsStream("/connectors.xml"))
    val connectionManager = new ConnectionManager(config)
}

object DashboardWebSystem {
    val AUTHENTICATION_REDIRECT_QUERY_PARAM = "redir"
    
    val homePage = "/"
	val loginPage = "/dashboard.zul"
}