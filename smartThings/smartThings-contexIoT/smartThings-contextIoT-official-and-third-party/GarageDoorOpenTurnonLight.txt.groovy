/**
 *  Garage Door Open - Turn on Light
 *
 *  Copyright 2015 Curtis Lesperance
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
    name: "Garage Door Open - Turn on Light",
    namespace: "CJL",
    author: "Curtis Lesperance",
    description: "When the garage door opens turn on the selected light(s) and optional only when the sun has set.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("When the garage door opens..."){
    	input "garageContact", "capability.contactSensor", title: "Where?"
    }
    section("Turn on these lights..."){
    	input "switches", "capability.switch", multiple: true
    }
    section("Turn off these lights after X minutes..."){
    	input "switchesOff", "capability.switch", multiple: true
        input "threshold", "number", title: "Minutes", required: false
    }
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
}

def installed()
{
	subscribe(garageContact, "contact.open", contactOpenHandler)
}

def updated()
{
    unsubscribe()
    subscribe(garageContact, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
    log.debug "$evt.value: $evt, $settings"

    //Check current time to see if it's after sundown.
    def s = getSunriseAndSunset(sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
    def now = new Date()
    def setTime = s.sunset
    def minuteDelay = threshold.toInteger() * 60
    log.debug "Sunset is at $setTime. Current time is $now"


    if (setTime.before(now)) {	//Executes only if it's after sundown.
        
        log.trace "Turning on switches: $switches"
        switches.on()
        
        log.debug "runIn($minuteDelay)"
        runIn(minuteDelay, turnSwitchesOff)
    }
}

def turnSwitchesOff(){
    log.debug "Turning off Switches"
	switchesOff.off()
}

private getSunriseOffset() {
    log.debug "Sunrise offset $sunriseOffsetValue"
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
    log.debug "Sunset offset $sunsetOffsetValue"
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}
