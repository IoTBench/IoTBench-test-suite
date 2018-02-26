/**
 *  App Name:   Turn on or off together
 *
 *  Author: 	chrisb 
 *  Date: 		2013-07-16
 *  
 *  This app "groups" a set of switches together so that if anyone is turned on
 *	or off, they all will go on or off.  This allows them to act as three-way
 *	without needing wiring between the switches.
 *	
 *  
 */

preferences {
    section("Turn on which switches or outlets?"){
		input "switches", "capability.switch", multiple: true
    }
}

def installed()
{
	subscribe(switches, "switch.on", switchOnHandler)
    subscribe(switches, "switch.off", switchOffHandler)
}

def updated()
{
	unsubscribe()
	subscribe(switches, "switch.on", switchOnHandler)
    subscribe(switches, "switch.off", switchOffHandler)
}


def switchOnHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
 	switches.on()
}


def switchOffHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
 	switches.off()
}
