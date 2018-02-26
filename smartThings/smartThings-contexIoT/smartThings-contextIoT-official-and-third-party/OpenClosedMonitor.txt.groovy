/**
 *  Open / Closed Monitor
 *
 *  Author: a.mattke@gmail.com
 *  Date: 2013-06-07
 */
 
preferences {
	section("When the door opens or closes..."){
		input "contact", "capability.contactSensor", title: "Door Contact", required: true
	}
	section("Send message via push notification"){
		input "openText", "text", title: "Open Message"
        input "closedText", "text", title: "Closed Message"
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
	subscribe(contact, "contact.open", sendOpenMessage)
    subscribe(contact, "contact.closed", sendClosedMessage)
}

def sendOpenMessage(evt) {
	log.debug "$evt.name: $evt.value, $openText"
	sendPush(openText)
	if (phone) {
		sendSms(phone, openText)
	}
}

def sendClosedMessage(evt) {
	log.debug "$evt.name: $evt.value, $closedText"
	sendPush(closedText)
	if (phone) {
		sendSms(phone, closedText)
	}
}
