/**
 *  AEON Power Strip Binding
 *  This app allows you to bind 4 Virtual On/Off Tiles to the 4 switchable outlets.
 *
 *  Author: chrisb
 *  Date: 12/19/2013
 */
preferences {
	section("Which AEON power strip is used?"){
		input "strip", "capability.Switch"
	}
	section("Select a Virtual Switch to bind to Outlet 1"){
		input "switch1", "capability.Switch"
	}
    section("Select a Virtual Switch to bind to Outlet 2"){
		input "switch2", "capability.Switch"
	}
    section("Select a Virtual Switch to bind to Outlet 3"){
		input "switch3", "capability.Switch"
	}
    section("Select a Virtual Switch to bind to Outlet 4"){
		input "switch4", "capability.Switch"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(switch1, "switch.on", switchOnOneHandler)
    subscribe(switch2, "switch.on", switchOnTwoHandler)
    subscribe(switch3, "switch.on", switchOnThreeHandler)
    subscribe(switch4, "switch.on", switchOnFourHandler)
    subscribe(switch1, "switch.off", switchOffOneHandler)
    subscribe(switch2, "switch.off", switchOffTwoHandler)
    subscribe(switch3, "switch.off", switchOffThreeHandler)
    subscribe(switch4, "switch.off", switchOffFourHandler)
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(switch1, "switch.on", switchOnOneHandler)
    subscribe(switch2, "switch.on", switchOnTwoHandler)
    subscribe(switch3, "switch.on", switchOnThreeHandler)
    subscribe(switch4, "switch.on", switchOnFourHandler)
    subscribe(switch1, "switch.off", switchOffOneHandler)
    subscribe(switch2, "switch.off", switchOffTwoHandler)
    subscribe(switch3, "switch.off", switchOffThreeHandler)
    subscribe(switch4, "switch.off", switchOffFourHandler)
}

def switchOnOneHandler(evt) {
	log.debug "switch on1"
	strip.on1()
}

def switchOnTwoHandler(evt) {
	log.debug "switch on2"
	strip.on2()
}

def switchOnThreeHandler(evt) {
	log.debug "switch on3"
	strip.on3()
}

def switchOnFourHandler(evt) {
	log.debug "switch on4"
	strip.on4()
}

def switchOffOneHandler(evt) {
	log.debug "switch off1"
	strip.off1()
}

def switchOffTwoHandler(evt) {
	log.debug "switch off2"
	strip.off2()
}

def switchOffThreeHandler(evt) {
	log.debug "switch off3"
	strip.off3()
}

def switchOffFourHandler(evt) {
	log.debug "switch off4"
	strip.off4()
}