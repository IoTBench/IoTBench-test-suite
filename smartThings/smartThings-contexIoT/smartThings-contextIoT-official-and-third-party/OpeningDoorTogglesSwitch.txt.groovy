/**
 *  Opening Door Toggles Switch
 *
 *  Author: SmartThings
 */
definition(
    name: "Opening Door Toggles Switch",
    namespace: "smartthings",
    author: "cdoyle",
    description: "Use your door as a switch. Go into a room to turn the light on, when you leave it shuts off after you go.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet-luminance@2x.png"
)

preferences {
	section("When the door opens...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Toggle a switch...") {
		input "switch1", "capability.switch"
	}
    section("And leave it on for how many minutes (optional)?") {
		input "minutesDelay", "number", title: "How long?", multiple: false, required: false
	}
}

def installed() {
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
    def switchValue = switch1.currentValue("switch")
    log.debug "switch = $switchValue"

	if (switchValue == "off") {
		log.trace "toggling light.on()"
    	switch1.on()
    }
    else {
    	def runDelay = minutesDelay
        runDelay = (runDelay != null && runDelay >= 0) ? runDelay * 60 : 0
		log.trace "toggling light.off() ... in $runDelay seconds"
        runIn(runDelay, "switchOff")
	}
}

private def switchOff() {
	switch1.off()
}
