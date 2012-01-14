package dridco.jmx
import dridco.jmx.monitor.MonitorsConfig


object DashboardSystem extends Logging {
    val userManager = new UserManager
    val config = MonitorsConfig(getClass.getResourceAsStream("/connectors.xml"))
    val connectionManager = new ConnectionManager(config)
}

object DashboardWebSystem {
    val AUTHENTICATION_REDIRECT_QUERY_PARAM = "redir"
    
    val homePage = "/"
	val loginPage = "/dashboard.zul"
}