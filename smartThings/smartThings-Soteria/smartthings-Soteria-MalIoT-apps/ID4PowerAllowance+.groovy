/**
 *  The app Power Allowance is modified by Z. Berkay Celik
 *  Email: zbc102@cse.psu.edu
 */
definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "Save energy or restrict total time an appliance (like a curling iron or TV) can be in use.  When a switch turns on, automatically turn it back off after a set number of minutes you specify.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When a switch turns on...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it off how many minutes later?") {
		input "minutesLater", "number", title: "When?"
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
	def delay = minutesLater * 60 * 0.5
	log.debug "Turning off in ${minutesLater} minutes (${delay}seconds)"
	runIn(delay, turnOffSwitch)
}

def turnOffSwitch() {
	theSwitch.off()
	turnOnSwitch()
}

def turnOnSwitch(){ // updated
	theSwitch.on()
}
