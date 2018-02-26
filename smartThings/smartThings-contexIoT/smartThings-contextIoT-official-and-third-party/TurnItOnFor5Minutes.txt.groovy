/**
 *  Turn It On For 5 Minutes
 *  Turn on a switch when a contact sensor opens and then turn it back off 5 minutes later.
 *
 *  Author: SmartThings
 */
preferences {
	section("When it opens..."){
		input "contact1", "capability.contactSensor"
	}
	section("Turn on a switch for 5 minutes..."){
		input "switch1", "capability.switch"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	switch1.on()
	switch1.off(delay: 300000)
}