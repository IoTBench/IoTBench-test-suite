/**
*  Walk Gentle Into That Good Night
*
*  Author: oneaccttorulethehouse@gmail.com
*  Date: 2014-02-01
*
* Borrowed heavily from Smart Nightlight and Big Turn OFF
*
* This app will turn off your lights after a set number minutes and then change the mode. 
* Works like Darken Behind Me without the need for motion sensors.
*
*
 */
preferences {
	section("When I touch the app turn these lights off"){
		input "switches", "capability.switch", multiple: true, required:true
	}
	section("And change to this mode...") {
		input "newMode", "mode", title: "Mode?"
	}
   section("After so many minutes (optional)"){
		input "waitfor", "number", title: "Off after (default 2)", required: false
	}
}


def installed()
{
	log.debug "Installed with settings: ${settings}"
	log.debug "Current mode = ${location.mode}"
	subscribe(app, appTouch)
}


def updated()
{
	log.debug "Updated with settings: ${settings}"
	log.debug "Current mode = ${location.mode}"
	unsubscribe()
	subscribe(app, appTouch)
}

def appTouch(evt) {
	log.debug "changeMode, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
    if (location.mode != newMode) {
   			setLocationMode(newMode)
			log.debug "Changed the mode to '${newMode}'"
    }	else {
    	log.debug "New mode is the same as the old mode, leaving it be"
    	}
    log.debug "appTouch: $evt"
    def delay = (waitfor != null && waitfor != "") ? waitfor * 60000 : 120000
	switches.off(delay: delay)
}
