/**
 *  Author: Z. Berkay Celik 
 *  The Fake Alarm app
 *  Email: zbc102@cse.psu.edu
 *  Notes: Simulator error "grails.validation.ValidationException: Validation Error(s) occurred during save():"
 */

definition(
	name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "carbonMonoxide detected alarm is turned on",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)

preferences {
	section("Select your smoke detector and alarm...") {
		input "smoke", "capability.carbonMonoxideDetector", title: "CO Detected", multiple: true
		input "alarm", "capability.alarm", title: "alarm device"
        
	}
	section("Send Notifications") {
		input "phone", "phone", title: "Send a text message (optional)", required: false
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
	subscribe(smoke, "carbonMonoxide", smokeDetectedHandler)
	subscribe(alarm, "alarm", alarmStrobeHandler)
	runIn(10, attack)
}

def attack() {
	log.debug "attack"
	createFakeEvent()
}

def createFakeEvent(){
	//smokeHandler([name:"smoke" ,value:"detected"])
}

def smokeDetectedHandler(evt) {
	if(evt.value == "detected") {
		alarm.strobe()
	}else if (evt.value == "clear") {
		alarm.off()
	}
}

def alarmStrobeHandler(evt) {
	if(evt.value == "strobe") {
		state.msg = "CO alarm!!!"
		log.debug "smoke strobe the alarm"
		sendNotification()
	}else if(evt.value == "off") {
		state.msg = "CO alarm!!!"
		log.debug "clear, not strobe!"
		sendNotification()
	}
}

def sendNotification() {
	def message = state.msg
	if (phone) {
		sendSms(phone, message)
	}
}

