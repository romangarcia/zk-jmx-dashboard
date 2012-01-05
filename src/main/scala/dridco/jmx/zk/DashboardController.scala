package dridco.jmx.zk

import org.zkoss.zk.ui.Component
import org.zkoss.zkmax.zul.Portalchildren
import org.zkoss.zul.Panel
import org.zkoss.zul.Panelchildren
import org.zkoss.zkmax.zul.Portallayout
import org.zkoss.zul.Label
import dridco.jmx.DashboardSystem._
import org.zkoss.zul.Tabpanel
import org.zkoss.zul.Tab
import org.zkoss.zul.Tabbox
import org.zkoss.zul.Tabs
import scala.collection.mutable.ListBuffer
import org.zkoss.zul.Tabpanels
import org.zkoss.zul.Vbox
import dridco.jmx.monitor.JmxMonitor
import org.zkoss.zul.Button
import org.zkoss.zul.Hbox
import dridco.jmx.monitor.Group
import org.zkoss.zul.Groupbox
import org.zkoss.zul.Caption
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import dridco.jmx.monitor.Action
import org.zkoss.zul.Textbox
import org.zkoss.zk.ui.metainfo.EventHandler
import dridco.jmx.monitor.Stat
import org.zkoss.zk.ui.AbstractComponent
import org.zkoss.zul.Toolbar
import org.zkoss.zul.Toolbarbutton
import org.zkoss.zul.Div
import dridco.jmx.monitor.Setting

class DashboardController extends SimpleController {

    val PORTALS_COUNT = 4
	val PORTALS_HEIGHT = "250px"
    val AVAILABLE_ICON = ("/image/available.png", "Available")
    val UNAVAILABLE_ICON = ("/image/unavailable.png", "Unavailable")
    
    var portalLayout:Portallayout = null
    
    override def doAfterCompose(component:Component) {
        super.doAfterCompose(component)

        var currentPortalIdx = 0
        
        val portalWidth = 100 / PORTALS_COUNT
        
        val portals = new ListBuffer[Portalchildren]()
        for (n <- 0 until PORTALS_COUNT) {
			portals += appendPortal(portalWidth)
        }
		
        val monitors = connectionManager.monitors()
        for (mon <- monitors) {
        	if (currentPortalIdx >= PORTALS_COUNT) currentPortalIdx = 0
			val currentPortal = portals(currentPortalIdx)

			appendMonitorPanel(currentPortal, mon)
            currentPortalIdx += 1
        }
        
    }
    
    def appendMonitorPanel(currentPortal:Portalchildren, mon:JmxMonitor) {
        
		// panel
		val monPanel = new Panel()
		currentPortal.appendChild(monPanel)
		monPanel.setTitle(mon.name)
		monPanel.setHeight(PORTALS_HEIGHT)
		monPanel.setId(mon.id)
		
		// panelChildren
		val monPanelChild = new Panelchildren()
		monPanel.appendChild(monPanelChild)

		// tabbox
		monPanelChild.appendChild(createMonitorTab(mon))
    }
    
    private def createMonitorTab(mon:JmxMonitor) = {
		val tabBox = new Tabbox()
		
		val tabs = new Tabs()
		val toolbar = new Toolbar()
		toolbar.setAlign("end")
//		toolbar.setHeight("20px")
		val tabPanels = new Tabpanels()
		
		tabBox.appendChild(tabs)
		tabBox.appendChild(toolbar)
		tabBox.appendChild(tabPanels)
      
		val (availableImage, availableText) = if (mon.available) AVAILABLE_ICON else UNAVAILABLE_ICON 
		val availableIcon = new Toolbarbutton()
//		availableIcon.setHeight("20px")
		availableIcon.setImage(availableImage)
		availableIcon.setTooltiptext(availableText)
		toolbar.appendChild(availableIcon)
		
		if (mon.available) appendMonitorTabs(mon, tabs, tabPanels)
				
		tabBox
    }

    private def appendMonitorTabs(mon:JmxMonitor, tabs:Tabs, tabPanels:Tabpanels) {
    
        // info tab
		if (!mon.information.isEmpty) {
			tabs.appendChild(new Tab("Info"))
			tabPanels.appendChild(createInfoTabPanel(mon))        
		}

		// actions tab
		if (!mon.actions.isEmpty) {
			tabs.appendChild(new Tab("Actions"))
			tabPanels.appendChild(createActionsTabPanel(mon))
		}
		
		// stats tab
		if (!mon.stats.isEmpty) {
		    val statsTab = new Tab("Stats")
			tabs.appendChild(statsTab)
			val statsTabPanel = createStatsTabPanel(mon)
			tabPanels.appendChild(statsTabPanel)
			
	    	statsTab.addEventListener("onSelect", new EventListener {
	    		def onEvent(event:Event) {
	    	        // refresh stats on focus
	    	        refreshStats(mon, statsTabPanel.getFirstChild())
	    		}
	    	})
		}

        // settings tab
		if (!mon.settings.isEmpty) {
			tabs.appendChild(new Tab("Settings"))
			tabPanels.appendChild(createSettingsTabPanel(mon))
		}

    }
    
