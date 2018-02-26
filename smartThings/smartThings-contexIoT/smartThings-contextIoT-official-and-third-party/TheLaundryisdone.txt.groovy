/**
 *  The Laundry is done!
 *
 *  Author: juano23@gmail.com
 *  Date: 2013-08-27
 */

preferences {
    section("If there's movement (device is active)..."){
		input "accelerationSensor", "capability.accelerationSensor", title: "Acceleration Sensor", required: true, multiple: false
	}
	section("And it has been active for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
    section("Then send this message in a push notification"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("And as text message to this number (optional)"){
		input "phone", "phone", title: "Phone Number", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(accelerationSensor, "acceleration", accelerationHandler)
}

def updated() {
	unsubscribe()
	log.debug "Updated with settings: ${settings}"
	subscribe(accelerationSensor, "acceleration", accelerationHandler)
}

def accelerationHandler(evt) {
    log.debug "Device: $evt.value"
	if (evt.value == "active") {
		log.debug "Working..."
        state.motionActiveTime = now()
        state.motionStopTime = 0
	} else if (evt.value == "inactive") {
    	state.motionStopTime = now()
		def elapsed = now() - state.motionActiveTime
        log.debug "It have been active for $elapsed"
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
            state.motionActiveTime = 0            
        	scheduleCheck()
        }
	}
}

def scheduleCheck() {
    def elapsedstop = now() - state.motionStopTime
	log.debug "Schedule check with motionStopTime: $elapsedstop"
    def motionState = accelerationSensor.currentState("acceleration")
    if (motionState.value == "inactive") {
    	if (elapsedstop >= 60000L) {
            log.debug "Motion has stayed inactive long enough since last check: sending message..."
            sendMessage()
        } else {
        	runIn(5,scheduleCheck)
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}

def sendMessage(evt) {
	def messageText = messageText != null ? messageText : "The Laundry is done!"
    log.debug "$messageText"
	sendPush(messageText)
	if (phone) {
		sendSms(phone, messageText)
	}
}
