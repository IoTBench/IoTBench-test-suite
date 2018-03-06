/**
 *  Copyright 2015 SmartThings
 *
 */

definition(
    name: "Call by reflection 2",
    namespace: "CSL",
    author: "Amit Sikder",
    updated: "Leo Babun and Z. Berkay Celik",
    description: "A string is used to invoke a method via call by reflection. A method is defined to leak state of a door. In line 47, a string with the state of the door lock is sent via a hard-coded phone number. In line 68, sendSms function is defined.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When I leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Lock the lock...") {
		input "lock1","capability.lock", multiple: true
		input "unlock", "enum", title: "Unlock when presence is detected?", options: ["Yes","No"]
        input("recipients", "contact", title: "Send notifications to") {
            input "spam", "enum", title: "Send Me Notifications?", options: ["Yes", "No"]
        }
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
	if (evt.value == "present") {
		if (unlock == "Yes") {
			def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
			if (anyLocked) {
				"$device"()
				sendMessage("The ${state.Dvice} is locked")
			}
			lock1.unlock()
		}
	}
	else {
		def nobodyHome = presence1.find{it.currentPresence == "present"} == null
		if (nobodyHome) {
			def anyUnlocked = lock1.count{it.currentLock == "locked"} != lock1.size()
			if (anyUnlocked) {
				sendMessage("Doors locked after everyone departed")
			}
			lock1.lock()
		}
	}
}

def device () {
	state.Device = "${lock1.displayName}"
}

def sendMessage(msg) {
	def phone = "(111)-111-1111"

    if (location.contactBookEnabled) {
        sendSms(phone, msg)
    }
    else {
        if (spam == "Yes") {
            sendPush msg
        }
    }
}
