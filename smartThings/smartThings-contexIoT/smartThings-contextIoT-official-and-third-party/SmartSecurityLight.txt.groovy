/**
 *  Smart Security Light
 *
 *  Author: SmartThings
 *
 *
 * Adapted from SmartThings' Smart NightLight by Barry Burke
 *
 * Changes:
 *		2014/09/23		Added support for physical override:
 *						* If lights turned on manually, don't turn them off if motion stops
 *						  but DO turn them off at sunrise (in case the are forgotten)
 *						* Double-tap ON will stop the motion-initiated timed Off event
 *						* Double-tap OFF will keep the lights off until it gets light, someone manually
 *						  turns on or off the light, or another app turns on the lights.
 * 						* TO RE-ENABLE MOTION CONTROL: Manually turn OFF the lights (single tap)
 *		2014/09/24		Re-enabled multi-motion detector support. Still only a single switch (for now).
 *						Added option to flash the light to confirm double-tap overrides
 *						* Fixed the flasher resetting the overrides
 *		2014/09/25		More work fixing up overrides. New operation mode:
 *						* Manual ON any time overrides motion until next OFF (manual or programmatic)
 *						* Manual OFF resets to motion-controlled
 *						* Double-tap manual OFF turns lights off until next reset (ON or OFF) or tomorrow morning
 *						  (light or sunrise-driven)
 *		2014/09/26		Code clean up around overrides. Single ON tap always disables motion; OFF tap re-enables
 *						motion. Double-OFF stops motion until tomorrow (light/sunrise)
 *		2014/09/30		Replaced evt.isPhysical() with evt.physical
 *						switched to atomicState.xxx to fix some timing-related errors
 *						BUG: Occaisionally will flash lights when manually turned ON from OFF state
 *
 *
 */
