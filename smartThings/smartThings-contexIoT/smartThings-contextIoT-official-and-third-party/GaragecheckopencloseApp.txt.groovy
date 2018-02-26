/**
 *  Garage check open/close App
 *
 *  Copyright 2014 Christopher Boerma
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
    name: "Garage check open/close App",
    namespace: "",
    author: "chrisb",
    description: "App to use with Ubi.  Checks if a garage is open or closed and opens or closes as needed.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
	section("Which Virtual Switch is the Close Trigger?") {
		input "triggerC", "capability.switch", title: "Which?"
	}
	section("Which Virtual Switch is the Open Trigger?") {
		input "triggerO", "capability.switch", title: "Which?"
	}
	section("Which Virtual Switch is the Close Check?") {
		input "checkC", "capability.switch", title: "Which?"
	}
	section("Which Virtual Switch is the Open Check?") {
		input "checkO", "capability.switch", title: "Which?"
	}
	section("Which door sensor should I check?"){
		input "door", "capability.contactSensor", title: "Which?" 
    }
    section("Which outlet/relay controls the Garage?"){
		input "outlet", "capability.switch", title: "Which?"
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
	switchesOff()
    subscribe(triggerC, "switch.on", garageClose)
	subscribe(triggerO, "switch.on", garageOpen)  
}

def switchesOff() {
	triggerC.off()
    triggerO.off()
    checkC.off()
    checkO.off()
}


def garageClose(evt) {
	//check if door is open
	if (door.currentContact == "closed") {
		//if it is closed, then turn on closed check - this signals Ubi to say that the door is already closed.
        checkC.on()
        }
    else {
    	//if it isn't close, then turn on the garage outlet.		
    	outlet.on()
    }
    //in 100 seconds turn off close check.
    runIn (100, switchesOff)
}

def garageOpen(evt) {
	//check if door is open
	if (door.currentContact == "open") {
		//if it is open, then turn on open check - this signals Ubi to say that the door is already open.
        checkO.on()
        }
    else {
    	//if it isn't open, then turn on the garage outlet.
    	outlet.on()
    }
    //in 100 seconds turn off close check.
    runIn (100, switchesOff)
}