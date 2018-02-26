/**
 *  Smart Nightlight
 *
 *  Author: SmartThings
 *
 */
preferences {
		section("Control these lights..."){
		input "lights", "capability.switch", multiple: true
	}
	section("Turning on when there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?"
	}
	section("And then off when there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
}



def updated() {
	unsubscribe()
	subscribe(motionSensor, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
			lights.on()
			state.lastStatus = "on"
		state.motionStopTime = null
	}
	else {
		state.motionStopTime = now()
	}
}

def illuminanceHandler(evt) {
	log.debug "$evt.name: $evt.value, lastStatus: $state.lastStatus, motionStopTime: $state.motionStopTime"
	def lastStatus = state.lastStatus
	if (lastStatus != "on" && evt.integerValue < 30 && !motionSensor) {
		lights.on()
		state.lastStatus = "on"
	}
	else if (lastStatus != "off" && evt.integerValue > 50) {
		lights.off()
		state.lastStatus = "off"
	}
	else if (state.motionStopTime && lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
			lights.off()
			state.lastStatus = "off"
		}
	}
}
