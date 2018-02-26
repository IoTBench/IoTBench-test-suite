/**
 *  one lamp to rule them all
 *      When a master plug finds that its connected device is drawing current, 
 *      it issues an ON to the other slave lamp modules.    
 *      When the current falls, it issues an OFF to the other lamp modules.
 *
 *  TODO:
 *   - unfortunately, at the least device I have, power events only occur once every 5 minutes.
 *     I kinda wanted an immediate notice on level change.
 *
 *  Author: gfranxman@gmail.com
 *  Date: 2014-01-04
 */
preferences {
	section("When this plug sees a load..."){
		input "mastermeteringplug", "capability.energyMeter", title: "Which master?"
	}
	section("light these lamps...") {
		input "lamp","capability.switch", multiple: true
	}
}

/*
 * app lifecycle
 */
def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}


/*
 * event handlers
 */

def appTouch(evt) {
	// called when the user hit the play button overlayed on the app icon
	log.debug "onelamp app touch event: poll, refresh"
    try{
    	//inspect()
    	mastermeteringplug.poll()
    	mastermeteringplug.refresh()
    } catch(e) {
    	log.debug "exception caught"
    }
}

def masterHandler(evt) {
	log.debug "master power level: $evt.value"
    if (evt.integerValue != 0 ) {
    	log.debug( "lamp on" )
		lamp.on()
	}
    if (evt.value == "0" ) {
    	log.debug( "lamp off" )
    	lamp.off()
    }

}


/*
 * library
 */
 
def initialize() {
	// subscribe to attributes, devices, locations, etc.
    log.trace "initialize:subscribing to power..." 
    inspect( mastermeteringplug )
    subscribe(mastermeteringplug, "power", masterHandler) // energy event keep track of kw/hr, fyi
    subscribe(app) // to enable our appTouch handler, add the play button to our icon
}

def inspect( aDevice ) {
		log.debug "inspecting master"
        for( c in aDevice.capabilities) {
        	log.debug "capability: $c.name"
        }
        
        try {
        	for( cmd in aDevice.supportedCommands ) {
        		log.debug "command: $cmd.name"
        	}
        } catch(e) {
        	log.warn "error checking supported commands: $e" 
        }
        
        for( a in aDevice.supportedAttributes ) {
        	log.debug "attribute: $a.name"
        }
}