    private def createInfoTabPanel(mon:JmxMonitor) = {

		val infoTabPanel = createTabPanel()
        val infoTabBox = new Vbox()
        infoTabPanel.appendChild(infoTabBox)
        
        for ((label, value) <- mon.information) {
        	infoTabBox.appendChild(new Label(label + ": " + value))
        }
		
		infoTabPanel
    }
    
    private def createTabPanel() = {
        val panel = new Tabpanel()
        panel.setHeight("170px")
        panel
    }
    
    private def createActionsTabPanel(mon:JmxMonitor) = {
        val actionTabPanel = createTabPanel()
        val actionsTabBox = new Hbox()
        actionTabPanel.appendChild(actionsTabBox)
        
        val groupedActions = mon.actions.groupBy( _.group )
        
        groupedActions.keys.foreach { groupOpt =>
            // append Group
            val groupName = groupOpt.getOrElse( new Group("") ).name
            val groupBox = new Groupbox()
            groupBox.appendChild(new Caption(groupName))
            actionsTabBox.appendChild(groupBox)

            // append Actions
            val actions = groupedActions.getOrElse( groupOpt, Seq() )
            actions.foreach { act =>
	            groupBox appendChild createActionButton(act)
            }
        }
        
        actionTabPanel
    }
    
    private def createActionButton(act:Action) = {
        val actButton = new Button(act.label)
        if (act.description.isDefined) {
        	actButton setTooltiptext(act.description.get)
        }
        actButton.addEventListener("onClick", new EventListener {
            def onEvent(event:Event) {
            	performCall( act.invoke() )
            }
        })
        actButton
    }

    private def createStatsTabPanel(mon:JmxMonitor) = {
    	val statsTabPanel = createTabPanel()
    	val statsTabBox = new Vbox()
        statsTabPanel.appendChild(statsTabBox)
        
        mon.stats.foreach { stat =>
            val statLabel = new Label(createLabelText(stat))
            statLabel.setAttribute("stat", stat)
            if (stat.description.isDefined) {
            	statLabel setTooltiptext(stat.description.get)
            }
            statsTabBox appendChild statLabel
        }
    	
    	statsTabPanel
    }
    
    private def createLabelText(stat:Stat) = {
        stat.label + ": " + stat.getValue().value
    }
    
    private def refreshStats(mon:JmxMonitor, statsTabBox:Component) {
        def recurseStatLabels(comp:Component) {
        	if (comp == null) return
        	
        	val statOpt = Option(comp.getAttribute("stat"))
        	if (statOpt.isDefined && comp.isInstanceOf[Label]) {
        	    val Some(stat:Stat) = statOpt
    			comp.asInstanceOf[Label].setValue(createLabelText(stat))
        	}
			
			recurseStatLabels(comp.getNextSibling())
        }

        recurseStatLabels(statsTabBox.getFirstChild())
        
    }

    private def createSettingsTabPanel(mon:JmxMonitor) = {
    	val settingsTabPanel = createTabPanel()
    	
    	// a div with overflow to allow scrolls
    	val settingsScrollingDiv = createScrollingContainer()
    	settingsTabPanel.appendChild(settingsScrollingDiv)
    	
		val settingsTabBox = new Vbox()
    	settingsScrollingDiv.appendChild(settingsTabBox)
    	
    	// group settings
    	val settingGroups = mon.settings.groupBy( _.group )
    	
    	settingGroups.keys.foreach { groupOpt =>
    	    val groupBox = createGroupBox(groupOpt)
	    	settingsTabBox appendChild groupBox
	    	
	    	val settings = settingGroups.getOrElse( groupOpt, Seq() )
	    	settings.foreach { set =>
		    	val inputTextbox = new Textbox(set.currentValue().toString())
		    	
		    	inputTextbox.addEventListener("onOk", 
	    			new EventListener {
			    		def onEvent(event:Event) {
			    			performCall( set.setValue(inputTextbox.getValue()) )
			    		}
			    	})
	
		    	val okButton = new Button("OK")
		    	okButton.addEventListener("onClick", 
		    	        new EventListener {
				    		def onEvent(event:Event) {
				    			performCall( set.setValue(inputTextbox.getValue()) )
				    		}
				    	})
				    	
		    	val settingBox = new Hbox(Array(new Label(set.label), inputTextbox, okButton))
		    	if (set.description.isDefined) {
		    		settingBox.setTooltiptext(set.description.get)
		    	}
		    	groupBox appendChild settingBox
    	    }
	    	
    	}
		settingsTabPanel
    }
    
    private def createGroupBox(groupOpt:Option[Group]) = {
        val groupName = groupOpt.getOrElse( new Group("") ).name
    	val groupBox = new Groupbox()
    	groupBox.appendChild(new Caption(groupName))
    	groupBox
    }
    
    private def createScrollingContainer() = {
    	val scroll = new Div()
    	scroll.setStyle("overflow:auto;")
    	scroll
    }
    
    private def performCall( f: => Unit ):Unit = {
        import Messagebox._
        val (result, msg) = 
	        try {
	        	f
	        	(true, "Succesfull Invocation")
	        } catch {
	            case e => (false, "Failed invocation!")
	        }
        val dialogType = if (result) INFORMATION else ERROR 
        show(msg, "JMX Invocation", OK, dialogType)
    } 
    
    private def appendPortal(width:Int) = {
    	val portal = new Portalchildren()
    	portal.setWidth(width + "%")
    	portal.setParent(portalLayout)
    	portal
    }

}