/**
 *  Text Me When I'm Not Here
 *
 *  Author: SmartThings
 */
preferences {
	section("When there's movement...") {
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("While I'm out...") {
		input "presence1", "capability.presenceSensor", title: "Who?"
	}
	section("Text me at...") {
		input "phone1", "phone", title: "Phone number?"
	}
}

def installed() {
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"
	
	if (presence1.latestValue == "not present") {
		// Don't send a continuous stream of text messages
		def deltaSeconds = 10
		def timeAgo = new Date(now() - (1000 * deltaSeconds))
		def recentEvents = motion1.eventsSince(timeAgo)
		log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
		def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 1

		if (alreadySentSms) {
			log.debug "SMS already sent to $phone1 within the last $deltaSeconds seconds"
		} else {
			log.debug "$motion1 has moved while you were out, texting $phone1"
			sendSms(phone1, "${motion1.label} ${motion1.name} moved while you were out")
		}
	} else {
		log.debug "Motion detected, but presence sensor indicates you are present"
	}
}