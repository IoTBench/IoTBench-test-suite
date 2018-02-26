/**
 *  Notify Me When It Opens
 *
 *  Author: SmartThings
 */
preferences {
	section("When the door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?"
	}
}

def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	log.debug "$contact1 was opened, sending push message to user"
	sendPush("Your ${contact1.label ?: contact1.name} was opened")
}