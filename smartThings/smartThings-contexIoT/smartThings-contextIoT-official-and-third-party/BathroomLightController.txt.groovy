/**
 *  Bathroom Light Controller
 *
 *  Copyright 2015 Alex Guyot
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
    name: "Bathroom Light Controller",
    namespace: "BathroomLightController",
    author: "Alex Guyot",
    description: "Bathroom light turns on when both doors are closed, off ten minutes later or when one or both doors are opened.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    // What doors should this app be configured for?
    section ("When these doors are both closed, or at least one is open...") {
        input "contact1", "capability.contactSensor"
        input "contact2", "capability.contactSensor"
    }
    // What light should this app be configured for?
    section ("Turn on/off a light...") {
        input "switch1", "capability.switch"
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
    subscribe(contact1, "contact", contactHandler1)
    subscribe(contact2, "contact", contactHandler2)
    subscribe(switch1, "switch", timeoutHandler)
}

// event handlers are passed the event itself
def contactHandler1(evt) {
    log.debug "$evt.value"

    if (evt.value == "open") { // if at least one door is open
        switch1.off(); // turn off the switch
    } else if (evt.value == "closed") { // if this door is closed
        if (contact2.currentValue("contact") == "closed") { // if the other door is also closed
        	switch1.on(); // turn on the switch
        }
    }
}

def contactHandler2(evt) {
    log.debug "$evt.value"

    if (evt.value == "open") { // if at least one door is open
        switch1.off(); // turn off the switch
    } else if (evt.value == "closed") { // if this door is closed
        if (contact1.currentValue("contact") == "closed") { // if the other door is also closed
        	switch1.on(); // turn on the switch
        }
    }
}

def timeoutHandler(evt) {
    // execute handler in ten minutes from now
    runIn(60*10, switchHandler)
}

def switchHandler() {
    switch1.off()
}
