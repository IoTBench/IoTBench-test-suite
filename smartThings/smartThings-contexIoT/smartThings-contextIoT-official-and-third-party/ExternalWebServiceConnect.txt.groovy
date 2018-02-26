/**
 *  External Web Service
 *
 *  Author: bod@bod.org
 *  Date: 2014-02-25
 */

import groovy.json.JsonBuilder

preferences {
	section("Which devices should be available?") {
		input "sensor", "capability.sensor", title: "Which sensors?", multiple: true, required: false
		input "presenceSensor", "capability.presenceSensor", title: "Which presence sensors?", multiple: true, required: false
		input "actuator", "capability.actuator", title: "Which actuators?", multiple: true, required: false
		input "switches", "capability.switch", title: "Which switches?", multiple: true, required: false
		input "colorControl", "capability.colorControl", title: "Which color controls?", multiple: true, required: false
		input "musicPlayer", "capability.musicPlayer", title: "Which music players?", multiple: true, required: false
		input "alarm", "capability.alarm", title: "Which alarms?", multiple: true, required: false
		input "energyMeter", "capability.energyMeter", title: "Which energy meters?", multiple: true, required: false
		input "indicator", "capability.indicator", title: "Which indicators?", multiple: true, required: false
		input "powerMeter", "capability.powerMeter", title: "Which power meters?", multiple: true, required: false
		input "smokeDetector", "capability.smokeDetector", title: "Which smoke detectors?", multiple: true, required: false
		input "carbonMonoxideDetector", "capability.carbonMonoxideDetector", title: "Which CO detectors?", multiple: true, required: false
		input "thermostat", "capability.thermostat", title: "Which thermostats?", multiple: true, required: false
	}
    /*
	section("or select by capability") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Which acceleration sensors?", multiple: true, required: false
		input "battery", "capability.battery", title: "Which battery levels?", multiple: true, required: false
		input "button", "capability.button", title: "Which buttons?", multiple: true, required: false
		input "configuration", "capability.configuration", title: "Which configurations?", multiple: true, required: false
		input "contactSensor", "capability.contactSensor", title: "Which contact sensors?", multiple: true, required: false
		input "illuminanceMeasurement", "capability.illuminanceMeasurement", title: "Which illuminance measurements?", multiple: true, required: false
		input "imageCapture", "capability.imageCapture", title: "Which image captures?", multiple: true, required: false
		input "locationMode", "capability.locationMode", title: "Which location modes?", multiple: true, required: false
		input "lock", "capability.lock", title: "Which locks?", multiple: true, required: false
		input "lockCodes", "capability.lockCodes", title: "Which lock codes?", multiple: true, required: false
		input "momentary", "capability.momentary", title: "Which momentaries?", multiple: true, required: false
		input "motionSensor", "capability.motionSensor", title: "Which motion sensors?", multiple: true, required: false
		input "polling", "capability.polling", title: "Which pollings?", multiple: true, required: false
		input "refresh", "capability.refresh", title: "Which refreshes?", multiple: true, required: false
		input "relativeHumidity", "capability.relativeHumidityMeasurement", title: "Which relative humidity sensors?", multiple: true, required: false
		input "signalStrength", "capability.signalStrength", title: "Which signal strengths?", multiple: true, required: false
		input "switchLevel", "capability.switchLevel", title: "Which switch levels?", multiple: true, required: false
		input "temperatureMeasurement", "capability.temperatureMeasurement", title: "Which temperature sensors?", multiple: true, required: false
		input "threeAxis", "capability.threeAxis", title: "Which three axis sensors?", multiple: true, required: false
		input "tone", "capability.tone", title: "Which tones?", multiple: true, required: false
		input "valve", "capability.valve", title: "Which valves?", multiple: true, required: false
		input "waterSensor", "capability.waterSensor", title: "Which water sensors?", multiple: true, required: false
	}
    */
}

mappings {

	path("/events/:id") {
		action: [
			GET: "showEvents"
		]
	}

	path("/devices") {
		action: [
			GET: "showDevice"
		]
	}
	path("/device/:id") {
		action: [
			GET: "showDevice",
			PUT: "updateDevice"
		]
	}

	path("/subscriptions") {
		action: [
        	GET: "showSubscription"
		]
	}
	path("/subscription/:id") {
		action: [
        	GET: "showSubscription",
			POST: "subscribeToDevice",
			DELETE: "unsubscribeDevice"
		]
	}
	path("/subscribe/:id") {
		action: [
        	GET: "showSubscription",
			POST: "subscribeToDevice",
			DELETE: "unsubscribeDevice"
		]
	}

}

//

