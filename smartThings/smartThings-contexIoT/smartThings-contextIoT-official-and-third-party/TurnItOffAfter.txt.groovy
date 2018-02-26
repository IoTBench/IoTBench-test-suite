/**
 *  Turn It Off After
 *
 *  Copyright 2014 Adam Jeffery
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
    name: "Turn It Off After",
    namespace: "",
    author: "Adam Jeffery",
    description: "Turns a switch off a set number of minutes after it was turned on.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
	section("When it is turned on..."){
		input "switch1", "capability.switch"
	}
    section("Minutes later to turn off (optional)..."){
		input "offAfter", "number", title: "Off after (default 5)", required: false
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
	subscribe(switch1, "switch.on", switchOnHandler)
}

def switchOnHandler(evt) {
	def offAfter = offAfter ?: 5
    def offAfterMilliseconds = offAfter * 60000;
    
	switch1.off(delay: offAfterMilliseconds)
}