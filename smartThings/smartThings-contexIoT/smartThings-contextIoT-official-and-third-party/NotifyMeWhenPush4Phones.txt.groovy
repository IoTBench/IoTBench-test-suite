/**
 *  Notify Me When modified to include push and SMS up to 4 phones.
 *
 *  Author: Scott Jones
 *  Date: 21 Aug 13
 */
preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Where?", required: false, multiple: true
	}
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input "phone1", "phone", title: "Phone Number", required: false
        input "phone2", "phone", title: "Phone Number", required: false
 		input "phone3", "phone", title: "Phone Number", required: false
        input "phone4", "phone", title: "Phone Number", required: false
		input "pushAndPhone", "enum", title: "Also send push", required: false, metadata: [values: ["Yes","No"]]
	}
	section("Minimum time between messages (optional, defaults to every message") {
		input "frequency", "decimal", title: "Minutes", required: false
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
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(water, "water.wet", eventHandler)
}

def eventHandler(evt) {
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			sendMessage(evt)
		}
	}
	else {
		sendMessage(evt)
	}
}

private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (!phone1 || pushAndPhone != "No") {
		log.debug "sending push"
		sendPush(msg)
	}
	if (phone1) {
		log.debug "sending SMS"
		sendSms(phone1, msg)
	}
	if (phone2) {
		log.debug "sending SMS"
		sendSms(phone2, msg)
	}
    if (phone3) {
		log.debug "sending SMS"
		sendSms(phone3, msg)
	}
	if (phone4) {
		log.debug "sending SMS"
		sendSms(phone4, msg)
	}    
    if (frequency) {
		state[evt.deviceId] = now()
	}
}

private defaultText(evt) {
	if (evt.name == "presence") {
		if (evt.value == "present") {
			"$evt.linkText has arrived at $location.name"
		}
		else {
			"$evt.linkText has left $location.name"
		}
	}
	else {
		evt.descriptionText
	}
}
