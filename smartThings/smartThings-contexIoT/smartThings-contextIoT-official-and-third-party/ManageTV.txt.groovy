/**
 *  Manage TV
 *
 *  Author: jpsilvaa@gmail.com
 *  Date: 2014-03-15
*/
preferences {
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}
// TODO: implement event handlers
/**
 *  Home Theater Control
 *
 *
 * Enable the following capabilities for the Device Type - button, switch, refresh
 *
 * Add the following custom commands - volUp, volDown, Input4, Input5, Input6, mute, unmute
 *
 *  Date: 2014-02-24
 */