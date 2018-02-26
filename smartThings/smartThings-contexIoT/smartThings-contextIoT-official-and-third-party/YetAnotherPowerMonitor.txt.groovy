/**
 *  Yet Another Power Monitor
 *
 *  Copyright 2015 Elastic Development
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
 *  The latest version of this file can be found at:
 *  https://github.com/jpansarasa/SmartThings/blob/master/SmartApps/YetAnotherPowerMonitor.groovy
 *
 *  Revision History
 *  ----------------
 *
 *  2015-01-04: Version: 1.0.0
 *  Initial Revision
 *  2015-01-05: Version: 1.0.1
 *  Reorganized preferences section
 *  2015-01-18: Version: 1.1.0
 *  Added option to disable polling
 */

import groovy.time.*

definition(
    name: "Yet Another Power Monitor",
    namespace: "elasticdev",
    author: "James P",
    description: "Using power monitoring switch, monitor for a change in power consumption, and alert when the power draw stops.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("About") {
        paragraph "Using power monitoring switch, monitor for a change in power consumption, and alert when the power draw stops."
        paragraph "Version 1.1"
    }

    section ("When this device stops drawing power") {
        input "meter", "capability.powerMeter", multiple: false, required: true
    }

    section ("Advanced options", hidden: true, hideable: true) {
        input "upperThreshold", "number", title: "start when power raises above (W)", description: "10", required: true
        input "lowerThreshold", "number", title: "stop when power drops below (W)", description: "5", required: true
    }

    section ("Send this message") {
        input "message", "text", title: "Notification message", description: "Washer is done!", required: true
    }

    section ("Notification method") {
        input "sendPushMessage", "boolean", title: "Send a push notification?", defaultValue: true
        input "phone", "phone", title: "Send a text message to:", required: false
    }

    section("Select Icon") {
        icon(title: "Pick an icon for the notification", required: true)
    }

    section ("Additionally", hidden: hideOptionsSection(), hideable: true) {
        input "enablePolling", "boolean", title: "Enable polling?", defaultValue: true
        input "interval", "number", title: "Polling interval in minutes:", description: "5", defaultValue: 5, required: false
        input "debugOutput", "boolean", title: "Enable debug logging?", defaultValue: false
    }
}

def installed() {
    log.trace "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.trace "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

/**
 *	Initialize the script
 *
 *	Create the scheduled event subscribe to the power event
 */
def initialize() {
    //Set the initial state
    state.cycleOn = false
    state.cycleStart = null
    state.cycleEnd =  null

    //Schedule the tickler to run on the defined interval
    def pollingInterval = (interval) ? interval : 5
    def ticklerSchedule = "0 0/${pollingInterval} * * * ?"

    if (debugOutput.toBoolean()) {
        if (enablePolling.toBoolean()) {
            log.debug "Polling every ${pollingInterval} minutes"
        }
        else {
            log.debug "Polling disabled"
        }
    }
    if (enablePolling.toBoolean()) {
        schedule(ticklerSchedule, tickler)
    }
    subscribe(meter, "power", powerHandler)
}

/**
 *	Scheduled event handler
 *  
 *	Called at the specified interval to poll the metering switch.
 *	This keeps the device active otherwise the power events do not get sent
 *
 *	evt		The scheduler event (always null)
 */
def tickler(evt) {
    meter.poll()

    def currPower = meter.currentValue("power")
    if (debugOutput.toBoolean() && currPower > upperThreshold) {
        log.debug "Power ${currPower}W above threshold of ${upperThreshold}W"
    }
    else if (debugOutput.toBoolean() && currPower <= lowerThreshold) {
        log.debug "Power ${currPower}W below threshold of ${lowerThreshold}W"
    }
}

/**
 *	Power event handler
 *
 *	Called when there is a change in the power value.
 *
 *	evt		The power event
 */
def powerHandler(evt) {
    if (debugOutput.toBoolean()) {
        log.debug "power evt: ${evt}"
        log.debug "state: ${state}"
    }

    def currPower = meter.currentValue("power")
    log.trace "Power: ${currPower}W"

	//If cycle is not on and power exceeds upper threshold, start the cycle
    if (!state.cycleOn && currPower > upperThreshold) {
        state.cycleOn = true
        state.cycleStart = now()
        log.trace "Cycle started."
    }
    // If the device stops drawing power, the cycle is complete, send notification.
    else if (state.cycleOn && currPower <= lowerThreshold) {
        send(message)
        state.cycleOn = false
        state.cycleEnd = now()
        def duration = state.cycleEnd - state.cycleStart
        log.trace "Cycle ended after ${duration} minutes."
    }
}

/**
 *	Sends the messages to the subscribers
 *
 *	msg		The string message to send to the subscribers
 */
private send(msg) {
    if (sendPushMessage.toBoolean()) {
        sendPush(msg)
    }

    if (phone) {
        sendSms(phone, msg)
    }
    if (debugOutput.toBoolean()) {
        log.debug msg
    }
}

/**
 * Enables/Disables the optional section
 */
private hideOptionsSection() {
    (interval || debugOutput) ? false : true
}
//EOF