/**
 *  Simple-Rules-Engine
 *
 *  Copyright 2015 Joe Craddock
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
import groovy.json.JsonSlurper
definition(
    name: "Simple Rules Engine",
    namespace: "JoeCraddock",
    author: "Joe Craddock",
    description: "Simple rules engine for Smart Things",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)


preferences {
  section("Allow External Service to Control These Things...") {
    input "switches", "capability.switch", title: "Which Switches?", multiple: true, required: false
    input "dimmers", "capability.switchLevel", title: "Which Dimmers?", multiple: true, required: false
    input "locks", "capability.lock", title: "Which Locks?", multiple: true, required: false
    input "thermostats", "capability.thermostat", title: "Which Thermostats?", multiple: true, required: false
    input "alarms", "capability.alarm", title: "Which Alarms?", multiple: true, required: false
    input "doorControls", "capability.doorControl", title: "Which Doors?", multiple: true, required: false
  }
  
  section("allow it to get data from these things") {
    input "presence", "capability.presenceSensor", title: "Which Presence?", multiple: true, required: false
    input "temperature", "capability.temperatureMeasurement", title: "Which Temperature?", multiple: true, required: false
    input "illuminance", "capability.illuminanceMeasurement", title: "Which Light Level?", multiple: true, required: false
    input "humidity", "capability.relativeHumidityMeasurement", title: "Which Hygrometer?", multiple: true, required: false
    input "motions", "capability.motionSensor", title: "Which Motion Sensors?", multiple: true, required: false
    input "contactSensors", "capability.contactSensor", title: "Which Contact Sensors?", multiple: true, required: false
    input "buttonDevices", "capability.button", title: "Which Buttons?", multiple: true, required: false
    input "waterSensors", "capability.waterSensor", title: "Which Water Sensors?", multiple: true, required: false
    input "carbonMonoxideDetectors", "capability.carbonMonoxideDetector", title: "Which Carbon Monoxide Detectors?", multiple: true, required: false
    input "smokeDetectors", "capability.smokeDetector", title: "Which Smoke Detectors?", multiple: true, required: false
    
  }
  
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def updateSubscriptions() {
	log.debug "updateSubscriptions()"
    initialize()
}

def initialize() {
    subscribe(switches, "switch", handler, [filterEvents: false])
    subscribe(dimmers, "level", handler, [filterEvents: false])
    subscribe(dimmers, "switch", handler, [filterEvents: false])
    subscribe(locks, "lock", handler, [filterEvents: false])
    subscribe(presence, "presence", handler, [filterEvents: false])
    subscribe(temperature, "temperature", handler, [filterEvents: false])
    subscribe(humidity, "humidity", handler, [filterEvents: false])
    subscribe(motions, "motion", handler, [filterEvents: false])
    subscribe(illuminance, "illuminance", handler, [filterEvents: false])
    subscribe(contactSensors, "contact", handler, [filterEvents: false])
    subscribe(alarms, "alarm", handler, [filterEvents: false])
    subscribe(buttonDevices, "button", buttonDevicehandler, [filterEvents: false])
    subscribe(waterSensors, "waterSensor", handler, [filterEvents: false])
    subscribe(carbonMonoxideDetectors, "carbonMonoxideDetector", handler, [filterEvents: false])
    subscribe(smokeDetectors, "smokeDetector", handler, [filterEvents: false])
    subscribe(doorControls, "doorControl", handler, [filterEvents: false])
    subscribe(location, modeChangeHandler)
}

def allThings() {
	def combined = (switches << dimmers << motions << locks << presence << temperature << humidity << thermostats << illuminance << contactSensors << alarms << buttonDevices << waterSensors << carbonMonoxideDetectors << smokeDetectors << doorControls).flatten()
	
    return combined
}

def handler(evt) {
	log.debug "event happened $evt.description - $evt.value"
    
    def url = "http://rulesengine.thesalthouse.co/EventHandler.ashx"
    
    httpPostJson(uri: url, path: '',  body: [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value]]) {
        log.debug "Event data successfully posted"
    }
}

def buttonDevicehandler(evt) {
    def parsedEventData = new JsonSlurper().parseText(evt.data)
	log.debug "${evt.name} ${parsedEventData.buttonNumber} - ${evt.value}"
    
    def data = [evt: [deviceId: evt.deviceId, name: evt.name, value: evt.value, buttonIndex: parsedEventData.buttonNumber]]
    
    postData(data)
}

def modeChangeHandler(evt) {
	log.debug "mode change: ${evt.name}"
    def data = [id: location.id, currentmode: location.currentMode.name, type:"location"]
    
    postData(data)
}

def postData(data){
	def url = "http://rulesengine.thesalthouse.co/EventHandler.ashx"
    
    httpPostJson(uri: url, path: '',  body: data) {
        log.debug "Event data successfully posted"
    }
}

mappings {
      path("/command") {action: [GET: "command"]}
      path("/list") {action: [GET: "getListOfThings"]}
      path("/locationdata") {action: [GET: "getLocationData"]}
      path("/data/:id/:attribute") {action: [GET: "getThingData"]}
      path("/updateSubscriptions") {action: [GET: "updated"]}
}

def oauthError() {[error: "OAuth token is invalid or access has been revoked"]}


def getListOfThings() {
    updated()
	def data = []
	switches?.each{data << device(it,"switch")}
	dimmers?.each{data << device(it,"dimmer")}
	motions?.each{data << device(it,"motion")}
	locks?.each{data << device(it,"lock")}
	presence?.each{data << device(it,"presence")}
	temperature?.each{data << device(it,"temperature")}
	humidity?.each{data << device(it,"humidity")}
	thermostats?.each{data << device(it,"thermostat")}
	illuminance?.each{data << device(it,"illuminance")}
	contactSensors?.each{data << device(it,"contact")}
	alarms?.each{data << device(it,"alarm")}
	buttonDevices?.each{data << device(it,"button")}
	waterSensors?.each{data << device(it,"waterSensor")}
	carbonMonoxideDetectors?.each{data << device(it,"carbonMonoxideDetector")}
	smokeDetectors?.each{data << device(it,"smokeDetector")}
	doorControls?.each{data << device(it,"doorControl")}
	
	data
}

def getLocationData() {
	def data = []

    location.helloHome?.getPhrases()?.each{data << [id: it.id, name: it.label, type: "hellohomephrase"]}
    location.modes?.each{data << [id: it.id, name: it.name, type: "mode"]}
	data << [id: location.id, currentmode: location.currentMode.name, type:"location"]
    
	data
}

def getThingData() {

    def id = params.id
    def attribute = params.attribute
    
    def device = allThings().find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	}
	else {
		def s = device.currentState(attribute)
		[id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
	}
    
}

void command() {
	log.debug "command received with params $params"
    
    def command = params.command
    def type = params.type
    def value = params.value
    def id = params.id
    def delay = params.delay as int
    
    if (command) 
    {
		def device

        if (type == "switch") 
        {
            device = switches?.find{it.id == id}
            if (device) 
            {
            	device."$command"([delay: delay])
            }
        }
        if (type == "alarm") 
        {
            device = alarms?.find{it.id == id}
            if (device) 
            {
                device."$command"([delay: delay])
            }
        }
        else if (type == "lock") 
        {
            device = locks?.find{it.id == id}
            if (device) 
            {
                device."$command"([delay: delay])
            }
        }
        else if (type == "doorControl") 
        {
            device = doorControls?.find{it.id == id}
            if (device) 
            {
                device."$command"([delay: delay])
            }
        }
        else if (type == "level") 
        {
            device = dimmers?.find{it.id == id}
            if (device) 
            {
            	device.setLevel(value as int, [delay: delay])
                
            }
        }
        else if (type == "heatingsetpoint") 
        {
            device = thermostats?.find{it.id == id}
            if (device) 
            {
                device.setHeatingSetpoint(value as int, [delay: delay])                
            }
        }
        else if (type == "coolingsetpoint") 
        {
            device = thermostats?.find{it.id == id}
            if (device) 
            {
            	device.setCoolingSetpoint(value as int, [delay: delay])
            }
        }
        else if (type == "pushnotification")
        {
        	sendPush(value)
        } 
        else if (type == "mode") 
        {
            if(location.mode != value) {
                setLocationMode(value)
            }
        } 
        else if (type == "hellohome") 
        {
    		location.helloHome.execute(value)
        }

    }
}

// not used
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