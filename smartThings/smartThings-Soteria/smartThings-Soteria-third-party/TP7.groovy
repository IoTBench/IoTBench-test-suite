/**
* TP7: Third-party app
* 
* More information:
* https://github.com/tslagle13/SmartThings/blob/master/Director-Series-Apps/Vacation-Lighting-Director/Vacation%20Lighting%20Director.groovy
* https://community.smartthings.com/t/app-to-turn-lights-on-off-automatically-while-away-simulate-presence/7628
* https://community.smartthings.com/t/new-app-vacation-light-director/7230?page=3
* 
* Notes: 
* Purpose: Occupancy light simulator will simulate your house being lived in even when you are not. Pick a set of lights and at a interval you specify the app will turn off all lights and 
* then turn on a random set of lights. Nice things is you can restrict this to mode/time/day of week. It is always triggered by mode change but will not run outside of specific 
* restrictions you set. Nice things is it will not run if someone is home and the mode changes. 
*/

definition(
    name: "Turn OFF",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turn your lights off when the SmartApp is tapped or activated",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When I touch the app, turn off...") {
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

def turnItOn(){
	switches?.off()
}

def changedLocationMode(evt) {
	log.debug "changedLocationMode: $evt"
	switches?.on
	runIn(1, turnItOn)
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	switches?.on()
	runIn(1, turnItOn)
}