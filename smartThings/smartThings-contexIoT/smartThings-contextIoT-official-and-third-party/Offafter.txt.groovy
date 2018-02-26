/**
 *  Turn a switch off after a period of time.
 *
 *  Author: garymullen@gmail.com
 */
preferences {
	section("When it is turned on..."){
		input "switch1", "capability.switch"
	}
	section("Turn it off after..."){
		input "max_on_time", "number", title: "Minutes?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(switch1, "switch", onHandler)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    subscribe(switch1, "switch", onHandler)
}

def onHandler(evt) {
	log.debug "onHandler called evt: ${evt.value}"
    
	if (evt.value == "on") {
        log.debug "scheduling turnOffHandler"
    	runIn( max_on_time * 60, turnOffHandler )
    } else {
    	log.debug "unscheduling turnOffHandler"
    	unschedule(turnOffHandler);
	}
    
}

def turnOffHandler() {
	log.debug "turnOffHandler called."
    switch1.off()
}
