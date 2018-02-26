/**
 *  My office is off-limits when I am gone
 *
 *  Author: Mark Jaquith
 *  Date: 2014-03-01
 */

preferences {
	section("Monitor this door") {
		input "door", "capability.contactSensor"
	}
	section("When this person is away") {
		input "person", "capability.presenceSensor"
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(door, "contact.open", doorOpen)
	subscribe(door, "contact.closed", doorClosed)
}

def doorOpen(evt) {
	log.trace "doorOpen($evt.name: $evt.value)"
		def presenceValue = person.find{it.currentPresence == "present"}
	if ( ! presenceValue ) {
		sendOpenedMessage()
	}
}

def doorClosed(evt) {
	log.trace "doorClosed($evt.name: $evt.value)"
		def presenceValue = person.find{it.currentPresence == "present"}
	if ( ! presenceValue ) {
		sendClosedMessage()
	}

}

void sendOpenedMessage() {
	def msg = "${door.displayName} was opened while ${person.displayName} was away"
	log.info msg
	sendPush msg
}

void sendClosedMessage() {
	def msg = "${door.displayName} was closed while ${person.displayName} was away"
	log.info msg
	sendPush msg
}