/**
 *  Rise and Shine
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */

 preferences {
	 section("When there's motion on any of these sensors") {
		 input "motionSensors", "capability.motionSensor", title: "Intrusion", multiple: true
	 }
	 section("after this time of day") {
		 input "timeOfDay", "time", title: "Time?"
	 }
	 section("Change to this mode") {
		 input "newMode", "mode", title: "Mode?"
	 }
	 section("and (optionally) turn on these appliances") {
		 input "switches", "capability.switch", multiple: true, required: false
	 }
}

def installed() {
	log.debug "installed, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	motionSensors.each {
		subscribe(it, "motion.active", motionActiveHandler)
	}
}

def updated() {
	log.debug "updated, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	unsubscribe()
	motionSensors.each {
		subscribe(it, "motion.active", motionActiveHandler)
	}
}

def motionActiveHandler(evt)
{
	log.debug "motion(evt.name: $evt.value), timeOfDay: $timeOfDay,  actionTakenOn: $state.actionTakenOn, currentMode: $location.mode, newMode: $newMode "
	def startTime = timeToday(timeOfDay)
	log.debug "now: ${new Date(now())}, startTime: ${startTime}"
	if (now() > startTime.time && location.mode != newMode) {
		def message = "Good morning! SmartThings changed the mode to '$newMode'"
		sendPush(message)
		setLocationMode(newMode)
		log.debug message

		def dateString = new Date().format("yyyy-MM-dd")
		log.debug "last turned on switches on ${state.actionTakenOn}, today is ${dateString}"
		if (state.actionTakenOn != dateString) {
			log.debug "turning on switches"
			state.actionTakenOn = dateString
			switches?.on()
		}

	}
	else {
		log.debug "not in time window, or mode is already set, currentMode = ${location.mode}, newMode = $newMode"
	}
}
