/**
 *  Light Quickly Follows Me
 *
 *  Author: SmartThings
 */


// Automatically generated. Make future change here.
definition(
    name: "Test Auto on then quick off",
    namespace: "",
    author: "jimmeadows@aol.com",
    description: "Turns light on when motion and quickly off when motion stops",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "seconds1", "number", title: "Seconds?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: false
	}
}

def installed() {
	subscribe(motion1, "motion", motionHandler)
}

def updated() {
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		log.debug "turning on light"
		switches.on()
	} else if (evt.value == "inactive") {
        log.debug "turning off shortly ..."
		runIn(seconds1 , scheduleCheck)
	}
}

def scheduleCheck() {
        switches.off()
    
}
