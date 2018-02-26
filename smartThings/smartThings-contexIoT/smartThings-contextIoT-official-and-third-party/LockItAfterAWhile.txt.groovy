/**
 *  Lock It After A While
 *
 *  Author: 	Chris LeBlanc (LeBlaaanc)
 *  Company: 	Clever Lever
 *  Email: 		chris@cleverlever.co
 *  Date: 		05/22/2014
 */

definition(
    name: "Lock It After A While",
    namespace: "cl",
    author: "Chris LeBlanc",
    description: "Locks a lock after a given period of time of being unlocked.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("Choose lock(s)") {
		input "lock1","capability.lock", multiple: true
	}
	section("After this many minutes") {
		input "after", "number", title: "Minutes", description: "10 minutes",  required: false
	}
    section("While I'm present") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true, required: false
	}
    section("Notification method") {
    		input "push", "bool", title: "Push notification", metadata: [values: ["Yes","No"]]
    }
}

def installed()
{
	subscribe(lock1, "lock", eventHandler)
}

def updated()
{
	unsubscribe()
	subscribe(lock1, "lock", eventHandler)
}

def eventHandler(evt)
{
	def delay = (after != null && after != "") ? after * 60 : 600
	runIn(delay, lockTheLocks)
    log.debug("runIn(${delay}, lockTheLocks)")
}

def lockTheLocks ()
{
	def sombodyHome = presence1.find{it.currentPresence == "present"} != null
    def anyUnlocked = lock1.count{it.currentLock == "locked"} != lock1.size()
    
	if (sombodyHome && anyUnlocked) {
		sendMessage("Doors locked after ${after} minutes.")
		lock1.lock()
        log.debug("Attemped to send message and locked locks.")
	}
}

def sendMessage(msg) 
{
	log.debug("sendMessage(${msg})")
	if (push) {
		sendPush msg
	}
}