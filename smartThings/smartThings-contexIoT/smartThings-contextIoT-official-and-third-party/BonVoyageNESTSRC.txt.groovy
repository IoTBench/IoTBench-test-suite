/**
 *  Bon Voyage
 *
 *  Author: SmartThings
 *	Edited by DOME
 *  Date: 2014-03-03
 *
 *  Monitors a set of presence detectors and triggers a mode change when everyone has left.
 *  Additionally, sets a Nest thermostat to Away and flashes LED notifications.
 */


// Automatically generated. Make future change here.
definition(
    name: "Bon Voyage NEST/SRC",
    namespace: "",
    author: "dome",
    description: "Sets Nest to away and flashes notifications on Smart Room Controller LED strips in addition to push notifications and mode changes. Requires Smart Room Controller for LED strip functionality - http://build.smartthings.com/projects/smartkitchen/",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/App-LightUpMyWorld@2x.png"
)

preferences {
	section("When all of these people leave home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
    section("Set this NEST to AWAY") {
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
	if (evt.value == "not present") {
		if (location.mode != newMode) {
			log.debug "checking if everyone is away"
			if (everyoneIsAway()) {
				log.debug "starting sequence"
				runIn(findFalseAlarmThreshold() * 60, "takeAction", [overwrite: false])
			}
		}
		else {
			log.debug "mode is the same, not evaluating"
		}
	}
	else {
		log.debug "present; doing nothing"
	}
}

def takeAction()
{
	if (everyoneIsAway()) {
		def threshold = 1000 * 60 * findFalseAlarmThreshold() - 1000
		def awayLongEnough = people.findAll { person ->
			def presenceState = person.currentState("presence")
			def elapsed = now() - presenceState.rawDateCreated.time
			elapsed >= threshold
		}
		log.debug "Found ${awayLongEnough.size()} out of ${people.size()} person(s) who were away long enough"
		if (awayLongEnough.size() == people.size()) {
			// TODO -- uncomment when app label is available
			//def message = "${app.label} changed your mode to '${newMode}' because everyone left home"
			def message = "SmartThings changed your mode to '${newMode}' because everyone left home"
			log.info message
			send(message)
			setLocationMode(newMode)
            log.debug( "Setting NEST to AWAY" )
			Nest.away()
            alert()
		} else {
			log.debug "not everyone has been away long enough; doing nothing"
		}
	} else {
    	log.debug "not everyone is away; doing nothing"
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


private everyoneIsAway()
{
	def result = true
	for (person in people) {
		if (person.currentPresence == "present") {
			result = false
			break
		}
	}
	log.debug "everyoneIsAway: $result"
	return result
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

private findFalseAlarmThreshold() {
	(falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold : 10
}
