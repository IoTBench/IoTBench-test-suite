/**
 *  Lock door
 *
 *  Author: Aaron
 */

preferences {
	section("When I touch the app, lock door...") {
		input "locks", "capability.lock", multiple: true
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
	locks?.lock()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	locks?.lock()
}
