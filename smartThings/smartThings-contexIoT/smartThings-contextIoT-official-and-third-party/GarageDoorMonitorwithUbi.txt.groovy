/**
 *  Garage Door Monitor
 *
 *  Author: SmartThings
 */
definition(
    name: "Garage Door Monitor with Ubi",
    namespace: "smartthings",
    author: "chrisb",
    description: "Monitor your garage door and get a text message and Ubi annoucement if it is open too long",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("When the garage door is open...") {
		input "multisensor", "capability.threeAxis", title: "Which?"
	}
	section("For too long...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
	section("Text me at (optional, sends a push notification if not specified)...") {
		input "phone", "phone", title: "Phone number?", required: false
	}
    section("Ubi token? (optional, if no token, no phrase sent)") {
    	input "behaviorToken", "text", title: "What is the Ubi Token?", required: false, autoCorrect:false
    }
}

def installed()
{
	subscribe(multisensor, "acceleration", accelerationHandler)
}

def updated()
{
	unsubscribe()
	subscribe(multisensor, "acceleration", accelerationHandler)
}

def accelerationHandler(evt) {
	def latestThreeAxisState = multisensor.threeAxisState // e.g.: 0,0,-1000
	if (latestThreeAxisState) {
		def isOpen = Math.abs(latestThreeAxisState.xyzValue.z) > 250 // TODO: Test that this value works in most cases...
		def isNotScheduled = state.status != "scheduled"

		if (!isOpen) {
			clearSmsHistory()
			clearStatus()
		}

		if (isOpen && isNotScheduled) {
			runIn(maxOpenTime * 60, takeAction, [overwrite: false])
			state.status = "scheduled"
		}

	}
	else {
		log.warn "COULD NOT FIND LATEST 3-AXIS STATE FOR: ${multisensor}"
	}
}

def takeAction(){
	if (state.status == "scheduled")
	{
		def deltaMillis = 1000 * 60 * maxOpenTime
		def timeAgo = new Date(now() - deltaMillis)
		def openTooLong = multisensor.threeAxisState.dateCreated.toSystemDate() < timeAgo

		def recentTexts = state.smsHistory.find { it.sentDate.toSystemDate() > timeAgo }

		if (!recentTexts) {
			sendTextMessage()
		}
		runIn(maxOpenTime * 60, takeAction, [overwrite: false])
	} else {
		log.trace "Status is no longer scheduled. Not sending text."
	}
}

def sendTextMessage() {
	log.debug "$multisensor was open too long, texting $phone"

	updateSmsHistory()
	def openMinutes = maxOpenTime * (state.smsHistory?.size() ?: 1)
	def msg = "Your ${multisensor.label ?: multisensor.name} has been open for more than ${openMinutes} minutes!"
	if (phone) {
		sendSms(phone, msg)
	}
	else {
		sendPush msg
	}
    if (behaviorToken) {
    	def phrase = msg.replaceAll(' ', '%20')
        httpGet("https://portal.theubi.com/webapi/behaviour?access_token=${behaviorToken}&variable=${phrase}")
    }
}

def updateSmsHistory() {
	if (!state.smsHistory) state.smsHistory = []

	if(state.smsHistory.size() > 9) {
		log.debug "SmsHistory is too big, reducing size"
		state.smsHistory = state.smsHistory[-9..-1]
	}
	state.smsHistory << [sentDate: new Date().toSystemFormat()]
}

def clearSmsHistory() {
	state.smsHistory = null
}

def clearStatus() {
	state.status = null
}
