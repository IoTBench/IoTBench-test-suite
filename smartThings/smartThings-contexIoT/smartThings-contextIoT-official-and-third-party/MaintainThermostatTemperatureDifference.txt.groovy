/**
 *  Maintain Thermostat Difference
 *
 *  Copyright 2014 Chris LeBlanc
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
    name: "Maintain Thermostat Temperature Difference",
    namespace: "cl",
    author: "Chris LeBlanc",
    description: "Maintains temperature difference between floor temperatures. For example: I have a third floor A/C unit which cannot maintain a cooler tempurature than the bottom units (or else it just runs and runs).",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {

	section("This thermostat must remain greater than (or less than when on heat)..."){
    	input "sourceThermostat", "capability.thermostat", title: "Select thermostat", required: true
    }
	section("This amount..."){
    	input "difference", "decimal", title: "Number of degrees (eg: 5)", required: true, multiple: true
    }
	section("Compared to this thermostat..."){
    	input "targetThermostat", "capability.thermostat", title: "Select thermostat(s)", required: true, multiple: true
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
	subscribe(sourceThermostat, "heatingSetpoint", eventHandler)
	subscribe(sourceThermostat, "coolingSetpoint", eventHandler)
}

def eventHandler(evt) {
	
    log.debug "value: $evt.value, $evt.name, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"

    if (evt.name == "heatingSetpoint")
    {
		int adjustedSetpoint = sourceThermostat.currentValue("heatingSetpoint") + difference
		targetThermostat.setHeatingSetpoint(adjustedSetpoint)
	  	log.debug "$adjustedSetpoint"
    }
    else if (evt.name == "coolingSetpoint")
    {
   		int adjustedSetpoint = sourceThermostat.currentValue("coolingSetpoint") - difference
    	targetThermostat.setCoolingSetpoint(adjustedSetpoint)
        log.debug "$adjustedSetpoint"
    }
}
