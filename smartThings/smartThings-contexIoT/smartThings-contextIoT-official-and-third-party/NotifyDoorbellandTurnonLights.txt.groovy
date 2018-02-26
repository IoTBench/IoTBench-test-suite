/**
 *  Notify Doorbell and Turn on Lights
 *
 *  Copyright 2014 Brandon Miller
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
    name: "Notify Doorbell and Turn on Lights",
    namespace: "bmmiller",
    author: "Brandon Miller",
    description: "SmartApp designed for very specific use-case.  My personal application of a door contact senses a closed state when the doorbell is pushed.  When this happens, send a notification.  Additionally, if it is dark out, turn on lights for duration time.",
    category: "Convenience",
    iconUrl: "https://dl.dropboxusercontent.com/u/3666885/doorbell-icon.png",
    iconX2Url: "https://dl.dropboxusercontent.com/u/3666885/doorbell-icon.png",
    iconX3Url: "https://dl.dropboxusercontent.com/u/3666885/doorbell-icon.png")


preferences {
	section("Choose one or more, when..."){
		input "contact", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
	}
    section("Turn on these switches..."){
    	input "switches", "capability.switch", title: "Switches To Turn On", required: false, multiple: true
    }    
    section("Turn switches off after..."){
		input "minutesOff", "number", title: "Minutes", required: false
	}
	section("Notification Settings"){
		input "sendPush", "enum", title: "Send Push Notification?", required: false, options: ["Yes","No"]
        input "messageText", "text", title: "Message Text", required: false
        input "frequency", "decimal", title: "Minimum time between messages (minnutes, optional)", required: false
	}   
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.closed", eventHandler)
}

def eventHandler(evt) {
	log.debug "Contact closed"
    
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
        	log.debug "Send Push Notification: ${evt}"
			sendMessage(evt)
		}
	}
	else {
    	log.debug "Send Push Notification: ${evt}"
		sendMessage(evt)
    }
    
    def s = getSunriseAndSunset()

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
    
    log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"
    
    if (riseTime.after(now) || setTime.before(now))
    {
    	log.debug "Turning on switches: ${switches}"
    	switches.on()
    	switches.off(delay: 60000 * minutesOff)
    }
    else
    {
    	log.debug "Not turning on because it is daytime"
    }
}

private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, sendPush:$sendPush, '$msg'"

	if (sendPush == "Yes") {
		log.debug "Sending Push Message"
		sendPush(msg)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}