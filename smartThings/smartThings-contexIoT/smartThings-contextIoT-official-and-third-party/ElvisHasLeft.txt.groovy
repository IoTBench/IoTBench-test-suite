/**
 *  Elvis Has Left
 *
 *  Author: chadon
 *  Date: 2015-01-31
 */
definition(
    name: "Elvis Has Left",
    namespace: "chadon",
    author: "chadon",
    description: "Set thermostats after door is closed and no motion is detected for a period of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage@2x.png"
)

preferences {

	section("Should the app be enabled...") {
		input "isEnabled", "boolean", title: "Enable?", value: "true"
	}
	section("When a contact sensor is closed...") {
		input "contactSensors", "capability.contactSensor", title: "Which contact sensors?", multiple: true
	}
	section("And no motion is detected...") {
		input "motionSensors", "capability.motionSensor", title: "Which motion sensors?", multiple: true
		input "minutes1", "number", title: "Delay(in minutes) before setpoint"
		//input "minutes2", "number", title: "Delay(in minutes) before setpoint2", required: false
	}
	section("Set thermostat...") {
		input "thermostats", "capability.thermostat", title: "Choose Thermostats", multiple: true
		input "thermMode", "enum", title: "Set Mode", options: ["Auto", "Heat", "Cool"]
		input "thermHeatSetpoint", "number", title: "Heat Setpoint", required: false
		input "thermCoolSetpoint", "number", title: "Cool Setpoint", required: false
	}
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "logHelloHome", "enum", title: "Log to Hello Home?", options: ["Yes", "No"], required: false
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send text message to", required: false
        }
	}

}

def installed() {
	if (isEnabled == "true")
		createSubscriptions()
}

def updated() {
	unsubscribe()
	if (isEnabled == "true")
		createSubscriptions()
}

def createSubscriptions()
{
	//subscribe(contactSensors, "contact.open", contactOpenHandler)
	subscribe(contactSensors, "contact.closed", contactClosedHandler)
	//subscribe(motionSensors, "motion.active", motionActiveHandler)
	//subscribe(motionSensors, "motion.inactive", motionInactiveHandler)

}


def contactClosedHandler(evt)
{
	log.debug "contact closed, setting schedule check"
	runIn(minutes1 * 60, scheduleCheck)
}

def scheduleCheck()
{
	log.debug "scheduleCheck"
	
	if (allQuiet()) {
		takeActions()
	}
}

private takeActions() {
	def message = "Elvis Has Left! Mode:$thermMode. "
	if (thermHeatSetpoint != null && thermHeatSetpoint != "") {
		thermostats.setHeatingSetpoint(thermHeatSetpoint)
		message += "Heat=$thermHeatSetpoint "
	}
	if (thermCoolSetpoint != null && thermCoolSetpoint != "") {
		thermostats.setCoolingSetpoint(thermCoolSetpoint)
		message += "Cool=$thermCoolSetpoint"
	}
	if (thermMode == "Auto") {
		thermostats.auto()
	} else if (thermMode == "Heat") {
		thermostats.heat()
	} else if (thermMode == "Cool") {
		thermostats.cool()
	}
	send(message)
	log.debug message
}

private allQuiet() {
	def threshold = minutes1 * 60 * 1000
	def t0 = new Date(now() - threshold)
	def result = true
	for (sensor in motionSensors) {
		def recentStates = sensor.statesSince("motion", t0)
		if (recentStates.find{it.value == "active"}) {
			log.debug "Found active state"
			result = false
			break
		}
	}
	if (result == true) {
		log.debug "No active states found"
	}
	result
}

private send(msg) {

	if (logHelloHome == "Yes") {
		//log.debug("sending Hello Home message")
		sendNotificationEvent(msg)
	}

	if (sendPushMessage == "Yes") {
		//log.debug("sending push message")
		sendPush(msg)
	}

	if (phoneNumber) {
		//log.debug("sending text message")
		sendSms(phoneNumber, msg)
	}

	//log.debug msg
}
