/**
 *  Arrival-based Night Light with Smart Auto-off
 *
 *  Copyright 2015 Michael Kowalchuk
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
    name: "Arrival-based Night Light with Smart Auto-off",
    namespace: "mjk",
    author: "Michael Kowalchuk",
    description: "Turn a light on when arriving if it's dark outside, then turn it back off after a period of time if they haven't been manually turned off already.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("When one of these people arrives") {
		input "people", "capability.presenceSensor", multiple: true
	}
    section("And it's dark...") {
		input "luminance", "capability.illuminanceMeasurement", title: "Where?"
	}
    section("Turn on this light...") {
		input "selectedSwitch", "capability.switch", multiple: false, title: "Where?"
	}
    section("And turn it off after..."){
		input "turnOffAfterMinutes", "number", title: "How many minutes?"
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
    state.timeLastTriggered = 0
	subscribe(people, "presence", presenceHandler)
}

def switchTimedOut() {
    log.debug "Timeout elapsed. Turning light off."
    selectedSwitch.off()
    cancelAll()
}

def switchOffHandler(evt) {
    log.debug "Light turned off manually. Cancelling timeout."
	cancelAll()
}

def cancelAll() {
    unschedule(switchTimedOut)
    unsubscribe(switchOffHandler)
}

def presenceHandler(evt) {
    if (evt.value != "present") {
        debug.log "evt isn't for presence."
    	return
    }
    if (selectedSwitch.latestState("switch") == "on") {
        log.debug "Light already on. Ignoring arrival."
        return
    }
    if (luminance.currentIlluminance > 40) {
        log.debug "Too bright. Ignoring arrival."
        return
    }
    if (now() < (state.timeLastTriggered + 2000*60)) { 
        log.debug "Triggered too recently. Ignoring arrival."
        return
    }


	selectedSwitch.on()

	def delaySeconds = 60 * turnOffAfterMinutes
    runIn(delaySeconds, switchTimedOut)

    subscribe(selectedSwitch, "switch.off", switchOffHandler)

	state.timeLastTriggered = now()
}