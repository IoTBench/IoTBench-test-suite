/**
 *  Monitor the temperature
 *
 *  Author: christianson.matt@gmail.com
 */
preferences {
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("When the temperature drops below...") {
		input "temperature1", "number", title: "Temperature?"
	}
	section("When the temperature rises above...") {
		input "temperature2", "number", title: "Temperature?"
	}
	section("How many minutes between alerts...") {
		input "alertInterval", "number", title: "Alert Interval?"
	}
	section("Text me at (optional, sends a push notification if not specified)...") {
		input "phone", "phone", title: "Phone number?", required: false
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
	def tooHot = temperature2
	def mySwitch = settings.switch1

	// TODO: Replace event checks with internal state (the most reliable way to know if an SMS has been sent recently or not).
	if (evt.doubleValue <= tooCold) {
		log.debug "Checking how long the temperature sensor has been reporting <= $tooCold"

        def alreadySentSms = alreadySentSms { recentEvent ->
        	recentEvent.doubleValue <= tooCold
        }
		if (!alreadySentSms) {
			log.debug "Temperature dropped below $tooCold:  sending SMS to $phone1"
			def msg = "${temperatureSensor1.label} is too cold, reporting a temperature of ${evt.value}${evt.unit} which is below ${temperature1}${evt.unit}"
			sendTextMessage(msg)
		}
	} else if (evt.doubleValue >= tooHot) {
		log.debug "Checking how long the temperature sensor has been reporting >= $tooHot"
        def alreadySentSms = alreadySentSms { recentEvent ->
        	recentEvent.doubleValue >= tooHot
        }
		if (!alreadySentSms) {
			log.debug "Temperature rose above $tooHot:  sending SMS to $phone1"
			def msg = "${temperatureSensor1.label} is too hot, reporting a temperature of ${evt.value}${evt.unit} which is above ${temperature2}${evt.unit}"
			sendTextMessage(msg)
		}
    }
    
}

def alreadySentSms(closure) {
	// Don't send a continuous stream of text messages
	def deltaMinutes = alertInterval
	def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
	def recentEvents = temperatureSensor1.eventsSince(timeAgo)
	log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
	def alreadySentSms = recentEvents.count { closure(it) } > 1
	return alreadySentSms
}

def sendTextMessage(msg) {
	if (phone) {
		sendSms(phone, msg)
	}
	else {
		sendPush msg
	}
}
