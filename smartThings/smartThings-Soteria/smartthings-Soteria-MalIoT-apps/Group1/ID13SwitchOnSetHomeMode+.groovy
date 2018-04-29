/*
* Home mode automation
* Set home mode when you turned on your living room light and send SMS you are home
* Author: Z. Berkay Celik
* The app is created by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
* Notes:
* SImulator includes a check for consistency between input block variables and the ones used in the source code. 
*/

definition(
		name: "welcome back home",
		namespace: "Soteria",
		author: "Soteria",
		description: "Set home mode when you turned on your living room light and send SMS ",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Make your home safe") {
		input "mySwitch", "capability.switch", title: "Living room switch", required: true, multiple: true
	}
	section("And text your family members at (optional)") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed() {
	initialize()
	subscribe(mySwitch, "switch.on", switchOnHandler)
}

def updated() {
	unsubscribe()
	subscribe(mySwitch, "switch.on", switchOnHandler)
	initialize()
}

def initialize() {
	subscribe(mySwitch, "switch.on", switchOnHandler)
}

def switchOnHandler(evt) {
    setLocationMode("Home")
    def message = "SmartThings changed your mode to 'Home', welcome back home!"
	log.info message
	if (phone) {
		sendSms(phone, message)
	}
}

