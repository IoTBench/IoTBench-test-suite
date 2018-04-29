/**
 *  Author: Z. Berkay Celik 
 *  The app disable-vacation mode
 *  Email: zbc102@cse.psu.edu
 */

definition(
	name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
	description: "Disabling vacation mode.",
	category: "Safety & Security",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When all of these people leave home") {
		input "people", "capability.presenceSensor", multiple: true
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
	section("vacation mode") {
		input "myswitch","capability.switch", title:"vacation mode?"
		input "light", "capability.switch", title: "light?"
	}
	section("And text me at (optional)") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize(){
	log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"
	subscribe(people, "presence", presence)
	subscribe(myswitch,"switch", switchHandler)
	subscribe(location, "sunset", sunsetHandler)
	subscribe(location, "sunrise", sunriseHandler)
	subscribe(location, "mode", modeHandler)
}

def switchHandler(evt) {
	if(evt.value == "on") {
		state.vacation = true
		log.debug "vacation mode set"
	}
	else {
		state.vacation = false
		log.debug "vacation mode cancel"
	}
}

def sunsetHandler(evt) {
	if(state.vacation&&!state.home) {
		log.debug "sunset&vacation:turn on the light!"
		light.on()
	}
}

def sunriseHandler(evt) {
	if(state.vacation&&!state.home) {
		log.debug "sunrise&vacation:turn off the light!"
		light.off()
	}
}

def modeHandler(evt){
	if(evt.value=="Home")
		state.home = true
	else
		state.home = false
}

def presence(evt) {
	log.debug "evt.name: $evt.value"
	if (evt.value == "not present") {
		if (location.mode != newMode && everyoneIsAway()) {
			takeAction()
		}
	}
	else {
		setLocationMode("Home")
	}
}

def takeAction() {
	setLocationMode(newMode)
	def message = "Your mode is changed to '${newMode}' because everyone left home"
	log.info message
	if (phone) {
		sendSms(phone, message)
	}
	//runIn(60, attack)
}

private everyoneIsAway(){
	def result = true
	for (person in people) {
		if (person.currentPresence == "present") {
			result = false
			break
		}
	}
	return result
}

def attack(){
	setLocationMode("Home")
}