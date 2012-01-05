package dridco.jmx.zk
import org.zkoss.zul.ListModelArray
import dridco.jmx.DashboardSystem
import org.zkoss.zkplus.databind.BindingListModelList
import scala.collection.JavaConversions
import org.zkoss.zk.ui.Component
import org.zkoss.zul.Grid
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Row
import dridco.jmx.monitor.JmxMonitor
import org.zkoss.zul.Label
import org.zkoss.zul.Button
import org.zkoss.zul.RowRenderer
import org.zkoss.zul.Combobox
import org.zkoss.zul.SimpleListModel
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Div
import org.zkoss.zk.ui.AbstractComponent
import dridco.jmx.monitor.Setting

class ListingController extends SimpleController {
	import JavaConversions.seqAsJavaList
	import DashboardSystem._
	
	var monitorsGrid:Grid = _
	var monitorCombobox:Combobox = _
	
	var settingsDiv:Div = _
	var settingsContainer:Div = _

	var actionsDiv:Div = _
	var actionsContainer:Div = _
	
	override def doAfterCompose(component:Component) {
	    super.doAfterCompose(component)
	    
	    val monitors = connectionManager.monitors()
	    val monitorNames = monitors.map( _.name ).toSet
	    monitorCombobox.setModel(new SimpleListModel(monitorNames.toList))
	}
	
	val rowRenderer = 
	    new RowRenderer {
			def render(row:Row, data:Any) {
				val mon = data.asInstanceOf[JmxMonitor]
				val check = new Checkbox()
				check.setId(mon.id)
				row.appendChild(check)
				row.appendChild(new Label(mon.name + " @ " + mon.connector.connectionSpec))
			}
		}
	
	def onSelect$monitorCombobox(event:Event) {
	      
	      actionsDiv.setVisible(false)
	      removeAllChildren (actionsContainer)
	      settingsDiv.setVisible(false)
	      removeAllChildren(settingsContainer)
	      
	      val itemOpt = Option(monitorCombobox.getSelectedItem())
	      if (itemOpt.isDefined) {
	          
	          val monitors = connectionManager.monitors()
	    	  val filteredMonitors = monitors.filter( _.name == itemOpt.get.getValue() )

			  // prepare settings
			  if (!filteredMonitors.isEmpty) {
				  renderTemplateMonitor(filteredMonitors(0))
			  }

	    	  monitorsGrid.setModel(new ListModelList(filteredMonitors))
	    	  monitorsGrid.setRowRenderer(rowRenderer)
	           
	      }
	      
	}
	
	def renderTemplateMonitor(templateMonitor:JmxMonitor) {
	    // render actions
	    if (!templateMonitor.actions.isEmpty) {
	    	actionsDiv.setVisible(true)
	    	actionsContainer.appendChild(new ActionsBox(templateMonitor))
	    }

	    // render settings
	    if (!templateMonitor.settings.isEmpty) {
	    	settingsDiv.setVisible(true)
	    	settingsContainer.appendChild(new SettingsBox(templateMonitor, false, (set:Setting[_], value:String) => {
	    		val selectedMonitorsId = selectedMonitorsForBulk
	    		val selectedMonitors = connectionManager.monitors(selectedMonitorsId)
				selectedMonitors.foreach { mon =>
					mon.applySetting(set.id, value)
	    		}
	    	}))
	    }
	}
	
	private def removeAllChildren(component:AbstractComponent) {
	    def removeSibling(p:AbstractComponent, childOpt:Option[Component]) {
	    	if (childOpt.isDefined) {
	    		val child = childOpt.get
				p.removeChild(child)
				removeSibling(p, Option(child.getNextSibling()))
	    	}
	    }
	    
	    removeSibling(component, Option(component.getFirstChild()))
	    
	}
	
	private def selectedMonitorsForBulk = {
	    def rowId(row:Row):List[String] = {
	        if (row == null) Nil
	        else {
	        	val monId = row.getFirstChild().asInstanceOf[Checkbox].getId()
    			monId :: rowId(row.getNextSibling().asInstanceOf[Row])
	        }
	    }
	    
	    rowId(monitorsGrid.getRows().getFirstChild().asInstanceOf[Row])
	}
	
    
}