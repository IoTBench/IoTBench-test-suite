/**
 *  Improved Smart Nightlight
 *
 *  Author: chrisb
 *	Date: 2014/01/23
 *
 *  I recommend creating a virtual device... and On/Off Button Tile that you can use to turn on or off this App.
 */
preferences {
	section("Control these lights..."){
		input "lights", "capability.switch", multiple: true
	}
    section("What do I use for the on/off switch?") {
    	input "powerSwitch", "capability.switch"
	}    
	section("Turning on when it's dark and there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?"
	}
	section("And then off when it's light or there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
	section("Using either on this light sensor (optional) or the local sunrise and sunset"){
		input "lightSensor", "capability.illuminanceMeasurement", required: false
	}
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Zip code (optional, defaults to location coordinates when location services are enabled)...") {
		input "zipCode", "text", required: false
	}
}



def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(motionSensor, "motion", motionHandler)
	if (lightSensor) {
		subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
	}
	else {
		astroCheck()
		schedule("0 1 * * * ?", astroCheck) // check every hour since location can change without event?
		schedule("0 * * * * ?", scheduleCheck)
	}
}


def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (enabled()) {
        	if (powerSwitch.currentSwitch == "on") {
				log.debug "turning on lights due to motion"
				lights.on()
				state.lastStatus = "on"
            }
            else {
            log.debug "Control switch is off, not turning on light."
            }
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
	if (lastStatus != "off" && evt.integerValue > 50) {
		lights.off()
		state.lastStatus = "off"
	}
	else if (state.motionStopTime) {
		if (lastStatus != "off") {
			def elapsed = now() - state.motionStopTime
			if (elapsed >= (delayMinutes ?: 0) * 60000L) {
				lights.off()
				state.lastStatus = "off"
			}
		}
	}
	else if (lastStatus != "on" && evt.value < 30){
		lights.on()
		state.lastStatus = "on"
	}
}

def scheduleCheck() {
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
			lights.off()
			state.lastStatus = "off"
		}
	}
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}

private enabled() {
	def result
	if (lightSensor) {
		result = lightSensor.currentIlluminance < 30
	}
	else {
		def t = now()
		result = t < state.riseTime || t > state.setTime
	}
	result
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

