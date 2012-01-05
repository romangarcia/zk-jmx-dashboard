package dridco.jmx.zk

import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.util.GenericForwardComposer
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.sys.ComponentsCtrl
import java.lang.reflect.Method
import dridco.jmx.UserManager
import dridco.jmx.DashboardSystem
import dridco.jmx.DashboardWebSystem._

//class Secured(val roles:String*) extends StaticAnnotation

class SimpleController extends GenericForwardComposer {

//    val userManager:UserManager = DashboardSystem.userManager
//    
//    override def doAfterCompose(component:Component) {
//		super.doAfterCompose(component)
//
//		if (!userManager.isAuthenticated && this.requiresAuthentication) {
//    	    execution.sendRedirect(homePage)
//		}
//    }
//    
//    override def onEvent(evt:Event) {
//		val controller = getController();
//		val mtd = ComponentsCtrl.getEventMethod(controller.getClass(), evt.getName());
//
//		if (mtd != null) {
//			checkAuthorization(mtd);
//		}
//		
//		super.onEvent(evt);
//	}    
//    
//    def checkAuthorization(mtd:Method) {
//		val annotations = mtd.getAnnotations();
//		for (annotation <- annotations) {
//			if (annotation.isInstanceOf[Secured]) {
//				val secured = annotation.asInstanceOf[Secured];
//				userManager.checkAccess(secured.roles)
//			}
//		}
//	}
//
//    protected def requiresAuthentication = true
}

