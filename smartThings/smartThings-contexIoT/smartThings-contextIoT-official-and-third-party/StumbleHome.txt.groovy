/**
 *  StumbleHome
 *
 *  Author: b.dahlem@gmail.com
 *  Date: 2013-08-21
 *
 */
 
preferences {
	section("When any of these people arrive home:") {
		input "people", "capability.presenceSensor", multiple: true
    }
    section("Turn on these lights:") {
    	input "lights", "capability.switch", multiple: true
    }
    section("Until one of these doors opens and closes:") {
    	input "doors", "capability.contactSensor", multiple: true
    }
    section("Or for this many minutes (default 3):") {
    	input "delay", "number", title: "minutes", required: false
    }

    section("Based either on this light sensor (optional) or the local sunrise and sunset"){
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
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	unschedule()
    initialize()
}

def initialize() {
	subscribe(people, "presence", presenceHandler)
	subscribe(doors, "contact", doorHandler)
    
	if (lightSensor) {
	}
	else {
		astroCheck()
		schedule("0 1 * * * ?", astroCheck) // check every hour since location can change without event?
	}
}

def astroCheck() {
	// Calculate the sunrise and sunset time for this zipcode
    
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}


private isDark() {
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

def presenceHandler(evt) {
	// Handle someone arriving
    
	log.debug "$evt.name: $evt.value"
    
    if ((evt.value == "present") && isDark()) {						// If someone got home and it's dark out
    	log.debug "$evt.displayName has arrived after dark"
        
		def offDelay = delay != null ? delay * 60000 : 3 * 60000	// Calculate the number of milliseconds of delay	
        state.offTime = now() + offDelay							// and the time to turn the lights back off

        turnOnLights()												// Turn on the pathway lights
        unschedule(turnOffLights)
        runIn(delay != null ? delay * 60 : 3 * 60, turnOffLights)	// And schedule them to turn off automatically if they aren't turned
        															// off some other way
    }
}


def doorHandler(evt) {
	// Handle a door closing

	if (state.litUp == true) {													// If the lights are on
    	log.debug "lights are on"
        if (now() < state.offTime) {											// and the timeout hasn't occurred
            if (evt.value == "closed") {
            	log.debug "Door closed, turning off the lights"					// go ahead and turn off the lights
                state.litUp = false
                
                turnOffLights()
            }
        }
        else {
        	log.debug "Door closure timed out, lights should already be off"	// if the timeout has occurred, cancel
        	state.litUp = false
        }
    }

}

def turnOnLights() {
	def map = state.lightMap ?: [:]												// Set up an array to store whether individual
																				// lights are being controlled
	state.litUp = true;															// Remember that we turned on the lights

	for (light in lights) {														// Go through all the lights
    	if (light.currentSwitch == "off") {								 		// if they are off
        	light.on()															// turn them on
            log.info "StumbleHome turned on ${light.displayName}."
			map[light.id] = true												// and note that they were turned on
        } 
        else {
        	log.info "StumbleHome skipped turning on ${light.displayName}."
        }
	}
    
    state.lightMap = map														// Save the array into the permanent state
    
	log.debug "lightMap: $state.lightMap"
}

def turnOffLights() {
	def map = state.lightMap ?: [:]												// Set up an array to store whether individual

	for (light in lights) {														// Go through all the lights
    	if (map[light.id] == true) {											// if they were turned on by the controller
        	light.off()															// turn them off
            map[light.id] = null												// and clear the note about eachlight
        }
    }

	state.lightMap = map														// Save the array into the permanent state

    unschedule(turnOffLights)													// Stop turnOffLights from running again
}
