/**
 *  Garage Door Opener
 *
 *  Author: SmartThings
 *
 *  Description: This allows the garage door to be opened or closed via the app. 
 *  			 It also turns on/off the garage light if pressed via app or on a timer if opened manually.
 */

// Automatically generated. Make future change here.
definition(
    name: "Garage Door Automation",
    namespace: "",
    author: "Curtis Lesperance",
    description: "This allows the garage door to be opened or closed via the app. It also turns on/off the garage light if pressed via app or on a timer if opened manually.",
    category: "My Apps",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.doors.garage.garage-closed?displaySize=2x",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.doors.garage.garage-closed?displaySize=2x",
    oauth: true
)

preferences {
	section("Garage") {
		input "garageDoorOpener", "capability.switch", title: "Garage Door Opener", description: "Which garage door opener?"
		input "garageLightSwitch", "capability.switch", title: "Garage Light Switches", description: "Which garage light switch(s)?", multiple: true
		input "lightSwitch", "capability.switch", title: "Other Light Switches", description: "Which other light switch(s) do you want on?", multiple: true, required: false
		input "garageDoorContact", "capability.threeAxis", title: "Garage Door Contact", description: "Which garage door contact?"
        input "lightOffDelay", "number", title: "Garage Light Off in X Min(s)", description: "Number of minutes to keep the light on after door closes if opened automatically (10 Min Default)", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe()
}

def updated() {
	unsubscribe()
	log.debug "Updated with settings: ${settings}"
	subscribe()
}
def subscribe(){

	subscribe(app, appTouchHandler)
	subscribeToCommand(garageDoorOpener, "on", garageSwitchOnCommand)
    subscribe(garageDoorContact, "acceleration", onDoorContactCommand)
}

def appTouch(evt) {
	log.debug "appTouch: $evt.value, $evt"
	log.debug "otherLightsOperation()"
    otherLightsOperation()
	log.debug "garageDoorOpener = On"
	garageDoorOpener.on()
	log.debug "garageDoorOpener = Off (1500 mil)"
	garageDoorOpener.off(delay: 1500)
}

def garageSwitchOnCommand(evt) {
	log.debug "garageSwitchOnCommand: $evt.value, $evt"
	garageDoorOpener.off(delay: 1500)
}

def onDoorContactCommand(evt)
{
	log.debug "onDoorContactCommand: calling garageLightOperation"
	garageLightOperation()
}

private garageLightOperation(){
	def latestThreeAxisState = multisensor.threeAxisState // e.g.: 0,0,-1000
    log.debug "latestThreeAxisState: ${latestThreeAxisState}"
	//if (latestThreeAxisState) {
	log.debug "garageLightOperation: ${garageDoorContact.currentContact}"
    def isOpen = Math.abs(latestThreeAxisState.xyzValue.z) > 250
    log.debug "isOpen: ${isOpen}"
	final lightDelay = lightOffDelay != null ? lightOffDelay * 60000 : 600000
    if(!isOpen) {
    	log.debug "garageLightOperation: light switch off in ${lightDelay / 60000} min(s)"
    	garageLightSwitch?.off(delay: lightDelay)
    }
    else{
    	log.debug "garageLightOperation: light switch on"
    	garageLightSwitch?.on()
    }

}

private otherLightsOperation(){	
    	log.debug "otherLightsOperation: light switch on"
    	lightSwitch?.on()
}