definition(
	name: "Smart Security Light",
	namespace: "smartthings",
	author: "SmartThings & Barry Burke",
	description: "Turns on lights when it's dark and motion is detected.  Turns lights off when it becomes light or some time after motion ceases. Optionally allows for manual override.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Control this light..."){
		input "light", "capability.switch", multiple: false, required: true
	}
	section("Turning on when it's dark and there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?", multiple: true, required: true
	}
	section("And then off when it's light or there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
	section("Using either on this light sensor (optional) or the local sunrise and sunset"){
		input "lightSensor", "capability.illuminanceMeasurement", required: false
        input "luxLevel", "number", title: "Darkness Lux level?", defaultValue: 50, required: true
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
	section ("Overrides") {
    	paragraph "Manual ON disables motion control. Manual OFF re-enables motion control."
		input "physicalOverride", "bool", title: "Physical override?", required: true, defaultValue: false
		paragraph "Double-tap OFF to lock light off until next ON or sunrise. Single-tap OFF to re-enable to motion-controlled."
		input "doubleTapOff", "bool", title: "Double-Tap OFF override?", required: true, defaultValue: true
        paragraph ""
        input "flashConfirm", "bool", title: "Flash lights to confirm overrides?", required: true, defaultValue: false
	}
}

def installed() {
	log.debug "Installed with settings: $settings"
	initialize()
}

def updated() {
	log.debug "Updated with settings: $settings"
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(motionSensor, "motion", motionHandler)
	
	if (physicalOverride) {
		subscribe(light, "switch.on", lightsOnHandler)
		subscribe(light, "switch.off", lightsOffHandler)
	}
	if (physicalOverride || doubleTapOff) {
		subscribe(light, "switch", switchHandler, [filterEvents: false])
	}

	if (light.currentSwitch == "on") {
		atomicState.physicalSwitch = true
		atomicState.lastStatus = "on"
	}
	else {
		atomicState.physicalSwitch = false
		atomicState.lastStatus = "off"
	}
    atomicState.keepOff = false
    atomicState.flashing = false

	if (lightSensor) {
		subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
	}
	else {
		astroCheck()
		def sec = Math.round(Math.floor(Math.random() * 60))
		def min = Math.round(Math.floor(Math.random() * 60))
		def cron = "$sec $min * * * ?"
		schedule(cron, astroCheck) // check every hour since location can change without event?
	}
}

def lightsOnHandler(evt) {				
    if ( atomicState.flashing ) { return }
    log.debug "OnHandler isPhys $evt.physical"
    
    atomicState.keepOff = false				// if ANYTHING turns ON the light, then exit "keepOff" mode
    if (!evt.physical) { 
    	atomicState.lastStatus = "on"
        atomicState.physicalSwitch = false
    } // somebody turn on the lights
}

def lightsOffHandler(evt) {				// if anything turns OFF the light, then reset to motion-controlled
    if ( atomicState.flashing ) { return }
    log.debug "offHandler isPhys $evt.physical"
    
	atomicState.physicalSwitch = false
    atomicState.lastStatus = "off"
}
 
def switchHandler(evt) {
    if ( atomicState.flashing ) { return }
   
	log.debug "switchHandler: $evt.name: $evt.value isPhys $evt.physical"

	if (evt.physical) {
		if (evt.value == "on") {
        	if (physicalOverride) {
                log.debug "Override ON, disabling motion-control"
            	atomicState.keepOff = false
        		if (delayMinutes) { unschedule ("turnOffMotionAfterDelay") }
            	if (atomicState.lastStatus == "on" && flashConfirm) { flashTheLight() }
                atomicState.lastStatus = "on"
            	atomicState.physicalSwitch = true						// have to set this AFTER we flash the lights :)
			}
		} 
		else if (evt.value == "off") {
			atomicState.physicalSwitch = false							// Somebody physically turned off the light
	        atomicState.keepOff = false									// Single off resets keepOff & physical overrides

			// use Event rather than DeviceState because we may be changing DeviceState to only store changed values
			def recentStates = light.eventsSince(new Date(now() - 4000), [all:true, max: 10]).findAll{it.name == "switch"}
			log.debug "${recentStates?.size()} states found, last at ${recentStates ? recentStates[0].dateCreated : ''}"

			if (lastTwoStatesWere("off", recentStates, evt)) {
				log.debug "detected two OFF taps, override motion w/lights OFF"
                
				if (doubleTapOff) { 									// Double tap enables the keepOff
					if (delayMinutes) { unschedule("turnOffMotionAfterDelay") }
					if (flashConfirm) { flashTheLight() }
                    atomicState.keepOff = true							// Have to set this AFTER we flash the lights :)
                }
            }
		}
	}
}

private lastTwoStatesWere(value, states, evt) {
	def result = false
	if (states) {
		log.trace "unfiltered: [${states.collect{it.dateCreated + ':' + it.value}.join(', ')}]"
		def onOff = states.findAll { it.physical || !it.type }
		log.trace "filtered:   [${onOff.collect{it.dateCreated + ':' + it.value}.join(', ')}]"

		// This test was needed before the change to use Event rather than DeviceState. It should never pass now.
		if (onOff[0].date.before(evt.date)) {
			log.warn "Last state does not reflect current event, evt.date: ${evt.dateCreated}, state.date: ${onOff[0].dateCreated}"
			result = evt.value == value && onOff[0].value == value
		}
		else {
			result = onOff.size() > 1 && onOff[0].value == value && onOff[1].value == value
		}
	}
	result
}

def motionHandler(evt) {
	log.debug "motionHandler: $evt.name: $evt.value"

	if (atomicState.physicalSwitch) { return }	// ignore motion if lights were most recently turned on manually

	if (evt.value == "active") {
		if (enabled()) {
			log.debug "turning on light due to motion"
			light.on()
			atomicState.lastStatus = "on"
		}
		atomicState.motionStopTime = null
	}
	else {
    	if (atomicState.keepOff) {
        	if (delayMinutes) { unschedule("turnOffMotionAfterDelay") }
        	return 
        }
        
		atomicState.motionStopTime = now()
		if(delayMinutes) {
			unschedule("turnOffMotionAfterDelay")				// This should replace any existing off schedule
			runIn(delayMinutes*60, "turnOffMotionAfterDelay", [overwrite: false])
		} 
		else {
			turnOffMotionAfterDelay()
		}
	}
}

def illuminanceHandler(evt) {
    if ( atomicState.flashing ) { return }
    
	log.debug "$evt.name: $evt.value, lastStatus: $atomicState.lastStatus, motionStopTime: $atomicState.motionStopTime"

	def lastStatus = atomicState.lastStatus					// its getting light now, we can turn off
    
    if (atomicState.keepOff && evt.integerValue >= luxLevel) { atomicState.keepOff = false } // reset keepOff
    
	if ((lastStatus != "off") && (evt.integerValue >= luxLevel)) {	// whether or not it was manually turned on
		light.off()
		atomicState.lastStatus = "off"
		atomicState.physicalSwitch = false
        atomicState.keepOff = false							// it's a new day
	}
	else if (atomicState.motionStopTime) {
		if (atomicState.physicalSwitch) { return }					// light was manually turned on

		if (lastStatus != "off") {
			def elapsed = now() - atomicState.motionStopTime
			if (elapsed >= (delayMinutes ?: 0) * 60000L) {
				light.off()
				atomicState.lastStatus = "off"
                atomicState.keepOff = false
			}
		}
	}
	else if (lastStatus != "on" && evt.integerValue < luxLevel) {
        if ( atomicState.keepOff || atomicState.physicalSwitch ) { return }					// or we locked it off for the night
		light.on()
		atomicState.lastStatus = "on"
	}
}

def turnOffMotionAfterDelay() {
	log.debug "In turnOffMotionAfterDelay"

    if (atomicState.keepOff) {
        if (delayMinutes) { unschedule("turnOffMotionAfterDelay") }
        return 
    }						// light was manually turned on
    													// Don't turn it off

	if (atomicState.motionStopTime && atomicState.lastStatus != "off") {
		def elapsed = now() - atomicState.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
			light.off()
			atomicState.lastStatus = "off"
		}
	}
}

//def scheduleCheck() {
//	log.debug "In scheduleCheck - skipping"
	//turnOffMotionAfterDelay()
//}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}

private flashTheLight() {
    def doFlash = true
    def onFor = onFor ?: 500
    def offFor = offFor ?: 500
    def numFlashes = numFlashes ?: 2
    
    atomicState.flashing = true

    if (atomicState.lastActivated) {
        def elapsed = now() - atomicState.lastActivated
        def sequenceTime = (numFlashes + 1) * (onFor + offFor)
        doFlash = elapsed > sequenceTime
    }

    if (doFlash) {
        atomicState.lastActivated = now()
        def initialActionOn = light.currentSwitch != "on"
        def delay = 1L
        numFlashes.times {
            if (initialActionOn) {
                light.on(delay: delay)
            }
            else {
                light.off(delay:delay)
            }
            delay += onFor
            if (initialActionOn) {
                light.off(delay: delay)
            }
            else {
                light.on(delay: delay)
            }
            delay += offFor
        }
    }
//    atomicState.lastActivated = ""
    atomicState.flashing = false
}

private enabled() {
	def result
    
    if (atomicState.keepOff || atomicState.flashing) {
    	result = false								// if OFF was double-tapped, don't turn on
    }
    else if (lightSensor) {
		result = (lightSensor.currentIlluminance < luxLevel)
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
