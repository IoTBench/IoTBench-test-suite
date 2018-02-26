/**
 *  Delayed Command Execution
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

def installed()
{
	subscribe(contact1, "contact", contactHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact", contactHandler)
}

def contactHandler(evt) {
	log.debug "$evt.value: $evt"
	if (evt.value == "open") {
		switch1.on(delay: 5000)
	} else if (evt.value == "closed") {
		switch1.off(delay: 5000)
	}
}
