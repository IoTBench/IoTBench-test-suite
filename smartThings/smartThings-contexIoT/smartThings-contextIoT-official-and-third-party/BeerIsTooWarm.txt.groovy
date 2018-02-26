/**
 *  It's Too Hot
 *
 *  Author: Scott Jones
 */
preferences {
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("When the temperature rises above...") {
		input "temperature1", "number", title: "Temp?"
	}
	section("Text me at...") {
		input "phone1", "phone", title: "Phone number?"
	}
}

def installed() {
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"

	def tooHot = temperature1

	// TODO: Replace event checks with internal state (the most reliable way to know if an SMS has been sent recently or not).
	if (evt.doubleValue >= tooHot) {
		log.debug "Checking how long the temperature sensor has been reporting >= $tooHot"

		// Don't send a continuous stream of text messages
		def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue <= tooHot } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent to $phone1 within the last $deltaMinutes minutes"
			// TODO: Send "Temperature back to normal" SMS
		} else {
			log.debug "Temperature rises above $tooHot:  sending SMS to $phone1"
			sendSms(phone1, "${temperatureSensor1.label} is too hot, reporting a temperature of ${evt.value}${evt.unit}")
		}
	}
}