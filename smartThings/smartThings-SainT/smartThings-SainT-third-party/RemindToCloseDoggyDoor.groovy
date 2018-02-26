/**
 *  Remind to close doggy door
 *
 *  Copyright 2014 George Sudarkoff
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
    name: "Remind to close doggy door",
    namespace: "com.sudarkoff",
    author: "George Sudarkoff",
    description: "Check that the doggy door is closed after the specified time and send a message if it's not.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
    section ("When this door") {
        input "door", "capability.contactSensor", title: "Which door?", multiple: false, required: true
    }
    section ("Still open past") {
        input "timeOfDay", "time", title: "What time?", required: true
    }
    section (title: "Notify") {
        input "sendPushMessage", "bool", title: "Send a push notification?"
        input "phone", "phone", title: "Send a Text Message?", required: false
    }

}

def installed() {
    initialize()
}

def updated() {
    unschedule()
    initialize()
}

def initialize() {
    schedule(timeToday(timeOfDay, location.timeZone), "checkDoor")
}

def checkDoor() {
    if (door.latestValue("contact") == "open") {
        log.debug "${door} is open, sending notification."
        def message = "Remember to close the ${door}!"
        send(message)
    }
}

private send(msg) {
    if (sendPushMessage != "No") {
        sendPush(msg)
    }

    if (phone) {
        sendSms(phone, msg)
    }

    log.debug msg
}

