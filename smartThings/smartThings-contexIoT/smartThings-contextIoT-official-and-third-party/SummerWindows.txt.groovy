/**
 *  Close/Open the Windows
 *
 *  Copyright 2014 Scottin Pollock
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
    name: "Summer Windows",
    namespace: "soletc.com",
    author: "Scottin Pollock",
    description: "Sends a notification to open/close windows when the outside temperature is lower/higher than the inside temperature.",
    category: "My Apps",
    iconUrl: "http://solutionsetcetera.com/stuff/STIcons/windows.png",
    iconX2Url: "http://solutionsetcetera.com/stuff/STIcons/windows@2x.png")


preferences {
	section("Outdoor sensor...") {
		input "temperatureSensorOut", "capability.temperatureMeasurement"
	}
    section("Indoor sensor...") {
		input "temperatureSensorIn", "capability.temperatureMeasurement"
	}
    section("How much cooler or warmer...") {
		input "offset", "number", title: "how many degrees.", required: true
	}
    section("Open/Close what?") {
		input "noteSuffix", "text", title: "Room and Door/Windows", required: true
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
	subscribe(temperatureSensorIn, "temperature", temperatureHandler)
}

// TODO: implement event handlers

def temperatureHandler(evt) {
	def inIS = evt.doubleValue
    def lastOut = temperatureSensorOut.currentTemperature - offset
    log.debug "in is $inIS and out is $lastOut"
    if (inIS > lastOut) {
        if (state.windows != "open") {
        	send("It's cooling off outside, open the ${settings.noteSuffix}!")
        	state.windows ="open"
            log.debug "open"
        }
    } else if (inIS < lastOut) {
    	if (state.windows != "closed") {
        	send("It's warming up outside, close the ${settings.noteSuffix}!")
        	state.windows ="closed"
            log.debug "closed"
    	}
  	}
}

private send(msg) {
	log.debug msg
	sendPush( msg )
}