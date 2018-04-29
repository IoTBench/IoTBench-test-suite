/**
 *  Author: Z. Berkay Celik
 *  Can we detect missing subscriptions through static analysis?
 *  This app is based on the location mode, locks or unlocks the doors through mode set via presence event. 
 *  Email: zbc102@cse.psu.edu
 *  Notes: Simulator allows dead codes. Event not subscribed, yet handler is there.
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "Set mode when you are at home or leave home",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
    section("When people leave home") {
        input "people", "capability.presenceSensor", multiple: true
    }
}

def initialize() {
    log.debug "Initializing"
    subscribe(people, "presence", presenceHandler)
    //updated
    //subscribe(location, modeChangeHandler)
}

def installed() {
    log.debug "Installed with settings: ${settings}"
    subscribe(people, "presence", presenceHandler)
    //subscribe(location, modeChangeHandler)
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    subscribe(people, "presence", presenceHandler)
}

def presenceHandler(evt) {
    log.debug "evt.name: $evt.value"
    if (evt.value == "not present") {
       setLocationMode("Away")     
    }
    else {
       setLocationMode("Home")    
    }  
}

def modeChangeHandler(session) {
    log.debug "ModeChange Handler function"
    log.debug "Inside modeChangeHandler. Current mode = ${location.mode}"

    if (location.mode == "Away") {
            log.debug "Mode is Away, Performing ArmAway"
            door.lock()
    }
    else if (location.mode == "Night") {
            log.debug "Mode is Night, Performing ArmStay"
            door.lock()
    }
    else if (location.mode == "Home") {
            log.debug "Mode is Night, Performing ArmStay"
            door.unlock()
    }
    else {
            log.debug "Mode is Home, Performing DisArm"
    }
}

