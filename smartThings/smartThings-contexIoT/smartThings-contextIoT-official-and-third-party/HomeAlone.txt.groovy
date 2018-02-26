/**
 *  Home Alone
 *
 *  Copyright 2015 Adam Ahrens
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
    name: "Home Alone",
    namespace: "",
    author: "Adam Ahrens",
    description: "Turn on random lights for random periods of time to mimic normal behavior while away from home.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When I'm in mode") {
		input "desiredMode", "mode", title: "Mode"
	}
    
    section("Turn on these lights randomly") {
        input "switches", "capability.switch", multiple: true, required: true
    }
    
    section("How long to keep lights on before selecting different ones") {
    	input "timeInMinutes", "number", title: "How many minutes", required: true
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
	// Need to know whenever the mode changes
    subscribe(location, "mode", changedLocationMode)
}

def changedLocationMode(event) {
	if (location.getCurrentMode() != desiredMode) {
    	unschedule(turnOnRandomLights)
        return
    }
    
    runIn(timeInMinutes * 60, turnOnRandomLights)
}

// Grabs a random number of lights that the User selected
// Turns them on
def turnOnRandomLights() {
	def randomSwitches = collectRandomSwitches(switches)
    randomSwitches*.on()
    
    def excludedSwitches = switches - randomSwitches
    excludedSwitches*.off()
    
    // Reschedule
    runIn(timeInMinutes * 60, turnOnRandomLights)
}

def collectRandomSwitches(switches) {
	// grab some random number based on switches.size
	def randomNumber = new Random().nextInt(switches.size() - 1) + 1
	
    // shuffle the order of switches
	def randomSwitches = switches.collect()
	Collections.shuffle(randomSwitches)
	
    // return the switches between 0 and randomNumber indexes
	randomSwitches[0..randomNumber]
}