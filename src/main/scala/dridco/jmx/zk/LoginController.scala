package dridco.jmx.zk

import org.zkoss.zk.ui.util.GenericForwardComposer
import org.zkoss.zk.ui.Component
import org.zkoss.zul.Textbox
import org.zkoss.zk.ui.event.Event
import dridco.jmx.UserManager
import dridco.jmx.DashboardWebSystem._
import dridco.jmx.DashboardSystem._
import org.zkoss.zul.Label
import dridco.jmx.DashboardWebSystem

class LoginController extends GenericForwardComposer {
    
    var usernameText:Textbox = _
    var passwordText:Textbox = _
    var messageLabel:Label = _
    
    def onClick$loginButton(event:Event) {
		import DashboardWebSystem._
	    
	    try {
	    	val token = userManager.loginUser(usernameText.getValue(), passwordText.getValue())
			
			val redirPage = 
			    if (requestScope.containsKey("redir")) {
			    	requestScope.get("redir").toString()
			    } else {
			        homePage
			    }
	    	
			execution sendRedirect redirPage
			
	    } catch {
	        case e:Exception => messageLabel setValue e.getMessage()
	    }
    }
	
}