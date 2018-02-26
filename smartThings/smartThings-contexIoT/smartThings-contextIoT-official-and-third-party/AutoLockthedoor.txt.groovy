/**
 *  Auto-Lock the door
 *
 *  Copyright 2014 Leo Alvarez
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
    name: "Auto-Lock the door",
    namespace: "",
    author: "Leo Alvarez",
    description: "Lock the door if it is closed X minutes after unlock",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When I arrive..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Choose your Door Sensor...") {
		input "contact", "capability.contactSensor", title: "Where?"
	}
    	section("Select Door Lock") {
		input name: "lock", type: "capability.lock", multiple: false
	}
	section("And verify that it locked after these many minutes (default 10)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
	section("Via text message at this number (or via push notification if not specified") {
		input "phone1", "phone", title: "Phone number (optional)", required: false
	}
 	section("") {
		input "phone2", "phone", title: "Phone number (optional)", required: false
	}  
 	section("") {
		input "phone3", "phone", title: "Phone number (optional)", required: false
	}   
}






def installed() {
	log.trace "installed()"
	subscribe(presence1, "presence.present", presence)
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe(presence1, "presence.present", presence)
}

def subscribe() {
	log.trace "subcribe()"
	subscribe(presence1, "presence.present", presence)
}

def presence(evt)
{
	log.trace "lockUnlock($evt.name: $evt.value)"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 600
    log.debug "Scheduling of door lock and check"
    runIn(delay, lockUnlockedTooLong, [overwrite: false])
	log.debug "Unlocked door due to arrival of $evt.displayName"
	lock.unlock()
	log.debug "Scheduled lockUnlockedTooLong in ${now() - t0} msec"
}

def lockUnlockedTooLong(){
	def contactState = contact.currentState("contact")
    def lockState = lock.currentState("lock")
    if (contactState.value == "closed") {
	log.debug "Door is Closed"
    	if (lockState.value == "unlocked") {
		log.debug "Door is unlocked"
        def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
			if (elapsed >= threshold) {
				log.debug "Door has been unlock long enough. Locking door."
				lock.lock()
			} else {
				log.debug "Lock has not been unlocked long enough since last check ($elapsed ms):  doing nothing"
			}	
	   	 } else {
         log.debug "Door is locked and closed"
        }
    } else {
    log.debug "Door is not closed. Send Alert"
    sendMessage1()
    }
}


def sendMessage1()
{
	def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 10
	def msg = "${contact.displayName} is open. Please go see why the door isn't shut"
	log.info msg
		sendPush msg
	if (settings.phone1) {
		sendSms phone1, msg
	}
	if (settings.phone2) {
		sendSms phone2, msg
	}
	if (settings.phone3) {
		sendSms phone3, msg
	}

}
