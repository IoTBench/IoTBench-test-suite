/**
 *  Unlock door
 *
 *  Author: Aaron
 */

preferences {
	section("When I touch the app, unlock door...") {
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
	locks?.unlock()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	locks?.unlock()
}
