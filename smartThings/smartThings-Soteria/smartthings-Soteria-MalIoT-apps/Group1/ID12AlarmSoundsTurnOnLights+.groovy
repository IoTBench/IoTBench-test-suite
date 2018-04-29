/*
* Safe alarm
* Turn on your lights and unlock the door when there is a smoke
* Author: Z. Berkay Celik
* The app is created by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
* Notes: Though the functionality of the app is benign, the apps might be chained to validate a property.
* Simulator does not support smokeDetector, error "grails.validation.ValidationException: Validation Error(s) occurred during save():"
*/

definition(
		name: "safe alarm",
		namespace: "Soteria",
		author: "Soteria",
		description: "This SmartApp turns on your lights and unlocks the door when there is a smoke for your safety",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Make your home safe") {
		input "smoke", "capability.smokeDetector", title: "smoke"
		input "alarm", "capability.alarm", title: "alarm"
		input "mySwitch", "capability.switch", title: "Turn on all light switches for your safety", required: true, multiple: true
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
}

def smokeHandler(evt) {
	if(evt.value == "detected") {
		alarm.on()
		mySwitch?.on()
	}else if(evt.value == "clear") {
		alarm.off()
	}
}
