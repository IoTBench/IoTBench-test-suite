/**
 *  SomeAtHome
 *
 *  Author: b.dahlem@gmail.com
 *  Date: 2013-08-20
 *
 *  BASED ON
 *
 *  Bon Voyage
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 *
 *  Monitors a set of presence detectors and triggers a mode change when everyone has left.
 */


// Automatically generated. Make future change here.
definition(
    name: "SomeAtHome",
    namespace: "",
    author: "b.dahlem@gmail.com",
    description: "One (or more) Person At Home Mode Change",
    category: "My Apps",
    iconUrl: "https://cdn1.iconfinder.com/data/icons/Vista-Inspirate_1.0/64x64/filesystems/folder_home.png",
    iconX2Url: "https://cdn1.iconfinder.com/data/icons/Vista-Inspirate_1.0/128x128/filesystems/folder_home.png",
    oauth: true
)

preferences {
	section("When all of these people are at home:") {
		input "peopleHome", "capability.presenceSensor", multiple: true
	}
    section("When all of these people are away (optional):") {
    	input "peopleAway", "capability.presenceSensor", multiple: true, required: false
    }
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
    section("And text me at (optional)") {
		input "phone", "phone", title: "Phone number?", required: false
	}
    section("or send SmartThings Notification (optional)") {
        input "pushNotification", "enum", title: "Send SmartThings Notification?", required: true,
            metadata :[
                values: ['Yes', 'No'
                        ]
            ]
    }
    section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${peopleHome.collect{it.label + ': ' + it.currentPresence}} ${peopleAway.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(peopleHome, "presence", presence)
    subscribe(peopleAway, "presence", presence)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${peopleHome.collect{it.label + ': ' + it.currentPresence}} ${peopleAway.collect{it.label + ': ' + it.currentPresence}}"
	unsubscribe()
	subscribe(peopleHome, "presence", presence)
    subscribe(peopleAway, "presence", presence)
}

def presence(evt)
{
	log.debug "evt.name: $evt.value"

	if (location.mode != newMode) {  						 		// If we are not already in the desired mode
        log.debug "checking everyone's presence status"  			// Check where everyone is located
        if (checkEveryone()) {										// Change modes once every (false alarm threshold) minutes
            log.debug "starting sequence"							// if people are home and away as specified 
            def delay = falseAlarmThreshold != null ? falseAlarmThreshold * 60 : 10 * 60
            runIn(delay, "takeAction")
        }	
        else {													    // otherwise, cancel any pending mode change
            log.debug "canceling"
		    unschedule("takeAction")
        }
    }
    else {
        log.debug "mode is the same, not evaluating"
    }
}

def takeAction()
{
	setLocationMode(newMode)
    
	// TODO -- uncomment when app label is available
	//def message = "${app.label} changed your mode to '${newMode}'."
	def message = "SmartThings changed your mode to '${newMode}'."
	log.info message
    if(pushNotification == "Yes")
    {
      log.debug "Sending SmartThings Notification" 
	  sendPush(message)
    }
	if (phone) {
		sendSms(phone, message)
	}

    unschedule("takeAction") // Temporary work-around to scheduling bug
}

private checkEveryone()
{
	def result = true
    
	for (person in peopleHome) {
		if (person.currentPresence != "present") {
			result = false
			break
		}
	}
    
	log.debug "peopleAreHome: $result"
    
    for (person in peopleAway) {
		if (person.currentPresence == "present") {
			result = false
			break
		}
	}
    
	log.debug "peopleAreAway: $result"
    
	return result
}