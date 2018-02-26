/**
 *  Light Follows Me
 *
 *  Author: SmartThings
 */

preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true
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
			state.inactiveAt = null
		}
		else {
			log.debug "${elapsed / 1000} sec since motion stopped"
		}
	}
}
