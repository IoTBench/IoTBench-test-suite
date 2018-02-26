/**
 *  Light up when I'm gone
 *
 *  Author: smartthings@malix.com
 *  Date: 2013-07-18
 */
preferences {

	section("When a presence sensor arrives/departs this location...") {
		input "presence", "capability.presenceSensor", title: "Which sensor?"
	}
    
	section("Turn this switch off on arrival and on on departure...") {
		input "switch1", "capability.switch"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(presence, "presence", presenceHandler)
}

def presenceHandler(evt) {
	if (evt.value == "present") {
		log.debug "${presence.label ?: presence.name} has arrived at ${location}, turning switch off"
        sendPush("${presence.label ?: presence.name} has arrived at ${location}, turning switch off")
		switch1.off()
	} else if (evt.value == "not present") {
		log.debug "${presence.label ?: presence.name} has left ${location}, turning switch on"
        sendPush("${presence.label ?: presence.name} has left ${location}, turning switch on")
		switch1.on()
	}
}