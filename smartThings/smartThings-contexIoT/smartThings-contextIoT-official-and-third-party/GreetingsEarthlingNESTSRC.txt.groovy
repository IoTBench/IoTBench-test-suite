/**
 *  Greetings Earthling NEST/Smart Room Controller
 *
 *  Author: SmartThings / dome
 *  Date: 2014-03-03
 */

// Automatically generated. Make future change here.
definition(
    name: "Greetings Earthling NEST/SRC",
    namespace: "",
    author: "dome",
    description: "Sets Nest to present and flashes notifications on Smart Room Controller LED strips in addition to push notifications and mode changes. Requires Smart Room Controller for LED strip functionality - http://build.smartthings.com/projects/smartkitchen/",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {

	section("When one of these people arrive at home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
    section("Set this NEST to PRESENT") {
		input "Nest", "capability.thermostat", required: false, multiple: true
	}
    section("Flash notifications with an LED strip") {
    	input "Ledstrip", "device.SmartRoomController", required: false, multiple: true
       	input "color", "enum", title: "What color?", required: false, multiple: false, options: ["White", "Red", "Green", "Blue", "Orange", "Purple", "Yellow"]
    	input "speed", "enum", title: "What flash pattern?", required: false, multiple: false, options: ["Fade", "Flash", "Strobe", "Persist"]
    }

	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
		input "phone", "phone", title: "Send a Text Message?", required: false
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
                log.debug( "Setting NEST to PRESENT" )
				Nest.present()
                alert()
			}
		}
	}
	else {
		log.debug "mode is the same, not evaluating"
	}
}

def alert() {
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
    	if (color == "Red") { Ledstrip.persistred() }
    	if (color == "Green") { Ledstrip.persistgreen() }
    	if (color == "Blue") { Ledstrip.persistblue() }
    	if (color == "Orange") { Ledstrip.persistorange() }
    	if (color == "Yellow") { Ledstrip.persistyellow() }
    	if (color == "Purple") { Ledstrip.persistpurple() }
    	if (color == "White") { Ledstrip.persistwhite() }    
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