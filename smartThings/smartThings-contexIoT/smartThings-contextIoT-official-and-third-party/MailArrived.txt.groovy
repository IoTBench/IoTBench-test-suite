/**
 *  Mail Arrived
 *
 *  Author: SmartThings
 */
preferences {
	section("When mail arrives...") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Where?"
	}
	section("Text me at...") {
		input "phone1", "phone", title: "Phone number?"
	}
}
def preferences() {
	[
		sections: [
			[
				title: "When mail arrives...",
				input: [
					[
						name: "accelerationSensor",
						title: "Where?",
						type: "capability.accelerationSensor",
						description: "Tap to set",
						multiple: false
					]
				]
			],
			[
				title: "Text me at...",
				input: [
					[
						name: "phone1",
						title: "Phone number?",
						type: "phone",
						description: "Tap to set",
						multiple: false
					]
				]
			]
		]
	]
}

def installed() {
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def updated() {
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
}

def accelerationActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	// Don't send a continuous stream of text messages
	def deltaSeconds = 5
	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = accelerationSensor.eventsSince(timeAgo)
	log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
	def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent to $phone1 within the last $deltaSeconds seconds"
	} else {
		log.debug "$accelerationSensor has moved, texting $phone1"
		sendSms(phone1, "Mail has arrived!")
	}
}