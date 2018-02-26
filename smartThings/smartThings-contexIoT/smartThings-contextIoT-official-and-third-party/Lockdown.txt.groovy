/**
 *  Lockdown
 *
 *  Author: P2Digital
 */
preferences {
	section("When I touch the app, lock all the doors...") {
		input "locks", "capability.lock", multiple: true
	}
}

def installed()
{
	subscribe(location)
	subscribe(app)
}

def updated()
{
	unsubscribe()
	subscribe(location)
	subscribe(app)
}

def changedLocationMode(evt) {
	log.debug "changedLocationMode: $evt"
	locks.lock()//switches?.off()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	locks.lock()
}



