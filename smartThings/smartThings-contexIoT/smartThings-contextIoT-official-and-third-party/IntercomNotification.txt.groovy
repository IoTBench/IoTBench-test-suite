/**
 *  Intercom Notification
 *
 *  Author: vrberry
 *  Date: 2014-06
 */
definition(
    name: "Intercom Notification",
    // namespace: "smartthings",
    author: "vrberry",
    description: "Get a push notification or text message when the intercom buzzer is triggered",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartthings-device-icons/Office/office9-icn.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-device-icons/Office/office9-icn@2x.png"
)

preferences {
	section("When this switch is on..."){
		input "ArduinoSwitch", "capability.switch", title: "Intercom Buzzer Activated", required: false
	}
	section("Send a push notification and/or an SMS message"){
    	input "pushAndPhone", "enum", title: "Also send an SMS?", required: false, metadata: [values: ["Yes","No"]]
		input "phone", "phone", title: "SMS Phone Number (optional)", required: false

	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(ArduinoSwitch, "switch.on", eventHandler, [filterEvents: false])
}

def eventHandler(evt) {
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			sendMessage(evt)
		}
	}
	else {
		sendMessage(evt)
	}
}

private sendMessage(evt) {
	def msg = "Apartment Intercom Buzzer Activated"
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (!phone || pushAndPhone != "No") {
		log.debug "sending push"
		sendPush(msg)
	}
	if (phone) {
		log.debug "sending SMS"
		sendSms(phone, msg)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}

