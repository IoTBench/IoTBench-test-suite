/**
 *  App Endpoint API Access Example
 *
 *  Author: SmartThings
 */


// Automatically generated. Make future change here.
definition(
    name: "OnTheLight",
    namespace: "",
    author: "Vinh Nguyen",
    description: "testing light endpoint",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: [displayName: "OnTheLight", displayLink: ""]
)

preferences {
	section("Allow light Endpoint to Control These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true
	}
    section("Unlock the lock..."){
		input "locks", "capability.lock", multiple: true
	}
}

mappings {
	path("/switches") {
		action: [
			GET: "listSwitches"
		]
	}
	path("/switches/:id") {
		action: [
			GET: "showSwitch"
            //PUT: "updateSwitch",
            //POST: "updateSwitch"
		]
	}
	path("/switches/:id/:command") {
		action: [
			GET: "updateSwitch"
            //PUT: "updateSwitch",
            //POST: "updateSwitch"
		]
	} 
    path("/locks") {
		action: [
			GET: "listLocks"
		]
	}
	path("/locks/:id") {
		action: [
			GET: "showLock"
		]
	}
	path("/locks/:id/:command") {
		action: [
			GET: "updateLock"
		]
	}
    path("/init") {
    	action: [
        	GET: "doInit"
        ]
    }
    path("/init/:stbip/:deviceid") {
    	action: [
        	GET: "doInit"
        ]
    }
    path("/action") {
    	action: [
        	GET: "doAction"
        ]
    }    
    path("/action/:actionType/:id/:person") {
    	action: [
        	GET: "doAction2"
        ]
    }    
    path("/action/:actionType/:id") {
    	action: [
        	GET: "doAction3"
        ]
    }    
    log.debug "path ${path}"
}


def installed() {
}

def updated() {
	unsubscribe()
}


//switches
def listSwitches() {
    log.debug "listSwitches: udate, request: params: ${params}, devices: $devices.id"
    
	switches.collect{device(it,"switch")}
}

def showSwitch() {
	show(switches, "switch")
}
void updateSwitch() {
	update(switches)
}
//locks
def listLocks() {
	if (locks != null) {
        locks.collect{device(it,"lock")}
        log.debug "listLocks: " + locks
    }
    else {
    	log.debug "listLocks, locks is null"
    }
}

def showLock() {
	show(locks, "lock")
}

void updateLock() {
	update(locks)
}

def deviceHandler(evt) {
}

def doActionImpl(actionType, id, person) {
	
    log.debug "GET action:  actionType: " + actionType + " id: " + id + " person: " + person
    
    
	log.debug "GET action: actionType: ${actionType}, id: ${id}, person: ${person}"
    // Must be a valid person
    //if (validatePerson(params.person)) {
        switch(actionType) {
        case "goodMorning":
            goodMorningAction(person)
            httpError(404, "Invalid Person")
            break;
        case "goodNight":
        	goodNightAction(person)
            break;
        case "leaveForWork":
            leaveForWorkAction(person)
            break;
        case "returnHome":
            returnHomeAction(person)
            break;
        default:
            log.debug "doAction: ERROR: person: ${person}"
            break;
        }
    //}
    //else {
    //	httpError(404, "Invalid Person")
    //}
    
}

private void update(devices) {
	log.debug "update, request: params: ${params}, devices: $devices.id, command2:${params.command}"
    
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
        log.debug "device ${device}"
	}
}


private show(devices, type) {
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def attributeName = type == "motionSensor" ? "motion" : type
		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}


private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}
/* init */
def doInit() {
	// Must be called first to force demo state
	log.debug "doInit: state: " + state + ", stb: " + params.stbip + ", deviceid: " + params.deviceid
    
	log.debug "doInit: lock1: " + lock1
	log.debug "doInit(EXIT): state: " + state
}

/* actions */
def doAction() {
	log.debug "GET action: actionType: ${params.actionType}, person: ${params.person}"
    doActionImpl(params.actionType, 0, params.person)
}

def doAction2() {
	log.debug "GET action: actionType: ${params.actionType}, id: ${params.id}, person: ${params.person}"
    doActionImpl(params.actionType, params.id, params.person)
}

def doAction3() {
	log.debug "GET action: actionType: ${params.actionType}, id: ${params.id}, person: None"
    doActionImpl(params.actionType, params.id, "John")
}

def goodMorningAction(person) {
	log.debug "goodMorningAction[" + person + "]"
}
def goodNightAction(person) {
	log.debug "goodNightAction[" + person + "]"
}

def leaveForWorkAction(person) {
	log.debug "leaveForWorkAction[" + person + "]"
}

def returnHomeAction(person) {
	log.debug "returnHomeAction[" + person + "]"
}