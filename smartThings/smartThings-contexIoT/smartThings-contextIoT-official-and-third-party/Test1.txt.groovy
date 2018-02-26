/**
 *  App Endpoint API Access Example
 *
 *  Author: SmartThings
 */

preferences {
	section("Allow Endpoint to Control These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true
        input "locks", "capability.lock", title: "Which Locks?", multiple: true
        input "thermostats", "capability.thermostat", title: "Which Thermostat?", multiple: true
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
		]
	}
	path("/switches/:id/:command") {
		action: [
			GET: "updateSwitch"
		]
	}
    path("/switches/:id/:command/:level") {
		action: [
			GET: "updateSwitch"
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
	path("/thermostats") {
		action: [
			GET: "listThermostats"
		]
	}
	path("/thermostats/:id") {
		action: [
			GET: "showThermostat"
		]
	}  
	//
	path("/thermostats/:id/:command/:temp") {
		action: [
			GET: "updateThermostat"
		]
	}      
}

def installed() {}

def updated() {}


//switches
def listSwitches() {
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
	locks.collect{device(it,"lock")}
}

def showLock() {
	show(locks, "lock")
}

void updateLock() {
	update(locks)
}


//thermostats
def listThermostats() {
	thermostats.collect{device(it,"thermostat")}
}

def showThermostat() {
	show(thermostats, "thermostat")
}

void updateThermostat() {

	def device = thermostats.find { it.id == params.id }
	def command = params.command
	def temp = params.temp
    
    log.debug "$command ${params.id} at $temp"

	if(command == 'heat')
	{
		device.setHeatingSetpoint(temp)
	}
	else if(command == 'cool')
	{
	  device.setCoolingSetpoint(temp)	
	}
}

def deviceHandler(evt) {}

private void update(devices) {
	log.debug "update, request: params: ${params}, devices: $devices.id"
    
    
	//def command = request.JSON?.command
    def command = params.command
	def level = params.level
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
            else if(command == "level")
            {
            	device.setLevel(level.toInteger())
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
		def attributeName = type == "motionSensor" ? "motion" : type
		def s = device.currentState(attributeName)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
}


private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}
