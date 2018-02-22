// Automatically generated. Make future change here.
definition(
    name: "Zwave Switch Indicator Light Manager",
    namespace: "sticks18",
    author: "Scott Gibson",
    description: "Changes the indicator light setting to always be off",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When these switches are toggled adjust the indicator...") { 
		input "mains", "capability.switch", 
			multiple: true, 
			title: "Switches to fix...", 
			required: false
	}
    
    section("When these switches are toggled adjust the indicator in reverse (useful for Linear brand)...") { 
		input "mains2", "capability.switch", 
			multiple: true, 
			title: "Switches to fix in reverse...", 
			required: false
	}

}

def installed()
{
        subscribe(mains, "switch.on", switchOnHandler)
	subscribe(mains, "switch.off", switchOffHandler)
        subscribe(mains2, "switch.on", switchOnHandler2)
	subscribe(mains2, "switch.off", switchOffHandler2)
}

def updated()
{
	unsubscribe()
	subscribe(mains, "switch.on", switchOnHandler)
	subscribe(mains, "switch.off", switchOffHandler)
        subscribe(mains2, "switch.on", switchOnHandler2)
	subscribe(mains2, "switch.off", switchOffHandler2)
	log.info "subscribed to all of switches events"
}

def switchOffHandler(evt) {
	log.info "switchoffHandler Event: ${evt.value}"
	mains?.indicatorWhenOn()
}

def switchOnHandler(evt) {
	log.info "switchOnHandler Event: ${evt.value}"
	mains?.indicatorWhenOff()
}

def switchOffHandler2(evt) {
	log.info "switchoffHandler2 Event: ${evt.value}"
	mains2?.indicatorWhenOff()
}

def switchOnHandler2(evt) {
	log.info "switchOnHandler2 Event: ${evt.value}"
	mains2?.indicatorWhenOn()
}
