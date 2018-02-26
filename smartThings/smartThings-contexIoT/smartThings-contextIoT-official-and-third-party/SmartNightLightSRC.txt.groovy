/**
 *  Smart Nightlight
 *
 *  Author: SmartThings / dome
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "Smart Night Light SRC",
    namespace: "",
    author: "dome",
    description: "A programmable and customizable night light app for controlling LED strip lighting. Requires Smart Room Controller - http://build.smartthings.com/projects/smartkitchen/",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
//	section("Control these lights..."){
//		input "lights", "capability.switch", multiple: true, required:false
//	}
    section("Control these LED strips") {
    	input "Ledstrip", "device.SmartRoomController", required: false, multiple: true
      	input "color", "enum", title: "What color?", required: true, multiple: false, options: ["White", "Red", "Green", "Blue"/*, "Orange", "Purple", "Yellow"*/]
    	input "brightness", "enum", title: "How bright?",required: true, multiple: false, options: ["Bright"/*, "Medium"*/, "Dim"]
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

def ledson(){
	if (brightness == "Dim")
    {
    	if (color == "Red") { Ledstrip.dimred() }
    	if (color == "Green") { Ledstrip.dimgreen() }
    	if (color == "Blue") { Ledstrip.dimblue() }
/*    	if (color == "Orange") { Ledstrip.dimorange() }
    	if (color == "Yellow") { Ledstrip.dimyellow() }
    	if (color == "Purple") { Ledstrip.dimpurple() }*/
    	if (color == "White") { Ledstrip.dimwhite() }    
    }
/*    if (brightness == "Medium")
    {
    	if (color == "Red") { Ledstrip.medred() }
    	if (color == "Green") { Ledstrip.medgreen() }
    	if (color == "Blue") { Ledstrip.medblue() }
    	if (color == "Orange") { Ledstrip.medorange() }
    	if (color == "Yellow") { Ledstrip.medyellow() }
    	if (color == "Purple") { Ledstrip.medpurple() }
    	if (color == "White") { Ledstrip.medwhite() }    
    }*/
    if (brightness == "Bright")
    {
    	if (color == "Red") { Ledstrip.red() }
    	if (color == "Green") { Ledstrip.green() }
    	if (color == "Blue") { Ledstrip.blue() }
/*    	if (color == "Orange") { Ledstrip.brightorange() }
    	if (color == "Yellow") { Ledstrip.brightyellow() }
    	if (color == "Purple") { Ledstrip.brightpurple() }*/
    	if (color == "White") { Ledstrip.white() }    
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
			log.debug "turning on lights due to motion"
//			lights.on()
            ledson()
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
	if (lastStatus != "off" && evt.integerValue > 50) {
//		lights.off()
        Ledstrip.off()
		state.lastStatus = "off"
	}
	else if (state.motionStopTime) {
		if (lastStatus != "off") {
			def elapsed = now() - state.motionStopTime
			if (elapsed >= (delayMinutes ?: 0) * 60000L) {
//				lights.off()
                Ledstrip.off()
				state.lastStatus = "off"
			}
		}
	}
	else if (lastStatus != "on" && evt.value < 30){
//		lights.on()
        ledson()
		state.lastStatus = "on"
	}
}

def scheduleCheck() {
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
//			lights.off()
            Ledstrip.off()
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

