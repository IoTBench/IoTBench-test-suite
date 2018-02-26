/**
 *  Foscam Turn Off LEDs with Mode Change
 *
 *  Copyright 2014 skp19
 *
 */
definition(
    name: "Foscam Turn Off LEDs with Mode Change",
    namespace: "skp19",
    author: "skp19",
    description: "Disables Foscam LED when the mode changes to the selected mode.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When the mode changes to...") {
		input "LedMode", "mode"
	}
    section("Disable these Foscam LEDs...") {
		input "cameras", "capability.imageCapture", multiple: true
	}
    section("Only between these times...") {
    	input "startTime", "time", title: "Start Time", required: false
        input "endTime", "time", title: "End Time", required: false
    }
}

def installed() {
    subscribe(location, checkTime)
}

def updated() {
	unsubscribe()
    subscribe(location, checkTime)
}

def modeLed(evt) {
    if (evt.value == LedMode) {
        log.trace "Mode changed to ${evt.value}. Disabling Foscam LED."
        cameras?.ledOff()
    }
    else {
        log.trace "Mode changed to ${evt.value}. Enabling Foscam LED."
        cameras?.ledAuto()
    }
}

def checkTime(evt) {
    if(startTime && endTime) {
        def currentTime = new Date()
    	def startUTC = timeToday(startTime)
    	def endUTC = timeToday(endTime)
	    if((currentTime > startUTC && currentTime < endUTC && startUTC < endUTC) || (currentTime > startUTC && startUTC > endUTC) || (currentTime < endUTC && endUTC < startUTC)) {
    		modeLed(evt)
    	}
    }
    else {
    	modeLed(evt)
    }
}
