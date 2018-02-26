/**
 *  AlarmThing AlertAll
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
    name: "AlarmThing AlertAll",
    namespace: "com.obycode",
    author: "ObyCode",
    description: "Send a message whenever any sensor changes on the alarm.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    section("Notify me when there is any activity on this alarm:") {
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
    subscribe(theAlarm, "contact", contactTriggered)
    subscribe(theAlarm, "motion", motionTriggered)
}

def contactTriggered(evt) {
    if (evt.value == "open") {
        sendPush("A door was opened")
    } else {
        sendPush("A door was closed")
    }
}

def motionTriggered(evt) {
    if (evt.value == "active") {
        sendPush("Alarm motion detected")
    }// else {
        //sendPush("Motion stopped")
    //}
}
