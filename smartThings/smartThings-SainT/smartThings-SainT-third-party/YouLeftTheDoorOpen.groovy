/**
 *  You left the door open!
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
    name: "You left the door open!",
    namespace: "com.obycode",
    author: "ObyCode",
    description: "Choose a contact sensor from your alarm system (AlarmThing) and get a notification when it is left open for too long.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Notify me when the door named...") {
        input "theSensor", "string", multiple: false, required: true
    }
    section("On this alarm...") {
        input "theAlarm", "capability.alarm", multiple: false, required: true
    }
    section("Is left open for more than...") {
        input "maxOpenTime", "number", title: "Minutes?"
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
    subscribe(theAlarm, theSensor, sensorTriggered)
}

def sensorTriggered(evt) {
    if (evt.value == "closed") {
        clearStatus()
    }
    else if (evt.value == "open" && state.status != "scheduled") {
        runIn(maxOpenTime * 60, takeAction, [overwrite: false])
        state.status = "scheduled"
    }
}

def takeAction(){
    if (state.status == "scheduled")
    {
        log.debug "$theSensor was open too long, sending message"
        def msg = "Your $theSensor has been open for more than $maxOpenTime minutes!"
        sendPush msg
        clearStatus()
    } else {
        log.trace "Status is no longer scheduled. Not sending text."
    }
}

def clearStatus() {
    state.status = null
}
