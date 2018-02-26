/**
 *  Turn me off quick
 *
 *  Author: seateabee@gmail.com
 *  Heavily based on Power Allowance by SmartThings
 *  Date: 2013-06-28
 */

// Automatically generated. Make future change here.
definition(
    name: "Turn me off quick",
    namespace: "",
    author: "seateabee@gmail.com",
    description: "Designed to turn off something after it's turned on with in a given number of seconds.  Works just like Power Allowance, but lets you specify be seconds instead of minutes.  Good possible use is to turn off an outlet used with a relay as a garage door opener shortly after triggering the outlet.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When a switch turns on...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it off how many SECONDS later?") {
		input "secondsLater", "number", title: "When?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: false])
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: false])
}

def switchOnHandler(evt) {
	log.debug "Switch ${theSwitch} turned: ${evt.value}"
	def delay = secondsLater * 1000 /*Because delay is counted in mill-seconds, multiple seconds by 1000 to get the proper delay. */
	log.debug "Turning off in ${secondsLater} minutes (${delay}ms)"
	theSwitch.off(delay: delay)
}