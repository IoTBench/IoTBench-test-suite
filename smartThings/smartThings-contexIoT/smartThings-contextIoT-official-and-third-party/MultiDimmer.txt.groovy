/**
 *  Multi Dimmer
 *
 *  Author: Brad Sileo
 *  Date: 2014-01-17
 *

 *
 */
preferences {
	section("Configure") {
		input "dimmer", "capability.switchLevel", title: "Which dimmer switch?", description: "Tap to select a switch", multiple: false
		input "bulbs", "capability.switchLevel", title: "Which lights to set?", description: "Tap to select switches/lights to turn on", required: true, multiple: true 
	
	}
}

def installed() {
	log.debug "Installing 'Multi Dim' with settings: ${settings}"

	commonInit()
}

def updated() {
	log.debug "Updating 'Multi Dim' with settings: ${settings}"
	unsubscribe()

	commonInit()
}

private commonInit() {
	subscribe(dimmer,"level",updateLevel)
    subscribe(dimmer,"switch.on",onHandler)
    subscribe(dimmer,"switch.off",offHandler)
}

def onHandler(evt) {
	 debug "Multi Dimmer: ON"
     //bulbs?.on()
}


def offHandler(evt) {
    debug "Multi Dimmer: OFF"
	bulbs?.off()
}



def updateLevel(evt) {
   debug "UpdateLevel: $evt"
	int level = dimmer.currentValue("level")
	debug "level: $level"
	
	bulbs.each { 
		debug "Setting dimmer: ${it} to level: ${level}"
		if (level == 0) { 
          it.off() 
        }
        else {
          it.on()
        }
    	it.setLevel(level)
	}

	debug "Done setting dimmers level to $level"
}



private debug(message) {
	log.debug "${message}"
}
