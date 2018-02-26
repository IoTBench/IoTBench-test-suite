/**
 *  Presence Change Push
 *
 *  Author: SmartThings
 */
preferences {
	section("When a presence sensor arrives or departs this location..") {
		input "presence", "capability.presenceSensor", title: "Which sensor?"
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
    	sendPush("${presence.label ?: presence.name} has arrived at the ${location}")
	} else if (evt.value == "not present") {
		log.debug "${presence.label ?: presence.name} has left the ${location}"
    	sendPush("${presence.label ?: presence.name} has left the ${location}")
	}
}