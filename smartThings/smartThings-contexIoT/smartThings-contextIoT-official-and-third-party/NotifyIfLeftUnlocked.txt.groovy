/**
 *  Notify If Left Unlocked
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
    name: "Notify If Left Unlocked",
    namespace: "com.sudarkoff",
    author: "George Sudarkoff <george@sudarkoff.com>",
    description: "Send a push or SMS notification (and lock, if it's closed) if a door is left unlocked for a period of time.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("If this lock...") {
        input "aLock", "capability.lock", multiple: false, required: true
        input "openSensor", "capability.contactSensor", title: "Open/close sensor (optional)", multiple: false, required: false
    }
    section("Left unlocked for...") {
        input "duration", "number", title: "How many minutes?", required: true
    }
    section("Notify me...") {
        input "pushNotification", "bool", title: "Push notification"
        input "phoneNumber", "phone", title: "Phone number (optional)", required: false
        input "lockIfClosed", "bool", title: "Lock the door if it's closed?"
    }
}

def installed()
{
    initialize()
}

def updated()
{
    unsubscribe()
    initialize()
}

def initialize()
{
    log.trace "Initializing with: ${settings}"
    subscribe(aLock, "lock", lockHandler)
}

def lockHandler(evt)
{
    log.trace "${evt.name} is ${evt.value}."
    if (evt.value == "locked") {
        log.debug "Canceling lock check because the door is locked..."
        unschedule(notifyUnlocked)
    }
    else {
        log.debug "Starting the countdown for ${duration} minutes..."
        state.retries = 0
        runIn(duration * 60, notifyUnlocked)
    }
}

def notifyUnlocked()
{
    // if no open/close sensor specified, assume the door is closed
    def open = openSensor?.latestValue("contact") ?: "closed"

    def message = "${aLock.displayName} is left unlocked and ${open} for more than ${duration} minutes."
    log.trace "Sending the notification: ${message}."
    sendMessage(message)

    if (lockIfClosed) {
        if (open == "closed") {
            log.trace "And locking the door."
            sendMessage("Locking the ${aLock.displayName} as prescribed.")
            aLock.lock()
        }
        else {
            if (state.retries++ < 3) {
                log.trace "Door is open, can't lock. Rescheduling the check."
                sendMessage("Can't lock the ${aLock.displayName} because the door is open. Will try again in ${duration} minutes.")
                runIn(duration * 60, notifyUnlocked)
            }
            else {
                log.trace "The door is still open after ${state.retries} retries, giving up."
                sendMessage("Unable to lock the ${aLock.displayName} after ${state.retries} retries, giving up.")
            }
        }
    }
}

def sendMessage(msg) {
    if (pushNotification) {
        sendPush(msg)
    }
    if (phoneNumber) {
        sendSMS(phoneNumber, msg)
    }
}
