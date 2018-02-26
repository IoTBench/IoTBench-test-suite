/**
 *  Change Mode In The Dark
 *
 *  Copyright 2014 Troy Kelly
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
    name: "Change Mode When It Is Dark",
    namespace: "itsdark",
    author: "Troy Kelly",
    description: "Change to a specific mode when a luminance sensor falls below 10lux",
    category: "Mode Magic",
    iconUrl: "https://aperim.com/smartthings/aperim.png",
    iconX2Url: "https://aperim.com/smartthings/aperim2x.png"
)


preferences {
    section("When these sensors are all dark") {
        input "sensors", "capability.illuminanceMeasurement", multiple: true
    }
    section("Change to this mode") {
        input "newMode", "mode", title: "Mode?"
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
	subscribe(sensors, "illuminance", lxChange)
	subscribe(location)
	subscribe(app)
}

def lxChange(evt)
{
	log.debug "$evt.name: $evt.value"
    if (location.mode == newMode) {
    	return false
	}
	if (allDark()) {
        setLocationMode(newMode)
        def message = "It got dark, so I have changed mode to '$newMode'"
        sendPush(message)
    }
}

def changedLocationMode(evt)
{
	if (evt == newMode) {
    	log.debug "Already in the new mode - do nothing"
        return false
    }
	log.debug "changedLocationMode: $evt, $settings"
	if (allDark()) {
    	if (location.mode != newMode) {
	    	setLocationMode(newMode)
            def message = "Mode changed and it was dark so I have changed mode to '$newMode'"
            sendPush(message)
        }
	}
}

def appTouch(evt)
{
	log.debug "appTouch: $evt, $settings"
}

def allDark()
{
	def result = true
    for (sensor in sensors) {
    	if ( !isDark(sensor) ) {
	        result = false
    	    break
        }
    }
    log.debug "allDark: $result"
    return result
}

def isDark(sensor)
{
	def result = false
    def sensorlux = sensor.latestValue("illuminance") 
	//log.debug "Is $sensorlux less than 10"
	if (sensor.latestValue("illuminance")  <= 10) {
    	result = true
    }
    return result
}
