/**
 *  Strobe When I am Home and Someone Arrives
 *
 *  Author: Jordan Terrell
 *  Date: 2013-07-27
 */
preferences {
	section("When I'm here...") {
		input "presence1", "capability.presenceSensor", title: "Who's here?"
	}
    
    section("And someone arrives...") {
    	input "presence2", "capability.presenceSensor", title: "Who arrives?"
    }
    
    section("Turn the alarm stobe on...") {
    	input "alarm", "capability.alarm", title: "Which alarm strobe?"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(presence2, "presence", onPresenceChange)
}

def onPresenceChange(evt) {
	log.debug "evt.name: $evt.value"
    def youreHere = presence1.latestValue == "present"
    def theyreHere = presence2.latestValue == "present"
    
    log.debug "Your Presence: ${presence1.latestValue}"
    log.debug "Their Presence: ${presence2.latestValue}"
    
    log.debug "You're Here: ${youreHere}"
    log.debug "They're Here: ${theyreHere}"
    
    if(youreHere && theyreHere) {
    	alarm.strobe()
    }
}