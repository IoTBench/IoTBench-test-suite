/**
 *  Laundry Monitor
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
 
import groovy.time.* 
 
definition(
    name: "Laundry Monitor",
    namespace: "bmmiller",
    author: "Brandon Miller",
    description: "This application is a modification of the SmartThings Laundry Monitor SmartApp.  Instead of using a vibration sensor, this utilizes Power (Wattage) draw from an Aeon Smart Energy Meter.",
    category: "Convenience",
    iconUrl: "http://www.vivevita.com/wp-content/uploads/2009/10/recreation_sign_laundry.png",
    iconX2Url: "http://www.vivevita.com/wp-content/uploads/2009/10/recreation_sign_laundry.png")


preferences {
	section("Tell me when this washer/dryer has stopped..."){
		input "sensor1", "capability.powerMeter"
	}
    
    section("Notifications") {
		input "sendPushMessage", "bool", title: "Push Notifications?"
		input "phone", "phone", title: "Send a text message?", required: false
	}

	section("System Variables"){
    	input "minimumWattage", "decimal", title: "Minimum running wattage", required: false, defaultValue: 50
        input "message", "text", title: "Notification message", description: "Laundry is done!", required: true
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
	subscribe(sensor1, "power", powerInputHandler)
}

def powerInputHandler(evt) {
	def latestPower = sensor1.currentValue("power")
    log.trace "Power: ${latestPower}W"
    
    if (!atomicState.isRunning && latestPower > minimumWattage) {
    	atomicState.isRunning = true
		atomicState.startedAt = now()
        atomicState.stoppedAt = null
        log.trace "Cycle started."
    } else if (atomicState.isRunning && latestPower < minimumWattage) {
    	atomicState.isRunning = false
        atomicState.stoppedAt = now()  
        log.debug "startedAt: ${atomicState.startedAt}, stoppedAt: ${atomicState.stoppedAt}"                    
        log.info message

        if (phone) {
            sendSms phone, message
        } else {
            sendPush message
        }
    }
    else {
    	// Do Nothing, no change in either direction
    }
}