/**
 *  Night Lamp
 *
 *  Author: yongkimleng@gmail.com
 *  Date: 2013-12-23
 */
preferences {
	section("Configuration") {
    	input "switches", "capability.switch", title: "Switches", multiple: true
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

def initialize() {
	schedule_events()
}

def schedule_events() {
	// reschedule this event
    runOnce(timeTodayAfter(new Date(), "01:00", location.timeZone), schedule_events)
    
    def sunriseset = getSunriseAndSunset([locationString : location.zipCode])
    
    log.debug("Night Lamp :: Sunrise @ " + sunriseset['sunrise'] + ", sunset @ " + sunriseset['sunset'])
    
    runOnce(sunriseset['sunrise'], sunrise)
    runOnce(sunriseset['sunset'], sunset)
}

def sunrise() {
	for(def s : switches) {
    	s.off()
    }
}

def sunset() {
	for(def s : switches) {
    	s.on()
    }
}