/**
 *  Button
 *
 *  Author: danielbarak@live.com
 *  Date: 2013-12-10
 */
preferences {
	section("Pick a Button...") {
    	input "button", "device.AeonKeyFob"
    }
	section("Push turns on..."){
		input "switchOn", "capability.switch", title: "Lights", required: true, multiple: true
	}
    
	section("Hold turns off..."){
		input "switchOff", "capability.switch", title: "Lights", required: true, multiple: true
	}
}
def installed() {
	log.debug "Installed with settings: ${settings}"
    subscribe(button, "button.pushed",allOn)
    subscribe(button, "button.held",allOff)

}

def updated() {
	log.debug "Updated with settings: ${settings}"
unsubscribe()
    subscribe(button, "button.pushed",allOn)
    subscribe(button, "button.held",allOff)

}

def allOn(evt) {
	log.debug "Turning All On"
    if(switchOn.currentSwitch == "on")
    {
    	switchOn.off()
    }
    else
    {
    switchOn.on()
    }
        
}
def allOff(evt) {
	log.debug "Turning All Off"
    
        switchOff.off()
}

// TODO: implement event handlers