/**
 *  Light on when closed, light off when open.
 *
 *  Copyright 2014 Dav Glass
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
    name: "Light on when closed, light off when open.",
    namespace: "davglass",
    author: "Dav Glass",
    description: "Light on when closed, light off when open.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section("Lights that need to be on?") {
        input "lights", "capability.switch", title: "Which Lights?", multiple: true, required: true
    }
    section("Which Door?") {
        input "door", "capability.contactSensor", multiple: false, required: true
    }
    section("Delay off for seconds?") {
    	input "seconds", "number", title: "Seconds", required: false
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    subscribe(door, "contact", handler)
}

def off() {
    lights.each {
    	log.debug "Turning off ${it.displayName}"
        it.off()
	}
}

def on() {
	lights.each {
        log.debug "Turning on ${it.displayName}"
    	it.on()
	}
}

def handler(evt) {
	log.debug "${evt.displayName} is ${evt.value}"
	if (evt.value == "open") {
    	if (seconds) {
        	log.debug "Delaying off for ${seconds} seconds"
        	runIn(seconds, off)
        } else {
			off()
        }
    } else {
		on()
    }
}