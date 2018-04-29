/*
* Auto-mode change
* notes: present sensor(?) is not used.
*/
preferences {
	page(name: "getPref", title: "Choose Devices and Modes", install:true, uninstall: true) {
    	section("Choose a switch to use...") {
			input "switch", "capability.switch", title: "Switch", multiple: false, required: true
   		}
   		 section("Change Mode when there is no motion and presence") {
    		input "motionSensor", "capability.motionSensor", title: "Choose motion sensor"
    		input "presenceSensors", "capability.presenceSensor", title: "Choose presence sensors", multiple: true
  }

		section("Change to a new mode when...") {
			input "onMode", "mode", title: "Switch is on", required: false
			input "offMode", "mode", title: "Switch is off", required: false 
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(motionSensor, "motion", "motionHandler")
	subscribe(controlSwitch, "switch", "switchHandler")
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(motionSensor, "motion", "motionHandler")
	subscribe(controlSwitch, "switch", "switchHandler")
}


def switchHandler(evt) {
	if (evt.value == "on") {
    	changeMode(offMode)
    } else {
    	changeMode(onMode)
    }
}


def motionHandler(evt) {
	if (evt.value == "on") {
    	changeMode(onMode)
    } else {
    	changeMode(offMode)
    }
}

def changeMode(newMode) {

	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
		}
		else {
		log.debug "Unable to change to undefined mode '${newMode}'"
		}
	}
}

