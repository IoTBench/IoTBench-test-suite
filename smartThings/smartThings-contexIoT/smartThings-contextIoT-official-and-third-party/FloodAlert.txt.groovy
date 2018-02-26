/**
 *  Flood Alert
 *
 *  Author: SmartThings
 */
preferences {
	section("When there's water detected...") {
		input "alarm", "capability.waterSensor", title: "Where?"
	}
	section("Text me at...") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed() {
	subscribe(alarm, "water.wet", waterWetHandler)
}

def updated() {
	unsubscribe()
	subscribe(alarm, "water.wet", waterWetHandler)
}

def waterWetHandler(evt) {
	def deltaSeconds = 60

	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = alarm.eventsSince(timeAgo)
	log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

	def alreadySentSms = recentEvents.count { it.value && it.value == "wet" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone within the last $deltaSeconds seconds"
	} else {
		def msg = "${alarm.label} ${alarm.name} is wet!"
		log.debug "$alarm is wet, texting $phone"
		sendPush(msg)
		if (phone) {
			sendSms(phone, msg)
		}
	}
}

