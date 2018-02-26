/**
 *  Turn It On When I'm Here
 *
 *  Author: SmartThings
 */
preferences {
	section("When I arrive and leave..."){
		input "presence1", "capability.presenceSensor", title: "Who?"
	}
	section("Turn on/off a light..."){
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(presence1, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
}

def presenceHandler(evt)
{
	def current = evt.value
	if (current == "present") {
		settings.switch1.on()
	}
	else if (current == "not present") {
		settings.switch1.off()
	}
}
