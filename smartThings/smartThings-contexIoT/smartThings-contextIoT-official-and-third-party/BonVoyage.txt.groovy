/**
 *  Bon Voyage
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 *
 *  Monitors a set of presence detectors and triggers a mode change when everyone has left.
 */

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
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(people, "presence", presence)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	unsubscribe()
	subscribe(people, "presence", presence)
}

def presence(evt)
{
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
		else {
			log.debug "mode is the same, not evaluating"
		}
	}
	else {
		log.debug "canceling"
		unschedule("takeAction")
	}
}

def takeAction()
{
	// TODO -- uncomment when app label is available
	//def message = "${app.label} changed your mode to '${newMode}' because everyone left home"
	def message = "SmartThings changed your mode to '${newMode}' because everyone left home"
	log.info message
	sendPush(message)
	if (phone) {
		sendSms(phone, message)
	}
	setLocationMode(newMode)
	unschedule("takeAction") // Temporary work-around to scheduling bug
}

private everyoneIsAway()
{
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
