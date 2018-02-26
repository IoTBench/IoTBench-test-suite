/**
 *  MyCustomApp
 *
 *  Copyright 2015 manish kumar
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
    name: "MyCustomApp",
    namespace: "none",
    author: "manish kumar",
    description: "development",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("Allow External Service to Control These Things...") {
    input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
    input "motions", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
    input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
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
}
mappings {
  path("/switches") {
    action: [
      GET: "listSwitches",
      PUT: "updateSwitches"
    ]
  }
  path("/switches/:id") {
    action: [
      GET: "showSwitch",
      PUT: "updateSwitch"
    ]
  }
}
void updateSwitch() {
    def command = request.JSON?.command
    if (command) {
      def mySwitch = switches.find { it.id == params.id }
      if (!mySwitch) {
        httpError(404, "Switch not found")
      } else {
        mySwitch."$command"()
      }
    }
}


// TODO: implement event handlers