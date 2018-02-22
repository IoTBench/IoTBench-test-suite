/**
 *	Virtual Buttons for Multi-Button Controllers (ex. Minimote or ZWN-SC7)
 *
 *	Author: obycode
 *	Date Created: 2015-05-13
 *
 */
definition(
    name: "Virtual Buttons",
    namespace: "com.obycode",
    author: "obycode",
    description: "Create virtual single button devices for each button of your multi-button device (ex. Minimote or ZWN-SC7)",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/unknown/zwave/remote-controller.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/unknown/zwave/remote-controller@2x.png"
)

preferences {
  section ("Select your button controller: ") {
    input "buttonDevice", "capability.button", title: "Which?", multiple: false, required: true
  }
}

def installed() {
  initialize()
}

def updated() {
}

def initialize() {
  def numButtons = buttonDevice.currentValue("numButtons").toInteger()
  log.info "Creating $numButtons virtual buttons"
  // Create the virtual buttons
  (1..numButtons).each {
    def d = addChildDevice("com.obycode", "Virtual Button", buttonDevice.id + ":" + it.toString(), null, [label:buttonDevice.displayName + " " + it.toString(), name:"Virtual Button", completedSetup: true])
  }

  // Subscribe to the button events
  subscribe(buttonDevice, "button", buttonEvent)
}

def uninstalled() {
  unsubscribe()
  def delete = getChildDevices()
  delete.each {
    deleteChildDevice(it.deviceNetworkId)
  }
}

def buttonEvent(evt) {
  log.debug "buttonEvent: $evt.name $evt.value ($evt.data)"
  def buttonNumber = evt.jsonData.buttonNumber

  def buttonId = buttonDevice.id + ":" + buttonNumber
  def children = getChildDevices()
  def childButton = children.find{ d -> d.deviceNetworkId == buttonId }

  switch (evt.value) {
    case "pushed":
      log.debug "pushing the virtual button"
      childButton.push()
      break
    case "held":
      log.debug "holding the virtual button"
      childButton.hold()
      break
    case "default":
      log.debug "releasing the virtual button"
      childButton.release()
      break
    default:
      log.debug "Unknown event: $evt.value"
  }
}
