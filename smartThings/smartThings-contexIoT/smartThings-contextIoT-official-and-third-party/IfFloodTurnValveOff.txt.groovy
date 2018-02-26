/**
 *  If Flood Turn Valve Off
 *
 *  Copyright 2014 Todd Wackford
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
    name: "If Flood Turn Valve Off",
    namespace: "bobkantor",
    author: "Todd Wackford",
    description: "If moisture is detected, turn a valve off. Code from Todd Wackford.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When there's water detected...") {
		input "alarm", "capability.waterSensor", title: "Where?", multiple:true
	}
    section("Turn this valve off...") {
    	input "valve", "capability.valve", title: "Which Valve?", required: true
    }
	section("Text me at...") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed() {
	subscribe(alarm, "water.wet", waterWetHandler)
}

def updated() {
	unsubscribe()
	subscribe(alarm, "water.wet", waterWetHandler)
}

def waterWetHandler(evt) {
	def deltaSeconds = 60

	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = alarm.eventsSince(timeAgo)
	log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

	def alreadySentSms = recentEvents.count { it.value && it.value == "wet" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone within the last $deltaSeconds seconds"
	} else {
    	if( valve ) {
           	valve?.close()
            log.debug "${valve.name} is closing"
        }
		def msg = "${alarm.label} ${alarm.name} is wet! ${valve.label} ${valve.name} has been turned off."
		sendPush(msg)
		if (phone) {
			sendSms(phone, msg)
            log.debug "$alarm is wet, texting $phone"
		}
	}
}