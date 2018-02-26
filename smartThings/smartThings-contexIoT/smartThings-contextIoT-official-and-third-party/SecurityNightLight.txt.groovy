/**
 *  Clever Night Light
 *
 *  Author: Brian Steere
 */

 preferences {

    section("Turn on") {
        input "switches", "capability.switch", title: "Things", multiple: true
    }

    section("Until:") {
        input "onUntil", "time", title: "Leave on until"
    }

    section("Turn on when there's movement..."){
        input "motion1", "capability.motionSensor", title: "Where?"
        input "motionOnTime", "number", title: "Leave on for how long (minutes)"
    }
}


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()

    initialize()
}

def initialize() {
	log.debug "Settings: $settings"
    
	schedule(onUntil, modeStopThings)
    schedule("1 5 0 * * ?", setupSchedule)
    
    subscribe(motion1, "motion", motionHandler)
    setupSchedule()
}

def setupSchedule() {
    def now = new Date()
    def times = getSunriseAndSunset()
    def sunrise = times.sunrise
    def sunset = times.sunset
    
    log.debug "Rise: $sunrise | Set: $sunset | Now: $now"
    
    def offTime = timeToday(onUntil)
    
    if(now > sunset) {
    	if(now < offTime) {
            log.debug "Before off time: $offTime"
            modeStartThings()
        }
    } else {
    	log.debug "Scheduling start: ${sunset}"
    	runOnce(sunset, modeStartThings)
    }
}

def motionHandler(evt) {    
    if(!state.modeStarted) {
        def now = new Date()
        def times = getSunriseAndSunset()
        def sunrise = times.sunrise
        def sunset = times.sunset
        
        if (evt.value == "active" && (now > sunset || now < sunrise)) {
            log.debug "Saw Motion"

            // Unschedule the stopping of things
            unschedule(motionStopThings)

            startThings()
            state.motionStarted = true;
        }
        else if (evt.value == "inactive") {
            log.debug "No More Motion"
            runIn(motionOnTime * 60, motionStopThings)
        }
    }
}

def modeStartThings() {
    log.debug "Mode starting things"
    state.modeStarted = true
    unschedule(motionStopThings)
    startThings()
}

def modeStopThings() {
    log.debug "Mode stopping things"
    state.modeStarted = false
    stopThings()
}

def motionStopThings() {
    stopThings()
    state.motionStarted = false
}

def startThings() {
    log.debug "Starting things"
    state.thingsOn = true
    switches.on()
}

def stopThings() {
    log.debug "Stopping things"
    switches.off()
    state.thingsOn = false
}