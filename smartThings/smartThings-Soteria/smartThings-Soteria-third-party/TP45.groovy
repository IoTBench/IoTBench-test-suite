preferences {
	section("Select Switch to monitor"){
		input "theSwitch", "capability.switch"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    initialize()
}

def onHandler(evt) {
	log.debug "Received on from ${theSwitch}"
    if(location.mode != "Sleeping") {
        setLocationMode("Sleeping")
    } else {
    	log.debug "Already Sleeping - ignoring"
    }
    //theSwitch.off()
}

def offHandler(evt) {
	log.debug "Received off from ${theSwitch}"
}

def initialize() {
	subscribe(theSwitch, "switch.On", onHandler)
    subscribe(theSwitch, "switch.Off", offHandler)
    }
