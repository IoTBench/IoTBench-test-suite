/**
 *  Notify Me When (LED Strip Edit)
 *
 *  Author: SmartThings / dome
 *  Date: 2014-03-03
 */

// Automatically generated. Make future change here.
definition(
    name: "Notify Me When SRC",
    namespace: "",
    author: "dome",
    description: "Flashes the LED strip momentarily when a presence detector comes into range. Requires Smart Room Controller - http://build.smartthings.com/projects/smartkitchen/",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
	}
    section("Send notifications and/or messages?"){
		input "notifications", "enum", title: "Send a message/push notification?", metadata:[values:["Yes","No"]], required:false
    }
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "text", title: "Message Text", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
		input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, metadata: [values: ["Yes","No"]]
	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
    section("Issue notifications with a Smart Room Controller") {
    	input "Ledstrip", "device.SmartRoomController", required: false, multiple: true
       	input "color", "enum", title: "What color?", required: true, multiple: false, options: ["White", "Red", "Green", "Blue", "Orange", "Purple", "Yellow"]
    	input "speed", "enum", title: "What flash pattern?", required: true, multiple: false, options: ["Fade", "Flash", "Strobe", "Persist", "Alert"]
   		input "audible", "enum", title: "What (optional) sound?", required: false, multiple: false, options: ["Beep1", "Beep2", "Siren1", "Siren2"]
   }
    
    
}


//		input "days", "enum", title: "What days would you like it to run on?", description: "Every day (default)", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

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
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
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

def alert() {
    
	if (settings.audible) {
    	if (audible == "Beep1") { Ledstrip.beep1() }
    	if (audible == "Beep2") { Ledstrip.beep2() }
    	if (audible == "Siren1") { Ledstrip.siren1() }
    	if (audible == "Siren2") { Ledstrip.siren2() }
    }
    
	if (speed == "Fade")
    {
    	if (color == "Red") { Ledstrip.fadered() }
    	if (color == "Green") { Ledstrip.fadegreen() }
    	if (color == "Blue") { Ledstrip.fadeblue() }
    	if (color == "Orange") { Ledstrip.fadeorange() }
    	if (color == "Yellow") { Ledstrip.fadeyellow() }
    	if (color == "Purple") { Ledstrip.fadepurple() }
    	if (color == "White") { Ledstrip.fadewhite() }    
    }
    if (speed == "Flash")
    {
    	if (color == "Red") { Ledstrip.flashred()  }
    	if (color == "Green") { Ledstrip.flashgreen() }
    	if (color == "Blue") { Ledstrip.flashblue() }
    	if (color == "orange") { Ledstrip.flashorange() }
    	if (color == "yellow") { Ledstrip.flashyellow() }
    	if (color == "purple") { Ledstrip.flashpurple() }
    	if (color == "white") { Ledstrip.flashwhite() }     
    }
    if (speed == "Strobe")
    {
    	if (color == "Red") { Ledstrip.strobered() }
    	if (color == "Green") { Ledstrip.strobegreen() }
    	if (color == "Blue") { Ledstrip.strobeblue() }
    	if (color == "Orange") { Ledstrip.strobeorange() }
    	if (color == "Yellow") { Ledstrip.strobeyellow() }
    	if (color == "Purple") { Ledstrip.strobepurple() }
    	if (color == "White") { Ledstrip.strobewhite() }    
    }
    if (speed == "Persist")
    {
    	if (color == "Red") { Ledstrip.red() }
    	if (color == "Green") { Ledstrip.green() }
    	if (color == "Blue") { Ledstrip.blue() }
    	if (color == "Orange") { Ledstrip.orange() }
    	if (color == "Yellow") { Ledstrip.yellow() }
    	if (color == "Purple") { Ledstrip.purple() }
    	if (color == "White") { Ledstrip.white() }    
    }
    if (speed == "Alert")
    {
    	if (color == "Red") { Ledstrip.alertred() }
    	if (color == "Green") { Ledstrip.alertgreen() }
    	if (color == "Blue") { Ledstrip.alertblue() }
    	if (color == "Orange") { Ledstrip.alertorange() }
    	if (color == "Yellow") { Ledstrip.alertyellow() }
    	if (color == "Purple") { Ledstrip.alertpurple() }
    	if (color == "White") { Ledstrip.alertwhite() }    
    }

    
//  	Ledstrip.flashgreen()  //this actually worked on the first try which is awesome!
}

/*
private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}
}
*/

private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"
	
    if ( notifications != "No" ) {

	if (!phone || pushAndPhone != "No") {
			log.debug "sending push"
			sendPush(msg)
		}
		if (phone) {
			log.debug "sending SMS"
			sendSms(phone, msg)
		}
    }
	if (frequency) {
		state[evt.deviceId] = now()
	}
    alert()
}

private defaultText(evt) {
	if (evt.name == "presence") {
		if (evt.value == "present") {
			if (includeArticle) {
				"$evt.linkText has arrived at the $location.name"
			}
			else {
				"$evt.linkText has arrived at $location.name"
			}
		}
		else {
			if (includeArticle) {
				"$evt.linkText has left the $location.name"
			}
			else {
				"$evt.linkText has left $location.name"
			}
		}
	}
	else {
		evt.descriptionText
	}
}

private getIncludeArticle() {
	def name = location.name.toLowerCase()
	def segs = name.split(" ")
	!(["work","home"].contains(name) || (segs.size() > 1 && (["the","my","a","an"].contains(segs[0]) || segs[0].endsWith("'s"))))
}
