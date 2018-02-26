/**
 *  Door Lock Code Access Message
 *
 *  Taken from skp19
 *
 * UPDATED: 2014-11-17
 *
 */
definition(
    name: "User door unlock notification",
    namespace: "rboy",
    author: "RBoy",
    description: "Sends a notification and text when a specific user unlocks the door",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Solution/doors-locks-active.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Solution/doors-locks-active@2x.png")

import groovy.json.JsonSlurper

preferences {
	section("Choose Locks") {
		input "lock", "capability.lock", multiple: true
	}
    section("Enter User Slot Number (This is not the code used to unlock the door)") {
    	input "userSlot", "number", defaultValue: "1"
    }
    section("Notification Options") {
    	input "distressMsg", "text", title: "Message to send"
    	input "notification", "bool", title: "Send notification"
    	input "phone", "phone", title: "Phone number to send SMS to (optional)", required: false
    }
    section("User Code Discovery Mode (Enable and unlock the door using desired code. A message will be sent containing the user code used to unlock the door.)") {
    	input "discoveryMode", "bool", title: "Enable"
    }
}

def installed() {
    subscribe(lock, "lock", checkCode)
}

def updated() {
	unsubscribe()
    subscribe(lock, "lock", checkCode)
}

def checkCode(evt) {
    log.debug "$evt.value: $evt, $settings"

    if(evt.value == "unlocked" && evt.data) {
    	def lockData = new JsonSlurper().parseText(evt.data)
        
        if(discoveryMode) {
        	sendPush "Door unlocked with user code $lockData.usedCode"
            return
        }
        
        if(lockData.usedCode == userSlot && discoveryMode == false) {
        	if (phone) {
        		sendSms(phone, distressMsg)
            }
            if (notification == true) {
            	sendPush "$distressMsg"
            }
        	log.info "Distress Message Sent"
        }
    }
}