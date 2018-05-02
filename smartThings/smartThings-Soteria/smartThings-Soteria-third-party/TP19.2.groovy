/**
 *  Daily 2-Temp Schedule
 *
 *  Copyright 2015 Daryl Bergeron
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
    name: "Daily 2-Temp Schedule",
    namespace: "d60402",
    author: "d60402",
    description: "Have your thermostat temperature automatically set every day when you wake up and when you go to bed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
    )

preferences {
    section("Thermostat") {
        input "thermostat", "capability.thermostat", title: "Which?", multiple:false

    }

    section("Daytime") {
    	input "daytime", "time", title: "At Time"
        input "dayHeatingSetpoint", "decimal", title: "When Heating"
        input "dayCoolingSetpoint", "decimal", title: "When Cooling"
    }

    section("Nighttime") {
    	input "nighttime", "time", title: "At Time"
        input "nightHeatingSetpoint", "decimal", title: "When Heating"
        input "nightCoolingSetpoint", "decimal", title: "When Cooling"
    }
}

def installed() {
    // subscribe to these events
    log.debug "Installed called with $settings"
    initialize()
}

def updated() {
    // we have had an update
    // remove everything and reinstall
    log.debug "Updated called with $settings"
    initialize()
}

def initialize() {
	scheduleDayTemp()
    scheduleNightTemp()
}

def scheduleDayTemp() {
   unschedule(setDayTemp)
    def timeNow = now()
    
    def scheduleDayTime = timeToday(daytime, location.timeZone)
    log.debug "Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}, scheduled day time is ${scheduleDayTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}"
    if (scheduleDayTime.time < timeNow) {
        log.debug "Current scheduling check time $scheduleDayTime has passed, scheduling check for tomorrow"
        scheduleDayTime = scheduleDayTime + 1
    }
    log.debug "Scheduling Day Temp change for " + scheduleDayTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)
    schedule(scheduleDayTime, setDayTemp) 
}

def scheduleNightTemp() {
   unschedule(setNightTemp)
    def timeNow = now()

    def scheduleNightTime = timeToday(nighttime, location.timeZone)
    log.debug "Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}, scheduled night time is ${scheduleNightTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}"
    if (scheduleNightTime.time < timeNow) {
        log.debug "Current scheduling check time $scheduleNightTime has passed, scheduling check for tomorrow"
        scheduleNightTime = scheduleNightTime + 1
    }
    log.debug "Scheduling Night Temp change for " + scheduleNightTime.format("EEE MMM dd yyyy HH:mm z", location.timeZone)
    schedule(scheduleNightTime, setNightTemp)

}

def setDayTemp() {
    log.debug "setDayTemp, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
    thermostat.setHeatingSetpoint(dayHeatingSetpoint)
    thermostat.setCoolingSetpoint(dayCoolingSetpoint)

    log.debug "Scheduling next check"
    scheduleDayTemp()
}

def setNightTemp() {
    log.debug "setNightTemp, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
    thermostat.setHeatingSetpoint(nightHeatingSetpoint)
    thermostat.setCoolingSetpoint(nightCoolingSetpoint)

    log.debug "Scheduling next check"
    scheduleNightTemp()
}