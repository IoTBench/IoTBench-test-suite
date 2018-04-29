/**
 *  Contact Closed, Switch On, then switch off
 *
 *  Copyright 2014 Paul Toben with help from Smartthings example code
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
    name: "Contact Closed, Switch On",
    namespace: "",
    author: "Paul Toben",
    description: "Turns a switch on when a contact sensor is closed, then turns it back off again.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section ("When the door closes...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section ("Turn on a switch...") {
		input "switch1", "capability.switch"
	}
	section("Turn it off how many seconds later?") {
		input "secondsLater", "number", title: "When?"
	}

}


def installed() {
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def contactClosedHandler(evt) {
	switch1.on()
    log.debug "Switch is on"
	def delay = secondsLater * 1000
    log.debug "Delay is ${delay} ms"
	switch1.off(delay: delay)
    log.debug "Switch is off"
}
