/**
 *  Switch Controls Outlet
 *
 *  Author: brian@bevey.org
 *  Date: 2013-11-16
 *
 *  A Z-Wave switch controls any given outlets.  Allows independent control of
 *  outlets by turning the switch off when already in an off state - or turning
 *  the switch on when already in an on state.
 *
 *  If switch is off: Turn switch on and it will turn on switch and any
 *  outlets.
 *  If switch is on: Turn switch off and it will turn off switch and any
 *  outlets.
 *  If switch is off: Turn switch off and it will turn on any outlets and
 *  switch will remain off.
 *  If switch is on: Turn switch on and it will turn off the switch and any
 *  outlets will remain on.
 */
 
 definition(
  name: "Switch Controls Outlet",
  namespace: "imbrianj",
  author: "brian@bevey.org",
  description: "A Z-Wave switch controls any given outlets.  Allows independent control of outlet by turning the switch off when already in an off state.",
  category: "Convenience",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("Turn on with which switch?") {
    input "wallSwitch", "capability.switch"
  }

  section("Turns on which outlets?") {
    input "outlets", "capability.switch", multiple: true
  }

  section("Use switch for additional toggling (on if already on, off if already off)?") {
    input "toggle", "enum", metadata: [values: ["Yes", "No"]], required: false
  }
}

def installed() {
  init()
}

def updated() {
  unsubscribe()
  init()
}

def init() {
  state.wallSwitch = wallSwitch.latestValue("switch")
  subscribe(wallSwitch, "switch", changeLights, [filterEvents: false])
}

def changeLights(evt) {
  if(evt.isPhysical()) {
    if((wallSwitch.latestValue("switch") == "on") &&
       (evt.value == "on") &&
       (toggle != "No") &&
       (state.wallSwitch == "on")) {
      log.info("Switch is on, but we want to toggle outlets")

      toggleOutlets()
    }

    else if((wallSwitch.latestValue("switch") == "off") &&
            (evt.value == "off") &&
            (toggle != "No") &&
            (state.wallSwitch == "off")) {
      log.info("Switch is off, but we want to toggle outlets")

      toggleOutlets()
    }

    else if(evt.value == "on") {
      log.info("Turning on lights")
      outlets?.on()
    }

    else if(evt.value == "off") {
      log.info("Turning off lights")
      outlets?.off()
    }
  }

  state.wallSwitch = wallSwitch.latestValue("switch")
}

def toggleOutlets() {
  if(outlets.findAll { it?.latestValue("switch") == "on" }) {
    log.info("Toggle lights off")

    outlets?.off()
  }

  else {
    log.info("Toggle lights on")

    outlets?.on()
  }
}
