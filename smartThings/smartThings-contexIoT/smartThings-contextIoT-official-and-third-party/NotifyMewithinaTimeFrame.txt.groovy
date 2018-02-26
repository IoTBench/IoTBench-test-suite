/**
 *  Notify Me within a Time Frame
 *
 *  Author: thecjreynolds
 *  Date: 2013-03-20
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
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
	}
    section("During this time window") {
        input "timeOfDay", "time", title: "Start Time?", required: true
        input "endTime", "time", title: "End Time?", required: true
    }
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
		input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, metadata: [values: ["Yes","No"]]
	}
	section("Minimum time between messages (optional, defaults to every message)") {
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
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
}

def eventHandler(evt) {
	def t0 = now()
    def timeZone = location.timeZone ?: timeZone(timeOfDay)
    def start = timeToday(timeOfDay, timeZone)
    def end = timeToday(endTime, timeZone)
    
    if (t0 >= start.time && t0 <= end.time) {       
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
}

private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (!phone || pushAndPhone != "No") {
		log.debug "sending push"
		sendPush(msg)
	}
	if (phone) {
		log.debug "sending SMS"
		sendSms(phone, msg)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}

private defaultText(evt) {
	if (evt.name == "presence") {
		if (evt.value == "present") {
			if (includeArticle) {
				"$evt.linkText has arrived at the $location.name"
			}
			else {
				"$evt.linkText has arrived at $location.name"
			}
		}
		else {
			if (includeArticle) {
				"$evt.linkText has left the $location.name"
			}
			else {
				"$evt.linkText has left $location.name"
			}
		}
	}
	else {
		evt.descriptionText
	}
}

private getIncludeArticle() {
	def name = location.name.toLowerCase()
	def segs = name.split(" ")
	!(["work","home"].contains(name) || (segs.size() > 1 && (["the","my","a","an"].contains(segs[0]) || segs[0].endsWith("'s"))))
}
