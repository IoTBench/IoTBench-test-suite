/**
 *  Text Me When There's Motion
 *
 *  Author: SmartThings
 */
preferences {
	section("When there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Text me at..."){
		input "phone1", "phone", title: "Phone number?"
	}
}

def installed()
{
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	log.trace "$evt.value: $evt, $settings"
	log.debug "$motion1 detected motion, texting $phone1"

	sendSms(phone1, "${motion1.label ?: motion1.name} detected motion")
}