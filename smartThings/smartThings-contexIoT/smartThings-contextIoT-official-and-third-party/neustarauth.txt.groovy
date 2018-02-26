/**
 *  Sample Web Services Application
 *
 *  Author: SmartThings
 */

preferences {
	section("Allow a web application to control these things...") {
		input "switches", "capability.switch", title: "Which  Switches?", multiple: true, required: false
		input "presences", "capability.presenceSensor", title: "Which  presence Sensors?", multiple: true, required: false
		input "motions", "capability.motionSensor", title: "Which  Motion Sensors?", multiple: true, required: false
		input "contacts", "capability.contactSensor", title: "Which  Contact Sensors?", multiple: true, required: false
		//input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
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
			PUT: "updateSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch",
			PUT: "updateSwitch"
		]
	}
	
    path("/switches/:id/:command") {
		action: [
			GET: "updateSwitch"
		]
	}
	path("/switches/subscriptions") {
		action: [
			POST: "addSwitchSubscription"
		]
	}
	path("/switches/subscriptions/:id") {
		action: [
			DELETE: "removeSwitchSubscription"
		]
	}

	//MOTION
	path("/motionSensors") {
		action: [
			GET: "listMotions",
		]
	}
	path("/motionSensors/:id") {
		action: [
			GET: "showMotion",
		]
	}
	path("/motionSensors/subscriptions") {
		action: [
			POST: "addMotionSubscription"
		]
	}
	path("/motionSensors/subscriptions/:id") {
		action: [
			DELETE: "removeMotionSubscription"
		]
	}

	//PRESENCE
	path("/presenceSensors") {
		action: [
			GET: "listPresences",
			PUT: "updatePresences"
		]
	}
	path("/presenceSensors/:id") {
		action: [
			GET: "showPresence",
			PUT: "updatePresence"
		]
	}
	path("/presenceSensors/subscriptions") {
		action: [
			POST: "addPresenceSubscription"
		]
	}
	path("/presenceSensors/subscriptions/:id") {
		action: [
			DELETE: "removePresenceSubscription"
		]
	}

	//CONTACT
	path("/contactSensors") {
		action: [
			GET: "listContacts",
			PUT: "updateContacts"
		]
	}
	path("/contactSensors/:id") {
		action: [
			GET: "showContact",
			PUT: "updateContact"
		]
	}
	path("/contactSensors/subscriptions") {
		action: [
			POST: "addContactSubscription"
		]
	}
	path("/contactSensors/subscriptions/:id") {
		action: [
			DELETE: "removeContactSubscription"
		]
	}

	path("/state") {
		action: [
			GET: "currentState"
		]
	}

}

def installed() {log.trace "Installed"}

def updated() {log.trace "Updated"}

def listAll() {
	listSwitches() + listMotions() + listPresences() + listContacts()
}

def listMotions() {
	motions.collect{device(it,"motionSensor")}
}
def listPresences() {
	presences.collect{device(it,"presenceSensor")}
}
def listSwitches() {
	switches.collect{device(it,"switch")}
}
def listContacts() {
	contacts.collect{device(it,"contactSensor")}
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
	addSubscription(switches, "switch")
}
def removeSwitchSubscription() {
	removeSubscription(switches)
}



def showMotion() {
	show(motions, "motionSensor")
}
def addMotionSubscription() {
	addSubscription(motions, "motionSensor")
}
def removeMotionSubscription() {
	removeSubscription(motions)
}



void updatePresences() {
	updateAll(presences)
}
def showPresence() {
	show(presences, "presenceSensor")
}
void updatePresence() {
	update(presences)
}
def addPresenceSubscription() {
	addSubscription(presences, "presenceSensor")
}
def removePresenceSubscription() {
	removeSubscription(presences)
}



void updateContacts() {
	updateAll(contacts)
}
def showContact() {
	show(contacts, "contactSensor")
}
void updateContact() {
	update(contacts)
}
def addContactSubscription() {
	addSubscription(contacts, "contactSensor")
}
def removeContactSubscription() {
	removeSubscription(contacts)
}

def deviceHandler(evt) {
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
	def device = (switches + motions + presences + contacts).find { it.id == params.id }
	if (!device) {
		httpError(404, "Device(switch,motion,presence) not found")
	}
	else {
		device.events(params)
	}
}

private void updateAll(devices) {
	def command = request.JSON?.command
	if (command) {
		devices."$command"()
	}
}


private void update(devices) {
	log.debug "update, request: params: ${params}, devices: $devices.id"
    
    
	//def command = request.JSON?.command
    def command = params.command
    //let's create a toggle option here
	if (command) 
    {
		def device = devices.find { it.id == params.id }
		if (!device) {
			httpError(404, "Device not found")
		} else {
        	if(command == "toggle")
       		{
            	if(device.currentValue('switch') == "on")
                  device.off();
                else
                  device.on();
       		}
       		else
       		{
				device."$command"()
            }
		}
	}
}
private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = type == "motionSensor" ? "motion" : (type == "presenceSensor" ? "presence" : (type == "contactSensor" ? "contact" : type))
		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}

private addSubscription(devices, attribute) {
	def deviceId = request.JSON?.deviceId
	def callbackUrl = request.JSON?.callbackUrl
	def myDevice = devices.find { it.id == deviceId }
	if (myDevice) {
		if (state[deviceId]) {
			log.debug "Switch subscription already exists, unsubcribing"
			unsubscribe(myDevice)
		}
		log.debug "Adding switch subscription" + callbackUrl
		state[deviceId] = [callbackUrl: callbackUrl]
		log.debug "Added state: $state"
		subscribe(myDevice, "switch", deviceHandler)
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