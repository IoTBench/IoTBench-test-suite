/**
 *  Contact countdown
 *
 *  Author: Stephen Beitzel <sbeitzel@pobox.com>
 *  Date: 2014-02-11
 */
        // myContact - reference to the contact sensor that initiates the run
        // mySwitch - reference to the switch we're controlling
        // firstState - reference to the initial state of the switch
        // finalState - reference to the final state of the switch
        // delaySeconds - how long to wait
        
preferences {
	section("Initiate countdown from contact sensor") {
    	input "myContact", "capability.contactSensor"
    }
    
    section("Initiate countdown on switch open") {
    	input "mySwitch", "capability.switch"
	}
    
    section("Countdown this many seconds") {
		input name: "delaySeconds", title: "Seconds?", type: "number", multiple: false
	}
    
    section("Initial switch state") {
    	input name: "firstState", title: "First set the switch to:", type: "bool", multiple: false
    }
    
    section("Final switch state") {
    	input name: "finalState", title: "Upon reaching countdown, set switch to:", type: "bool", multiple: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(myContact.contact)
    subscribe(mySwitch.switch)
}

def contact(evt) {
	log.debug "Contact event: ${evt.value}"
    if (evt.value == "open") {
    	log.debug "Starting a new countdown"
        // set the switch
        if (firstState.value == true) {
        	mySwitch.on()
        } else {
        	log.debug("firstState.value is not equal to true, it is ${firstState.value}")
            mySwitch.off()
        }
        // schedule the countdown
        runIn(delaySeconds, countedDown)
    }
}

def countedDown() {
	log.debug "Counted down"
    if (finalState.value == true) {
    	mySwitch.on()
    } else {
        mySwitch.off()
    }
}