/**
 *  Light Up The Night
 *
 *  Author: SmartThings
 */
preferences {
	section("Monitor the luminosity...") {
		input "lightSensor", "capability.illuminanceMeasurement"
	}
	section("Turn on a light...") {
		input "lights", "capability.switch", multiple: true
	}
}

def installed() {
	subscribe(lightSensor, "illuminance", illuminanceHandler)
}

def updated() {
	unsubscribe()
	subscribe(lightSensor, "illuminance", illuminanceHandler)
}

// New aeon implementation
def illuminanceHandler(evt) {
	def lastStatus = state.lastStatus
	if (lastStatus != "on" && evt.integerValue < 30) {
		lights.on()
		state.lastStatus = "on"
	}
	else if (lastStatus != "off" && evt.integerValue > 50) {
		lights.off()
		state.lastStatus = "off"
	}
}
