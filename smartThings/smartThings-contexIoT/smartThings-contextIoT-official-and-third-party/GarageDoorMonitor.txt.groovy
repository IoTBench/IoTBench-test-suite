/**
 *  Garage Door Monitor
 *
 *  Author: SmartThings
 */
preferences {
	section("When the garage door is open...") {
		input "multisensor", "device.smartSenseMulti", title: "Where?"
	}
	section("For too long...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
	section("Text me at...") {
		input "phone", "phone", title: "Phone number?"
	}
}

def installed()
{
	subscribe(multisensor, "acceleration", accelerationHandler, [filterEvents: false])
}

def updated()
{
	unsubscribe()
	subscribe(multisensor, "acceleration", accelerationHandler, [filterEvents: false])
}

/*
The "acceleration" message comes during acceleration, but also is reported every 2.5 minutes, so we listen for
that and then check if the garage door has been open for longer than the threshold.
*/
def accelerationHandler(evt) {
	def latestThreeAxisState = multisensor.latestState("threeAxis") // e.g.: 0,0,-1000

	if (latestThreeAxisState) {
		def latestThreeAxisDate = latestThreeAxisState.dateCreated.toSystemDate()
		def isOpen = Math.abs(latestThreeAxisState.xyzValue.z) > 250 // TODO: Test that this value works in most cases...

		if (isOpen) {
			def deltaMillis = 1000 * 60 * maxOpenTime
			def timeAgo = new Date(now() - deltaMillis)
			def openTooLong = latestThreeAxisDate < timeAgo
			log.debug "openTooLong: $openTooLong"
			def recentTexts = state.smsHistory.find { it.sentDate.toSystemDate() > timeAgo }
			log.debug "recentTexts: $recentTexts"

			if (openTooLong && !recentTexts) {
				def openMinutes = maxOpenTime * (state.smsHistory?.size() ?: 1)
				sendTextMessage(openMinutes)
			}
		}
		else {
			clearSmsHistory()
		}
	}
	else {
		log.warn "COULD NOT FIND LATEST 3-AXIS STATE FOR: ${multisensor}"
	}
}

def sendTextMessage(openMinutes) {
	log.debug "$multisensor was open too long, texting $phone"

	updateSmsHistory()
	sendSms(phone, "Your ${multisensor.label ?: multisensor.name} has been open for more than ${openMinutes} minutes!")
}

def updateSmsHistory() {
	if (!state.smsHistory) state.smsHistory = []
	state.smsHistory << [sentDate: new Date().toSystemFormat()]
}

def clearSmsHistory() {
	state.smsHistory = null
}
