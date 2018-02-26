/**
 *  On, But Not Forgotten
 *
 *  Turn your lights on when an open/close sensor opens and off when the 
 *    sensor closes or after specified period of time.
 *
 *  Can optionally provide a delay between door close and light off.
 *
 *
 *	Uses
 *
 *    - Keep your garage lights on for a few minutes after 
 *      your garage door closes. You know... so you can see stuff.)
 *  
 *    - Closet. Turn lights on/off when door is open/closed, but 
 *      shut them off if the door is left open.
 *
 *    - Basic on when door is open, off when door is closed.
 *
 *
 *  Copyright 2014 Matthew Nichols
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
    name: "On, But Not Forgotten",
    namespace: "name.nichols.matt.smartapps",
    author: "Matthew Nichols",
    description: "Turns on a switch when a contact sensor opens, turns off when closed or after given period",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When the door opens/closes...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Turn on/off a light...") {
		input "switch1", "capability.switch"
	}
    section("Pause this many minutes after door closed before turning off...") {
		input name: "pauseMinutes", title: "Minutes?", type: "number", multiple: false, required: false
	}
    section("If door does not close, turn the light off after this amount of time...") {
		input name: "offMinutes", title: "Minutes?", type: "number", multiple: false, required: false
	}
    section("Force the light off after this amount of time (regardless of how it was turned on)...") {
		input name: "forceOffMinutes", title: "Minutes?", type: "number", multiple: false, required: false
	}
}

def installed() {
	subscribe(contact1, "contact", contactHandler)
	subscribe(switch1, "switch.on", switchOnHandler)
	subscribe(switch1, "switch.off", switchOffHandler)
    state.doorTrigger = false
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact", contactHandler)
	subscribe(switch1, "switch.on", switchOnHandler)
	subscribe(switch1, "switch.off", switchOffHandler)
    state.doorTrigger = false
}

def contactHandler(evt) {
	log.info "Door $evt.value"
	if (evt.value == "open") {
    	// Always reset the schedule when the door opens.
    	unschedule("scheduledTurnOff")
        state.doorTrigger = true
        if (switch1.latestValue("switch") == "off") {
			switch1.on()
        }
        else {
        	scheduleLightShutOff()
        }
	} else if (evt.value == "closed") {
		if ((pauseMinutes ?: 0) > 0) {
	        if (switch1.latestValue("switch") == "on") {
				shutOffIn(pauseMinutes * 60)
            }
		} else {
			switch1.off()
		}
	}
}

def switchOnHandler(evt)
{
	scheduleLightShutOff()
}

def scheduleLightShutOff() {
	def off = (offMinutes ?: 0) * 60
	def forced = (forceOffMinutes ?: 0) * 60
    
    def timeout = 0
    if (state.doorTrigger) {
    	// Use the smallest timeout
    	if (off > 0) timeout = off
        if (forced > 0) {
        	if (timeout > 0) {
            	timeout = timeout < forced ? timeout : forced
            }
            else {
            	timeout = forced
           	}
        }
    }
    else {
    	timeout = forced
    }
    
	if (timeout > 0) {
		shutOffIn(timeout)
	}
    state.doorTrigger = false
}

def shutOffIn(seconds) {
	unschedule("scheduledTurnOff")
	log.info("Scheduling light shutoff in ${seconds} seconds")
	runIn(seconds, "scheduledTurnOff")
}

def switchOffHandler(evt)
{
	unschedule("scheduledTurnOff")
}

def scheduledTurnOff() {
	switch1.off()
	unschedule("scheduledTurnOff") // Temporary work-around to scheduling bug
}
