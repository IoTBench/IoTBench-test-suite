/**
 *  Nicolas
 *
 *  Author: SmartThings
 */
preferences {
	section(" Nick When I touch the app, turn off...") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(location, changedLocationMode)
	subscribe(app, appTouch)
}

def updated()
{
	unsubscribe()
	subscribe(location, changedLocationMode)
	subscribe(app, appTouch)
}

def changedLocationMode(evt) {
	log.debug "changedLocationMode: $evt"
	switches?.off()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	switches?.off()
}
