/**
 *  Lock door when sensor closes
 *
 *  Author: nema.darban@gmail.com
 *  Date: 2013-07-15
 */
 
preferences {
	section("When this door closes..."){
		input "contact", "capability.contactSensor", title: "Door Contact", required: true
	}
	section("Lock these locks..."){
		input "locks", "capability.lock", title: "Locks...", required: true, multiple: true
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
	//Probably don't need to lock the door when its opening and closing
    //subscribe(contact, "contact.open", lockAllLocks)
    subscribe(contact, "contact.closed", lockAllLocks)
        
}

def lockAllLocks(evt) {
	log.debug "lockAllLocks: $evt"
    log.trace "Locking Locks: $locks"
	locks.lock()
}
