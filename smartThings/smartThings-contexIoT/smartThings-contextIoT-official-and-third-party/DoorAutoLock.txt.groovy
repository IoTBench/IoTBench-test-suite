/**
 *  Door Lock 2.0
 *
 *  Author: Josh Foshee
 *  Date: 2013-08-23
 */
preferences {
	section("When a door unlocks...") {
		input "lock1", "capability.lock"
	}
	section("Lock it how many minutes later?") {
		input "minutesLater", "number", title: "When?"
	}
}
def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(lock1, "lock", doorUnlockedHandler, [filterEvents: false])
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	subscribe(lock1, "lock", doorUnlockedHandler, [filterEvents: false])
}

def lockDoor() {                                                // This process locks the door.
               lock1.lock()                                     // Don't need delay because the process is scheduled to run later
}

def doorUnlockedHandler(evt) {
		log.debug "Lock ${lock1} was: ${evt.value}"

                if (evt.value == "lock") { 
                        log.debug "Human Locked ${lock1}, No need to lock door"
                        unschedule( lockDoor ) 	
                }                
		if (evt.value == "unlocked") {                  // If the human (or computer) unlocks the door then...
        	        log.debug "Locking in ${minutesLater} minutes (${delay}ms)"
		        def delay = minutesLater * 60           // runIn uses seconds so we don't need to multiple by 1000
                        runIn( delay, lockDoor)                 // ...schedule the door lock procedure to run x minutes later.
                }
}