/**
 *  Power On When Motion
 *
 *  Author: thecjreynolds@gmail.com
 *  Date: 2013-11-07
 */
preferences {
	section("When Motion is Detected"){
		input "motion", "capability.motionSensor", title: "Motion Here", required: true, multiple: false
	}
	section("Turn ON a light..."){
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: true, multiple: true
	}
	section("If no motion is detected for this many minutes, turn light(s) OFF (optional)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(motion, "motion.active", motionActive)
	subscribe(motion, "motion.inactive", motionInActive)
}

def motionActive(evt) {
	mySwitch.on();
	state.InActiveTurnOff = false
}

def motionInActive(evt) {
	if (frequency) {  	
        if(!state.InActiveTurnOff) 
        {
        	state.InActiveTurnOff = true             	
			runIn(frequency * 60, turnOffLight)
        }
	}
}

def turnOffLight() {
	if(state.InActiveTurnOff)
    {
        mySwitch.off();
        state.InActiveTurnOff = false
    }
}