definition(
    name: "SmartApp Test2",
    namespace: "SmartApp Test2",
    author: "kms",
    description: "hahahah",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: [displayName: "kms OAuth client", displayLink: "kms OAuth link"]
)

preferences {
  section("Allow External Service to Control These Things...") {
    input "switch1", "capability.switch", title: "Which Switches?", required: false
  }
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {   
    log.debug "switch's id: " + switch1.id
    
    subscribe(switch1, "switch.on", onSwitchOn)
    subscribe(switch1, "switch.off", onSwitchOff)
}

mappings {
  path("/switch") {
    action: [
      GET: "getSwitchStatus"
    ]
  }
  path("/switch/:command") {
    action: [
      GET: "updateSwitch",
      PUT: "updateSwitches"
    ]
  }
  path("/switches/:id") {
    action: [
      GET: "showSwitch",
      PUT: "updateSwitch"
    ]
  }
}

void getSwitchStatus() {
	log.debug "getSwitchStatus"
    def value = switch1.currentValue("switch")[0]
    log.debug "$value"
    httpError(200, ""+value)
}

void updateSwitch() {

	log.debug "updateSwitch"
    log.debug "$params.command"
    log.debug "$switch1.id"
    
	switch1."$params.command"()
    
    //switches?.sendEvent(name: "switch", value: "$params.command")
	/*
    def command = request.JSON?.command
    if (command) {
      def mySwitch = switches.find { it.id == params.id }
      if (!mySwitch) {
        httpError(404, "Switch not found")
      } else {
      	mySwitch."$command"()
     	httpError(400,"OK")
      }
    }
    */
}
void onSwitchOn() {
	log.debug "onSwitchOn"
}
void onSwitchOff() {
	log.debug "onSwitchOff"
}