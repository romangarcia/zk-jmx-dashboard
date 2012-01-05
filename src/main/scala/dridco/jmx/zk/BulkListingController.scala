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
import JavaConversions.seqAsJavaList
import DashboardSystem._
import org.zkoss.zul.Comboitem
import org.zkoss.zul.ComboitemRenderer
import org.zkoss.zul.Textbox

class BulkListingController extends SimpleController {
	
	var monitorsGrid:Grid = _
	var monitorCombobox:Combobox = _

	var settingCombobox:Combobox = _
	
	var settingInputDiv:Div = _
	var settingTextbox:Textbox = _
	
	val monSetRenderer = 
		new RowRenderer {
			def render(row:Row, data:Any) {
				val mon = data.asInstanceOf[JmxMonitor]
				val check = new Checkbox()
				check.setId(mon.id)
				row.appendChild(check)
				row.appendChild(new Label(mon.id))
				val selOpt = Option(settingCombobox.getSelectedItem().getId())
				if (selOpt.isDefined) {
					val setOpt = mon.settings.find( _.id == selOpt.get )
					val set = setOpt.getOrElse {
					    throw new IllegalArgumentException("No valid setting found for [" + selOpt.get + "]")
					}
					
					row.appendChild(new Label(set.currentValue().toString()))
				}
			}
		}

	
	override def doAfterCompose(component:Component) {
	    super.doAfterCompose(component)
	    
	    val monitors = connectionManager.monitors()
	    
	    val monitorNames = monitors.map( _.name ).toSet
	    
	    monitorCombobox.setModel(new SimpleListModel(monitorNames.toList))
	}
	
	val settingsComboRenderer = new ComboitemRenderer {
	    def render(item:Comboitem, data:Object) {
	        val set = data.asInstanceOf[Setting[_]]
	        item.setLabel(set.label)
	        item.setDescription(set.description.getOrElse(set.label))
	        item.setId(set.id)
	    }
	}
	
	def onSelect$monitorCombobox(event:Event) {
	      
//	      settingsDiv.setVisible(false)

  
		val itemOpt = Option(monitorCombobox.getSelectedItem())
		if (itemOpt.isDefined) {
			val filteredMonitors = connectionManager.monitors(itemOpt.get.getValue().toString)

			if (!filteredMonitors.isEmpty) {
				val templateMonitor = filteredMonitors(0)
				settingCombobox.setModel(new ListModelList(templateMonitor.settings.toList))
				settingCombobox.setItemRenderer(settingsComboRenderer)
			}
		}
	}

	def onSelect$settingCombobox(event:Event) {

	    val itemOpt = Option(monitorCombobox.getSelectedItem())
	    if (itemOpt.isDefined) {
	    	// show all monitors for this setting
	        val filteredMonitors = connectionManager.monitors(itemOpt.get.getValue().toString )
			monitorsGrid.setModel(new ListModelList(filteredMonitors))
			monitorsGrid.setRowRenderer(monSetRenderer)

			// show setting input textbox
			if (!filteredMonitors.isEmpty) {
				settingInputDiv.setVisible(true)
				val templateMonitor = filteredMonitors(0)
				val setting = event.getData().asInstanceOf[Setting[_]]
				
				settingTextbox.setValue("")
			}
	        
			
			
	    }
	    
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