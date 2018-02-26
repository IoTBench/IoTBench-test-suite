/**
 *  Turn It On When I'm Here
 *
 *  Author: barry
 */
preferences {
	section("When I arrive and leave..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Turn on/off a light..."){
		input "switch1", "capability.switch", multiple: true
	}
    section("For this amount of time (enter 0 for indefinite)") {
		input "minutes", "number", title: "Minutes?"
	}

	section("After this time of day") {
		input "timeOfDay", "time", title: "Time?"
	}
    
}

def installed()
{
	initSettings()
}

def updated()
{
	unsubscribe()
    initSettings()
}

def initSettings()
{
	subscribe(presence1, "presence", presenceHandler)
	if (state.modeStartTime == null) {
		state.modeStartTime = 0
	}    
}

def scheduleSwitchOff(durationMinutes)
{
	if (durationMinutes == 0) {
    	log.debug "leaving on indefinitely"
    } else {
        switch1.off(delay: durationMinutes * 60000)
        log.debug "Switching off in $durationMinutes minutes"
    }
}

def presenceHandler(evt)
{
	log.debug "presenceHandler $evt.name: $evt.value"
	def current = presence1.currentValue("presence")
	log.debug current
	def presenceValue = presence1.find{it.currentPresence == "present"}
	log.debug "presenceValue = $presenceValue"
	if ((presenceValue) && correctTime()) {
		switch1.on()
		log.debug "Someone's home!"   
        scheduleSwitchOff(minutes)
	}

}

private correctTime() {
	def t0 = now()
	def modeStartTime = new Date(state.modeStartTime)
	def startTime = timeTodayAfter(modeStartTime, timeOfDay, location.timeZone)
	if (t0 >= startTime.time) {
		log.debug "The current time of day (${new Date(t0)}), startTime = ($startTime)"
		true
	} else {
		log.debug "The current time of day (${new Date(t0)}), is not in the correct time window ($startTime):  doing nothing"
		false
	}
}



