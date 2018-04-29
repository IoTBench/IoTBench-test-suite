/**
 * TP11: Third-party app 
 * Power Allowance (reversed)
 *
 *  Author: SmartThings (edited by baldeagle072)
 *  Soteria Analyzed.
 */
definition(
    name: "Power Allowance (reversed)",
    namespace: "baldeagle072",
    author: "SmartThings",
    description: "Turn on a switch after a certain amount of time after it has been turned off",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When a switch turns off...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it on how many minutes later?") {
		input "minutesLater", "number", title: "When?"
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
	def delay = minutesLater * 60
	log.debug "Turning off in ${minutesLater} minutes (${delay}seconds)"
	runIn(delay, turnOnSwitch)
}

def turnOnSwitch() {
	theSwitch.on()
}