/**
 *  Lights Off, When Closed
 *
 *  Author: SmartThings
 */
preferences {
	section ("When the door closes...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section ("Turn off a light...") {
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.closed", contactClosedHandler)
}

def contactClosedHandler(evt) {
	switch1.off()
}