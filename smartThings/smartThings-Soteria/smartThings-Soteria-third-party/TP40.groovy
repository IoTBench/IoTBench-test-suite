/**
 *  Thermostat Boost
 *
 *  Copyright 2014 Tim Slagle
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
    name: "Thermostat Boost",
    namespace: "tslagle13",
    author: "Tim Slagle",
    description: "Turn on the thermostat for a certain period of time and then back off after that time has expired.  Good for when you need to pull some moisture out of the air but don't want to forget to turn the thermostat off.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  section("Turn on these thermostats") {
    input "thermostats", "capability.thermostat", multiple: true
  }
  section("To which mode?") {
    input "turnOnTherm", "enum", metadata: [values: ["auto", "cool", "heat"]], required: false
  }
  section("For how long?") {
    input "turnOffDelay", "decimal", defaultValue:30
  }
}  

def installed() {
subscribe(app, appTouch)
}

def updated() {
  subscribe(app, appTouch)
}

def appTouch(evt) {
	def mode = turnOnTherm
    thermostats?."${mode}"()
 	thermoShutOffTrigger()
}

def thermoShutOffTrigger() {
    log.info("Starting timer to turn off thermostat")
    def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 60 
    state.turnOffTime = now()

    runIn(delay, "thermoShutOff")
  }

def thermoShutOff() {
    thermostats?.off()
}