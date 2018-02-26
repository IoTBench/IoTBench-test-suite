/**
 * Once A Day Lights w/ Dimmer
 *
 * Author: Ryan Nathanson
 *
 * Date: 06/03/2013
 */
preferences {

section("On Time") {
		input "onTime", "time", title: "When?", required: false
       
        
}

section("Off Time") {
		input "offTime", "time", title: "When?", required: false
        }


section("These Dimming light(s)"){
input "MultilevelSwitch", "capability.switchLevel", multiple: true, required: false
}
section("How Bright?"){
     input "number", "number", title: "Percentage, 0-99", required: false
    }
    
section("These Non Dimming light(s)"){
input "switches1", "capability.switch", multiple: true, required: false
}

}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(onTime, turnlighton)
	schedule(offTime, turnlightoff)

}

def updated(settings) {
	unschedule()
	schedule(onTime, turnlighton)
	schedule(offTime, turnlightoff)

}

def turnlighton() {
	log.debug "Turning on switches"
	settings.MultilevelSwitch?.setLevel(number)
    switches1?.on()

}

def turnlightoff() {
	log.debug "Turning off switches"
	settings.MultilevelSwitch?.off()
    switches1?.off()
}