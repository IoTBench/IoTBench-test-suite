/**
 *  Nightlight with Hue
 *
 *  Copyright 2014 Sean Atkinson
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

definition(
    name: "Smart Nightlight with Hue",
    namespace: "smartthings",
    author: "Sean Atkinson",
    description: "Turns on lights when it's dark and motion is detected.  Turns lights off when it becomes light or some time after motion ceases.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Control these bulbs...") {
			input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:true, multiple:true
		}
	section("Choose light effects...")
			{
				input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: [
					["Soft White":"Soft White - Default"],
					["White":"White - Concentrate"],
					["Daylight":"Daylight - Energize"],
					["Warm White":"Warm White - Relax"],
					"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
				input "lightLevel", "enum", title: "Light Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
			}
	section("Turning on when it's dark and there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?"
	}
	section("And then off when it's light or there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
	section("Using this light sensor..."){
		input "lightSensor", "capability.illuminanceMeasurement", required: true
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
}


def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (enabled()) {
			log.debug "turning on lights due to motion"
			takeAction(evt)
			state.lastStatus = "on"
		}
		state.motionStopTime = null
	}
	else {
		state.motionStopTime = now()
		if(delayMinutes) {
			runIn(delayMinutes*60, turnOffMotionAfterDelay, [overwrite: false])
		} else {
			turnOffMotionAfterDelay()
		}
	}
}

def illuminanceHandler(evt) {
	log.debug "$evt.name: $evt.value, lastStatus: $state.lastStatus, motionStopTime: $state.motionStopTime"
	def lastStatus = state.lastStatus
	if (lastStatus != "off" && evt.integerValue > 50) {
		hues.off()
		state.lastStatus = "off"
	}
	else if (state.motionStopTime) {
		if (lastStatus != "off") {
			def elapsed = now() - state.motionStopTime
			if (elapsed >= (delayMinutes ?: 0) * 60000L) {
				hues.off()
				state.lastStatus = "off"
			}
		}
	}
	else if (lastStatus != "on" && evt.value < 30){
		takeAction(evt)
		state.lastStatus = "on"
	}
}

def turnOffMotionAfterDelay() {
	log.debug "In turnOffMotionAfterDelay"
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
			hues.off()
			state.lastStatus = "off"
		}
	}
}

def scheduleCheck() {
	log.debug "In scheduleCheck - skipping"
	//turnOffMotionAfterDelay()
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

private takeAction(evt) {


	if (frequency) {
		state[frequencyKey(evt)] = now()
	}

	def hueColor = 0
	def saturation = 100

	switch(color) {
		case "White":
			hueColor = 52
			saturation = 19
			break;
		case "Daylight":
			hueColor = 53
			saturation = 91
			break;
		case "Soft White":
			hueColor = 23
			saturation = 56
			break;
		case "Warm White":
			hueColor = 20
			saturation = 80 //83
			break;
		case "Blue":
			hueColor = 70
			break;
		case "Green":
			hueColor = 39
			break;
		case "Yellow":
			hueColor = 25
			break;
		case "Orange":
			hueColor = 10
			break;
		case "Purple":
			hueColor = 75
			break;
		case "Pink":
			hueColor = 83
			break;
		case "Red":
			hueColor = 100
			break;
	}

	state.previous = [:]

	hues.each {
		state.previous[it.id] = [
			"switch": it.currentValue("switch"),
			"level" : it.currentValue("level"),
			"hue": it.currentValue("hue"),
			"saturation": it.currentValue("saturation")
		]
	}

	log.debug "current values = $state.previous"

	def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
	log.debug "new value = $newValue"

	hues*.setColor(newValue)
}