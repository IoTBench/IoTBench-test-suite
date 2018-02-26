/**
 *  Honey, I'm Home!
 *
 *  Author: oneaccttorulethehouse@gmail.com
 *  Date: 2014-01-26
 *
 * 	Heavily borrowed from Night Light/Sunrise/Sunset and Greetings Earthling.
 *
 *	Will turn on lights and change the mode when someone arrives home and it is dark outside.
 *
 *
 */


preferences {

	section("When one of these people arrive at home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
/* MINE */
    	section("After sunset and before sunrise, turn on a light...") {
    		input "lights", "capability.switch", title: "Switches", required: false, multiple: true
        	}
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", required: false
	}
/* END */
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
		input "phone", "phone", title: "Send a Text Message?", required: false
	}

}

def installed() {
	initialize()
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(people, "presence", presence)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	unsubscribe()
	subscribe(people, "presence", presence)
	unschedule()
	initialize()
}

def initialize() {
	scheduleAstroCheck()
	astroCheck()
}

def scheduleAstroCheck() {
	def min = Math.round(Math.floor(Math.random() * 60))
	def exp = "0 $min * * * ?"
    log.debug "$exp"
	schedule(exp, astroCheck) // check every hour since location can change without event?
    state.hasRandomSchedule = true
}

def astroCheck() {
	if (!state.hasRandomSchedule && state.riseTime) {
    	log.info "Rescheduling random astro check"
        unschedule("astroCheck")
    	scheduleAstroCheck()
    }
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"
	if (state.riseTime != riseTime.time || state.setTime != setTime.time) {
		state.riseTime = riseTime.time
		state.setTime = setTime.time

		unschedule("sunriseHandler")
		unschedule("sunsetHandler")

		if (riseTime.after(now)) {
			log.info "scheduling sunrise handler for $riseTime"
			runOnce(riseTime, sunriseHandler)
		}

		if (setTime.after(now)) {
			log.info "scheduling sunset handler for $setTime"
			runOnce(setTime, sunsetHandler)
		}
	}
}

private enabled() {
	def result
	def t = now()
	result = t < state.riseTime || t > state.setTime
	result
}


def presence(evt)
{
	log.debug "evt.name: $evt.value"
	def threshold = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? (falseAlarmThreshold * 60 * 1000) as Long : 10 * 60 * 1000L

	if (location.mode != newMode) {

		def t0 = new Date(now() - threshold)
		if (evt.value == "present") {

			def person = getPerson(evt)
			def recentNotPresent = person.statesSince("presence", t0).find{it.value == "not present"}
			if (recentNotPresent) {
				log.debug "skipping notification of arrival of ${person.displayName} because last departure was only ${now() - recentNotPresent.date.time} msec ago"
			}
			else {
				def message = "${person.displayName} arrived at home, changing mode to '${newMode}'"
				log.info message
				send(message)
				setLocationMode(newMode)
					 if (enabled()) {
						log.debug "turning on lights due to presence"
						lights.on()
						state.lastStatus = "on"
					}

			}
		}
	}
	else {
		log.debug "mode is the same, not evaluating"
	}
}

private getPerson(evt)
{
	people.find{evt.deviceId == it.id}
}

private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}

	if ( phone ) {
		log.debug( "sending text message" )
		sendSms( phone, msg )
	}

	log.debug msg
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

