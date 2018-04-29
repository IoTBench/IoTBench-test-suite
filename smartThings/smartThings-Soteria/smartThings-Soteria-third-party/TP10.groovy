/**
 * TP10 Third Party app  
 * Dual Zone Smart Night Light
 * https://raw.githubusercontent.com/tslagle13/SmartThingsPersonal/master/smartapps/tslagle13/dual-zone-smart-night-light.src/dual-zone-smart-night-light.groovy
 * Soteria Analyzed.
 * Notes: conflicting device attributes with different events
 */
definition(
    name: "Dual Zone Smart Night Light",
    namespace: "tslagle13",
    author: "Tim Slagle",
    description: "Turns on lights using motion and contact sensor. Both values must be closed/not active in order for lights to turn off.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Control these lights..."){
		input "lights", "capability.switch", multiple: true
	}
	section("Turning on when a contact opens and there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?", required:true, multiple:true
        input "contactSensor", "capability.contactSensor", title: "Which?", required:true, multiple:true
	}
	section("And then off when it's light or there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
}


def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motionSensor, "motion", motionHandler)
    subscribe(contactSensor, "contact", contactHandler)
    //subscribe(app, turnOffMotionAfterDelay)
}
    
def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		
			log.debug "turning on lights due to motion"
			lights.on()
			state.lastStatus = "on"
		
		state.motionStopTime = null
	}
	else {																					// Motion has stoped
		state.motionStopTime = now()																																// The on button was NOT pushed so...
        	if(delayMinutes) {																	// If the user set a delay then...
				runIn(delayMinutes*60, turnOffMotionAfterDelay)				// Schedule the "lights off" for later.
			} else {																			// Otherwise...
				turnOffMotionAfterDelay()														// Run the lights off now.
			
       } 
	}
}

def contactHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "open") {
		
			log.debug "turning on lights due to motion"
			lights.on()
			state.lastStatus = "on"
		
		state.motionStopTime = null
	}
	else {																					// Motion has stoped
		state.motionStopTime = now()
																																// The on button was NOT pushed so...
        	if(delayMinutes) {																	// If the user set a delay then...
				runIn(delayMinutes*60, turnOffMotionAfterDelay)				// Schedule the "lights off" for later.
			} else {																			// Otherwise...
				turnOffMotionAfterDelay()														// Run the lights off now.
			}
        
	}
}

def turnOffMotionAfterDelay(evt) {
	log.debug "In turnOffMotionAfterDelay"
	if (allOk){
			lights.off()
	}	
	
    else{
    runIn(delayMinutes*60, turnOffMotionAfterDelay)
    log.debug("scheduling")
    }
}

private getAllOk() {
motionOk && doorsOk
}

private getMotionOk() {
	def result = !motionSensors.latestValue("motion").contains("active")
	log.trace "motionOk = $result"
	result
}

private getDoorsOk() {
	def result = !contactSensor.latestValue("contact").contains("open")
	log.trace "doorsOk = $result"
	result
}