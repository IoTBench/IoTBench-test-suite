/**
 *  upnp discovery
 *
 *  Copyright 2014 Josh Bohde
 *
 */
definition(
  name: "UPnP Media Discovery",
  namespace: "joshbohde",
  author: "Josh Bohde",
  description: "Discover UPnP Media Players (e.g, kodi) on the network",
  category: "Convenience",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
  iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
  page(name:"discovery", title:"Media Player Discovery", content:"discover", refreshTimeout:5, install: true, uninstall: true)
}


def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  initialize()
}

def uninstalled() {
	def devices = getChildDevices()
	devices.each {
		deleteChildDevice(it.deviceNetworkId)
	}
}


def initialize() {
  log.debug "Initialized"
  unsubscribe()
  state.subscribe = false

  if(selectedMediaPlayer) {
    addMediaPlayer()
  }
}

def addMediaPlayer(){
  selectedMediaPlayer.each {
    def d = getPrettyDevices()[it]
    def dni = d.ip + ":" + d.port
    def found = getChildDevice(dni)
    if(!found){
      def child = addChildDevice("joshbohde", "UPnP Media Player", dni, d.hub, [label:d.name, completedSetup: true])
      child.initChild(d)
    }
  }
}


def discover() {
  int refreshCount = !state.bridgeRefreshCount ? 0 : state.bridgeRefreshCount as int
  state.bridgeRefreshCount = refreshCount + 1
  def refreshInterval = 3

  def options = getOptions()
  def numFound = options.size() ?: 0

  if(!state.subscribe) {
    subscribe(location, null, locationHandler, [filterEvents:false])
    state.subscribe = true
  }

  // Television discovery request every 15 seconds
  if((refreshCount % 5) == 0) {
    find()
  }

  return dynamicPage(name:"discovery", title:"Media Player Discovery Started!", refreshInterval:refreshInterval, uninstall: true) {
    section("Please wait while we discover your media players. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
      input "selectedMediaPlayer", "enum", required:true, title:"Select Media Player (${numFound} found)", multiple:true, options:options
    }
  }
}

Map getOptions(){
  def map = [:]
  getPrettyDevices().each {
		def key = it.key
		def value = "${it.value.name}"
		map["${key}"] = value

  }
  map
}


def find(){
  sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:MediaRenderer:1", physicalgraph.device.Protocol.LAN))
}

def service(m){
  [
    "type": m.serviceType.text(),
    "scdp": m.SCPDURL.text(),
    "control": m.controlURL.text(),
    "events": m.eventSubURL.text()
   ]
}

def locationHandler(evt) {
  def description = evt.description
  def hub = evt?.hubId

  def parsedEvent = parseEventMessage(description)
  parsedEvent << ["hub":hub]
  log.debug(parsedEvent)

  if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:MediaRenderer:1")){
      def devices = getDevices()

      if (!(devices."${parsedEvent.ssdpUsn.toString()}")){
        devices << ["${parsedEvent.ssdpUsn.toString()}":parsedEvent]
      }
      get(parsedEvent.ip, parsedEvent.port, parsedEvent.ssdpPath)
  }
  else if (parsedEvent.headers && parsedEvent.body){

    def headerString = new String(parsedEvent.headers.decodeBase64())
    def bodyString = new String(parsedEvent.body.decodeBase64())
    def body = new XmlSlurper().parseText(bodyString)

    def udn = body?.device?.UDN?.text()
    def device = getDevices().find { it.value.ssdpUsn?.contains(udn) }?.value

    device += ["id": udn,
               "name": body?.device?.friendlyName.text(),
               "services": body?.device?.serviceList?.service?.list()?.collect { service(it) }
              ]
    getPrettyDevices() << [(udn): device]
  }

}

def getDevices()
{
  if (!state.devices) { state.devices = [:] }
  state.devices
}

def getPrettyDevices(){
  if (!state.prettyDevices) { state.prettyDevices = [:] }
  state.prettyDevices
}


private def parseEventMessage(Map event) {
  log.debug(event)
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
        event.ssdpUsn = valueString
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


private get(String ip, String port, String path) {

  def port_ = convertHexToInt(port)
  def ip_ = convertHexToIP(ip)

  log.trace("get: $ip:$port$path")
  sendHubCommand(new physicalgraph.device.HubAction("""GET $path HTTP/1.1\r\nHOST: $ip_:$port_\r\n\r\n""", physicalgraph.device.Protocol.LAN, "$ip:$port"))
}

private Integer convertHexToInt(hex) {
  Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
