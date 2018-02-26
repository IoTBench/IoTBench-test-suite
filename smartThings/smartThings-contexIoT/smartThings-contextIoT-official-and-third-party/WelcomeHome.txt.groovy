/**
 *  Greetings Earthling
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */
definition(
    name: "Welcome Home!",
    namespace: "sriquier",
    author: "Scott Riquier",
    description: "Monitors a set of presence detectors and turns on switches when someone arrives home.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {

	section("When one of these people arrive at home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Turn on these switches") {
		input "switches", "capability.switch", title: "Switches?", multiple: true
	}
	section("Turn on these dimmers") {
		input "dimmers", "capability.switchLevel", title: "Dimmers?", multiple: true
	}
	section("Only after sunset") {
		input "onlyDark", "enum", title: "Only when dark?", options: ["Yes", "No"]
	}
	section("Then turn them off in (defaults to keep them on)") {
		input "offThreshold", "decimal", title: "Number of minutes", required: false
	}
	section("Unless the person was gone less than (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
	section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phone", "phone", title: "Send a Text Message?", required: false
        }
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(people, "presence", presence)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	unsubscribe()
	subscribe(people, "presence", presence)
}

def presence(evt)
{
	log.debug "evt.name: $evt.value"
    
	def threshold = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? (falseAlarmThreshold * 60 * 1000) as Long : 10 * 60 * 1000L
    
    def t0 = new Date(now() - threshold)
    
    if (evt.value == "present") {

        def person = getPerson(evt)
        def recentNotPresent = person.statesSince("presence", t0).find{it.value == "not present"}
        if (recentNotPresent) {
            log.debug "skipping notification of arrival of ${person.displayName} because last departure was only ${now() - recentNotPresent.date.time} msec ago"
        }
        else if (onlyDark == "Yes" && getSunriseAndSunset().sunset.time > now()){
        	log.debug "not turning on any switches because it is not yet sunset."
        }
        else {
            def message = "${person.displayName} arrived at home, turning on requested switches."
            log.info message
            sendNotificationEvent(message)
            send(message)
			switches?.on()
            dimmers?.setLevel(99)
            if (offThreshold != null && offThreshold != "" && offThreshold > 0) {
            	runIn(60*offThreshold, switchesOff)
            }
        }
    }
}

def switchesOff() {
	log.debug "turning off requested switches after arrival as requested."
    sendNotificationEvent("Turning off switches ${offThreshold} minutes after arrival as requested.")
	switches?.off()
}

private getPerson(evt)
{
	people.find{evt.deviceId == it.id}
}

private send(msg) {
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (sendPushMessage == "Yes") {
            log.debug("sending push message")
            sendPush(msg)
        }

        if (phone) {
            log.debug("sending text message")
            sendSms(phone, msg)
        }
    }

	log.debug msg
}