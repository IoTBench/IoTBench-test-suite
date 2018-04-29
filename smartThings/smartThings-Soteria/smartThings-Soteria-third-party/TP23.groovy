/**
 *  Turn on temporarily with motion, lock out for a while after motion
 *
 *  written by Aaron Herzon
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
    name: "Turn on temporarily with motion, lock out for a while after motion",
    namespace: "AaronZON",
    author: "Aaron Herzon",
    description: "SmartApp that turns on a switch for selected length of time when motion is sensed, then ignores motion for some time before enabling again.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select Motion Sensor(s) you want to Use") {
        input "motions", "capability.motionSensor", title: "Motion Detectors", required: true, multiple: true
	}
    section("Select switch(es) you want to Use") {
        input "switches", "capability.switch", title: "Switches", required: true, multiple: true
	}
    section ("Set time values") {
		input "onTimeMinutes", "number", title: "On time, minutes", required: true, defaultValue: 1
        
        input "ignoreTimeMinutes", "number", title: "Ignore motion after off time, minutes", required: true, defaultValue: 30
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motions, "motion.active", handleMotionEvent)
}

def handleMotionEvent(evt) {
	log.debug "Motion detected, turn swith(es) on for $onTime seconds and disable for $ignoreTimeMinutes minutes"
    switches?.on()
    runIn(onTimeMinutes*60,turnOff)  
    runIn((ignoreTimeMinutes*60 + onTimeMinutes*60),initialize)
    unsubscribe() 
}

def turnOff() {
    switches?.off()
    log.info "Switch(es) off"
}