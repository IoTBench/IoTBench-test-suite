/**
 *  Power Allowance
 *
 *  Author: SmartThings
 *  Modified by: Chris Sader, to allow for entering Seconds instead of Minutes
 */
 
preferences {
	section("When a switch turns on...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it off how many seconds later?") {
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
	def delay = secondsLater * 1000
	log.debug "Turning off in ${secondsLater} seconds (${delay}ms)"
	theSwitch.off(delay: delay)
}
