/**
 *  Let There Be Light!
 *  Turn your lights on when an open/close sensor opens and off when the sensor closes.
 *
 *  Author: SmartThings
 */
definition(
    name: "Let There Be Light!",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights on when a SmartSense Multi is opened and turn them off when it is closed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

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
}

def updated() {
	unsubscribe()
	subscribe(contact1, "contact", contactHandler)
}

def contactHandler(evt) {
	log.debug "$evt.value"
	if (evt.value == "open") {
		switch1.on()
	} else if (evt.value == "closed") {
		switch1.off()
	}
}