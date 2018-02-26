/**
 *  Advanced Learning Automatic Response Machine (ALARM) Service Manager (based on SONOS example)
 *
 *  Copyright 2014 Robert A. Pickering Jr. & Todd M. Whitehead
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
definition(
    name: "ALARM Controller",
    namespace: "pickerin",
    author: "Robert A. Pickering Jr. & Todd M. Whitehead",
    description: "ALARM Controller Service Manager",
    category: "Safety & Security",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.security.alarm.on",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.security.alarm.on?displaySize=2x")


preferences {
    page(name:"alarmControllerDiscovery", title:"ALARM Controller Setup", content:"alarmControllerDiscovery", refreshTimeout:5)
/*
    section("For too long...") {
        input "maxOpenTime", "number", title: "Minutes?"
    }
    section("Text me at (optional, sends a push notification if not specified)...") {
        input "phone", "phone", title: "Phone number?", required: false
    }*/
}

def pollingTask() {
    log.debug "Polling"
    def devices = getChildDevices()
    devices.each {
        it.poll()
        
        def leftDoor= it.latestState("leftDoor");
        def rightDoor= it.latestState("rightDoor");
        
        def leftValue= leftDoor.value
        def rightValue= rightDoor.value
                        
        if( leftValue != "closed")
        {
            def deltaMillis = 1000 * 60 * 30//maxOpenTime
            def timeAgo = new Date(now() - deltaMillis)
            
            def openTooLong = leftDoor.dateCreated.toSystemDate() < timeAgo
        
            if( openTooLong)
            {
                sendTextMessage()               
            }
        }
        else if( rightValue != "closed")
        {
            def deltaMillis = 1000 * 60 * 30//maxOpenTime
            def timeAgo = new Date(now() - deltaMillis)
            
            def openTooLong = rightValue.dateCreated.toSystemDate() < timeAgo
        
            if( openTooLong)
            {
                sendTextMessage()               
            }
        }
        else
        {
            state.sentMessage= null
        }
    }
}

def sendTextMessage() {

    if (!state.sentMessage)
    {
        log.debug "Garage open too long, texting $phone"

        state.sentMessage= true;

        def msg = "Garage has been open for more than 30 minutes!"
        
        if (phone) {
            sendSms(phone, msg)
        }
        else {
            sendPush msg
        }
    }
}

