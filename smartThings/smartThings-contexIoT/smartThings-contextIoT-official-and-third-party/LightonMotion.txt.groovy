/**
 *  Test
 *
 *  Author: kennyyork@centralite.com
 *  Date: 2013-12-24
 */
preferences
{
    section("Select Motion Detector") {
        input "motion_detector", "capability.motionSensor", title: "Where?"
    }
    section("Select Light") {
        input "switches", "capability.switch", multiple: true
    }
    section("Turn off after how many minutes?") {
        input "minutesLater", "number", title: "Minutes?"
    }
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    unschedule()
	initialize()
}

def turnOff() {
	log.debug "Timer fired, turning of light(s)"
	switches.off()
}

def motionHandler(evt) {
	if (evt.value == "active") {                // If there is movement then...
        log.debug "Motion detected, turning on light and killing timer"
        switches.on()
        unschedule( turnOff )                   // ...we don't need to turn it off.
    }
    else {                                      // If there is no movement then...
        def delay = minutesLater * 60           // runIn uses seconds
        log.debug "Motion cleared, turning off switches in ${minutesLater} minutes (${delay}s)."
        runIn( delay, turnOff )                 // ...schedule to turn off in x minutes.
    }
}

def initialize() {
	log.info "Initializing, subscribing to motion event at ${motionHandler} on ${motion_detector}"
    subscribe(motion_detector, "motion", motionHandler)
}
// TODO: implement event handlers