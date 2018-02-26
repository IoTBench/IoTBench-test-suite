/**
 *  Someone is Knocking - Door Closed
 *
 *  Author: scottjones67@gmail.com
 *  Date: 2013-10-09
 */

// Automatically generated. Make future change here.
definition(
    name: "Someone is Knocking - Door Closed",
    namespace: "",
    author: "scottjones67@gmail.com",
    description: "Detects acceleration (knocking) only when the door is closed. Optionally turns on Light(s) for a specific amount of time.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When someone knocks on this door..."){
		input "acceleration", "capability.accelerationSensor", title: "Knocking Detected"
        input "contact", "capability.contactSensor", title: "And the door is closed"
	}
    section("Turn on these lights"){
		input "switches", "capability.switch", title: "Turn On (optional)", required: false, multiple: true
        input "delayMinutes", "number", title: "And off after minutes?", required: false
	}	
    section("Send this message (optional, sends standard message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input "phone1", "phone", title: "Phone Number (optional)", required: false
        input "phone2", "phone", title: "Phone Number (optional)", required: false
 		input "phone3", "phone", title: "Phone Number (optional)", required: false
        input "phone4", "phone", title: "Phone Number (optional)", required: false
		input "pushAndPhone", "enum", title: "Also send push", required: false, metadata: [values: ["Yes","No"]]
	}
	section("Minimum time between messages (optional, defaults to every message") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	subscribeToEvents()
}

def updated() {
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(acceleration, "acceleration.active", knockHandler)
  	subscribe(contact, "contact.closed", doorClosed)
}

def doorClosed(evt) {
  state.lastClosed = now()
}

def knockHandler(evt) {
	wait: (2*1000)
	if ((contact.latestValue("contact") == "closed") &&
    	(now() - (60 * 1000) > state.lastClosed)) {
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
}

def lightsoff(){
switches.off()
}

private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"
	if (!phone1 || pushAndPhone == "Yes") {
		log.debug "sending push"
		sendPush(msg)
	}
	if (phone1) {
		log.debug "sending SMS"
		sendSms(phone1, msg)
	}
	if (phone2) {
		log.debug "sending SMS"
		sendSms(phone2, msg)
	}
    if (phone3) {
		log.debug "sending SMS"
		sendSms(phone3, msg)
	}
	if (phone4) {
		log.debug "sending SMS"
		sendSms(phone4, msg)
	}    
    if (switches){
    	switches.on()
		runIn(delayMinutes * 60, lightsoff)
    }
	if (frequency) {
		state[evt.deviceId] = now()
	}
}
private defaultText(evt) {
	"Someone is knocking at $contact"
}