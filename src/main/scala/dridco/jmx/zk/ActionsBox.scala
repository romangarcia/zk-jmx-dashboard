package dridco.jmx.zk

import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.metainfo.ComponentDefinition
import org.zkoss.zul.Hbox
import org.zkoss.zk.ui.ext.ScopeListener
import java.util.Collection
import org.zkoss.zk.ui.IdSpace
import org.zkoss.zk.scripting.Namespace
import org.zkoss.zk.au.AuService
import dridco.jmx.monitor.JmxMonitor
import dridco.jmx.monitor.Group
import org.zkoss.zul.Groupbox
import org.zkoss.zul.Caption
import dridco.jmx.monitor.Action
import org.zkoss.zul.Button
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox

/**
 * Custom component for Actions groups
 */
class ActionsBox(mon:JmxMonitor) extends Hbox {

    val groupedActions = mon.actions.groupBy( _.group )
    
    groupedActions.keys.foreach { groupOpt =>
        // append Group
        val groupName = groupOpt.getOrElse( new Group("") ).name
        val groupBox = new Groupbox()
        groupBox.appendChild(new Caption(groupName))

        // append Actions
        val actions = groupedActions.getOrElse( groupOpt, Seq() )
        actions.foreach { act =>
            groupBox appendChild createActionButton(act)
        }

        appendChild(groupBox)
    }
    
    private def createActionButton(act:Action) = {
    	val actButton = new Button(act.label)
    	if (act.description.isDefined) {
    		actButton setTooltiptext(act.description.get)
    	}
    	actButton.addEventListener("onClick", new EventListener {
    		def onEvent(event:Event) {
    			invokeAction( act )
    		}
    	})
    	actButton
    }

    private def invokeAction( act:Action ):Unit = {
		import Messagebox._
		val (result, msg) = 
			try {
				act.invoke()
				(true, "Succesfull Invocation")
			} catch {
			case e => (false, "Failed invocation!")
			}

		val dialogType = if (result) INFORMATION else ERROR 
				show(msg, "JMX Invocation", OK, dialogType)
    } 


}