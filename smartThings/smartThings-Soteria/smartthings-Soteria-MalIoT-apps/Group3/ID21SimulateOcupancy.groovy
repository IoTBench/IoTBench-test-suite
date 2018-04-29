/*
* simulate-occupancy
* Author: Z. Berkay Celik
* The app is created by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/

definition(
    name: "simulate-occupancy",
    namespace: "soteria",
    author: "Soteria",
    description: "Turn on lights and turn them off after some time",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png")

preferences {
  section("When I touch the app, turn on...") {
    input "switches", "capability.switch", multiple: true
  }
  section("For how long between turn on and turn off?") {
      input "turnOffDelay", "decimal", defaultValue:30
  }
}  

def installed() {
  subscribe(app, appTouch)
}

def updated() {
  subscribe(app, appTouch)
}

def appTouch(evt) {
  def mode = turnOnTherm
    switches.on()
    lightOffTrigger()
}

def lightOffTrigger() {
    log.info("Starting timer to turn off lights")
    def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 60 
    state.turnOffTime = now()
    runIn(delay, "lightTurnOff")
  }

def lightTurnOff() {
    switches.off()
}