/**
 *  Door's Unlocked
 *
 *  Author: Jordan Thurston
 *  Date: 2014-05-21
 */

// Automatically generated. Make future change here.
definition(
    name: "Door's Unlocked",
    namespace: "",
    author: "jthurston422@gmail.com",
    description: "Remind me if I leave the door unlocked.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When I leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Check the lock...") {
		input "lock1","capability.lock", multiple: true
		input "spam", "enum", title: "Send Me Notifications?", metadata: [values: ["Yes","No"]]
	}
}

def installed()
{
	subscribe(presence1, "presence", presence)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presence)
}

def presence(evt)
{
	def nobodyHome = presence1.find{it.currentPresence == "present"} == null
	if (nobodyHome) {
		def anyUnlocked = lock1.count{it.currentLock == "locked"} != lock1.size()
		if (anyUnlocked) {
        	log.trace "$evt.value: $evt, $settings"
			log.debug "Everyone left and the ${lock1} is unlocked, sending push message to user"
			sendMessage("You left the ${lock1} unlocked!")
		}
		lock1.lock()
	}
}

def sendMessage(msg) {
	if (spam == "Yes") {
		sendPush msg
	}
}