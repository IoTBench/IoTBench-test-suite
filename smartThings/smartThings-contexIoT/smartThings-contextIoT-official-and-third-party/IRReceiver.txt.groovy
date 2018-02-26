/**
 *  IR Receiver
 *
 *  Author: danny@smartthings.com
 *  Date: 2013-03-31
 */
 
preferences {
	section("Pick an IR device...") {
		input "id", "device.IrBlaster", title: "Which?"
    }
	section("Button A turns on or off..."){
		input "switch1", "capability.switch", title: "This light", required: false
	}
	section("Button B turns on or off..."){
		input "switch2", "capability.switch", title: "This light", required: false
	}    
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(id, "button.B", handleB)
        subscribe(id, "button.A",handleA)
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
        subscribe(id, "button.B", handleB)
        subscribe(id, "button.A",handleA)
}

def handleA(evt) {
	log.debug "received button A"
    if (switch1.currentValue("switch") == "on") {
        switch1.off()
    }
    else {
        switch1.on()
    }
}

def handleB(evt) {
	log.debug "received button B"
    if (switch2.currentValue("switch") == "on") {
        switch2.off()
    }
    else {
        switch2.on()
    }
}