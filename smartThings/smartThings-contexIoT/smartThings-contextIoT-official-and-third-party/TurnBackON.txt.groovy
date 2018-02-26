/**
 *  Turn Back ON
 *
 *  Author: nema.darban@gmail.com
 *  Date: 2014-02-03
 */
preferences {
	section("When a switch turns off...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it on how many minutes later?") {
		input "minutes", "number", title: "When?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: false])
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(theSwitch, "switch.off", switchOnHandler, [filterEvents: false])
}

def switchOnHandler(evt) {
	log.debug "Switch ${theSwitch} turned: ${evt.value}"
	def delay = minutes * 60 * 1000 /*Because delay is counted in mill-seconds, multiple seconds by 1000 to get the proper delay. */
	log.debug "Turning on in ${minutes} minutes"
	theSwitch.on(delay: delay)
}