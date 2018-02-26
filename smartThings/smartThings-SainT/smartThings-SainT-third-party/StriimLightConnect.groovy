/**
 *  StriimLight Connect v 0.1
 *
 *  Author: SmartThings - Ulises Mujica - obycode
 */

definition(
	name: "StriimLight (Connect)",
	namespace: "obycode",
	author: "SmartThings - Ulises Mujica - obycode",
	description: "Allows you to control your StriimLight from the SmartThings app. Control the music and the light.",
	category: "SmartThings Labs",
	iconUrl: "http://obycode.com/img/icons/AwoxGreen.png",
  iconX2Url: "http://obycode.com/img/icons/AwoxGreen@2x.png"
)

preferences {
    page(name: "MainPage", title: "Find and config your StriimLights",nextPage:"", install:true, uninstall: true){
    	section("") {
            href(name: "discover",title: "Discovery process",required: false,page: "striimLightDiscovery", description: "tap to start searching")
        }
        section("Options", hideable: true, hidden: true) {
            input("refreshSLInterval", "number", title:"Enter refresh interval (min)", defaultValue:"5", required:false)
        }
    }
    page(name: "striimLightDiscovery", title:"Discovery Started!", nextPage:"")
}

def striimLightDiscovery()
{
	if(canInstallLabs())
	{
		int striimLightRefreshCount = !state.striimLightRefreshCount ? 0 : state.striimLightRefreshCount as int
		state.striimLightRefreshCount = striimLightRefreshCount + 1
		def refreshInterval = 5

		def options = striimLightsDiscovered() ?: []

		def numFound = options.size() ?: 0

		if(!state.subscribe) {
			subscribe(location, null, locationHandler, [filterEvents:false])
			state.subscribe = true
		}

		//striimLight discovery request every 5 //25 seconds
		if((striimLightRefreshCount % 8) == 0) {
			discoverstriimLights()
		}

		//setup.xml request every 3 seconds except on discoveries
		if(((striimLightRefreshCount % 1) == 0) && ((striimLightRefreshCount % 8) != 0)) {
			verifystriimLightPlayer()
		}

		return dynamicPage(name:"striimLightDiscovery", title:"Discovery Started!", nextPage:"", refreshInterval:refreshInterval) {
			section("Please wait while we discover your Striim Light. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
				input "selectedstriimLight", "enum", required:false, title:"Select Striim Light(s) (${numFound} found)", multiple:true, options:options
			}
		}
	}
	else
	{
		def upgradeNeeded = """To use SmartThings Labs, your Hub should be completely up to date.

		To update your Hub, access Location Settings in the Main Menu (tap the gear next to your location name), select your Hub, and choose "Update Hub"."""

		return dynamicPage(name:"striimLightDiscovery", title:"Upgrade needed!", nextPage:"", install:false, uninstall: true) {
			section("Upgrade") {
				paragraph "$upgradeNeeded"
			}
		}
	}
}

private discoverstriimLights()
{
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:DimmableLight:1", physicalgraph.device.Protocol.LAN))
}


private verifystriimLightPlayer() {
	def devices = getstriimLightPlayer().findAll { it?.value?.verified != true }

	devices.each {
		verifystriimLight((it?.value?.ip + ":" + it?.value?.port), it?.value?.ssdpPath)
	}
}

