/**
 *  Turn Off when Open
 *	Returns to the previous outlet state when the sensor closes again
 *
 *  Author: Caleb Caraway
 */
definition(
    name: "Turn Off when Open - 1",
    namespace: "Ellipse",
    author: "Lisa Benson",
    description: "Turns off an outlet when a sensor is left open.  Statefull",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("When the door opens..."){
		input "contact", "capability.contactSensor", title: "Where?"
	}
	section("Turn off an outlet..."){
		input "switches", "capability.switch", multiple: true
	}
    section("How long to delay"){
		input "delayMinutes", "number", title: "Minutes?", required: false
	}
}


def installed()
{
	subscribe(contact, "contact.open", contactOpenHandler)
    subscribe(contact, "contact.closed", contactClosedHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact, "contact.open", contactOpenHandler)
	subscribe(contact, "contact.closed", contactClosedHandler) 
}

def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
	if (evt.value == "open") {
		log.info "Door has been opened"
		state.lastStatus = "open"
		state.contactOpenTime = now()
        state.switchPreviousState = []
        switches.eachWithIndex {obj, i ->
            state.switchPreviousState.putAt(i,obj.currentState("switch").value)
        }
        if(delayMinutes) {
			runIn(delayMinutes*60, turnOffAfterDelay, [overwrite: false])
		} else {
			turnOffAfterDelay()
		}
	}
}

def contactClosedHandler(evt)  {
	log.debug "$evt.value: $evt, $settings"
    if (evt.value == "closed") {
    	log.info "Door has been closed"
        state.lastStatus = "closed"
        state.contactOpenTime = null
        turnOnIfOff()
    }
}

def turnOffAfterDelay() {
	log.debug "Running turnOffAfterDelay"
	if (contact.currentState("contact").value == "open" && state.lastStatus != "closed") {
		def elapsed = now() - state.contactOpenTime
		if (elapsed >= (delayMinutes ?: 0) * 60000L) {
        	log.trace "Turning off outlets: $switches"
			switches.off()
		}
	}
}

def turnOnIfOff() {
	log.debug "Running turnOnIfOff"
    log.debug "state.switchPreviousState is $state.switchPreviousState"
    switches.eachWithIndex {it, i ->
    	if (state.switchPreviousState.getAt(i) == "on")  {
        	log.debug "turning ${it.label ?: it.name} on."
            it.on()
        }
    }
}