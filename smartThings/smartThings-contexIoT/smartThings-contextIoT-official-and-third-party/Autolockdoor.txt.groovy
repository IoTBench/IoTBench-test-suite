/**
 *  Auto-Lock Doors v2
 *
 *  Author: Chris Sader (@csader)
 *  In collaboration with @chrisb
 */


// Automatically generated. Make future change here.
definition(
    name: "Auto lock door",
    namespace: "talhart",
    author: "http://github.com/smartthings-users/smartapp.auto-lock-door",
    description: "Locks door automatically after x minutes",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

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

                if (evt.value == "lock") {                      // If the human locks the door then...
                        unschedule(lockDoor)                	// ...we don't need to lock it later. 	
                }                
		if (evt.value == "unlocked") {                  		// If the human (or computer) unlocks the door then...
        	        log.debug "Locking in ${minutesLater} minutes (${delay}ms)"
		        def delay = minutesLater * 60          		   // runIn uses seconds so we don't need to multiple by 1000
                        runIn(delay, lockDoor)                 // ...schedule the door lock procedure to run x minutes later.
                }
}