/**
 *  Mode change based on switch
 *
 *  Author: Chris Sturgis
 *  Date: 2014-6-21
 *  Soteria analyzed.
 */

// Automatically generated. Make future change here.
definition(
    name: "Mode Change based on switch",
    namespace: "",
    author: "Chris Sturgis",
    description: "Changes mode based on the state of a dummy on/off switch",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png"
)

preferences {
	section("Select Switch to monitor"){
		input "theSwitch", "capability.switch"
	}
    section("Select the mode to change from"){
    	input "mode1", "mode", title: "From Mode...", required: false
    }
    section("Select the mode to change to"){
    	input "mode2", "mode", title: "To Mode..."
    }
}

def initialize() {
    subscribe(theSwitch, "switch.On", onHandler)
    subscribe(location, "location.mode", currentMode)
    subscribe(theSwitch, "switch.Off", offHandler)
    log.debug "mode currently $location.mode"
    log.debug "mode1 = $mode1 mode2= $mode2"
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    initialize()
}

def onHandler(evt) {
	log.debug "Received on from $theSwitch"
    currentMode()
    theSwitch.off()
}
def currentMode(evt) {
    if(mode1 != null){
    	if(location.mode == mode1) {
    	modeChange()
        }
        else {
        log.debug "Current mode is $location.mode not $mode1"
        }
    }
    else{
    	if(mode1 == null) {
        	modeChange()}
	}
}

def modeChange() {
	setLocationMode(mode2)
    log.debug "Mode changed to $mode2"
}

def offHandler(evt) {
	log.debug "Received off from $theSwitch, current mode $location.mode"
}

