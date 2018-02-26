/**
 *  Welcome Home at Night
 *
 *  Copyright 2014 Adam Platt
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
    name: "Welcome Home at Night",
    namespace: "platta",
    author: "Adam Platt",
    description: "When presence is detected and the time is after sunset, turn on the lights. Designed for turning on exterior lights when arriving home at night.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When People Arrive...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
    section("Turn on lights...") {
    	input "switch1", "capability.switch", multiple: true
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
	subscribe(presence1, "presence", presenceHandler)
}

def presenceHandler(evt) {
	if (evt.value == "present") {
    	log.debug "Presence detected."
        
        // Get the times for sunrise and sunset based on the hub's location
		def sunTimes = getSunriseAndSunset()
		if (sunTimes.sunset.before(new Date())) {
        	log.debug "Presence detected after sunset. Turning on lights."
            
        	switch1.on()
        }
    }
}