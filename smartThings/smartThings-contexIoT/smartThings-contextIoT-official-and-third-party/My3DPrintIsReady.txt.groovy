/**
 *  My 3D Print Is Ready
 *
 *  Author: Juan Pablo Risso
 *  Date: 2013-05-03
 */

// Automatically generated. Make future change here.
definition(
    name: "My 3D Print Is Ready",
    namespace: "",
    author: "juano23@gmail.com",
    description: "It sends a push notification or a text message when the printer stops vibrating.",
    category: "Convenience",
    iconUrl: "http://thinkmakelab.com/images/3dprintersmall.png",
    iconX2Url: "http://thinkmakelab.com/images/3dprinterbig.png",
    iconX3Url: "http://thinkmakelab.com/images/3dprinterbig.png",
    oauth: true)

preferences {
	section("If there's movement (device is active)..."){
		input "accelerationSensor", "capability.accelerationSensor", title: "Acceleration Sensor", required: true, multiple: false
	}
	section("And it has been no movement for..."){
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
    state.motionActiveTimes = 0
	subscribe(accelerationSensor, "acceleration", accelerationHandler)
}

def updated() {
	unsubscribe()
	log.debug "Updated with settings: ${settings}"
	state.motionActiveTimes = 0
    subscribe(accelerationSensor, "acceleration", accelerationHandler)
}

def accelerationHandler(evt) {
	log.debug "Device: $evt.value and motionActiveTimes = $state.motionActiveTimes"
	if (evt.value == "active") {
		log.debug "Working..."
        state.motionActiveTimes = state.motionActiveTimes + 1
        state.motionStopTime = 0
        unschedule()
	} else {
        state.motionStopTime = now()
		scheduleCheck() 
	}
}

def scheduleCheck() {
	def elapsedstop = now() - state.motionStopTime
	log.debug "Schedule check with motionStopTime: $elapsedstop"
    def motionState = accelerationSensor.currentState("acceleration")
    if (motionState.value == "inactive") {
    	if (elapsedstop >= (delayMinutes ?: 0) * 60000L) {
            if (state.motionActiveTimes >= 30) {
            	log.debug "Motion has stayed inactive long enough since last check: sending message..."
            	sendMessage()
            } else {
                log.debug "False alarm... waiting for action..."
            	state.motionActiveTimes = 0
            }
        } else {
        	runIn(3, "scheduleCheck", [cassandra: true, overwrite: false])
        }
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}

def sendMessage(evt) {
	def messageText = messageText != null ? messageText : "My 3D Print is ready"
    log.debug "$messageText"
	sendPush(messageText)
	if (phone) {
		sendSms(phone, messageText)
	}
    state.motionActiveTimes = 0
}
