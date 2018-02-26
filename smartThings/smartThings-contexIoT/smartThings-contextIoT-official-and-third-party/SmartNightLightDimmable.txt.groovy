/**
 *  Smart Night Light Dimmable
 *
 *  Copyright 2014 Tim Slagle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "Smart Night Light - Dimmable",
    namespace: "",
    author: "Tim Slagle",
    description: "Smart night light but with dim option.",
    category: "Convenience",
    iconUrl: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png",
    iconX2Url: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png"
)

preferences {
	section("Control these lights..."){
		input "lights", "capability.switch", multiple: true
    }    
    section  ("Set the following dimmers..."){ 
        input "dimmers", "capability.switchLevel", multiple: true, required:false
        input "level", "number", title: "How bright?, 0-99", required:false
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
		schedule("0 0 0/1 1/1 * ?", astroCheck) // check every hour since location can change without event?
		schedule("0 * * * * ?", scheduleCheck)
	}
}


def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (enabled()) {			
            log.debug "turning on lights due to motion"
			settings.dimmers?.setLevel(level)
            lights.on()
			state.lastStatus = "on"
            state.dimmers = "on"
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
	if (lastStatus != "off" && evt.integerValue > 999) {
		lights.off()
        dimmers.off()
        state.dimmers = "off"
		state.lastStatus = "off"
	}
	else if (state.motionStopTime) {
		if (lastStatus != "off") {
			def elapsed = now() - state.motionStopTime
			if (elapsed >= (delayMinutes ?: 0) * 60000L) {
				lights.off()
                dimmers.off()
        		state.dimmers = "off"
				state.lastStatus = "off"
			}
		}
	}
	else if (lastStatus != "on" && evt.value < 30){
		lights.on()
        settings.dimmers?.setLevel(level)
		state.lastStatus = "on"
	}
}


def scheduleCheck() {
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
        	log.debug "Turning off ${lights} and ${dimmers} now"
			lights.off()
            dimmers.off()
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

