/**
 *  Let There Be Light!
 *  Turn your lights on when an open/close sensor opens and off when the sensor closes.
 *
 *  Author: SmartThings
 */
preferences {
	section("When the door opens/closes...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Turn on/off a light...") {
		input "switch1", "capability.switch"
	}
}

def installed() {
	subscribe(contact1, "contact", contactHandler)
    log.debug "installed"
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact", contactHandler)
    log.debug "updated"
}

def contactHandler(evt) {
	log.debug "$evt.value"
	if (evt.value == "open") {
		switch1.on()
	} else if (evt.value == "closed") {
		switch1.off()
	}
}
