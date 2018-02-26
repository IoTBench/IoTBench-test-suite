/**
 *  Good Night
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */
preferences {
	section("When there is no motion on any of these sensors") {
		input "motionSensors", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("for this amount of time") {
		input "minutes", "number", title: "Minutes?"
	}
	section("after this time of day") {
		input "timeOfDay", "time", title: "Time?"
	}
	section("and (optionally) these switches are all off") {
		input "switches", "capability.switch", multiple: true, required: false
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
}

def installed() {
	log.debug "Current mode = ${location.mode}"
	createSubscriptions()
}

def updated() {
	log.debug "Current mode = ${location.mode}"
	unsubscribe()
	createSubscriptions()
}

def createSubscriptions()
{
	subscribe(motionSensors, "motion.active", motionActiveHandler)
	subscribe(motionSensors, "motion.inactive", motionInactiveHandler)
	subscribe(switches, "switch.off", switchOffHandler)

	def period = minutes <= 5 ? 1 : 5
	def cron = "0 */$period * * * ?"
	schedule(cron, "scheduleCheck")
	log.debug "cron: $cron"
}

def switchOffHandler(evt) {
	if (state.motionStoppedAt) {
		def elapsed = now() - state.motionStoppedAt
		if (elapsed >= threshold && switchesOk()) {
			takeActions()
		}
	}
}

def motionActiveHandler(evt)
{
	log.debug "clearing motion stopped timer"
	if (state.motionStoppedAt) {
		state.motionStoppedAt = null
	}
}

def motionInactiveHandler(evt)
{
	def startTime = timeToday(timeOfDay)
	if (now() > startTime.time && location.mode != newMode) {
		if (allQuiet()) {
			log.debug "starting motion stopped timer"
			state.motionStoppedAt = now()
		}
		else {
			log.debug "Other sensor still active"
		}
	}
	else {
		log.debug "not in window, startTime: $startTime, currentMode = ${location.mode}, newMode = $newMode"
	}
}

def scheduleCheck()
{
	log.debug "scheduleCheck, motionStoppedAt = ${state.motionStoppedAt}, currentMode = ${location.mode}, newMode = $newMode"
	if (state.motionStoppedAt) {
		log.debug "MINUTES: $minutes"
		def elapsed = now() - state.motionStoppedAt
		def threshold = 1000 * 60 * (minutes)
		if (elapsed >= threshold && switchesOk()) {
			takeActions()
		}
		else {
			log.debug "${elapsed / 1000} sec since motion stopped"
		}
	}
}

private takeActions() {
	state.motionStoppedAt = null
	if (location.mode != newMode) {
		def message = "Goodnight! SmartThings changed the mode to '$newMode'"
		sendPush(message)
		setLocationMode(newMode)
		log.debug message
	}
}

private switchesOk()
{
	def result = true
	for (it in (switches ?: [])) {
		if (it.currentSwitch == "on") {
			result = false
			break
		}
	}
	result
}

private allQuiet()
{
	def result = true
	for (sensor in motionSensors) {
		if (sensor.currentMotion == "active") {
			result = false
			break
		}
	}
	result
}
