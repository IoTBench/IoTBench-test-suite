/**
 *  Turn It On For 3 Seconds
 *  Turn on a switch and then turn it back off 3 seconds later
 *
 *  Author: WalterP
 */

preferences {
	section("When I touch the app, turn on a switch for 3 seconds...") {
		input "switch1", "capability.switch"
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
	switch1.on()
	switch1.off(delay: 3000)
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	switch1.on()
	switch1.off(delay: 3000)
}