/**
 *  Optik Home
 *
 *  Author: Eti team
 *  Date: 2014-03-28
 */

import grails.converters.JSON


// Automatically generated. Make future change here.
definition(
    name: "eti_optik_home",
    namespace: "eti",
    author: "Eti team",
    description: "Description goes here",
    category: "Fun & Social",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: [displayName: "Optik Home", displayLink: ""]
)

appSetting "httpUrl"

preferences {
    section("Monitor") {
        input "switches",
            "capability.switch",
            title: "Switches",
            multiple: true,
            description: "Which Switches to manage?"

        input "sensors",
            "capability.sensor",
            title:"Sensors",
            multiple:true,
            description:"Which sensor?"

        input "cameras",
            "capability.imageCapture",
            title:"Cameras",
            multiple:true,
            description:"Which cameras?"
           
        input "webPresence",
			"capability.presenceSensor",
			title:"Web Presence",
            multiple: true,
            description:"Which sensor?"
    }
}

mappings {
    path("/location") {
        action: [
            GET: "showLocation"
        ]
    }

    path("/devices") {
        action: [
            GET: "listDevices"
        ]
    }

    path("/devices/:id") {
        action: [
            GET: "showDevice"
        ]
    }

    path("/switches/:id/:command/:attribute") {
        action: [
            GET: "updateSwitch"
        ]
    }
    
    path("/update/:id") {
        action: [
            POST: "updateDevice"
        ]
    }
    path("/sms/:number") {
        action: [
            POST: "httpSendSMS"
        ]
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

// Helper Functions
def all_devices() {
    return settings.switches + settings.sensors + settings.webPresence + settings.cameras
}

private device_json(it) {
    if (!it) { return null }

    def values = [:]
    def attrs = [:]
    for (a in it.supportedAttributes) {
        values[a.name] = it.currentState(a.name)
        attrs[a.name] = [datatype: a.dataType, name: a.name, values: a.values]
    }

    return [capabilities: it.capabilities.collect { it.preferenceName },
            label: it.displayName,
            name: it.name,
            id: it.id,
            deviceNetworkId: it.deviceNetworkId,
            values: values,
            attrs: attrs]
}


// locations
def showLocation() {

    return [
        "name": location.name,
        "id": location.id,
        "latitude": location.latitude,
        "longitude": location.longitude,
        "mode": location.mode,
        "timezone": location.timeZone ? location.timeZone.ID : location.timeZone
    ]
}

// devices

def listDevices() {
    return all_devices().collect{device_json(it)}
}

def showDevice() {
    def device = all_devices().find { it.id == params.id }
    if (!device) {
        httpError(404, "Device not found")
        return
    }
    return device_json(device)
}

// switches
def updateSwitch() {
    update(all_devices())
}

def updateDevice() {
    updateJSON(all_devices())
}

def httpSendSMS() {
    log.debug "sms, request: params: ${params}"
    def number = params.number
    def message = request.JSON?.message
    return sendSms(number, message)
}

def deviceChanged(evt) {
    final PUSH_HOSTNAME = appSettings.httpUrl

    //def url = PUSH_HOSTNAME + "handler/smartthings/" + evt.deviceId
    def url = PUSH_HOSTNAME + "handler/smartthings"
    log.debug "Pushing to ${url}"

    def data = [:]
    data['name'] =  evt.name;
    data['deviceId'] =  evt.deviceId;
    //data['isStateChange'] =  evt.isStateChange()
    //data['isPhysical'] = evt.isPhysical()
    data['description'] =  evt.description
    data['descriptionText'] =  evt.descriptionText
    data['displayName'] =  evt.displayName
    data['date'] =  evt.isoDate
    // source
    data['values[name]'] = evt.name
    data['values[value'] = evt.value
    data['values[unit'] = evt.unit

    log.trace "temperatureChagne, evt: ${evt} ---, settings: ||$data||"

    def successClosure = { response ->
      log.debug "Request was successful, ${response.getData()}"
    }
    // TODO: subscribe to attributes, devices, locations, etc.
    def params = [
      uri: url,
      success: successClosure,
      body: data
    ]
    httpPost(params)
}

def event(evt) {
    log.trace "evt, evt: [${evt}], settings: [${settings}]"
}

def initialize() {
    log.debug "all_devices: ${all_devices}"
    for ( item in all_devices() ) {
        log.debug "item: ${item.name}"
        for (c in item.supportedAttributes) {
            log.debug "Subscribing to ${item.name} => ${c.name}"
            subscribe(item, c.name, deviceChanged )
        }
    }
}

private update(devices) {
    log.debug "update, request: params: ${params}, devices: $devices.id"

    //def attr = request.JSON?.attribute
    def command = params.command
    def attr = params.attribute
    def value = params.value
    
    if (!attr) { attr = "switch"; }
    //let's create a toggle option here
    if (!command) {
        httpError(404, "Command found")
        return
    }

    def device = devices.find { it.id == params.id }
    if (!device) {
        httpError(404, "Device not found")
        return
    }

    if(command == "toggle")
    {
        if(device.currentValue(attr) == "on") {
            device.off();
            command = "off";
        } else {
            device.on();
            command = "on";
        }
    }
    else
    {
    	if (value) {
        	device."$command"(value)
        } else {
        	device."$command"()
        }
    }
    return [(attr): command ]
    // FIXME - state doesn't update quick enough
    //return [ state: device.currentValue('switch') ]
}

private updateJSON(devices) {
    log.debug "update, request: params: ${params}"

    def attr = request.JSON?.attribute
    if (!attr) { attr = "switch"; }
    def command = request.JSON?.command
    def value = request.JSON?.value

    log.debug "attr: ${attr}, command: ${command}, value: ${value}"

    //let's create a toggle option here
    if (!command) {
        httpError(404, "Command found")
        return
    }

    def ids = params.id.split(",");
    log.debug "update2, request: params: ${ids}"


    def filteredDevices = devices.findAll { ids.contains(it.id) }
    if (!filteredDevices || filteredDevices.size() == 0) {
   		log.debug("Unable to find device");
        httpError(404, "Device not found")
        return
    }
    
    if(command == "toggle") 
    {
        for (device in filteredDevices) 
        {
            log.debug "device: ${device}, attr: ${attr}, command: ${command}, value: ${value}"
            if(device.currentValue(attr) == "on") {
                device.off();
                command = "off";
            } else {
                device.on();
                command = "on";
            }
        }
    }
    else
    {
        log.debug "device: ${filteredDevices}, attr: ${attr}, command: ${command}, value: ${value}"

        if (value) {
            log.debug("device.${command}(${value})");
            filteredDevices*."$command"(value)
        } else {
            log.debug("device.${command}()");
            filteredDevices*."$command"()
        }
    }

    return ["success": "true" ]
 }