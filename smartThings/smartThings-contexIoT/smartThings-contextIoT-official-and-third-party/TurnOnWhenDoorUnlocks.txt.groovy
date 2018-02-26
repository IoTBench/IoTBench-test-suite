/**
 *  Turn On When Door Unlocks
 *
 *  Copyright 2014 skp19
 *
 */
definition(
    name: "Turn On When Door Unlocks",
    namespace: "skp19",
    author: "skp19",
    description: "Turns on a device when the door is unlocked",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Which door locks?") {
		input "lock1", "capability.lock", multiple: true
	}
    section("Which switches?") {
    	input "switches", "capability.switch", multiple: true, required: false
    }
    section("Turn on or off?") {
    	input "turnon", "bool", title: "Turn on when door unlocks?", required: false
        input "turnoff", "bool", title: "Turn off when door locks?", required: false
    }
    section("Change mode?") {
    	input "unlockmode", "mode", title: "Change to this mode when the door unlocks", required: false
        input "lockmode", "mode", title: "Change to this mode when the door locks", required: false
    }
    section("Only between these times...") {
    	input "startTime", "time", title: "Start Time", required: false
        input "endTime", "time", title: "End Time", required: false
    }
    section(title: "More Options", hidden: hideOptions(), hideable: true) {
    	input "turnoffdelay", "number", title: "Delay turning off (Minutes)", required: false
    }
}

def installed() {
    subscribe(lock1, "lock", checkTime)
}

def updated() {
	unsubscribe()
    subscribe(lock1, "lock", checkTime)
}

def turniton(evt) {
    log.debug "$evt.value: $evt, $settings"
    if(evt.value == "unlocked") {
		if(turnon && switches) {
			log.trace "Turning on switches: $switches"
        	switches.on()
		}
		
        if(unlockmode) {
           	changeMode(unlockmode)
        }

    }

    if(evt.value == "locked") {
		if(turnoff && switches) {
            if(turnoffdelay) {
            	log.trace "Waiting to turn off switches for $turnoffdelay minutes"
	            def turnOffDelaySeconds = 60 * turnoffdelay
				runIn(turnOffDelaySeconds, turnitoff)
            }
        	else {
        		turnitoff()
            }
        }
		
        if(lockmode) {
			changeMode(lockmode)
        }
    }
}

def turnitoff() {
	log.trace "Turning off switches: $switches"
	switches.off()
}

def changeMode(newMode) {
	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
			log.trace "Mode changed to '${newMode}'"
		}
		else {
			log.trace "Undefined mode '${newMode}'"
		}
	}
}

def checkTime(evt) {
    if(startTime && endTime) {
        def currentTime = new Date()
    	def startUTC = timeToday(startTime)
    	def endUTC = timeToday(endTime)
	    if((currentTime > startUTC && currentTime < endUTC && startUTC < endUTC) || (currentTime > startUTC && startUTC > endUTC) || (currentTime < endUTC && endUTC < startUTC)) {
    		turniton(evt)
    	}
    }
    else {
    	turniton(evt)
    }
}

def hideOptions() {
	false
}
