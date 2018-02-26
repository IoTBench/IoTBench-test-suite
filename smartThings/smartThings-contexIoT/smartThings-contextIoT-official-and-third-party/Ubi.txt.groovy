/**
 *  Sample Web Services Application
 *
 *  Author: SmartThings
 */

preferences {
	section("Allow a web application to control these things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
		input "motions", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
		input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
        input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
		input "presenceSensors", "capability.presenceSensor", title: "Which Presence Sensors?", multiple: true, required: false
        
	}
}

mappings {
	path("/list") {
		action: [
			GET: "listAll"
		]
	}

	path("/events/:id") {
		action: [
			GET: "showEvents"
		]
	}

	path("/switches") {
		action: [
			GET: "listSwitches",
			PUT: "updateSwitches",
            POST: "updateSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch",
			PUT: "updateSwitch",
            POST: "updateSwitch"
		]
	}
	path("/switches/subscriptions") {
    log.debug "switches added"
		action: [
			POST: "addSwitchSubscription"
		]
	}
	path("/switches/subscriptions/:id") {
		action: [
			DELETE: "removeSwitchSubscription",
            GET: "removeSwitchSubscription"
		]
	}

	path("/motionSensors") {
		action: [
			GET: "listMotions",
			PUT: "updateMotions",
            POST: "updateMotions"
            
		]
	}
	path("/motionSensors/:id") {
		action: [
			GET: "showMotion",
			PUT: "updateMotion",
            POST: "updateMotion"
		]
	}
	
    path("/motionSensors/subscriptions") {
    log.debug "motionSensors added"
		action: [
			POST: "addMotionSubscription"
		]
	}
	path("/motionSensors/subscriptions/:id") {
    log.debug "motionSensors Deleted"
		action: [
			DELETE: "removeMotionSubscription",
            GET : "removeMotionSubscription"
		]
	}

	path("/locks") {
		action: [
			GET: "listLocks",
			PUT: "updateLock",
            POST: "updateLock"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock",
			PUT: "updateLock",
            POST: "updateLock"
		]
	}
	path("/locks/subscriptions") {
		action: [
			POST: "addLockSubscription"
		]
	}
	path("/locks/subscriptions/:id") {
		action: [
			DELETE: "removeLockSubscription",
            GET : "removeLockSubscription"
		]
	}




path("/contactSensors") {
		action: [
			GET: "listContactSensors",
			PUT: "updateContactSensor",
            POST: "updateContactSensor"
		]
	}
	path("/contactSensors/:id") {
		action: [
			GET: "showContactSensor",
			PUT: "updateContactSensor",
            POST: "updateContactSensor"
		]
	}
	path("/contactSensors/subscriptions") {
    log.debug "contactSensors/subscriptions"
		action: [
			POST: "addContactSubscription"
		]
	}
	path("/contactSensors/subscriptions/:id") {
		action: [
			DELETE: "removeContactSensorSubscription",
            GET : "removeContactSensorSubscription"
		]
	}
    
path("/presenceSensors") {
		action: [
			GET: "listPresenceSensors",
			PUT: "updatePresenceSensor",
            POST: "updatePresenceSensor"
		]
	}
	path("/presenceSensors/:id") {
		action: [
			GET: "showPresenceSensor",
			PUT: "updatePresenceSensor",
            POST: "updatePresenceSensor"
		]
	}
	path("/presenceSensors/subscriptions") {
    log.debug "PresenceSensors/subscriptions"
		action: [
			POST: "addPresenceSubscription"
		]
	}
	path("/presenceSensors/subscriptions/:id") {
		action: [
			DELETE: "removePresenceSensorSubscription",
            GET : "removePresenceSensorSubscription"
		]
	}
    
        
    
    
	path("/state") {
		action: [
			GET: "currentState"
		]
	}

}

def installed() 
{
//	subscribe(motions, "motion.active", motionOpenHandler)

//	subscribe(contactSensors, "contact.open", contactOpenHandler)
//	log.trace "contactSensors Installed"

}

def updated() {

//	unsubscribe()
//    subscribe(motions, "motion.active", motionOpenHandler)
//	subscribe(contactSensors, "contact.open", contactOpenHandler)

//log.trace "contactSensors Updated"

}

def listAll() {
	listSwitches() + listMotions() + listLocks() + listContactSensors() + listPresenceSensors()

}
def listContactSensors() {

contactSensors.collect{device(it,"contactSensor")}
}


void updateContactSensors() {
	updateAll(contactSensors)
}
def showContactSensor() {
	show(contactSensors, "contact")
}
void updateContactSensor() {
	update(contactSensors)
}
    
def addContactSubscription() {
 	log.debug "addContactSensorSubscription,  params: ${params}"
	addSubscription(contactSensors, "contact")
}
def removeContactSensorSubscription() {
	removeSubscription(contactSensors)
}



