/**
 *  Light Follows Me
 *
 *  Author: SmartThings & CoryS
 */

preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true, required: false 
	}
   
        section("Dim These Lights") {
        input "MultilevelSwitch", "capability.switchLevel", multiple: true, required: false
        }
   
   
    section("How Bright?"){
     input "number", "number", title: "Percentage, 0-99", required: false
    }
}



def installed()
{
	subscribe(motion1, "motion", motionHandler)
	schedule("0 * * * * ?", "scheduleCheck")
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
	unschedule()
	schedule("0 * * * * ?", "scheduleCheck")
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"

	if (evt.value == "active") {
		log.debug "turning on lights"
		switches.on()
                settings.MultilevelSwitch?.setLevel(number)
		state.inactiveAt = null
	} else if (evt.value == "inactive") {
		if (!state.inactiveAt) {
			state.inactiveAt = now()
		}
	}
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	if (state.inactiveAt) {
		def elapsed = now() - state.inactiveAt
		def threshold = 1000 * 60 * minutes1
		if (elapsed >= threshold) {
			log.debug "turning off lights"
			switches.off()
            MultilevelSwitch.off()
			state.inactiveAt = null
		}
		else {
			log.debug "${elapsed / 1000} sec since motion stopped"
		}
	}
}
