/**
 *  Schedule a Garage Door Close
 *
 *  Author: jeffloyd@gmail.com
 *  Date: 2013-08-10
 */

// Automatically generated. Make future change here.
definition(
    name: "Garage Door Close on a Schedule",
    namespace: "",
    author: "jeffloyd@gmail.com",
    description: "Schedule your Garage Door to Close if it is a certain time, and the garage door is open.  Good for people who want to make sure they do not accidentally leave their Garage Door open all night.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("Choose your Garage Door Sensor...") {
		input "garageDoorStatus", "capability.contactSensor", title: "Where?"
	}
    section("Select Garage Door Opener to Control...") {
		input name: "switches", type: "capability.switch", multiple: true
	}
	section("If Open, Close My Garage Door at...") {
		input "time1", "time", title: "When?"
	}
}

def installed()
{
	log.debug "Installed with settings: ${settings}"
	schedule(time1, "scheduleCheck")
}

def updated()
{
	def now = new Date()
	log.debug "Current Time is $now"
	unsubscribe() //TODO no longer subscribe like we used to - clean this up after all apps updated
	unschedule()
	schedule(time1, "scheduleCheck")
}

def scheduleCheck()
{
	log.trace "scheduledCheck"
    def currentState = garageDoorStatus.contactState
	if (currentState?.value == "open") {
		log.debug "Garage Door was open - Closing."
        switches.on()
	} else {
		log.debug "Garage Door was not open. No Action."
	}
}
