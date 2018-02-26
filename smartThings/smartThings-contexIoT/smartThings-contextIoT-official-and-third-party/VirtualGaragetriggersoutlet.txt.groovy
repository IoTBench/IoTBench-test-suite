/**
 *  Virtual Garage Triggers Outlet
 *
 *  Author: chrisb
 */
preferences {
	section("When a Virtual Garage Door is tapped..."){
		input "GarageSensor1", "capability.contactSensor", title: "Which?"
	}
	section("Trigger which outlet?"){
		input "switches", "capability.switch"
	}
}


def installed()
{
	subscribe(GarageSensor1, "buttonpress.true", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(GarageSensor1, "buttonpress.true", contactOpenHandler)
}

def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
	log.trace "Turning on switches: $switches"
	switches.on()
    log.trace "Turning off switches: $switches"
    switches.off(delay: 4000)
}

