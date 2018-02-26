/**
 *  Turn me on quick
 *
 *  Author: chrisb
 *  Heavily based on Power Allowance by SmartThings
 *  Date: 2013-12-15
 * 
 *  Created for a user who had a Genie 1000 that require the relay to be ON for his wired control panel to work properly.
 *  Turning off the relay triggers a turn on a given number of seconds later.
 */
 
preferences {
	section("When a switch turns off...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it on how many SECONDS later?") {
		input "secondsLater", "number", title: "When?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(theSwitch, "switch.off", switchOffHandler, [filterEvents: false])
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(theSwitch, "switch.off", switchOffHandler, [filterEvents: false])
}

def switchOffHandler(evt) {
	log.debug "Switch ${theSwitch} turned: ${evt.value}"
	def delay = secondsLater * 1000 /* Because delay is counted in mill-seconds, multiple seconds by 1000 to get the proper delay. */
	log.debug "Turning on in ${secondsLater} minutes (${delay}ms)"
	theSwitch.on(delay: delay)
}