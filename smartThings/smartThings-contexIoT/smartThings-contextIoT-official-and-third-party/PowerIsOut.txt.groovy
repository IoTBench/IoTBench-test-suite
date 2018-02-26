/**
 *  Power Is Out
 *
 *  Copyright 2014 Jesse Ziegler
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
    name: "Power Is Out",
    namespace: "smartthings",
    author: "Jesse Ziegler",
    description: "Alert me of power loss using motion detector's change from wired-power to battery-power. SmartThings hub and internet connection must be working! You can connect the hub and internet connection device (e.g. modem, router, etc.) to a battery backup power strip so that the motion detector and detect the loss and the hub and router will still have enough power to get the message out before they fail as well.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {
	section("When there is wired-power loss on...") {
			input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Via a push notification and a text message(optional)"){
    	input "pushAndPhone", "enum", title: "Send Text?", required: false, metadata: [values: ["Yes","No"]]
		input "phone1", "phone", title: "Phone Number (for Text, optional)", required: false

	}
}

def installed() {
	subscribe(motion1, "powerSource.battery", onBatteryPowerHandler)
    subscribe(motion1, "powerSource.powered", PoweredPowerHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "powerSource.battery", onBatteryPowerHandler)
    subscribe(motion1, "powerSource.powered", PoweredPowerHandler)
}


def onBatteryPowerHandler(evt) {
	log.trace "$evt.value: $evt"
	def msg = "${motion1.label ?: motion1.name} sensed Power is Out!"
    
	log.debug "sending push for power is out"
	sendPush(msg)
    
    if ( phone1 && pushAndPhone ) {
    	log.debug "sending SMS to ${phone1}"
   	sendSms(phone1, msg)
	}
}

def PoweredPowerHandler(evt) {
	log.trace "$evt.value: $evt"
	def msg = "${motion1.label ?: motion1.name} sensed Power is Back On!"
    
	log.debug "sending push for power is back on"
	sendPush(msg)
    
    if ( phone1 && pushAndPhone ) {
    	log.debug "sending SMS to ${phone1}"
    	sendSms(phone1, msg)
	}
}