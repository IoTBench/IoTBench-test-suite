/**
 *  Door Open Warning Light
 *
 *  Copyright 2015 Brian Brewder
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
	name: "Door Open Warning Light",
	namespace: "bbrewder",
	author: "Brian Brewder",
	description: "Turns on a light if a door is left open. When all the doors are closed, turns the light back off.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	// What doors should this app be configured for?
	section ("If the door is left open...") {
		input "contacts", "capability.contactSensor", title: "Doors?", multiple: true
	}
	section ("And nobody is in the room...") {
		input "sensors", "capability.motionSensor", title: "Motion Sensors?", multiple: true
	}
	section ("For too long...") {
		input name: "wait", title: "Minutes?", defaultValue: "10", type: "number"
	}
	// What light should this app be configured for?
	section ("Turn on this light...") {
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
	subscribe(contacts, "contact", contactHandler)
	subscribe(sensors, "motion", sensorHandler)
    state.scheduledLightOn = false
}

// event handlers are passed the event itself
def contactHandler(evt) {
	log.debug " Contact changed state."

	checkForWarning();
}

// event hanlders are passed the event itself
def sensorHandler(evt) {
	log.debug " Sensor changed state (${evt.displayName}: ${evt.value})."

	checkForWarning();
}

def checkForWarning() {
	def isOpen = isDoorOpen()
	def isOccupied = isRoomOccupied()
	def isOn = switch1.currentValue('switch') == 'on'

	// The contactSensor capability can be either "open" or "closed"
	// If it's "open", turn on the light!
	// If it's "closed" turn the light off.
	if (!isOpen || isOccupied) {
		if(isOn) {
			log.debug " All doors are closed or the room is occupied. Turn off light."
			switch1.off()
		}
        
        if(state.scheduledLightOn) {
			unschedule('turnOnLight');
            state.scheduledLightOn = false;
        }
	} else if (isOn) {
		log.debug " The switch is already on."
	} else if (wait > 0) {
		log.debug " Re-check the state in $wait minutes"
		runIn(60*wait, turnOnLight)
        state.scheduledLightOn = true;
	} else { 
		log.debug " There is an open door and the room is not occupied. Turn the light on."
		switch1.on();
	}
}

def turnOnLight() {
	log.debug " Check to see if we still need to turn on the light."
    state.scheduledLightOn = false;
	if (isDoorOpen() && !isRoomOccupied()) {
		log.debug " The door is still open and the room is not occupied. Turn the light on."
		switch1.on()
	}
}

def isDoorOpen() {
	contacts.find({ 
		if(it.contactState == null) return false;
		it.contactState.value == "open" 
	}) != null
}

def isRoomOccupied() {
	sensors.find({ 
		if(it.motionState == null) return false;
		it.motionState.value == "active" 
	}) != null
}
