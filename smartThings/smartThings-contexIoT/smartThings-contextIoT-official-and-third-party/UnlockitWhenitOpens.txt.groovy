/**
 *  Unlock It When It Opens
 *
 *  Author: Jeremy R. Whittaker
 */
preferences {
	section("When the door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Unlock the lock...") {
		input "lock1","capability.lock", multiple: true
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
		lock1.unlock()
        log.debug "Lock ${lock1} was: ${evt.value}"
	}
}