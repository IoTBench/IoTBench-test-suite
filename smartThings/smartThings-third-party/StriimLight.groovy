/**
*  Striim Light v0.1
*
*  Author: SmartThings - Ulises Mujica (Ule) - obycode
*
*/

preferences {
  input(name: "customDelay", type: "enum", title: "Delay before msg (seconds)", options: ["0","1","2","3","4","5"])
  input(name: "actionsDelay", type: "enum", title: "Delay between actions (seconds)", options: ["0","1","2","3"])
}
metadata {
  // Automatically generated. Make future change here.
  definition (name: "Striim Light", namespace: "obycode", author: "SmartThings-Ulises Mujica") {
    capability "Actuator"
    capability "Switch"
    capability "Switch Level"
    capability "Color Control"
    capability "Refresh"
    capability "Sensor"
    capability "Polling"

    attribute "model", "string"
    attribute "temperature", "number"
    attribute "brightness", "number"
    attribute "lightMode", "string"

    command "setColorHex" "string"
    command "cycleColors"
    command "setTemperature"
    command "White"

    command "subscribe"
    command "unsubscribe"
  }

  // Main
  standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
    state "on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#79b821", nextState:"off"
    state "off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"on"
  }
  standardTile("reset", "device.reset", inactiveLabel: false, decoration: "flat") {
    state "default", label:"Reset Color", action:"reset", icon:"st.lights.philips.hue-single"
  }
  standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
    state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
  }
  standardTile("cycleColors", "device.color", inactiveLabel: false, decoration: "flat") {
    state "default", label:"Colors", action:"cycleColors", icon:"st.unknown.thing.thing-circle"
  }
  standardTile("dimLevel", "device.level", inactiveLabel: false, decoration: "flat") {
    state "default", label:"Dimmer Level", icon:"st.switches.light.on"
  }

  controlTile("rgbSelector", "device.color", "color", height: 3, width: 3, inactiveLabel: false) {
    state "color", action:"color control.setColor"
  }
  controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, range:"(0..100)") {
    state "level", label:"Dimmer Level", action:"switch level.setLevel"
  }
  controlTile("tempSliderControl", "device.temperature", "slider", height: 1, width: 2, inactiveLabel: false, range:"(0..100)") {
    state "temperature", label:"Temperature", action:"setTemperature"
  }
  valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
    state "level", label: 'Level ${currentValue}%'
  }
  valueTile("lightMode", "device.lightMode", inactiveLabel: false, decoration: "flat") {
    state "lightMode", label: 'Mode: ${currentValue}', action:"White"
  }

  main(["switch"])
  details(["switch", "levelSliderControl", "tempSliderControl", "rgbSelector", "refresh", "cycleColors", "level", "lightMode"])
}