def listPresenceSensors() {

presenceSensors.collect{device(it,"presenceSensor")}
}


void updatePresenceSensors() {
	updateAll(presenceSensors)
}
def showPresenceSensor() {
	show(presenceSensors, "presence")
}
void updatePresenceSensor() {
	update(presenceSensors)
}
    
def addPresenceSubscription() {
 	log.debug "addPresenceSensorSubscription,  params: ${params}"
	addSubscription(presenceSensors, "presence")
}
def removePresenceSensorSubscription() {
	removeSubscription(presenceSensors)
}



def listSwitches() {
	switches.collect{device(it,"switch")}
}
void updateSwitches() {
	updateAll(switches)
}
def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}
def addSwitchSubscription() {
 	log.debug "addSwitchSubscription,  params: ${params}"
	addSubscription(switches, "switch")
}
def removeSwitchSubscription() {
	removeSubscription(switches)
}

def listMotions() {
	motions.collect{device(it,"motionSensor")}
}
void updateMotions() {
	updateAll(motions)
}
def showMotion() {
	show(motions, "motion")
}
void updateMotion() {
	update(motions)
}
def addMotionSubscription() {

	addSubscription(motions, "motion")
}
def removeMotionSubscription() {
	removeSubscription(motions)
}

def listLocks() {
	locks.collect{device(it,"lock")}
}
void updateLocks() {
	updateAll(locks)
}
def showLock() {
	show(locks, "lock")
}
void updateLock() {
	update(locks)
}
def addLockSubscription() {
	addSubscription(locks, "lock")
}
def removeLockSubscription() {
	removeSubscription(locks)
}

/*
def motionOpenHandler(evt) {
//log.trace "$evt.value: $evt, $settings"

	log.debug "$motions was active, sending push message to user"
	//sendPush("Your ${contact1.label ?: contact1.name} was opened")
    
    
	httpPostJson(uri: "http://automatesolutions.ca/test.php", path: '', body: [evt: [value: "motionSensor Active"]]) {
	log.debug "Event data successfully posted"
    }

}
def contactOpenHandler(evt) {
	//log.trace "$evt.value: $evt, $settings"

	log.debug "$contactSensors was opened, sending push message to user"
	//sendPush("Your ${contact1.label ?: contact1.name} was opened")
    
    
	httpPostJson(uri: "http://automatesolutions.ca/test.php", path: '', body: [evt: [value: "ContactSensor Opened"]]) {
	log.debug "Event data successfully posted"
    }
		
    
}
*/


def deviceHandler(evt) {
	log.debug "~~~~~TEST~~~~~~"
	def deviceInfo = state[evt.deviceId]
	if (deviceInfo) {
		httpPostJson(uri: deviceInfo.callbackUrl, path: '', body: [evt: [value: evt.value]]) {
			log.debug "Event data successfully posted"
		}
	} else {
		log.debug "No subscribed device found"
	}
}

def currentState() {
	state
}

def showStates() {
	def device = (switches + motions + locks).find { it.id == params.id }
	if (!device) {
		httpError(404, "Switch not found")
	}
	else {
		device.events(params)
	}
}

private void updateAll(devices) {
	def command = request.JSON?.command
	if (command) {
        command = command.toLowerCase()
		devices."$command"()
	}
}

private void update(devices) {
	log.debug "update, request: ${request.JSON}, params: ${params}, devices: $devices.id"
	//def command = request.JSON?.command
    def command = params.command
	if (command) {
    	command = command.toLowerCase()
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
			device."$command"()
		}
	}
}

private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
    	
		def attributeName = type
        
       
        
		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}

private addSubscription(devices, attribute) {
	//def deviceId = request.JSON?.deviceId
	//def callbackUrl = request.JSON?.callbackUrl
    
    log.debug "addSubscription,  params: ${params}"
    
    def deviceId = params.deviceId
    def callbackUrl = params.callbackUrl
    
 
    
    
	def myDevice = devices.find { it.id == deviceId }
	if (myDevice) {
		if (state[deviceId]) {
			log.debug "Switch subscription already exists, unsubcribing"
			unsubscribe(myDevice)
		}
		log.debug "Adding switch subscription" + callbackUrl
		state[deviceId] = [callbackUrl: callbackUrl]
		log.debug "Added state: $state"
		subscribe(myDevice, attribute, deviceHandler)
	}
}

private removeSubscription(devices) {
	def deviceId = params.id
	def device = devices.find { it.id == deviceId }
	if (device) {
		log.debug "Removing $device.displayName subscription"
		state.remove(device.id)
		unsubscribe(device)
	}
}

private device(it, type) {
	it ? [id: it.id, label: it.displayName, type: type] : null
}
