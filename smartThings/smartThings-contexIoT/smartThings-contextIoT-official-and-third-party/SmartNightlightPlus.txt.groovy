/**
 *  Smart Nightlight
 *
 *  Author: Chrisb
 *
 */
definition(
    name: "Smart Nightlight Plus",
    namespace: "sirhcb",
    author: "chrisb",
    description: "Turns on lights when it's dark and motion is detected.  Turns lights off when it becomes light or some time after motion ceases.  Stay on or change off time when the switch is turned on.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Control these lights..."){
		input "lights", "capability.switch", multiple: true
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
    section ("When so I turn off it the switch was turn on? (optional, if nothing is entered, won't turn off.)") {
    	input "bigDelayMinutes", "number", title: "Minutes?", required: false
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
	}
    subscribe(lights, "switch.on", delayChange, [filterEvents: false])
    subscribe(lights, "switch.off", turnedOff)
    state.pushed = ""
}

def turnedOff(evt) {
	state.pushed = ""
	}
    
def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (enabled()) {
			log.debug "turning on lights due to motion"
			lights.on()
			state.lastStatus = "on"
		}
		state.motionStopTime = null
	}
	else {																					// Motion has stoped
		state.motionStopTime = now()
		if (state.pushed == "pushed") {														// The on button was pushed so...
        	if(bigDelayMinutes) {																// If the user set long delay then...
				runIn(bigDelayMinutes*60, turnOffMotionAfterDelay, [overwrite: false])			// Schedule the "lights off" for later.
			} else {																			// Otherwise...
				unschedule(turnOffMotionAfterDelay)												// Make sure lights don't go out.
			}
        } else {																			// The on button was NOT pushed so...
        	if(delayMinutes) {																	// If the user set a delay then...
				runIn(delayMinutes*60, turnOffMotionAfterDelay, [overwrite: false])				// Schedule the "lights off" for later.
			} else {																			// Otherwise...
				turnOffMotionAfterDelay()														// Run the lights off now.
			}
        }
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

def turnOffMotionAfterDelay(evt) {
	log.debug "In turnOffMotionAfterDelay"
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
			lights.off()
			state.lastStatus = "off"
		}
	}
}

def scheduleCheck() {
	log.debug "In scheduleCheck - skipping"
	//turnOffMotionAfterDelay()
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}

def delayChange(evt) {
	if (evt.isPhysical()) {
    	state.pushed = "pushed"
	}
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

