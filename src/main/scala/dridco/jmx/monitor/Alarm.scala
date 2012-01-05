package dridco.jmx.monitor

object AlarmState extends Enumeration {
    val OK, WARNING, FATAL = Value 
}

abstract class Alarm {
    def state:AlarmState.Value
    def info:String
}