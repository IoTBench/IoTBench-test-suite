/**
 *  Notify If It's Too Cold
 *
 *  Author: Derron Simon
 *
 *  The "It's Too Cold" SmartApp requires the configuration of a heater and an SMS number.  This variation does not.
 *
 *  Heavily based on: It's Too Cold by SmartThings
 */
 
preferences {
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("When the temperature drops below...") {
		input "temperature1", "number", title: "Temperature?"
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

	def tooCold = temperature1

	if (evt.doubleValue <= tooCold) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooCold"

		// Don't send a continuous stream of text messages
		def deltaMinutes = 10 // TODO: Ask for "retry interval" in prefs?
		def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		def recentEvents = temperatureSensor1.eventsSince(timeAgo)
		log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		def alreadySentSms = recentEvents.count { it.doubleValue <= tooCold } > 1

		if (alreadySentSms) {
			log.debug "Notification already sent within the last $deltaMinutes minutes"
		} else {
			log.debug "Temperature dropped below $tooCold:  sending notification"
			sendPush("${temperatureSensor1.label} is too cold, reporting a temperature of ${evt.value}${evt.unit}")
		}
	}
}