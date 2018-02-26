/**
 *  Wemo Insight Connect
 *
 *  Copyright 2014 Nicolas Cerveaux
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
    name: "Wemo Insight Connect",
    namespace: "wemo",
    author: "Nicolas Cerveaux",
    description: "Allows you to integrate your WeMo Insight Switch with SmartThings.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/wemo.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/wemo@2x.png"
) {
    appSetting "debug"
}

preferences {
    page(name:"firstPage", title:"Wemo Insight Setup", content:"firstPage")
}

private debug(data) {
    if(appSettings.debug == "true"){
        log.debug(data)
    }
}

private discoverAllWemoTypes() {
    sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:Belkin:device:insight:1", physicalgraph.device.Protocol.LAN))
}

private getFriendlyName(String deviceNetworkId) {
    sendHubCommand(new physicalgraph.device.HubAction("""GET /setup.xml HTTP/1.1
HOST: ${deviceNetworkId}

""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

private verifyDevices() {
    def insightSwitches = getWemoInsightSwitches().findAll { it?.value?.verified != true }
    insightSwitches.each {
        getFriendlyName((it.value.ip + ":" + it.value.port))
    }
}

def firstPage() {
    if(canInstallLabs()) {
        int refreshCount = !state.refreshCount ? 0 : state.refreshCount as int
        state.refreshCount = refreshCount + 1
        def refreshInterval = 5

        debug("REFRESH COUNT :: ${refreshCount}")

        if(!state.subscribe) {
            // subscribe to answers from HUB
            subscribe(location, null, locationHandler, [filterEvents:false])
            state.subscribe = true
        }

        //ssdp request every 25 seconds
        if((refreshCount % 5) == 0) {
            discoverAllWemoTypes()
        }

        //setup.xml request every 5 seconds except on discoveries
        if(((refreshCount % 1) == 0) && ((refreshCount % 5) != 0)) {
            verifyDevices()
        }

        def insightSwitchesDiscovered = insightSwitchesDiscovered()

        return dynamicPage(name:"firstPage", title:"Discovery Started!", nextPage:"", refreshInterval: refreshInterval, install:true, uninstall: selectedSwitches != null || selectedMotions != null || selectedLightSwitches != null) {
            section("Select a device...") {
                input "selectedInsightSwitches", "enum", required:false, title:"Select Insight Switches \n(${insightSwitchesDiscovered.size() ?: 0} found)", multiple:true, options:insightSwitchesDiscovered
            }
        }
    } else {
        def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

        return dynamicPage(name:"firstPage", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
            section("Upgrade") {
                paragraph "$upgradeNeeded"
            }
        }
    }
}

def devicesDiscovered() {
    def insightSwitches = getWemoInsightSwitches()
    def list = []

    list = insightSwitches?.collect{ [app.id, it.ssdpUSN].join('.') }
}

def insightSwitchesDiscovered() {
    debug("Dicovered insight switches")
    def insightSwitches = getWemoInsightSwitches().findAll { it?.value?.verified == true }
    def map = [:]
    insightSwitches.each {
        def value = it.value.name ?: "WeMo Insight Switch ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
        def key = it.value.mac
        map["${key}"] = value
    }
    map
}

def getWemoInsightSwitches() {
    if (!state.insightSwitches) { state.insightSwitches = [:] }
    state.insightSwitches
}

def installed() {
    debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    debug "Updated with settings: ${settings}"
    initialize()
}

def resubscribe() {
    refresh()
}

def refresh() {
    refreshDevices()
}

def refreshDevices() {
    def devices = getAllChildDevices()
    devices.each { d ->
        d.refresh()
    }
}

def subscribeToDevices() {
    debug("subscribeToDevices() called")
    def devices = getAllChildDevices()
    devices.each { d ->
        debug('Call subscribe on '+d.id)
        d.subscribe()
    }
}

def addInsightSwitches() {
    def insightSwitches = getWemoInsightSwitches()

    selectedInsightSwitches.each { dni ->
        def selectedInsightSwitch = insightSwitches.find { it.value.mac == dni } ?: insightSwitches.find { "${it.value.ip}:${it.value.port}" == dni }

        def d
        if (selectedInsightSwitch) {
            d = getChildDevices()?.find {
                it.dni == selectedInsightSwitch.value.mac || it.device.getDataValue("mac") == selectedInsightSwitch.value.mac
            }
        }

        if (!d) {
            d = addChildDevice("wemo", "Wemo Insight Switch", selectedInsightSwitch.value.mac, selectedInsightSwitch?.value.hub, [
                "label": selectedInsightSwitch?.value?.name ?: "Wemo Insight Switch",
                "data": [
                    "mac": selectedInsightSwitch.value.mac,
                    "ip": selectedInsightSwitch.value.ip,
                    "port": selectedInsightSwitch.value.port
                ]
            ])
        }
    }
}

def initialize() {
    // remove location subscription afterwards
    unsubscribe()
    state.subscribe = false
    
    if (selectedInsightSwitches) {
        addInsightSwitches()
    }
    
    // run once subscribeToDevices
    subscribeToDevices()
    
    //setup cron jobs
    schedule("10 * * * * ?", "subscribeToDevices")
}

def locationHandler(evt) {
    if(evt.name == "ping") {
        return ""
    }
    
    def description = evt.description
    def hub = evt?.hubId
    def parsedEvent = parseDiscoveryMessage(description)
    parsedEvent << ["hub":hub]
    
    if (parsedEvent?.ssdpTerm?.contains("Belkin:device:insight")) {
        def insightSwitches = getWemoInsightSwitches()

        if (!(insightSwitches."${parsedEvent.ssdpUSN.toString()}")) { //if it doesn't already exist
            insightSwitches << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
        } else { // just update the values
            def d = insightSwitches."${parsedEvent.ssdpUSN.toString()}"
            boolean deviceChangedValues = false

            if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
                d.ip = parsedEvent.ip
                d.port = parsedEvent.port
                deviceChangedValues = true
            }

            if (deviceChangedValues) {
                def children = getChildDevices()
                children.each {
                    if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
                        it.subscribe(parsedEvent.ip, parsedEvent.port)
                    }
                }
            }

        }
    } else if (parsedEvent.headers && parsedEvent.body) {
        def headerString = new String(parsedEvent.headers.decodeBase64())
        def bodyString = new String(parsedEvent.body.decodeBase64())
        def body = new XmlSlurper().parseText(bodyString)

        if (body?.device?.deviceType?.text().startsWith("urn:Belkin:device:insight")) {
            def insightSwitches = getWemoInsightSwitches()
            def wemoInsigthSwitch = insightSwitches.find {it?.key?.contains(body?.device?.UDN?.text())}
            if (wemoInsigthSwitch) {
                wemoInsigthSwitch.value << [name:body?.device?.friendlyName?.text(), verified: true]
            } else {
                log.error "/setup.xml returned a wemo device that didn't exist"
            }
        }
    }
}

private def parseDiscoveryMessage(String description) {
    def device = [:]
    def parts = description.split(',')
    parts.each { part ->
        part = part.trim()
        if (part.startsWith('devicetype:')) {
            def valueString = part.split(":")[1].trim()
            device.devicetype = valueString
        } else if (part.startsWith('mac:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.mac = valueString
            }
        } else if (part.startsWith('networkAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ip = valueString
            }
        } else if (part.startsWith('deviceAddress:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.port = valueString
            }
        } else if (part.startsWith('ssdpPath:')) {
            def valueString = part.split(":")[1].trim()
            if (valueString) {
                device.ssdpPath = valueString
            }
        } else if (part.startsWith('ssdpUSN:')) {
            part -= "ssdpUSN:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpUSN = valueString
            }
        } else if (part.startsWith('ssdpTerm:')) {
            part -= "ssdpTerm:"
            def valueString = part.trim()
            if (valueString) {
                device.ssdpTerm = valueString
            }
        } else if (part.startsWith('headers')) {
            part -= "headers:"
            def valueString = part.trim()
            if (valueString) {
                device.headers = valueString
            }
        } else if (part.startsWith('body')) {
            part -= "body:"
            def valueString = part.trim()
            if (valueString) {
                device.body = valueString
            }
        }
    }

    device
}

private Boolean canInstallLabs() {
    return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware) {
    return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions() {
    return location.hubs*.firmwareVersionString.findAll { it }
}
