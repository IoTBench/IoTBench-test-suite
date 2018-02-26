/**
 *  Presence Change Text
 *
 *  Author: SmartThings
 */
preferences {
	section("When a presence sensor arrives or departs this location..") {
		input "presence", "capability.presenceSensor", title: "Which sensor?"
	}
	section("Send a text message to...") {
		input "phone1", "phone", title: "Phone number?"
	}
}


def installed() {
	subscribe(presence, "presence", presenceHandler)
}

def updated() {
	unsubscribe()
	subscribe(presence, "presence", presenceHandler)
}

def presenceHandler(evt) {
	if (evt.value == "present") {
		log.debug "${presence.label ?: presence.name} has arrived at the ${location}"
    	sendSms(phone1, "${presence.label ?: presence.name} has arrived at the ${location}")
	} else if (evt.value == "not present") {
		log.debug "${presence.label ?: presence.name} has left the ${location}"
		sendSms(phone1, "${presence.label ?: presence.name} has left the ${location}")
	}
}