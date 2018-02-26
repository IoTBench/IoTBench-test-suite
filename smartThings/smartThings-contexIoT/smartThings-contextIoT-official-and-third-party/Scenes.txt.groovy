/**
 *  Scenes
 *
 *  Author: Ryan Nathanson
 */
 
preferences {
                section("When I Change To This Mode") {
                input "newMode", "mode", title: "Mode?"
       
        }
        section("Lock these doors") {
                input "lock", "capability.lock", multiple: true, required: false
        }
section("Unlock these doors") {
                        input "unlock", "capability.lock", multiple: true, required: false
        }
        section("Dim These Lights") {
        input "MultilevelSwitch", "capability.switchLevel", multiple: true, required: false
        }
   
   
    section("How Bright?"){
     input "number", "number", title: "Percentage, 0-99", required: false
    }
 
section("Turn On These Switches"){
input "switcheson", "capability.switch", multiple: true, required: false
}
 
section("Turn Off These Switches"){
input "switchesoff", "capability.switch", multiple: true, required: false
}
}
 
 
def installed() {
subscribe(location)
subscribe(app)
 
}
 
def updated() {
unsubscribe()
subscribe(location)
subscribe(app)
 
}
 
 
def uninstalled() {
unsubscribe()
}
 
 
def changedLocationMode(evt) {
 
    switcheson?.on()
    switchesoff?.off()
        settings.MultilevelSwitch?.setLevel(number)
        lock?.lock()
        unlock?.unlock()
               
 
               
 
}
 
def appTouch(evt) {
 
    switcheson?.on()
    switchesoff?.off()
        settings.MultilevelSwitch?.setLevel(number)
        lock?.lock()
        unlock?.unlock()
               
 
               
 
}