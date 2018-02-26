/**
 *  Turn It On For x Minutes if another light is off
 *  Turn on a switch when a contact sensor opens and then turn it back off 5 minutes later.
 *
 *  Author: SmartThings
 */
definition(
    name: "Turn It On x Minutes if Light is Off",
    namespace: "dblanken",
    author: "dblanken",
    description: "When a SmartSense Multi is opened, a switch will be turned on, and then turned off after 5 minutes.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("When it turns off..."){
		input "switch1", "capability.switch"
	}
	section("Turn off a switch..."){
		input "switch2", "capability.switch"
	}
    section("For how many minutes after if still off?"){
    	input "waitMinutes", "decimal", required: false, defaultValue: 5
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(switch1, "switch.off", switchOffHandler)
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(switch1, "switch.off", switchOffHandler)

}

def switchOffHandler(evt) {
    def delay = 60 * waitMinutes
    runIn(delay, turnOffSwitch)	
}

// Look at the switches: if the switch in question is off and the one we want off as well is on, then turn it off
// Otherwise, end because when the switch goes off again, it'll resubscribe
def turnOffSwitch() {
	def dependentSwitchState = switch1.currentState("switch").value
    def targetSwitchState = switch2.currentState("switch").value
    
    if (dependentSwitchState == "off" && targetSwitchState == "on"){
    	//log.debug "I am off, so goodbye"
		switch2.off()
    }
}