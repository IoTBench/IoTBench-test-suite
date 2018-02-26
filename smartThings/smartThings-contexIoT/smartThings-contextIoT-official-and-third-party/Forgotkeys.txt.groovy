/**
 *  Forgot keys?
 *
 *  Copyright 2014 Samuel Rinnetmki
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
    name: "Forgot keys?",
    namespace: "blip",
    author: "Samuel Rinnetmki",
    description: "Did you leave home without your keys? Attach a proximity token to your keyring. This app sends a push notification to your phone if your phone leaves home but your keys are not moving.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
	section("When I leave home with...") {
		input "me", "capability.presenceSensor", title: "Your phone"
	}
	section("And leave behind...") {
		input "keys", "capability.presenceSensor", title: "Your keys"
	}
	section("Send text message at this number (or via push notification if not specified)") {
		input "phone", "phone", title: "Phone number (optional)", required: false
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
	subscribe(me, "presence.not present", presence)
}

def presence(evt) {
	if (keys.currentPresence == "present") {
		sendMessage("It looks like you forgot your ${keys.displayName} at ${location}!")
	}
}

def sendMessage(msg) {
	log.info msg
	if (phone) {
		sendSms phone, msg
	}
	else {
		sendPush msg
	}
}
