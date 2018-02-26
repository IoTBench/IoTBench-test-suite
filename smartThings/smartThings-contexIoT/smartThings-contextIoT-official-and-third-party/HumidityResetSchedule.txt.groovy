/**
 *  Humidity Reset Schedule
 *
 *  Copyright 2014 Brandon Miller
 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  Description: This SmartApp is designed to only work with a Nest or similar device type which
 *  also contains the function setHumiditySetpoint() that I created.  Without this function, this smart app will
 *  simply blow up.  Please check my Nest device type, also hosted here for specifics.
 *
 */

import groovy.map.*
 
definition(
    name: "Humidity Reset Schedule",
    namespace: "bmmiller",
    author: "Brandon Miller",
    description: "Humidity Reset Schedule",
    category: "Convenience",
    iconUrl: "http://axxind.com/img/icons/96/humidity.png",
    iconX2Url: "http://axxind.com/img/icons/96/humidity.png")


preferences {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
    
    section("System Variables") {
		input "maxHumiditySetpoint", "number", title: "Maximum Humidity Setpoint", required: true, defaultValue: 45
        input "minHumiditySetpoint", "number", title: "Minimum Humidity Setpoint", required: true, defaultValue: 15
        input "maxOAtoEnable", "number", title: "Maximum OA to Enable", required: true, defaultValue: 50
        input "updatePeriod", "number", title: "Update Period (minutes: 1-30)", required: true, defaultValue: 15
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
    
    if (updatePeriod > 30 || updatePeriod < 1)
    	updatePeriod = 30
    
    def cron = "0 */${updatePeriod} * * * ?"    

	log.debug "Schedule: ${cron}"

	subscribe(thermostat, "humidity", schedule(cron, updateHumiditySetpoint))
}

def updateHumiditySetpoint()
{
	def response = getWeatherFeature("conditions")
    def OATemp
    if (response) {
        log.debug "Outside Air Temp: ${response?.current_observation?.temp_f}F"
        OATemp = response?.current_observation?.temp_f
    }

	if (OATemp < maxOAtoEnable) {
        def humiditySetpoint = OATemp*0.5+minHumiditySetpoint;

        if (humiditySetpoint > maxHumiditySetpoint)
            humiditySetpoint = maxHumiditySetpoint
        else if (humiditySetpoint < minHumiditySetpoint)
            humiditySetpoint = minHumiditySetpoint

        log.debug "Calculated Value: ${humiditySetpoint}%"

        thermostat.setHumiditySetpoint(humiditySetpoint) 
     } else {
     	log.debug "It's too warm outside, so don't bother with the update call"
     }
}

// catchall
def event(evt)
{
	log.debug "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}