/**
 *  LED Sign Controller
 *
 *  Author: jeremy@jwitconsulting.com
 *  Date: 2014-01-19
 */
preferences {
	section("When the door opens or closes...") {
		input "multisensor", "device.smartSenseMulti", title: "Where?"
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
	subscribe(multisensor, "contact", handleContact)
}

def handleContact(evt) {
	httpGet("http://www.smartthings.com", successClosure)
}

def successClosure = { response ->
	log.debug "Request was successful, $response"
}