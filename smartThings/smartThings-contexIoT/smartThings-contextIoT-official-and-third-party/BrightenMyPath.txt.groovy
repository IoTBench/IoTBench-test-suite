/**
 *  Brighten My Path
 *
 *  Author: SmartThings
 */
preferences {
	section("When there's movement...") {
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("Turn on a light...") {
		input "switch1", "capability.switch", multiple: true
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
	switch1.on()
}
