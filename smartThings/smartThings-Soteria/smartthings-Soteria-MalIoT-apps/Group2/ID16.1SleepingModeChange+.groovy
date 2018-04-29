/**
 *  The app violates general properties when installed with other apps
 *  Included in repo by Z. Berkay Celik
 *  Email: zbc102@cse.psu.edu
 */
definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "Soteria",
    description: "Set modes based on your light switch",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
	section("Mode Follows This Switch") {
		input "theSwitch", "capability.switch", title: "Choose Switch"
    }
    section("Modes"){
		input "onMode", "enum", title: "Mode when On", options:["onMode", "offMode"], required: true
		input "offMode", "enum", title: "Mode when Off", options:["onMode", "offMode"], required: true
	}
}

def subscribeToEvents()
{
	subscribe(theSwitch, "switch.on", onHandler)
    subscribe(theSwitch, "switch.off", offHandler)
}

def installed()
{
	subscribeToEvents()
}

def updated()
{
	unsubscribe()
	subscribeToEvents()
}

def onHandler(evt){
	setLocationMode(onMode)
}

def offHandler(evt){
	setLocationMode(offMode)
}