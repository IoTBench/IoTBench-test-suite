/*
* Change Mode based on your bedroom switch and time
* Adapted from third party app https://community.smartthings.com/t/first-app-detecting-mode-change/13002/4
* Author: Z. Berkay Celik
* The app is modified by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/

definition(
        name: "welcome back home, lock your doors",
        namespace: "Soteria",
        author: "Soteria",
        description: "You may forget locking your door when you are in home mode, safe lock will lock when home mode is set.",
        category: "Safety & Security",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Select your bedroom switch to monitor"){
        input "theSwitch", "capability.switch"
    }
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
        setLocationMode("Home Day")
    }
    else {
        log.debug "Nighttime"
            setLocationMode("Home Night")
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
    if (evt.value == "Home Day"){
        log.debug "Changed to Home Day"
    }
    else {
        log.debug "Changed to something else"
    }
}

