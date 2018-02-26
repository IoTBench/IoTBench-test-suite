/**
 *  ThingSpeak Logger
 *
 *  Author: florianz
 *  Date: 2013-11-27
 *
 *
 *  Create a ThingSpeak channel with a write key. The app must be given the channel id and key.
 *  Then, create a field for each device and name the field according to the label given to the
 *  device in SmartThings.
 *
 */
preferences {
	log.debug "[preferences]"
    section("Log devices...") {
        input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
        input "contacts", "capability.contactSensor", title: "Contacts", required: false, multiple: true
        input "accelerations", "capability.accelerationSensor", title: "Accelerations", required: false, multiple: true
        input "motions", "capability.motionSensor", title: "Motions", required: false, multiple: true
        input "switches", "capability.switch", title: "Switches", required: false, multiple: true
    }
    
    section ("ThinkSpeak channel id...") {
        input "channelId", "number", title: "Channel id"
    }
    
    section ("ThinkSpeak write key...") {
        input "channelKey", "text", title: "Channel key"
    }
}

def installed() {
	log.debug "[installed]"
    initialize()
}

def updated() {
	log.debug "[updated]"
    unsubscribe()
    initialize()
}

def initialize() {
	log.debug "[initialize]"
    subscribe(temperatures, "temperature", handleTemperatureEvent)
    subscribe(contacts, "contact", handleContactEvent)
    subscribe(accelerations, "acceleration", handleAccelerationEvent)
    subscribe(motions, "motion", handleMotionEvent)
    subscribe(switches, "switch", handleSwitchEvent)
    
    updateChannelInfo()
    log.debug state.fieldMap
}

def handleTemperatureEvent(evt) {
	log.debug "[handleTemperatureEvent]"
    logField(evt) { it.toString() }
}

def handleContactEvent(evt) {
	log.debug "[handleContactEvent]"
    logField(evt) { it == "open" ? "1" : "0" }
}

def handleAccelerationEvent(evt) {
	log.debug "[handleAccelerationEvent]"
    logField(evt) { it == "active" ? "1" : "0" }
}

def handleMotionEvent(evt) {
	log.debug "[handleMotionEvent]"
    logField(evt) { it == "active" ? "1" : "0" }
}

def handleSwitchEvent(evt) {
	log.debug "[handleSwitchEvent]"
    logField(evt) { it == "on" ? "1" : "0" }
}

private getFieldMap(channelInfo) {
	log.debug "[getFieldMap]"
    def fieldMap = [:]
    channelInfo?.findAll { it.key?.startsWith("field") }.each { fieldMap[it.value?.trim()] = it.key }
    return fieldMap
}

private updateChannelInfo() {
	log.debug "[updateChannelInfo]"
    log.debug "Retrieving channel info for ${channelId} with key ${channelKey}"
    
    def url = "http://api.thingspeak.com/channels/${channelId}/feed.json?key=${channelKey}&results=0"
    httpGet(url) {
        response ->
        if (response.status != 200 ) {
            log.debug "ThingSpeak data retrieval failed, status = ${response.status}"
        } else {
            state.channelInfo = response.data?.channel
            log.debug "Channel info: ${state.channelInfo}"
        }
    }
    
    state.fieldMap = getFieldMap(state.channelInfo)
    log.debug "Field map: ${state.fieldMap}"
}

private logField(evt, Closure c) {
	log.debug "[logField]"
    def deviceName = evt.displayName.trim()
    def fieldNum = state.fieldMap[deviceName]
    if (!fieldNum) {
        log.debug "Device '${deviceName}' has no field"
        return
    }
    
    def value = c(evt.value)
    log.debug "Logging to channel ${channelId}, ${fieldNum}, value ${value}"

    def url = "http://api.thingspeak.com/update?key=${channelKey}&${fieldNum}=${value}"
    httpGet(url) { 
        response -> 
        if (response.status != 200 ) {
            log.debug "ThingSpeak logging failed, status = ${response.status}"
        }
    }
}