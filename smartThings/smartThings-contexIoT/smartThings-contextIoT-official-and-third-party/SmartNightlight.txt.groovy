/**
 *  Smart Nightlight
 *
 *  Author: SmartThings
 *
 */
preferences {
	section("Monitor the luminosity..."){
		input "lightSensor", "capability.illuminanceMeasurement"
	}
	section("And control these lights..."){
		input "lights", "capability.switch", multiple: true
	}
	section("Turning on when its dark and there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?"
	}
	section("And then off when its light or there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
}



def installed() {
	subscribe(motionSensor, "motion", motionHandler)
	subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
}

def updated() {
	unsubscribe()
	subscribe(motionSensor, "motion", motionHandler)
	subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (lightSensor.latestValue("illuminance") < 30) {
			lights.on()
			state.lastStatus = "on"
		}
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
