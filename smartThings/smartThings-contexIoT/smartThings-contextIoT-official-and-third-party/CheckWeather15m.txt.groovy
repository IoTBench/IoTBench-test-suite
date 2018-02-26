/**
 *  Weather Station Controller
 *
 *  Copyright 2014 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *  Modified to check every 15mintues (900 seconds)
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "Check Weather 15m",
    namespace: "Check Weather 15m",
    author: "Euisung Lee",
    description: "Refresh weather tile every 15min",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section {
		input "weatherDevices", "device.smartweatherStationTile"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unschedule()
	initialize()
}

def initialize() {
    scheduledEvent()
}

def scheduledEvent() {
	log.trace "scheduledEvent()"
    runIn(900, scheduledEvent, [overwrite: false])
	weatherDevices.refresh()
    state.lastRun = new Date().toSystemFormat()
}