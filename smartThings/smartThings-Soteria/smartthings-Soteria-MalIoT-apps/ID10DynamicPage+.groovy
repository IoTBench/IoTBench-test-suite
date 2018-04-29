/**
 *  Nobody Home
 *  Monitors a set of presence detectors and triggers a mode change when everyone has left.
 *  When everyone has left, sets mode to a new defined mode.
 *  When at least one person returns home, set the mode back to a new defined mode.
 *  When someone is home - or upon entering the home, their mode may change dependent on sunrise / sunset.
 *
 *  Triggers other app and  Soteria valdiation for dynamic page support.
 *
 *  The app "Nobody Home" is modified by Z. Berkay Celik
 *  Email: zbc102@cse.psu.edu
 *  Notes: Configuration errors occur 1) users find it complex to configure the apps, 2) Context verification is hard through optional blocks such as description, "title". 
 *  3) This allows an adversary  distribute malicious apps easily.
 */

definition(
  name: "Soteria",
  namespace: "Soteria",
  author: "IoTBench",
  description: "When everyone leaves, change mode.  If at least one person home, switch mode based on sun position.",
  category: "Mode Magic",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
  section("When all of these people leave home") {
    input "people", "capability.presenceSensor", multiple: true
  }

  section("Change to this mode to...") {
    input "newAwayMode",    "mode", title: "Everyone is away"
    input "newSunsetMode",  "mode", title: "At least one person home and nightfall"
    input "newSunriseMode", "mode", title: "At least one person home and sunrise"
  }

  //updated
   section("Be sure you locked the door if it is open...") {
        input "lock1", "capability.lock", required: true
   }

  section("Away threshold (defaults to 10 min)") {
    input "awayThreshold", "decimal", title: "Number of minutes", required: false
  }

  section("Notifications") {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
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
  subscribe(people,   "presence", presence)
  subscribe(location, "sunrise",  setSunrise)
  subscribe(location, "sunset",   setSunset)

  state.sunMode = location.mode
}

def setSunrise(evt) {
  changeSunMode(newSunriseMode)
}

def setSunset(evt) {
  changeSunMode(newSunsetMode)
}

def changeSunMode(newMode) {
  state.sunMode = newMode

  if(everyoneIsAway() && (location.mode == newAwayMode)) {
    log.debug("Mode is away, not evaluating")
  }

  else if(location.mode != newMode) {
    def message = "${app.label} changed your mode to '${newMode}'"
    send(message)
    setLocationMode(newMode)
  }

  else {
    log.debug("Mode is the same, not evaluating")
  }
}

def presence(evt) {
  if(evt.value == "not present") {
    log.debug("Checking if everyone is away")

    if(everyoneIsAway()) {
      log.info("Starting ${newAwayMode} sequence")
      def delay = (awayThreshold != null && awayThreshold != "") ? awayThreshold * 60 : 10 * 60
      runIn(delay, "setAway")
    }
  }

  else {
    if(location.mode != state.sunMode) {
      log.debug("Checking if anyone is home")

      if(anyoneIsHome()) {
        log.info("Starting ${state.sunMode} sequence")

        changeSunMode(state.sunMode)
      }
    }
    else {
      log.debug("Mode is the same, not evaluating")
    }
  }
}

def setAway() {
  if(everyoneIsAway()) {
    if(location.mode != newAwayMode) {
      def message = "${app.label} changed your mode to '${newAwayMode}' because everyone left home"
      log.info(message)
      send(message)
      setLocationMode(newAwayMode)
    }
    else {
      log.debug("Mode is the same, not evaluating")
    }
  }
  else {
    log.info("Somebody returned home before we set to '${newAwayMode}'")
  }
}

private everyoneIsAway() {
  def result = true

  if(people.findAll { it?.currentPresence == "present" }) {
    result = false
  }

  log.debug("everyoneIsAway: ${result}")

  return result
}

private anyoneIsHome() {
  def result = false

  if(people.findAll { it?.currentPresence == "present" }) {
    result = true
  }

  log.debug("anyoneIsHome: ${result}")

  return result
}

private send(msg) {
  if(sendPushMessage != "No") {
    log.debug("Sending push message")
    sendPush(msg)
  }

  log.debug(msg)
}