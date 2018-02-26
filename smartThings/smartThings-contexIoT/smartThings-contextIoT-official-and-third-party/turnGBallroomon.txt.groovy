/**
 *  turn_GB_all_room_on
 *
 *  Copyright 2014 gibyeong park
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
    name: "turn_GB_all_room_on",
    namespace: "GB",
    author: "gibyeong park",
    description: "for test",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Turn on which A/C or fan...") {
		input "switch1", "capability.switch", required: true
	}
	section("Turn on when there's movement..."){
		input "ct1", "capability.ContactSensor", title: "Where?"
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

	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(switch1, "switch.on", switchHandler)
    subscribe(switch1, "switch.off", switchHandler)
    subscribe(ct1, "contact.open", ctopenHandler)
    subscribe(ct1, "contact.closed", ctclosedHandler)

}

// TODO: implement event handlers
def switchHandler() {
}

def ctopenHandler() {
	switch1?.on()
}
def ctclosedHandler() {
	switch1?.off()
}