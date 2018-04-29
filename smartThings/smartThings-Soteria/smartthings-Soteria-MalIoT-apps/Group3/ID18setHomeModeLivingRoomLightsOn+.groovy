/*
* Change Mode based on your bedroom switch 
* Author: Z. Berkay Celik
* The third-party app "detection mode change" is modified by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/

definition(
        name: "welcome back home",
        namespace: "Soteria",
        author: "Soteria",
        description: "Set mode based on your switch",
        category: "Safety & Security",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Select your bedroom switch to monitor"){
        input "theSwitch", "capability.switch"
    }
    // logic can be added to get the modes from user
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated(settings) {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}
def initialize() {
    subscribe(theSwitch, "switch.On", onHandler)
    subscribe(theSwitch, "switch.Off", offHandler)
    subscribe(location, modeChangeHandler)
}

def onHandler(evt) {
    if(getSunriseAndSunset().sunrise.time < now() && getSunriseAndSunset().sunset.time > now()){
        log.debug "Daytime"
        setLocationMode("Home")
    }
    else {
        log.debug "Nighttime"
            setLocationMode("Night")
        }
    log.debug "Received on from ${theSwitch}"
}

def offHandler(evt) {
    if(getSunriseAndSunset().sunrise.time < now() && getSunriseAndSunset().sunset.time > now()){
        log.debug "Daytime"
        setLocationMode("Away Day")
    }
    else {
        log.debug "Nighttime"
        setLocationMode("Away Night")
    }
    log.debug "Received off from ${theSwitch}"
}

def modeChangeHandler(evt) {
    if (evt.value == "Home"){
        log.debug "Changed to Home Day"
    }
    else {
        log.debug "Changed to something else"
    }
}

