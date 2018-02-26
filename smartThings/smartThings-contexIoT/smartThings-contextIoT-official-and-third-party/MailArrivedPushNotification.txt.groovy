/**
 *  Mail Arrived - Push Notification
 *  This is just a modification of the example "Notify Me When"
 *  Author: av8rdude
 *  7/3/2013
 */
preferences {
	section("When Mail Arrives..."){
		input "acceleration", "capability.accelerationSensor", title: "Where?", required: true, multiple: true
	}
	section("Then send this message in a push notification"){
		input "messageText", "text", title: "Message Text"
	}
	section("And as text message to this number (optional)"){
		input "phone", "phone", title: "Phone Number", required: false
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
	subscribe(acceleration, "acceleration.active", sendMessage)
}

def sendMessage(evt) {
	log.debug "$evt.name: $evt.value, $messageText"
	sendPush(messageText)
	if (phone) {
		sendSms(phone, messageText)
	}
}