def allDevices() {
    def deviceMap = [:]
	def deviceList =    
	  [ accelerationSensor, actuator, alarm, battery, button, colorControl,
		carbonMonoxideDetector, configuration, contactSensor, energyMeter,
		illuminanceMeasurement, imageCapture, indicator, locationMode, lock,
		lockCodes, momentary, motionSensor, musicPlayer, polling, powerMeter,
		presenceSensor, refresh, relativeHumidity, sensor, signalStrength,
		smokeDetector, switches, switchLevel, temperatureMeasurement, tone,
		thermostat, threeAxis, valve, waterSensor
	  ].flatten()
	deviceList.retainAll( { it } )
    deviceList.unique( { it?.id } )
    deviceList.each{ deviceMap.putAt(it.id, it) }
	return deviceMap
}

def installed() {
	state.devices = [:]
    state.webhooks = [:]
	//state = allDevices()
    //state.devices.each { log.debug "$it" } 
	log.trace "Installed"
}

def updated() {
	state.devices = [:]
    state.webhooks = [:]
	//state = allDevices()
    //state.devices.each { log.debug "$it" } 
	log.trace "Updated"
}

def uninstalled() {
	log.trace "Uninstalled"
}



// this handler is subscribed with SmartThings to receive device events
// when called, it POSTs the event (as json) to the webhook subscribed for that device

def deviceHandler(evt) {
	if (!state.devices) {
    	state.devices = allDevices()
		log.debug "Updated device list"
    }
	def webhook = state.webhooks[evt.deviceId]
	if (webhook) {
		httpPostJson(uri: webhook, path: '', body: [evt: [value: evt.value]]) {
			log.debug "Event data posted"
		}
	} else {
		log.debug "Event handler called, but $evt.deviceId is not subscribed"
	}
}

// 

def showEvents() {
	log.debug "showEvents, request: ${request.JSON}, params: ${params}"
	if (!state.devices) {
    	state.devices = allDevices()
		log.debug "showEvents populated device list"
    }
    def json = new JsonBuilder( theDevice.events(params) )
	if (params.id) {
    	def theDevice = state.devices[params.id]
        if (theDevice) {
            json( theDevice.events(params) )
        }
        else {
            return httpError(404, "$params.id not found")
        }
    }
    return json.content
}

// 

def showSubscription() {
	log.debug "showSubscription, request: ${request.JSON}, params: ${params}"
	def json = new JsonBuilder()
	if (params.id) {
    	if (state.webhooks[params.id]) {
        	json.call( [ 'id' : params.id, 'url' : state.webhooks[params.id] ] )
        } else {
			return httpError(404, "$params.id not subscribed")
    	}        
    } else {
    	log.debug "webhooks: $state.webhooks"
		json.call( state.webhooks )
	}
    return json.content
}

//

def subscribeToDevice() {
	log.debug "subscribeToDevice, request: ${request.JSON}, params: ${params}"

	if (!state.devices) {
    	state.devices = allDevices()
		log.debug "subscribeToDevice populated device list"
    }
    if (params.id) {
        def theDevice = state?.devices[params.id]
        if (theDevice) {
            def callbackUrl = request.JSON?.url
            if ( state?.webhooks[theDevice.id] ) {
                log.debug "$device.displayName already subscribed, unsubscribing first"
                unsubscribe(theDevice)
            }

            log.debug "Subscribing $theDevice.displayName ($theDevice.id) to " + callbackUrl
            state?.webhooks[theDevice.id] = callbackUrl
            subscribe(theDevice, theDevice.supportedAttributes, deviceHandler)
        } else {
            return httpError(404, "$params.id not found")
        }
    } else {
    	return httpError(400, "a device ID is required")
    }
}

//

def unsubscribeDevice() {
	log.debug "unsubscribeDevice, request: ${request.JSON}, params: ${params}"

	if (params.id) {
        def webhook = state.webhooks[params.id]
        if (webhook) {
            log.debug "Unsubscribing $params.id"
            unsubscribe(theDevice)
            state.webhooks.remove(params.id)
        } else {
            return httpError(404, "$params.id not subscribed")
        }
    } else {
    	return httpError(400, "a device ID is required")
    }
}

//

def showDevice() {
	log.debug "showDevice, request: ${request.JSON}, params: ${params}"

	def returnValue
	if (!state.devices) {
    	state.devices = allDevices()
		log.debug "showDevice populated device list"
	}

	if (params.id) {
        def theDevice = state.devices[params.id]
        if (theDevice) {
		   	log.debug "(show device $params.id)"
            returnValue = [ theDevice.flatten(), theDevice.capabilities ]
        }
        else {
            return httpError(404, "$params.id not found")
        }
	} else {
	   	log.debug "(show all devices)"
		returnValue = state.devices.collect( { k,v -> v } )
    }
    log.debug returnValue.toString()
    return returnValue.toString()
}

def void updateDevice() {
	log.debug "updateDevice, request: ${request.JSON}, params: ${params}"
	
	if (!state.devices) {
    	state.devices = allDevices()
		log.debug "updateDevice populated device list"
    }
    def command = request.JSON?.command
	if (command) {
		def theDevice = state.devices[params.id]
		if (theDevice) {
			theDevice."$command"()
		} else {
			httpError(404, "$params.id not found")
		}
	}
}
