/*
* ModeChanger
*
*/

preferences {
	page(name: "getPref", title: "Choose devices and Modes", install:true, uninstall: true) {
    	
   		section("Choose a motion detector to use...") {
			input "motionSensor", "capability.motion", title: "motion", multiple: false, required: true
   		}
		section("Change to a new mode when...") {
			input "onMode", "mode", title: "mode", required: false
			input "offMode", "mode", title: "mode", required: false 
		}
	}
}

def installed() {
	log.debug "Installed settings: ${settings}"
	subscribe(motionSensor, "motion", "motionHandler")
	
}

def updated() {
	log.debug "Updated settings: ${settings}"

	unsubscribe()
	subscribe(motionSensor, "motion", "motionHandler")
	
}


def motionHandler(evt) {
	if (evt.value == "inactive") {
    	changeMode(onMode)
    } else {
    	changeMode(offMode)
    }
}

def changeYourMode(newMode) {

	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		}
		else {
			log.debug "Error in mode ${newMode}'"
		}
	}
}

