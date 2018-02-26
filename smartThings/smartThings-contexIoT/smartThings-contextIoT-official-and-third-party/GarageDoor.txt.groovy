/**
 *  Notify Me When
 *
 *  Author: SmartThings
 *  Date: 2013-03-20
 */
preferences {
	section("Choose one or more, when..."){
		
		input "Switch1", "capability.switch", title: "Switch Turned On", required: true
	
	}


}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(Switch1, "switch.on", nowturnoff)
	
}

def nowturnoff(evt) {
	log.debug "nowturnoff: $evt"
	Switch1.off()
    }

