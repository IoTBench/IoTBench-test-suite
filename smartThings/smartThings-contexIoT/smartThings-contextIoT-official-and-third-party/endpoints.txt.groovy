/**
 *  App Endpoint API Access Example
 *
 *  Author: SmartThings
 */


// Automatically generated. Make future change here.
definition(
    name: "endpoints",
    namespace: "",
    author: "todd@wackford.net",
    description: "My End Point App",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("Allow Endpoint to Control These Things...") {
		input "switches", "capability.switch", title: "Which Switches?", multiple: true
        input "locks", "capability.lock", title: "Which Locks?", multiple: true
        input "thermostats", "capability.thermostat", title: "Which Thermostat?", multiple: true
		input "temprature", "capability.temperatureMeasurement", title: "Which Temp Devices?", multiple: true
    	input "presence", "capability.presenceSensor", title: "Which presence Devices?", multiple: true
        input "alarm", "capability.alarm", title: "Which alarm Devices?", multiple: true,required: false
        input "water", "capability.waterSensor", title: "Which moisture Devices?", multiple: true
        input "motion", "capability.motionSensor", title: "Which motion Devices?", multiple: true
    }
}

mappings {

	path("/temps") {
    	action: [
        	GET: "listTemps"
        ]
	}
    
    path("/temps/:id") {
		action: [
			GET: "showTemp"
		]
	}
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

//Temprature devices
def listTemps() {
	temps.collect{device(it,"temperature")}
}
def showTemp() {
	show(temps, "temp")
}

//switches
def listSwitches() {
	switches.collect{device(it,"switch")}
}

def showSwitch() {
	show(switches + locks + thermostats + temprature + presence + alarm + water + motion, "switch")
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
		httpError(404, "This Device not found")
	}
	else {
    	def supportedAttributes = device.supportedAttributes
        def attributes = []
        def a = ""
        for(attribute in supportedAttributes){
        	a = device.currentState(attribute.toString())
            if ( a != null)
        		attributes += [attribute: a]
        }
        //def commands = device.supportedCommands
        //def capabilities = null //device.capabilities
        
		[id: device.id, name: device.name, label: device.displayName, attributes: attributes]
        //[id: device.id, name: device.name, label: device.displayName, attributes: attributes, commands: commands, capabilities: capabilities]
	}
}


private device(it, type) {
	if ( type == "switch") {
    	def s = it.currentState("switch")
        def l = it.latestValue("level")
        //def stateList = it.statesSince((now()-86400000))
        //def icon = it.icon
		it ? [	id: it.id, label: it.label,
        		stateList: stateList,
        		currentState: s?.value,
                //currentIcon: icon,
                unitTime: s?.date?.time,
                level: l, 
                type: type] : null
    }
    else if (type == "temps" ) {
    	//def s = it.curretTemperature
        def l = it.currentState("temperature")
        //def backDate = new Date(now()-86400000)
        //def stateList = it.statesSince((now()-86400000))
        it ? [	id: it.id, label: it.label, 
        		currentTemp: l?.value,
                //currentIcon: icon,
                unitTime: l?.date?.time,
                type: type] : null
    }
    else if (type == "lock" ) {
    	//def s = it.curretTemperature
        def l = it.currentState("lock")
        it ? [	id: it.id, label: it.label, 
        		currentState: l?.value,
                //currentIcon: icon,
                unitTime: l?.date?.time,
                type: type] : null
    }
    else {
    	it ? [id: it.id, label: it.label, type: type] : null
    }
}