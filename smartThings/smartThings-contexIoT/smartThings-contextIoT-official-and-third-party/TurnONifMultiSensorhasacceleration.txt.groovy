/**
 *  Turn ON if Multi Sensor has acceleration
 *
 *  Author: John Essey
 *  Date: 2014-04-09
 *  Summary: If device acceleration = active/yes then turn on light for "x" number of minutes. This is to act as an open/close
 *  sensor if you have a recessed door, or other issues using the SmartSense Multi as an open/close device. 
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "Turn ON if Multi Sensor has acceleration",
    namespace: "johneeeee",
    author: "John Essey",
    description: "If acceleration detected, then turn on light for x number of minutes. This is to act as an open/close sensor if you have a recessed door, or other issues using the SmartSense Multi as an open/close device. ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
     section("When movement is detected..."){
		input "accelerationSensor", "capability.accelerationSensor", title: "Which Door?", multiple: true
     }
     section("Turn on a light..."){
		input "switches", "capability.switch", multiple: true
     }
	 section("Between this time at night:") {
		 input "timeOfDay1", "time", title: "Time?"
	 }
     section("And this time in the morning:") {
		 input "timeOfDay2", "time", title: "Time?"
	 }
     section ("Turn off after this many minutes") {
     input "minutesLater", "number", title: "Delay"
     }
     }

def installed() {
	initialize()
}

def updated()
{
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def initialize(){
subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def lightsoff(){
switches.off()
}

def accelerationActiveHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
    def startTime = timeToday(timeOfDay1)
    def endTime = timeToday(timeOfDay2)
  	if (now() < startTime.time && now() > endTime.time) 
    {
    }
    else {
        switches.on()
        def delay = minutesLater * 60
	runIn(delay, lightsoff)
    log.debug "turning lights off aftet delay"
          }
  

}
