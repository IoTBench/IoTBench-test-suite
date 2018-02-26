/**
 *  Lock It When It Closes
 *
 *  Author: Jeremy R. Whittaker
 */
preferences {
	section("When the door closes..."){
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Lock the lock...") {
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
	if (evt.value == "closed") {
		lock1.lock()
        log.debug "door locked"
	}
}