def parse(description) {
  def results = []
  try {
    def msg = parseLanMessage(description)
    if (msg.headers) {
      def hdr = msg.header.split('\n')[0]
      if (hdr.size() > 36) {
        hdr = hdr[0..35] + "..."
      }

      def sid = ""
      if (msg.headers["SID"]) {
        sid = msg.headers["SID"]
        sid -= "uuid:"
        sid = sid.trim()

      }

      if (!msg.body) {
        if (sid) {
          updateSid(sid)
        }
      }
      else if (msg.xml) {
        // log.debug "received $msg"
        // log.debug "${msg.body}"

        // Process level status
        def node = msg.xml.Body.GetLoadLevelTargetResponse
        if (node.size()) {
          return sendEvent(name: "level", value: node, description: "$device.displayName Level is $node")
        }

        // Process subscription updates
        // On/Off
        node = msg.xml.property.Status
        if (node.size()) {
          def statusString = node == 0 ? "off" : "on"
          return sendEvent(name: "switch", value: statusString, description: "$device.displayName Switch is $statusString")
        }

        // Level
        node = msg.xml.property.LoadLevelStatus
        if (node.size()) {
          return sendEvent(name: "level", value: node, description: "$device.displayName Level is $node")
        }

        // TODO: Do something with Temperature, Brightness and Mode in the UI
        // Temperature
        node = msg.xml.property.Temperature
        if (node.size()) {
          def temp = node.text().toInteger()
          if (temp > 4000) {
            temp -= 4016
          }
          def result = []
          result << sendEvent(name: "temperature", value: temp, description: "$device.displayName Temperature is $temp")
          result << sendEvent(name: "lightMode", value: "White", description: "$device.displayName Mode is White")
          return result
        }

        // brightness
        node = msg.xml.property.Brightness
        if (node.size()) {
          return sendEvent(name: "brightness", value: node, description: "$device.displayName Brightness is $node")
        }

        // Mode?
        // node = msg.xml.property.CurrentMode
        // if (node.size()) {
        // }

        // Color
        try {
          def bodyXml = parseXml(msg.xml.text())
          node = bodyXml.RGB
          if (node.size()) {
            def fields = node.text().split(',')
            def colorHex = '#' + String.format('%02x%02x%02x', fields[0].toInteger(), fields[1].toInteger(), fields[2].toInteger())
            def result = []
            result << sendEvent(name: "color", value: colorHex, description:"$device.displayName Color is $colorHex")
            result << sendEvent(name: "lightMode", value: "Color", description: "$device.displayName Mode is Color")
            return result
          }
        }
        catch (org.xml.sax.SAXParseException e) {
          // log.debug "${msg.body}"
        }

        // Not sure what this is for?
        if (!results) {
          def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
          .replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
          .replaceAll('\n\n','\n').encodeAsHTML() : ""
          results << createEvent(
            name: "mediaRendererMessage",
            value: "${msg.body.encodeAsMD5()}",
            description: description,
            descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
            data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
            isStateChange: false, displayed: false)
        }
      }
      else {
        def bodyHtml = msg.body ? msg.body.replaceAll('(<[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
        .replaceAll('(</[a-z,A-Z,0-9,\\-,_,:]+>)','\n$1\n')
        .replaceAll('\n\n','\n').encodeAsHTML() : ""
        results << createEvent(
          name: "unknownMessage",
          value: "${msg.body.encodeAsMD5()}",
          description: description,
          descriptionText: "Body is ${msg.body?.size() ?: 0} bytes",
          data: "<pre>${msg.headers.collect{it.key + ': ' + it.value}.join('\n')}</pre><br/><pre>${bodyHtml}</pre>",
          isStateChange: true, displayed: true)
      }
    }
  }
  catch (Throwable t) {
    //results << createEvent(name: "parseError", value: "$t")
    sendEvent(name: "parseError", value: "$t", description: description)
    throw t
  }
  results
}

def installed() {
  sendEvent(name:"model",value:getDataValue("model"),isStateChange:true)
  def result = [delayAction(5000)]
  result << refresh()
  result.flatten()
}

def on(){
  dimmableLightAction("SetTarget", "SwitchPower", getDataValue("spcurl"), [newTargetValue: 1])
}

def off(){
  dimmableLightAction("SetTarget", "SwitchPower", getDataValue("spcurl"), [newTargetValue: 0])
}

def poll() {
  refresh()
}

def refresh() {
  def eventTime = new Date().time

  if(eventTime > state.secureEventTime ?:0) {
    if ((state.lastRefreshTime ?: 0) > (state.lastStatusTime ?:0)) {
      sendEvent(name: "status", value: "no_device_present", data: "no_device_present", displayed: false)
    }
    state.lastRefreshTime = eventTime
    log.trace "Refresh()"
    def result = []
    result << subscribe()
    result << getCurrentLevel()
    result.flatten()
  }
  else {
    log.trace "Refresh skipped"
  }
}

def setLevel(val) {
  dimmableLightAction("SetLoadLevelTarget", "Dimming", getDataValue("dcurl"), [newLoadlevelTarget: val])
}

def setTemperature(val) {
  // The API only accepts values 2700 - 6000, but it actually wants values
  // between 0-100, so we need to do this weird offsetting  (trial and error)
  def offsetVal = val.toInteger() + 4016
  awoxAction("SetTemperature", "X_WhiteLight", getDataValue("xwlcurl"), [Temperature: offsetVal.toString()])
}

def White() {
  def lastTemp = device.currentValue("temperature") + 4016
  awoxAction("SetTemperature", "X_WhiteLight", getDataValue("xwlcurl"), [Temperature: lastTemp])
}

def setColor(value) {
  def colorString = value.red.toString() + "," + value.green.toString() + "," + value.blue.toString()
  awoxAction("SetRGBColor", "X_ColorLight", getDataValue("xclcurl"), [RGBColor: colorString])
}

def setColorHex(hexString) {
  def colorString = convertHexToInt(hexString[1..2]).toString() + "," +
    convertHexToInt(hexString[3..4]).toString() + "," +
    convertHexToInt(hexString[5..6]).toString()
  awoxAction("SetRGBColor", "X_ColorLight", getDataValue("xclcurl"), [RGBColor: colorString])
}

// Custom commands

// This method models the button on the StriimLight remote that cycles through
// some pre-defined colors
def cycleColors() {
  def currentColor = device.currentValue("color")
  switch(currentColor) {
    case "#0000ff":
      setColorHex("#ffff00")
      break
    case "#ffff00":
      setColorHex("#00ffff")
      break
    case "#00ffff":
      setColorHex("#ff00ff")
      break
    case "#ff00ff":
      setColorHex("#ff0000")
      break
    case "#ff0000":
      setColorHex("#00ff00")
      break
    case "#00ff00":
      setColorHex("#0000ff")
      break
    default:
      setColorHex("#0000ff")
      break
  }
}

def subscribe() {
  log.trace "subscribe()"
  def result = []
  result << subscribeAction(getDataValue("deurl"))
  result << delayAction(2500)
  result << subscribeAction(getDataValue("speurl"))
  result << delayAction(2500)
  result << subscribeAction(getDataValue("xcleurl"))
  result << delayAction(2500)
  result << subscribeAction(getDataValue("xwleurl"))
  result
}

def unsubscribe() {
  def result = [
  unsubscribeAction(getDataValue("deurl"), device.getDataValue('subscriptionId')),
  unsubscribeAction(getDataValue("speurl"), device.getDataValue('subscriptionId')),
  unsubscribeAction(getDataValue("xcleurl"), device.getDataValue('subscriptionId')),
  unsubscribeAction(getDataValue("xwleurl"), device.getDataValue('subscriptionId')),


  unsubscribeAction(getDataValue("deurl"), device.getDataValue('subscriptionId1')),
  unsubscribeAction(getDataValue("speurl"), device.getDataValue('subscriptionId1')),
  unsubscribeAction(getDataValue("xcleurl"), device.getDataValue('subscriptionId1')),
  unsubscribeAction(getDataValue("xwleurl"), device.getDataValue('subscriptionId1')),

  ]
  updateDataValue("subscriptionId", "")
  updateDataValue("subscriptionId1", "")
  result
}

def getCurrentLevel()
{
  dimmableLightAction("GetLoadLevelTarget", "Dimming", getDataValue("dcurl"))
}

def getSystemString()
{
  mediaRendererAction("GetString", "SystemProperties", "/SystemProperties/Control", [VariableName: "UMTracking"])
}

private getCallBackAddress()
{
  device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private dimmableLightAction(String action, String service, String path, Map body = [:]) {
  // log.debug "path is $path, service is $service, action is $action, body is $body"
  def result = new physicalgraph.device.HubSoapAction(
    path:    path ?: "/DimmableLight/$service/Control",
    urn:     "urn:schemas-upnp-org:service:$service:1",
    action:  action,
    body:    body,
    headers: [Host:getHostAddress(), CONNECTION: "close"])
  result
}

private awoxAction(String action, String service, String path, Map body = [:]) {
  // log.debug "path is $path, service is $service, action is $action, body is $body"
  def result = new physicalgraph.device.HubSoapAction(
    path:    path ?: "/DimmableLight/$service/Control",
    urn:     "urn:schemas-awox-com:service:$service:1",
    action:  action,
    body:    body,
    headers: [Host:getHostAddress(), CONNECTION: "close"])
  result
}

private subscribeAction(path, callbackPath="") {
  def address = getCallBackAddress()
  def ip = getHostAddress()
  def result = new physicalgraph.device.HubAction(
    method: "SUBSCRIBE",
    path: path,
    headers: [
    HOST: ip,
    CALLBACK: "<http://${address}/notify$callbackPath>",
    NT: "upnp:event",
    TIMEOUT: "Second-600"])
  result
}

private unsubscribeAction(path, sid) {
  def ip = getHostAddress()
  def result = new physicalgraph.device.HubAction(
    method: "UNSUBSCRIBE",
    path: path,
    headers: [
    HOST: ip,
    SID: "uuid:${sid}"])
  result
}

private delayAction(long time) {
  new physicalgraph.device.HubAction("delay $time")
}

private Integer convertHexToInt(hex) {
  Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
  [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
  def parts = getDataValue("dni")?.split(":")
  def ip = convertHexToIP(parts[0])
  def port = convertHexToInt(parts[1])
  return ip + ":" + port
}

private updateSid(sid) {
  if (sid) {
    def sid0 = device.getDataValue('subscriptionId')
    def sid1 = device.getDataValue('subscriptionId1')
    def sidNumber = device.getDataValue('sidNumber') ?: "0"
    if (sidNumber == "0") {
      if (sid != sid1) {
        updateDataValue("subscriptionId", sid)
        updateDataValue("sidNumber", "1")
      }
    }
    else {
      if (sid != sid0) {
        updateDataValue("subscriptionId1", sid)
        updateDataValue("sidNumber", "0")
      }
    }
  }
}

private dniFromUri(uri) {
  def segs = uri.replaceAll(/http:\/\/([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+:[0-9]+)\/.+/,'$1').split(":")
  def nums = segs[0].split("\\.")
  (nums.collect{hex(it.toInteger())}.join('') + ':' + hex(segs[-1].toInteger(),4)).toUpperCase()
}

private hex(value, width=2) {
  def s = new BigInteger(Math.round(value).toString()).toString(16)
  while (s.size() < width) {
    s = "0" + s
  }
  s
}
