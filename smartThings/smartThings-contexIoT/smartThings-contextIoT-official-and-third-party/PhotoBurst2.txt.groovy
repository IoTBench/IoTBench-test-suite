/**
 *  Photo Burst When...
 *
 *  Author: SmartThings
 *
 *  Date: 2013-09-30
 */

definition(
    name: "Photo Burst 2",
    namespace: "smartthings",
    author: "Steve",
    description: "Take a burst of photos and send a push notification when...",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/photo-burst-when@2x.png"
)

preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
	}
	section("Take a burst of pictures") {
		input "camera", "capability.imageCapture"
		input "burst", "number", title: "How many? (default 5)", defaultValue:5
	}
	section("Then send this message in a push notification or text message (default 'Photo burst')"){
		input "messageText", "text", title: "Message Text", defaultValue: "Photo burst"
	}
    section( "Push notification?" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
    }
	section("And as text message to this number (optional)"){
		input "phone", "phone", title: "Phone Number", required: false
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
	subscribe(contact, "contact.open", sendMessage)
	subscribe(acceleration, "acceleration.active", sendMessage)
	subscribe(motion, "motion.active", sendMessage)
	subscribe(mySwitch, "switch.on", sendMessage)
	subscribe(arrivalPresence, "presence.present", sendMessage)
	subscribe(departurePresence, "presence.not present", sendMessage)
}

def sendMessage(evt) {
	log.debug "$evt.name: $evt.value, $messageText"
    if ( sendPushMessage != "No" ) {
       sendPush(messageText)
    }


	camera.take()
	(1..((burstCount ?: 5) - 1)).each {
		camera.take(delay: (500 * it))
	}

	if (phone) {
		sendSms(phone, messageText)
	}
}