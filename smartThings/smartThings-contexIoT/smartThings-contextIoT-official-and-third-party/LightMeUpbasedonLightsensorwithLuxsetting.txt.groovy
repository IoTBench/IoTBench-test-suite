/**
 *  Light Up The Night
 *
 *  Author: SmartThings 
 *  ADW added support for lumin value
 *      and off value will be lumin + 100 
 */
preferences {
	section("Monitor the luminosity...") {
		input "lightSensor", "capability.illuminanceMeasurement"
	}
	section("Turn on a light...") {
		input "lights", "capability.switch", multiple: true
	}
    section("For how dark...") {
		input "lumins", "number", title: "Illuminance?"
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
	if (lastStatus != "on" && evt.integerValue < (lumins)) {
		lights.on()
		state.lastStatus = "on"
	}
	else if (lastStatus != "off" && evt.integerValue > (lumins + 100)) {
		lights.off()
		state.lastStatus = "off"
	}
}
