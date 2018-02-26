/**
 *  Darken Behind Me
 *
 *  Author: SmartThings
 */
preferences {
	section("When there's no movement...") {
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Turn off a light...") {
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(motion1, "motion.inactive", motionInactiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.inactive", motionInactiveHandler)
}

def motionInactiveHandler(evt) {
	switch1.off()
}