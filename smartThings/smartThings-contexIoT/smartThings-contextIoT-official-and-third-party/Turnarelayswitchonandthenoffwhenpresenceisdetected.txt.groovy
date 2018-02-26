/**
 *  Turn a relay switch on and then off when presence is detected
 *
 *  Copyright 2014 Jai Vasanth
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
    name: "Turn a relay switch on and then off when presence is detected",
    namespace: "j-vasanth",
    author: "Jai Vasanth",
    description: "Intended to open a garage door when a car arrives. This smartapp will fire a relay switch when presence is detected. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png") {
    appSetting "switchOnTimeInSeconds"
}


preferences {
	section("Garage door") {
		input "doorSensor", "capability.contactSensor", title: "Which sensor?"
		input "doorSwitch", "capability.momentary", title: "Which switch?"
	}
	section("Car(s) using this garage door") {
		input "car", "capability.presenceSensor", title: "Presence sensor", description: "Which car?", required: true
	}
	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "number", title: "Number of minutes the car needs to have been gone", required: false,
        	defaultValue: 10
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(car, "presence", carPresenceHandler)

}

def carPresenceHandler(evt)
{
	log.info "$evt.name: $evt.value"
    // In order to open the door, the car needs to have been gone for atleast the false alarm threshold minutes
   	final minimumAbsenseRequiredInMs = falseAlarmThreshold * 60 * 1000

	if (evt.value == "present") {
		// This is fired when a car comes home. We would like to verify that this was not a redundant notification by
        // looking at previous event that was fired and ensuring that it was a departure event. Also we check that the last 
        // two events were fired at a threshold time apart
        List lastTwoEvents = car.events()
        if (lastTwoEvents.size() < 2) {
            log.debug("Was notable to fetch 2 events from ${car.displayName}")
            return
        } 
        def currentEvent = lastTwoEvents.get(0);
        def lastEvent = lastTwoEvents.get(1);
		if (currentEvent.value.equals("present") 
        	&& lastEvent.value.equals("not present")
            && currentEvent.date.getTime() - lastEvent.date.getTime() > minimumAbsenseRequiredInMs) {
            // Now we are positive that this is a genuine car arrival for which we need to open the garage door
            if (doorSensor.currentContact == "open") {
            	log.debug("Not opening ${doorSensor.displayName} since it is already open!")
            } else {
            	log.debug("Opening the garage door...")
            	doorSwitch.push()
            }
        } else {
			log.debug "Not opening ${doorSwitch.displayName} since the car arrival criteria was not met. Please look at the code doc"
            log.debug "CurrentEvent: ${currentEvent.name}, ${currentEvent.value}, ${currentEvent.date}"
            log.debug "LastEvent: ${lastEvent.name}, ${lastEvent.value}, ${lastEvent.date}"
        }
	}
	else {
    	log.debug("Car ${car.displayName} has departed")
	}
}