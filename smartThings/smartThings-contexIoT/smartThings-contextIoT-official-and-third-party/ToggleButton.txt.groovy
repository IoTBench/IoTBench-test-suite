preferences {
	section("When I push a button on this remote") {
		input "button", "capability.button", title: "Button"
	}
    section("Which button:") {
		input "whichButton", "number", defaultValue:1    
    }
	section("Toggle these switches") {
		input "switches", "capability.switch", multiple: true, required: false
	}
	section("Turn off these switches") {
		input "offSwitches", "capability.switch", multiple: true, required: false
	}
	section("Turn on these switches") {
		input "onSwitches", "capability.switch", multiple: true, required: false
	}
}
 
def subscribeToEvents()
{
	subscribe(button, "button.pushed", pushHandler)
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
 
def pushHandler(evt) {
	if (evt.data.contains(whichButton.toString())) {
 
		// log.debug evt.value
	    onSwitches?.on()
		offSwitches?.off()
        switches.collect{ d ->
            log.debug "${d.displayName} is ${d.currentSwitch}"
            if(d.currentSwitch == "on") d.off()
            else d.on()
        }
    }
}