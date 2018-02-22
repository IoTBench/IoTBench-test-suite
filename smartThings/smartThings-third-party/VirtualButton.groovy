/**
 *  Virtual Button
 *
 *  Copyright 2015 obycode
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
metadata {
	definition (name: "Virtual Button", namespace: "com.obycode", author: "obycode") {
		capability "Button"
		capability "Sensor"

		command "push"
		command "hold"
		command "release"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("button", "device.button", canChangeIcon: true, inactiveLabel: false, width: 2, height: 2) {
			state "default", label: '', icon: "st.secondary.off", action: "push"
			state "pressed", label: 'Pressed', icon: "st.illuminance.illuminance.dark", backgroundColor: "#66ccff", action: "release"
			state "held", label: 'Held', icon: "st.illuminance.illuminance.light", backgroundColor: "#0066ff", action: "release"
		}

		main "button"
		details(["button"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	if (description == "updated") {
    	sendEvent(name: "button", value: "released")
    }
}

// handle commands
def push() {
	log.debug "Executing 'push'"
    sendEvent(name: "button", value: "pushed", /*data: [buttonNumber: button], descriptionText: "$device.displayName button $button was pressed",*/ isStateChange: true)
}

def hold() {
	log.debug "Executing 'hold'"
	sendEvent(name: "button", value: "held", /*data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held",*/ isStateChange: true)
}

def release() {
	log.debug "Executing 'release'"
	sendEvent(name: "button", value: "default", /*data: [buttonNumber: button], descriptionText: "$device.displayName button $button was held",*/ isStateChange: true)
}
