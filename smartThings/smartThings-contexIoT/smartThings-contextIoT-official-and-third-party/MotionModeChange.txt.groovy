/**
 *  Motion Mode Change
 *
 *  Copyright 2014 David Cates
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
    name: "Motion Mode Change",
    namespace: "mode changes when motion is detected during certain times",
    author: "David Cates",
    description: "mode changes when motion is detected during certain times",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
section("When there's motion on this sensor") {
input "motionSensors", "capability.motionSensor", multiple: false
}
section("During this time window") {
input "timeOfDay", "time", title: "Start Time?"
input "endTime", "time", title: "End Time?", required: true
}
section("Change to this mode") {
input "newMode", "mode", title: "Mode?"
}
section("And (optionally) turn on these appliances") {
input "switches", "capability.switch", multiple: true, required: false
}
section( "Notifications" ) {
input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
input "phoneNumber", "phone", title: "Send a Text Message?", required: false
}
}

def installed() {
log.debug "installed, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
initialize()
}

def updated() {
log.debug "updated, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
unsubscribe()
initialize()
}

def initialize() {
log.trace "timeOfDay: $timeOfDay, endTime: $endTime"
subscribe(motionSensors, "motion.active", motionActiveHandler)
subscribe(location, modeChangeHandler)
if (state.modeStartTime == null) {
state.modeStartTime = 0
}
}
def modeChangeHandler(evt) {
state.modeStartTime = now()
}

def motionActiveHandler(evt)
{

def t0 = now()
def timeZone = location.timeZone ?: timeZone(timeOfDay)
def start = timeToday(timeOfDay, timeZone)
def end = timeToday(endTime, timeZone)
log.debug "startTime: $start, endTime: $end, t0: ${new Date(t0)}, currentMode: $location.mode, newMode: $newMode" 

if (t0 >= start.time && t0 <= end.time && location.mode != newMode) {
	def message = "SmartThings changed the mode to '$newMode'"
	send(message)
	setLocationMode(newMode)
	log.debug message

	log.debug "turning on switches"
	switches?.on()

}
else {
	log.debug "not in time window, or mode is already set, currentMode = ${location.mode}, newMode = $newMode"
}
}

private send(msg) {
if ( sendPushMessage != "No" ) {
log.debug( "sending push message" )
sendPush( msg )
}

if ( phoneNumber ) {
	log.debug( "sending text message" )
	sendSms( phoneNumber, msg )
}

log.debug msg

}