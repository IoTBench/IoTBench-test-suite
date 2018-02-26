/**
 *  SmartAlarmAlert
 *
 *  Copyright 2014 ObyCode
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
    name: "SmartAlarmAlert",
    namespace: "com.obycode",
    author: "ObyCode",
    description: "Alert me when my alarm status changes (armed, alarming, disarmed).",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    section("Alert me when this alarm changes state (arming, armed, disarmed, alarm):") {
        input "theAlarm", "capability.alarm", multiple: false, required: true
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
    log.debug "in initialize"
    subscribe(theAlarm, "alarmStatus", statusChanged)
}

def statusChanged(evt) {
    if (evt.value == "away") {
        sendPush("Alarm armed to 'away'")
    } else if (evt.value == "stay") {
        sendPush("Alarm armed to 'stay'")
    } else if (evt.value == "arming") {
        sendPush("Alarm is arming")
    } else if (evt.value == "alarm") {
        sendPush("ALARM IS GOING OFF!")
    } else if (evt.value == "disarmed") {
        sendPush("Alarm is disarmed")
    }
}