private verifystriimLight(String deviceNetworkId, String ssdpPath) {
	String ip = getHostAddress(deviceNetworkId)
	if(!ssdpPath){
		ssdpPath = "/"
	}

	sendHubCommand(new physicalgraph.device.HubAction("""GET $ssdpPath HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
	//sendHubCommand(new physicalgraph.device.HubAction("""GET /aw/DimmableLight_SwitchPower/scpd.xml HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}

Map striimLightsDiscovered() {
	def vstriimLights = getVerifiedstriimLightPlayer()
	def map = [:]
	vstriimLights.each {
		def value = "${it.value.name}"
		def key = it.value.ip + ":" + it.value.port
		map["${key}"] = value
	}
	map
}

def getstriimLightPlayer()
{
	state.striimLights = state.striimLights ?: [:]
}

def getVerifiedstriimLightPlayer()
{
	getstriimLightPlayer().findAll{ it?.value?.verified == true }
}

def installed() {
	initialize()}

def updated() {
	unschedule()
	initialize()
}

def uninstalled() {
	def devices = getChildDevices()
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}

def initialize() {
	// remove location subscription aftwards
	unsubscribe()
	state.subscribe = false

	unschedule()

	if (selectedstriimLight) {
		addstriimLight()
	}
    scheduleActions()
    scheduledRefreshHandler()
}

def scheduledRefreshHandler() {
	refreshAll()
}

def scheduledActionsHandler() {
    syncDevices()
	runIn(60, scheduledRefreshHandler)

}

private scheduleActions() {
	def minutes = Math.max(settings.refreshSLInterval.toInteger(),3)
    def cron = "0 0/${minutes} * * * ?"
   	schedule(cron, scheduledActionsHandler)
}



private syncDevices() {
	log.debug "syncDevices()"
	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	discoverstriimLights()
}

private refreshAll(){
	log.trace "refresh all"
	childDevices*.refresh()
}

def addstriimLight() {
	def players = getVerifiedstriimLightPlayer()
	def runSubscribe = false
	selectedstriimLight.each { dni ->
		def d = getChildDevice(dni)
		log.trace "dni $dni"
		if(!d) {
			def newLight = players.find { (it.value.ip + ":" + it.value.port) == dni }
			if (newLight){
				//striimLight
				d = addChildDevice("obycode", "Striim Light", dni, newLight?.value.hub, [label:"${newLight?.value.name} Striim Light","data":["model":newLight?.value.model,"dcurl":newLight?.value.dcurl,"deurl":newLight?.value.deurl,"spcurl":newLight?.value.spcurl,"speurl":newLight?.value.speurl,"xclcurl":newLight?.value.xclcurl,"xcleurl":newLight?.value.xcleurl,"xwlcurl":newLight?.value.xwlcurl,"xwleurl":newLight?.value.xwleurl,"udn":newLight?.value.udn,"dni":dni]])
			}
			runSubscribe = true
		}
	}
}

def locationHandler(evt) {
	def description = evt.description
	def hub = evt?.hubId
	def parsedEvent = parseEventMessage(description)
	def msg = parseLanMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:DimmableLight:1"))
	{ //SSDP DISCOVERY EVENTS
		log.debug "Striim Light device found" + parsedEvent
		def striimLights = getstriimLightPlayer()


		if (!(striimLights."${parsedEvent.ssdpUSN.toString()}"))
		{ //striimLight does not exist
			striimLights << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		}
		else
		{ // update the values

			def d = striimLights."${parsedEvent.ssdpUSN.toString()}"
			boolean deviceChangedValues = false
			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port) {
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				deviceChangedValues = true
			}
			if (deviceChangedValues) {
                def children = getChildDevices()
				children.each {
                    if (parsedEvent.ssdpUSN.toString().contains(it.getDataValue("udn"))) {
						it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port)) //could error if device with same dni already exists
						it.updateDataValue("dni", (parsedEvent.ip + ":" + parsedEvent.port))
						log.trace "Updated Device IP"
					}
				}
			}
		}
	}
	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:MediaRenderer:1"))
	{ //SSDP DISCOVERY EVENTS
		log.debug "in media renderer section!!!!"
	}
	else if (parsedEvent.headers && parsedEvent.body)
	{ // MEDIARENDER RESPONSES
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())

		def type = (headerString =~ /Content-Type:.*/) ? (headerString =~ /Content-Type:.*/)[0] : null
		def body
		if (bodyString?.contains("xml"))
		{ // description.xml response (application/xml)
			body = new XmlSlurper().parseText(bodyString)
			log.debug "got $body"

			// Find Awox devices
			if ( body?.device?.manufacturer?.text().startsWith("Awox") && body?.device?.deviceType?.text().contains("urn:schemas-upnp-org:device:DimmableLight:1"))
			{
				def dcurl = ""
				def deurl = ""
				def spcurl = ""
				def speurl = ""
				def xclcurl = ""
				def xcleurl = ""
				def xwlcurl = ""
				def xwleurl = ""

				body?.device?.serviceList?.service?.each {
					if (it?.serviceType?.text().contains("Dimming")) {
						dcurl = it?.controlURL.text()
						deurl = it?.eventSubURL.text()
					}
					else if (it?.serviceType?.text().contains("SwitchPower")) {
						spcurl = it?.controlURL.text()
						speurl = it?.eventSubURL.text()
					}
					else if (it?.serviceType?.text().contains("X_ColorLight")) {
						xclcurl = it?.controlURL.text()
						xcleurl = it?.eventSubURL.text()
					}
					else if (it?.serviceType?.text().contains("X_WhiteLight")) {
						xwlcurl = it?.controlURL.text()
						xwleurl = it?.eventSubURL.text()
					}
				}


				def striimLights = getstriimLightPlayer()
				def player = striimLights.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (player)
				{
					player.value << [name:body?.device?.friendlyName?.text(),model:body?.device?.modelName?.text(), serialNumber:body?.device?.UDN?.text(), verified: true,dcurl:dcurl,deurl:deurl,spcurl:spcurl,speurl:speurl,xclcurl:xclcurl,xcleurl:xcleurl,xwlcurl:xwlcurl,xwleurl:xwleurl,udn:body?.device?.UDN?.text()]
				}

			}
		}
		else if(type?.contains("json"))
		{ //(application/json)
			body = new groovy.json.JsonSlurper().parseText(bodyString)
		}
	}
}

private def parseEventMessage(Map event) {
	//handles striimLight attribute events
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

		def body = new groovy.json.JsonSlurper().parseText(bodyString)
	} else {
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
