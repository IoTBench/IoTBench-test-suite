/**
 *  Turn on one or more switches at a specified time and turn them off at a later time.
 *  When user present turns on the switch and when it is midnight turns off the switch. 
 *  Author: Z. Berkay Celik
 *  Presence sensor and user specified times may conflict and may create an unexpected behavior.
 *  Email: zbc102@cse.psu.edu
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "Turn on one or more switches at a specified time and turn them off at a later time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("Select switches to control...") {
		input name: "switches", type: "capability.switch", multiple: true
	}
	section("When all of these people leave home") { //updated
        input "person", "capability.presenceSensor", multiple: true
    }
	section("Turn them all on at...") {
		input name: "startTime", title: "Turn On Time?", type: "time"
	}
	section("And turn them off at...") {
		input name: "stopTime", title: "Turn Off Time?", type: "time"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")
	subscribe(people, "presence", presenceHandler)
}

def updated(settings) {
	unschedule()
	schedule(startTime, "startTimerCallback")
	schedule(stopTime, "stopTimerCallback")
	subscribe(person, "presence", presenceHandler)
}

//updated
def presenceHandler(evt) {
    log.debug "evt.name: $evt.value"
    if (evt.value == "not present") {
        switches.off()
    }
    else {
        switches.on()
    }
}

def startTimerCallback() {
	log.debug "Turning on switches"
	switches.on()
}

def stopTimerCallback() {
	log.debug "Turning off switches"
	switches.off()
}

