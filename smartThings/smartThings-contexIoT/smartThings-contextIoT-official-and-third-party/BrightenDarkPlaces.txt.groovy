/**
 *  Brighten Dark Places
 *
 *  Author: SmartThings
 */
preferences {
	section("When the door opens...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("And it's dark...") {
		input "luminance1", "capability.illuminanceMeasurement", title: "Where?"
	}
	section("Turn on a light...") {
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	def lightSensorState = luminance1?.currentIlluminance as Float
	log.debug "SENSOR = $lightSensorState"
	if (lightSensorState != null && lightSensorState < 10) {
		log.trace "light.on() ... [luminance: ${lightSensorState}]"
		switch1.on()
	}
}
