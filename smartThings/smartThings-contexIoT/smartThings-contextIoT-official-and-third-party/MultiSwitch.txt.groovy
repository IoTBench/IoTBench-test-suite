/**
 *  Multi Switch
 *
 *  Author: SmartThings
 */

preferences {
	section("When this switch is toggled...") {
		input "master", "capability.switch", title: "Where?"
	}
	section("Turn on/off these switches...") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
  	subscribe(master, "switch.On", switchOn)
    subscribe(master, "switch.Off", switchOff)
}

def updated()
{
	unsubscribe()
	subscribe(master, "switch.On", switchOn)
    subscribe(master, "switch.Off", switchOff)
}

def switchOn(evt) {
	log.debug "Switches On"
        switches?.on()
}
def switchOff(evt) {
	log.debug "Switches Off"
        switches?.off()
}

