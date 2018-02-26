preferences {
	section("When I push a button on this remote") {
		input "button", "capability.button", title: "Device"
		input "whichButton", "number", defaultValue:1, title: "Which button"    
    }
	section ("When I push the button") {
		input "pushMode", "mode", title: "Change mode to", required: false
		input "switchOn", "capability.switch", title: "Turn on switches", required: false, multiple: true
		input "switchOff", "capability.switch", title: "Turn off switches", required: false, multiple: true
	}
	section ("When I hold the button") {
		input "heldMode", "mode", title: "Change mode to?", required: false
		input "heldSwitchOn", "capability.switch", title: "Turn on switches", required: false, multiple: true
		input "heldSwitchOff", "capability.switch", title: "Turn off switches", required: false, multiple: true
	}
	section ("Only during a certain time") {
		input "starting", "time", title: "Starting", required: false
		input "ending", "time", title: "Ending", required: false
	}
}
 
def subscribeToEvents()
{
	subscribe(button, "button.pushed", pushHandler)
    subscribe(button, "button.held", heldHandler)
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
 
def pushHandler(event) {
	log.info "pushHandler"
	if (event.data.contains(whichButton.toString())) {
		if (timeOk) {
			log.info "Executing push handler"
			changeMode(pushMode)
			if (switchOn) {
				switchOn.on()
			}
			if (switchOff) {
				switchOff.off()
			}
		} else {
			log.info "Out of time range"
		}
	}
}
 
def heldHandler(event) {
	log.info "heldHandler"
	if (event.data.contains(whichButton.toString())) {
		if (timeOk) {
			log.info "Executing held handler"
			changeMode(heldMode) 
			if (heldSwitchOn) {
				heldSwitchOn.on()
			}
			if (heldSwitchOff) {
				heldSwitchOff.off()
			}
		} else {
			log.info "Out of time range"
		}
	}
}
 
def changeMode(newMode) {
	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
			log.info "changed the mode to '${newMode}'"
		}
		else {
			log.info "tried to change to undefined mode '${newMode}'"
		}
	}
}
 
private getTimeOk() {
	def result = true
    log.info "getTimeOK"
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}