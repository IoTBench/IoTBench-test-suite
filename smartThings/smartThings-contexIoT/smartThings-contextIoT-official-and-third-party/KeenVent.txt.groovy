/**
 *  Keen Smart vent
 *
 *  Author: John Tubert
 */
 
import grails.converters.JSON
import grails.web.JSONBuilder 
import org.codehaus.groovy.grails.web.json.*; 
            

// Automatically generated. Make future change here.
definition(
    name: "KeenVent",
    namespace: "",
    author: "Will@keenhome.io",
    description: "Keen vent smart app",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: [displayName: "KeenHome", displayLink: ""]
)

preferences {
	section("Allow Endpoint to Control These Things...") {
		//input "switchLevel", "capability.switchLevel", title: "Which switchLevel?", multiple: true
        input "switches", "capability.switch", title: "Which Switches?", multiple: true 
	}    
    section("Optionally choose temperature sensor to use instead of the thermostat's... ") {
		input "sensor", "capability.temperatureMeasurement", title: "Temp Sensors", required: false
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
    
    path("/temperature") {
		action: [
			GET: "getTemperature"
		]
	}
    
    path("/state/:id") {
		action: [
			GET: "getSavedValue"
		]
	}
    
    path("/level/:id/:num") {
		action: [
			GET: "setLevel"
		]
	}
    
    path("/level/:id") {
		action: [
			GET: "getLevel"
		]
	}
    
    path("/state/:id/:value") {
		action: [
			GET: "setSavedValue"
		]
	}
    
    path("/init") {
		action: [
			GET: "initScheduale"
		]
	}
    
    path("/user") {
    	action: [
        	GET: "showUser"
        ]
    }
}

def showUser() {
	[locationID: location.id, timeZone: location.timeZone.getID()]
}

def installed() {}

def updated() {}

def initScheduale() {
	unschedule
    log.debug "now ${now()}"
	
    //runs every hours at 30 mins past the hour
    //schedule("0 30 * * * ?","runScheduale")
    
    //runIn(60, "runScheduale")
    
    //runOnce(, "runScheduale")
    
    
    def result =[:]
	result.name = "status"
    result.value = "cron started"

	result as JSON
    
}

def runScheduale() {
	log.debug "runScheduale ${now()}"
    //sendSms("16468413703", "runScheduale")
}

def setLevel() {
	log.debug "setLevel: ${params.num} id ${params.id}"
    
    if(params.num){
    	switches.setLevel(params.num.toInteger())
        
        def result =[:]
        result.name = "level was set"
        result.value = params.num

        result as JSON
    }
}

def getLevel() {
	log.debug "getLevel id ${params.id}"
    
    def result =[:]
	result.name = "level"
    result.value = "5"

	result as JSON
}

def getTemperature() {
	sensor.currentTemperature  
    //sensor.getCurrentState("temperature")
}

def getSavedValue(){
	log.debug "get: ${params.id} / ${state[params.id]}"
    
    def result =[:]
	result.name = [params.id]
    result.value = state[params.id]

	result as JSON
    
    
    //state[params.id]
}

def setSavedValue(){
	state[params.id] = params.value
	
    
    log.debug "set: ${params.id} / ${state[params.id]}"
}


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



def deviceHandler(evt) {}

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
	} else {
		[id: device.id, label: device.displayName, level: device.currentValue("level"), type: type]
	}
}


private device(it, type) {
	it ? [id: it.id, label: it.displayName, type: type] : null
}
