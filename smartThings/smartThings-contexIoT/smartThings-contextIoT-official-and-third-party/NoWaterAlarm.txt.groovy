/**
 *  No Water
 *
 *  Author: Alex
 */
preferences {
	section("When there's no water detected...") {
		input "alarm", "capability.waterSensor", title: "Where?"
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
		input "phone", "phone", title: "Send a Text Message?", required: false
	}
}

def installed() {
	subscribe(alarm, "water.dry", waterWetHandler)
}

def updated() {
	unsubscribe()
	subscribe(alarm, "water.dry", waterWetHandler)
}

def waterWetHandler(evt) {
	def deltaSeconds = 30

	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = alarm.eventsSince(timeAgo)
	log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

	def alreadySentSms = recentEvents.count { it.value && it.value == "wet" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone within the last $deltaSeconds seconds"
	} else {
		def msg = "${alarm.displayName} has no water!"
		log.debug "$alarm is dry, texting $phone"
		sendPush(msg)
		if (phone) {
			sendSms(phone, msg)
		}
	}
}

