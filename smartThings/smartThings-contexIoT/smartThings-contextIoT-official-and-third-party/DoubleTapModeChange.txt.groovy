/**
 *  Change Mode On Double Tap
 *
 *  Author: Jeremy R. Whittaker
 *  Date: 2013-10-26
 *  This mode change works great if you use a switch in your room to arm/disarm your system. 
 *  When you go to bed you can double tap down and set mode to sleeping(arm).  
 *  When you wake up you can simply double tap up to set the mode to day time(disarm) to disable your alarm.
 */
preferences {
	section("When this switch is double-tapped...") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Double tap up change to this mode") {
		input "upMode", "mode", title: "Up Mode?"
	}
	section("Double tap down change to this mode") {
		input "downMode", "mode", title: "Down Mode?"
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
		input "phoneNumber", "phone", title: "Send a Text Message?", required: false
	}
}

def installed() {
	log.debug "Current mode = ${location.mode}"
	subscribe(master, "switch", switchHandler, [filterEvents: false])
	subscribe(location, modeChangeHandler)
}

def updated() {
	log.debug "Current mode = ${location.mode}"
	unsubscribe()
	subscribe(master, "switch", switchHandler, [filterEvents: false])
	subscribe(location, modeChangeHandler)
}

def switchHandler(evt) {
	log.info evt.value

	// use Event rather than DeviceState because we may be changing DeviceState to only store changed values
	def recentStates = master.eventsSince(new Date(now() - 4000), [all:true, max: 10]).findAll{it.name == "switch"}
	log.debug "${recentStates?.size()} STATES FOUND, LAST AT ${recentStates ? recentStates[0].dateCreated : ''}"

	if (evt.isPhysical()) {
		if (evt.value == "on" && lastTwoStatesWere("on", recentStates, evt)) {
			log.debug "detected two taps, disarming system"
			takeUpActions()
		} else if (evt.value == "off" && lastTwoStatesWere("off", recentStates, evt)) {
			log.debug "detected two taps, arming system"
			takeDownActions()
		}
	}
	else {
		log.trace "Skipping digital on/off event"
	}
}

private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}

	if ( phoneNumber ) {
		log.debug( "sending text message" )
		sendSms( phoneNumber, msg )
	}

	log.debug msg
}

def takeUpActions() {
	log.debug "changeMode, location.mode = $location.mode, upMode = $upMode, location.modes = $location.modes"
	if (location.mode != upMode) {
		if (location.modes?.find{it.name == upMode}) {
			setLocationMode(upMode)
			send "Double tap up has changed mode to '${upMode}'"
		}
		else {
			send "Double tap up has tried to change to undefined mode '${upMode}'"
		}
	}
}

def takeDownActions() {
	log.debug "changeMode, location.mode = $location.mode, downMode = $downMode, location.modes = $location.modes"
	if (location.mode != downMode) {
		if (location.modes?.find{it.name == downMode}) {
			setLocationMode(downMode)
			send "Double tap down has changed the mode to '${downMode}'"
		}
		else {
			send "Double tap down has tried to change to undefined mode '${downMode}'"
		}
	}
}

private lastTwoStatesWere(value, states, evt) {
	def result = false
	if (states) {

		log.trace "unfiltered: [${states.collect{it.dateCreated + ':' + it.value}.join(', ')}]"
		def onOff = states.findAll { it.isPhysical() || !it.type }
		log.trace "filtered:   [${onOff.collect{it.dateCreated + ':' + it.value}.join(', ')}]"

		// This test was needed before the change to use Event rather than DeviceState. It should never pass now.
		if (onOff[0].date.before(evt.date)) {
			log.warn "Last state does not reflect current event, evt.date: ${evt.dateCreated}, state.date: ${onOff[0].dateCreated}"
			result = evt.value == value && onOff[0].value == value
		}
		else {
			result = onOff.size() > 1 && onOff[0].value == value && onOff[1].value == value
		}
	}
	result
}

def modeChangeHandler(evt) {
	state.modeStartTime=now()
}