def alarmControllerDiscovery()
{
    if(true)
    {
        int discoveryRefreshCount = !state.discoveryRefreshCount ? 0 : state.discoveryRefreshCount as int
        state.discoveryRefreshCount = discoveryRefreshCount + 1
        def refreshInterval = 3

        def options = alarmControllersDiscovered() ?: []

        def numFound = options.size() ?: 0

        if(!state.subscribe) {
            log.trace "subscribe to location"
            subscribe(location, null, locationHandler, [filterEvents:false])
            state.subscribe = true
        }

        // discovery request every 5 //25 seconds
        if((discoveryRefreshCount % 8) == 0) {
            discoverAlarmControllers()
        }

        //json profile request every 3 seconds except on discoveries
        if(((discoveryRefreshCount % 1) == 0) && ((discoveryRefreshCount % 8) != 0)) {
            verifyAlarmController()
        }

        return dynamicPage(name:"alarmControllerDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
            section("Please wait while we discover your AlarmController. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
                input "selectedSmartFace", "enum", required:false, title:"Select Alarm Controller (${numFound} found)", multiple:true, options:options
            }
        }
    }
    else
    {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"alarmControllerDiscovery", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

private verifyAlarmController() {
    def devices = getSmartFace().findAll { it?.value?.verified != true }

    if(devices) {
        log.warn "UNVERIFIED CONTROLLERS!: $devices"
    }

    devices.each {
        log.warn (it?.value)
        log.warn (it?.value?.ip + ":" + it?.value?.port)
        verifyAlarmControllers((it?.value?.ip + ":" + it?.value?.port))
    }
}

private verifyAlarmControllers(String deviceNetworkId) {

    log.trace "dni: $deviceNetworkId"
    String ip = getHostAddress(deviceNetworkId)

    log.trace "ip:" + ip

    sendHubCommand(new physicalgraph.device.HubAction("""GET /AlarmController/1 HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

private discoverAlarmControllers()
{
    //consider using other discovery methods

    log.debug("Sending lan discovery urn:schemas-upnp-org:device:AlarmController:1")
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:AlarmController:1", physicalgraph.device.Protocol.LAN))
}

Map alarmControllersDiscovered() {
    def v = getVerifiedAlarmControllers()
        log.trace "getVerifiedAlarmControllers"
        log.trace v
    def map = [:]
    v.each {
        def value = "${it.value.name}"
        def key = it.value.ip + ":" + it.value.port
        map["${key}"] = value
        log.trace key 
    }
    map
}

def getSmartFace()
{
    state.smartFace = state.smartFace ?: [:]
}

def getVerifiedAlarmControllers()
{
    getSmartFace()
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unschedule()
    initialize()
}


def uninstalled() {
    def devices = getChildDevices()
    log.trace "deleting ${devices.size()} AlarmController"
    devices.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

def initialize() {

    unsubscribe()
    state.subscribe = false

    unschedule()
    scheduleActions()

    if (selectedSmartFace) {
        addSmartFace()
    }

    scheduledActionsHandler()


    def minutes = 1//settings.interval.toInteger()
    if (minutes > 0) {
        // Schedule polling daemon to run every N minutes
        log.trace "Scheduling polling daemon to run every ${minutes} minutes."
        schedule("0 0/${minutes} * * * ?", pollingTask)
    }
}

def scheduledActionsHandler() {
    log.trace "scheduledActionsHandler()"
    syncDevices()
    refreshAll()

    // TODO - for auto reschedule
    if (!state.threeHourSchedule) {
        scheduleActions()
    }
}

private scheduleActions() {
    def sec = Math.round(Math.floor(Math.random() * 60))
    def min = Math.round(Math.floor(Math.random() * 60))
    def hour = Math.round(Math.floor(Math.random() * 3))
    def cron = "$sec $min $hour/3 * * ?"
    log.debug "schedule('$cron', scheduledActionsHandler)"
    schedule(cron, scheduledActionsHandler)

    // TODO - for auto reschedule
    state.threeHourSchedule = true
    state.cronSchedule = cron
}

private syncDevices() {
    log.trace "Doing smartFace Device Sync!"
    //runIn(300, "doDeviceSync" , [overwrite: false]) //schedule to run again in 5 minutes

    if(!state.subscribe) {
        subscribe(location, null, locationHandler, [filterEvents:false])
        state.subscribe = true
    }

    discoverAlarmControllers()
}

private refreshAll(){
    log.trace "refreshAll()"
    childDevices*.refresh()
    log.trace "/refreshAll()"
}

def addSmartFace() {
    def players = getVerifiedAlarmControllers()
    def runSubscribe = false
    selectedSmartFace.each { dni ->
        def d = getChildDevice(dni)
        if(!d) {
            def newPlayer = players.find { (it.value.ip + ":" + it.value.port) == dni }
            log.trace "newPlayer = $newPlayer"
            log.trace "dni = $dni"
            d = addChildDevice("lttlrck", "Alarm Controller", dni, newPlayer?.value.hub, [label:"${newPlayer?.value.name} AlarmController"])
            log.trace "created ${d.displayName} with id $dni"

            d.setModel(newPlayer?.value.model)
            log.trace "setModel to ${newPlayer?.value.model}"

            runSubscribe = true
        } else {
            log.trace "found ${d.displayName} with id $dni already exists"
        }
    }
}

def locationHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId

    def parsedEvent = parseEventMessage(description)
    parsedEvent << ["hub":hub]

//        log.trace "evt"+evt
    log.trace parsedEvent

    if (parsedEvent?.ssdpTerm?.contains("lttlrck:AlarmController"))
    { //SSDP DISCOVERY EVENTS

//    state.smartFace= [:]

        log.trace "smartFace found:"+parsedEvent?.ssdpTerm
        def smartFace = getSmartFace()

        if (!(smartFace."${parsedEvent.ssdpUSN.toString()}"))
        { //smartFace does not exist
            smartFace << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
  log.trace "smartFace:"+ smartFace
        }
        else
        { // update the values

            log.trace "Device was already found in state..."

            def d = smartFace."${parsedEvent.ssdpUSN.toString()}"
            boolean deviceChangedValues = false

            if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port
                deviceChangedValues = true
                log.trace "Device's port or ip changed..."
            }

            if (deviceChangedValues) {
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        log.trace "updating dni for device ${it} with mac ${parsedEvent.mac}"
                        it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port)) //could error if device with same dni already exists
                    }
                }
            }
        }
    }
    else if (parsedEvent.headers && parsedEvent.body)
    { // RESPONSES
        def headerString = new String(parsedEvent.headers.decodeBase64())
        def bodyString = new String(parsedEvent.body.decodeBase64())

        def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null
        def body
        log.trace "REPONSE TYPE: $type"
        log.trace "BODY TYPE: $bodyString"
        if (type?.contains("json"))
        {
            body = new groovy.json.JsonSlurper().parseText(bodyString)

            if (body?.device?.modelName.startsWith("lttlrck"))
            {
                def sonoses = getSmartFace()

                def player = sonoses.find {it?.key?.contains(body?.device?.key)}
                if (player)
                {
                    player.value << [name:body?.device?.name,model:body?.device?.modelName, serialNumber:body?.device?.serialNum, verified: true]
                }
                else
                {
                    log.error "/xml/device_description.xml returned a device that didn't exist"
                }
            }
        }
        else if(type?.contains("json"))
        { //(application/json)
            body = new groovy.json.JsonSlurper().parseText(bodyString)
            log.trace "GOT JSON $body"
        }

    }
    else {
        log.trace "cp desc: " + description
        //log.trace description
    }
}

private def parseEventMessage(Map event) {
    //handles smartFace attribute events
    return event
}

private def parseEventMessage(String description) {
    def event = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            event.devicetype = valueString
        }
        else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.mac = valueString
            }
        }
        else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.ip = valueString
            }
        }
        else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.port = valueString
            }
        }
        else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                event.ssdpPath = valueString
            }
        }
        else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpUSN = valueString
            }
        }
        else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                event.ssdpTerm = valueString
            }
        }
        else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                event.headers = valueString
            }
        }
        else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                event.body = valueString
            }
        }
    }

    event
}


/////////CHILD DEVICE METHODS
def parse(childDevice, description) {
    def parsedEvent = parseEventMessage(description)

    if (parsedEvent.headers && parsedEvent.body) {
        def headerString = new String(parsedEvent.headers.decodeBase64())
        def bodyString = new String(parsedEvent.body.decodeBase64())
        log.trace "parse() - ${bodyString}"

        def body = new groovy.json.JsonSlurper().parseText(bodyString)
    } else {
        log.trace "parse - got something other than headers,body..."
        return []
    }
}

private Integer convertHexToInt(hex) {
    Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress(d) {
    def parts = d.split(":")
    def ip = convertHexToIP(parts[0])
    def port = convertHexToInt(parts[1])
    return ip + ":" + port
}

private Boolean canInstallLabs()
{
    return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
    return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
    return location.hubs*.firmwareVersionString.findAll { it }
}


