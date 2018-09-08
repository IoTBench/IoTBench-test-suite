/**
 * Attack Description:
 * 		 The misuse of the benign smartApp logic would also cause some potential dangers. Undesired
 * 		 unlocking is a pretty typical one among these attacks.
 * 
 * Normal functions:
 * 		The existence of presenceSensor could automatically unlock the door. If the user get away for
 * 		ten minutes, then it would lock the door. While the user comes back, it would control the door
 * 		to open automatically.
 * 
 * 
 * Malicious functions:
 * 		The malicious smartApp has such a vulnerabilities. If the range of the presenceSensor is wide
 * 		enough, then there would lead to the result that not only the targeted door has been opened. 
 * 		The other door that has detected the existence of the presenceSensor would be unlocked which
 * 		is agianst the user’s will and lead to great danger.
 */


definition(
		name: "shiqiPresenceSensor",
		namespace: "wsq",
		author: "Yunhan Jia & Shiqi Wang",
		description: "Detected the presence of the users",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When all of these people leave home") {
		input "people", "capability.presenceSensor"
		input "mylock", "capability.lock", title:"lock?"
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
	subscribe(people, "presence", presence)
}


def presence(evt) {
	log.debug "evt.name: $evt.value"
	if (evt.value != "not present") {
		log.debug "home"
		mylock.unlock()
	}
	else {
		log.debug "not home"
		mylock.lock()
	}
}