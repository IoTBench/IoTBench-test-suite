/**
 *  Find my Keys
 *
 *  Author: dome
 *
 */



definition(
    name: "Find my keys",
    namespace: "",
    author: "dome",
    description: "Activate the beep function on selected Presence tags.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Family/App-IMadeIt.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Family/App-IMadeIt@2x.png",
    oauth: true
)

preferences {
	section("Use these presence tags...") {
		input "presence", "capability.tone", multiple: true

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
    //presence?.beep() //no need for beep on event update
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
    presence?.beep()
}
