package dridco.jmx.zk
import org.zkoss.zul.Hbox
import dridco.jmx.monitor.JmxMonitor
import org.zkoss.zul.Vbox
import org.zkoss.zul.Label
import org.zkoss.zul.Button
import org.zkoss.zul.Div
import org.zkoss.zul.Textbox
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Event
import dridco.jmx.monitor.Group
import org.zkoss.zul.Groupbox
import org.zkoss.zul.Caption
import org.zkoss.zul.Messagebox
import dridco.jmx.monitor.Setting
import org.zkoss.zul.Cell
import org.zkoss.zk.ui.Component

class SettingsBox(mon:JmxMonitor, showValues:Boolean, apply: (Setting[_], String) => Unit) extends Hbox {

	// a div with overflow to allow scrolls
	val settingsScrollingDiv = createScrollingContainer()
	appendChild(settingsScrollingDiv)
	
	val settingsTabBox = new Vbox()
	settingsTabBox.setSpacing("5px")
	settingsScrollingDiv.appendChild(settingsTabBox)
	
	// group settings
	val settingGroups = mon.settings.groupBy( _.group )
	
	// create groups
	settingGroups.keys.foreach { groupOpt =>
	    val groupBox = createGroupBox(groupOpt)
    	settingsTabBox appendChild groupBox
    	
    	val settings = settingGroups.getOrElse( groupOpt, Seq() )
    	// create setting inputs
    	settings.foreach { set =>
	    	groupBox appendChild createSettingInputBox(set, showValues)
	    }
    	
	}
    	
	private def createSettingTextBox[T](set:Setting[T], showValues:Boolean) = {
	    val textbox = new Textbox()
	    textbox.setTooltiptext {
	        set.label + "\n" +
	        set.description.getOrElse("") + "\n" + 
	        "Default: " + set.defaultValue.getOrElse("None") + "\n" +
	        "Recommended: " + set.recommendedValue.getOrElse("None")
	    }
	    
	    if (showValues) textbox.setValue(set.currentValue().toString())
	    textbox
	}
	
	private def createSettingInputBox[T](set:Setting[T], showValues:Boolean) = {
		val settingBox = new Hbox()

		// label
		settingBox.appendChild(cell("60%", new Label(set.label)))
		
		// textbox
		val inputTextbox = createSettingTextBox(set, showValues)
	    val eventListener = new EventListener {
	    		def onEvent(event:Event) {
	    			applySetting( set, inputTextbox.getValue() )
	    		}
	    	}
    	
	    inputTextbox.addEventListener("onOk", eventListener)
	    settingBox appendChild cell("30%", inputTextbox)

	    // ok button
    	val okButton = new Button("OK")
    	okButton.addEventListener("onClick", eventListener)
		    	
	    settingBox appendChild cell("10%", okButton)
	    
    	if (set.description.isDefined) {
    		settingBox.setTooltiptext(set.description.get)
    	}
	    
	    settingBox
	}
	
	def cell(width:String, child:Component) = {
	    val cell = new Cell
		cell.setWidth(width)
	    cell.appendChild(child)
	    cell
	}

    private def createScrollingContainer() = {
    	val scroll = new Div()
    	scroll.setStyle("overflow:auto;")
    	scroll
    }

    private def createGroupBox(groupOpt:Option[Group]) = {
        val groupName = groupOpt.getOrElse( new Group("") ).name
    	val groupBox = new Groupbox()
        groupBox.setMold("3d")
    	groupBox.appendChild(new Caption(groupName))
    	groupBox
    }
    
    private def applySetting( set:Setting[_], value:String ):Unit = {
        import Messagebox._
        val (result, msg) = 
	        try {
	        	apply(set, value)
	        	(true, "Setting succesfully applied")
	        } catch {
	            case e => (false, "Failed setting modification!")
	        }
        val dialogType = if (result) INFORMATION else ERROR 
        show(msg, "JMX Invocation", OK, dialogType)
    } 

}