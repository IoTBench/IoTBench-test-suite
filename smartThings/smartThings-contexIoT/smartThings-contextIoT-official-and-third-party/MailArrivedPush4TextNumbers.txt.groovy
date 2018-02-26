/**
 *  Mail Arrived
 *	This app will send a push message (optional) and text 4 numbers (optional) with a custom message.
 *
 *  Author: Scott Jones
 *	21 Aug 13
 */
preferences {
	section("When mail arrives...") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Where?"
	}
	section("Then send this message"){
		input "messageText", "text", title: "Message Text"
        input "pushMsg", "enum", title: "Send push message?", metadata: [values: ["Yes","No"]], required: false
    }
	section("Send Text Message to...(optional)") {
		input "phone1", "phone", title: "Phone number?", required: false
	}
	section("Send Text Message to...(optional)") {
		input "phone2", "phone", title: "Phone number?", required: false
	}    
	section("Send Text Message to...(optional)") {
		input "phone3", "phone", title: "Phone number?", required: false
	} 
	section("Send Text Message to...(optional)") {
		input "phone4", "phone", title: "Phone number?", required: false
	} 
}

def installed() {
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def accelerationActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	// Don't send a continuous stream of text messages
	def deltaSeconds = 5
	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = accelerationSensor.eventsSince(timeAgo)
	log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
	def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 3

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone1 within the last $deltaSeconds seconds"
	} else {
		log.debug "$accelerationSensor has moved, texting $phone1"
		if (pushMsg == "Yes") {
			log.debug "sending push"
			sendPush(messageText)
		}
		if (phone1) {
			log.debug "sending SMS"
			sendSms(phone1, messageText)
		}
		if (phone2) {
			log.debug "sending SMS"
			sendSms(phone2, messageText)
		}
		if (phone3) {
			log.debug "sending SMS"
			sendSms(phone3, messageText)
		}
		if (phone4) {
			log.debug "sending SMS"
			sendSms(phone4, messageText)
		}	
    } 
}