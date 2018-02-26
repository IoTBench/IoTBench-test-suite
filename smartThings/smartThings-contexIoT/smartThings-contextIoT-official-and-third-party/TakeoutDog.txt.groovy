/**
 *	Take Out Dog
 * 
 *  Author: Chuck Pearce
 *  Date: 2015-03-10
 * 
 * Items under "trigger when enabled" will occur when the "Take out dog" switch is triggered.
 * Items under "return trigger" determine what causes the switch to automatically turn off. If you select both a delay and contact then the delay time will be used as a failsafe.
 * For example, if you set the delay to 10, then after 10 minutes even if the contact is not tripped 2x the system will execute the return procedure.
 * Items under "trigger when returned" will occur when the trigger is executed.
 */

definition(
	name: "Take out Dog",
	namespace: "chuck-pearce",
	author: "Chuck Pearce",
	description: "Used to execute a mode or hello home, dekay or wait for a door sensor 2x then change mode or hello home",
	category: "Convenience",
	iconUrl:   "http://i.imgur.com/kztKxez.png",
	iconX2Url: "http://i.imgur.com/kztKxez.png",
	iconX3Url: "http://i.imgur.com/kztKxez.png",
	"count": 0
)

preferences {
    page(name: "TakeOutDog")
}

def TakeOutDog() {
  dynamicPage(name:"TakeOutDog", title:"Take out Dog", install: true, uninstall: true) {

  	def phrases = location.helloHome?.getPhrases()*.label
  	if (phrases) {
    	phrases.sort()
    }
	section("Auto Trigger When"){
		input("lock", "capability.lock", title: "Lock is Unlocked", required: false)
		input("timeStart", "time", title: "Time Start", required: false)
		input("timeEnd", "time", title: "Time End", required: false)
		input("modes", "mode", multiple: true, title: "Enabled Modes", required: false)
	}
	section("Actions When Enabled") {
		if (phrases) {
        	input name: "homePhrasesEnabled", type: "enum", title: "Execute Hello Home", multiple: true,required: false, options: phrases, refreshAfterSelection: true
		}
		input name: "modeEnabled", type: "mode", title: "Change to Mode", required: false
		input name: "lightsEnabled", type: "capability.switch", multiple: true, title: "Lights On", required: false
     
    }
	section("Return Trigger"){
		input(name: "delay", title: "Delay Before Returned", type: "int", description: "in minutes", defaultValue: "10", required: false )
		input(name: "contact", title: "Contact Detection", type: "capability.contactSensor", description: "Sensor to detect open/close two times", required: false )
	}
    section("Trigger When Returned") {
      if (phrases) {
        input name: "homePhrasesReturn", type: "enum", title: "Execute Hello Home", multiple: true,required: false, options: phrases, refreshAfterSelection: true
      }
		input name: "modeReturn", type: "mode", title: "Change to Mode", required: false
		input name: "lightsReturn", type: "capability.switch", multiple: true, title: "Lights Off", required: false
		input name: "lockReturn", type: "capability.lock", title: "Lock Door", required: false
    }    
	section("Virtual Switch"){
		input("virtualSwitch", "boolean", title: "Install Virtual Switch" )
	}    
	section("Notification"){
		input("notifyTrigger", "boolean", title: "Notify When Triggerd", required: false)
		input("notifyReturn", "boolean", title: "Notify When Returned", required: false)
		input("notifyMsg", "text", title: "Notify Message", required: false)
	}
  }
}

/* Initialization */
def installed() {
    log.debug "Installed with settings: ${settings}"
    
    if (virtualSwitch) {
	    addVirtualSwitch()
	}

    initialize()
}

def uninstalled() {
	unschedule()
	unsubscribe()
	if (virtualSwitch) {
		def deleteDevices = getAllChildDevices()
		deleteDevices.each { deleteChildDevice(it.deviceNetworkId) }
	}
}	

def updated() { 
    log.debug "Updated with settings: ${settings}"
    if (virtualSwitch) {
		if (!getChildDevice("TakeOutDog")) {
			addVirtualSwitch()
		}
    }
    initialize()
}

def addVirtualSwitch () {
	addChildDevice("chuck-pearce", "Take out Dog", 'TakeOutDog', null, ["name": "Take Out Dog",  "completedSetup": true])
}

def initialize() { 
	if (lock) {
		subscribe(lock, "lock", startTakeOut)
	}
}

private getTimeOk() {
	def result = true
	if (timeStart && timeEnd) {
		def currTime = now()
		def start = timeToday(timeStart).time
		def stop = timeToday(timeEnd).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}


def startTakeOut(lock) {
	
	if (!getTimeOk() || !getModeOk()) {
		return
	}

	if (lock && lock.value != "unlocked") {
		return
	}

    if (virtualSwitch) {
		def child = getChildDevice("TakeOutDog")
		child.updateDeviceStatus(1)
	}

	if (notifyTrigger) {
		if (!notifyMsg) {
			notifyMsg = "Taking the dog out"
		}
		sendPush( "$notifyMsg started")
	}

	state.closeCount = 0

	if (lightsEnabled) {
		lightsEnabled?.on()
	}

	if (delay) {
		runIn(60*delay.toInteger(), endTakeOut)
	}

	if (settings.homePhrasesEnabled) {
      location.helloHome.execute(settings.homePhrasesEnabled)
	}

	if (contact) {
		subscribe(contact, "contact.closed", doorClosed)
	}

	if (modeEnabled) {
		setLocationMode(modeEnabled)
	}

}

def doorClosed (evt) {
	state.closeCount = state.closeCount + 1
	log.debug "The close count is $state.closeCount"
	if (state.closeCount >= 2) {
		endTakeOut()
	}
}

def endTakeOut() {
	unschedule()
	unsubscribe()
    
    if (virtualSwitch) {
		def child = getChildDevice("TakeOutDog")
		child.updateDeviceStatus(0)
	}

	if (modeReturn) {
		setLocationMode(modeReturn)
	}

	if (notifyReturn) {
		if (!notifyMsg) {
			def notifyMsg = "Taking the dog out"
		}
		sendPush( "$notifyMsg ended")
	}

	if (settings.homePhrasesReturn) {
      location.helloHome.execute(settings.homePhrasesReturn)
	}
	
	if (lightsReturn) {
		lightsReturn?.off()
	}

	if (lockReturn) {
		lockReturn.lock()
	}

}