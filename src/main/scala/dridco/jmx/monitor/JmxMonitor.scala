 package dridco.jmx.monitor
 import dridco.jmx.Logging

case class JmxCredentials(username:String, password:String)

abstract class JmxMonitor(val id:String) extends Logging {
    
    // returns a generic name for this monitor
    def name:String
    
	// returns all actions available on this monitor
	def actions: Seq[Action] = Seq()
	
	// returns all stats available on this monitor
	def stats: Seq[Stat] = Seq()
	
	// returns all settings available on this monitor
	def settings: Seq[Setting[_]] = Seq();
    
    def available: Boolean

    def connector: MonitorConnector
    
    def alarms: Seq[Alarm]
    
    def information: Map[String, Any]
	
	// [internal] stats actor, which reads stats using underlying JMX connection
	private val statsActor = new StatsActor(this).start()
	
	// to invoke a given actionID. Could also use anAction.invoke()
    def invoke(actionId:String) {
	    val action = actions.find( _.id == actionId ).getOrElse {
	    	throw new IllegalArgumentException("Action " + actionId + " not available for Monitor " + id)
	    }
	    
		action.invoke()
	}
    
    def applySetting(settingId:String, value:String) {
        val setting = settings.find( _.id == settingId ).getOrElse {
            throw new IllegalArgumentException("Setting " + settingId + " not available for Monitor " + id)
        }
        
        setting.setValue(value)
    }
    
    // to subscribe as a listener for this monitor stats actor
    def subscribeStats(listener:StatsListener) {
        statsActor ! ('subscribe, listener)
    }
    
    
}