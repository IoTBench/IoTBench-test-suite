/*
* ModeChanger
* used by users for various purposes.
*/
preferences {
	page(name: "getPref", title: "Choose Switch and Modes", install:true, uninstall: true) {
    	section("Choose a switch to use...") {
			input "switch", "capability.switch", title: "Switch", multiple: false, required: true
   		}
		section("Change to a new mode when...") {
			input "onMode", "mode", title: "Switch is on", required: false
			input "offMode", "mode", title: "Switch is off", required: false 
		}
	}
}

def installed() {
	log.debug "Installed settings: ${settings}"

	subscribe(controlSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated  settings: ${settings}"

	unsubscribe()
	
	subscribe(controlSwitch, "switch", "switchHandler")
}


def switchHandler(evt) {
	if (evt.value == "on") {
    	changeMode(offMode)
    } else {
    	changeMode(onMode)
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

