/**
 *  Notify Me When
 *
 *  Author: SmartThings
 *  Date: 2013-03-20
 */
preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
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
	subscribe(contact, "contact.open", sendMessage)
	subscribe(acceleration, "acceleration.active", sendMessage)
	subscribe(motion, "motion.active", sendMessage)
	subscribe(mySwitch, "switch.on", sendMessage)
	subscribe(arrivalPresence, "presence.present", sendMessage)
	subscribe(departurePresence, "presence.not present", sendMessage)
}

def sendMessage(evt) {
	log.debug "$evt.name: $evt.value, $messageText"
	sendPush(messageText)
	if (phone) {
		sendSms(phone, messageText)
	}
}
