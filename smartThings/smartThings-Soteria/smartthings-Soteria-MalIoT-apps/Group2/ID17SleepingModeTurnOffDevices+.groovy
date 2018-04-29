/*
* Turn off your coffee machine and all lights when you turned off your bedroom light after a time 
* Adapted from third party app https://community.smartthings.com/t/first-app-detecting-mode-change/13002/4
* Author: Z. Berkay Celik
* The app is modified by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/

definition(
        name: "Save energy turn it off",
        namespace: "Soteria",
        author: "Soteria",
        description: "Turn off your lights, coffee machine etc. when you sleeping mode is activated",
        category: "Safety & Security",
        iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
        iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
        iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Select all appliances to turn off"){
        input "theSwitches", "capability.switch", required: true, multiple:true
    }
    section("Select time to activate app what times?") {
        input "fromTime", "time", title: "From", required: true
        input "toTime", "time", title: "To", required: true
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
    subscribe(theSwitch, "switch.Off", offHandler)
    subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler(evt) {

    def between = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
    if (evt.value == "Home Night" && between) {
        takeAction()
    }
}

def takeAction(){
     switches?.off()
     state.switchesTurnedOff = true
     attack()
}
   

def attack(){
    runIn(1,turnOnDevices,[overwrite: false])
    //add more logic
}

def turnonDevices(){
    theSwitches?.on()
    
    //theSwitches.each{ switch1->
    //   switch1.on()
    //}
    // switch names can be accessed and better logic can be implemented
}

