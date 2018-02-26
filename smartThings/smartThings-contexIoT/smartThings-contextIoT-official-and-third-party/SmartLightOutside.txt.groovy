// The MIT License (MIT)
//
// Copyright (c) 2015 Eric Cirone
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

/**
 *  SmartLight Outside
 *
 *  Author: 	Eric Cirone
 *  Company: 	Eric Cirone
 *  Email: 		ecirone@gmail.com
 *  Date: 		01/18/2015
 */
definition(
    name: "SmartLight Outside",
    namespace: "ericcirone",
    author: "Eric Cirone",
    description: "Turn on outside light when someone arrives or when a door is unlocked, but only when the sun is down. Turn off after so many minutes",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/rise-and-shine@2x.png"
)

preferences {
	section("Dimmer to control") {
		input "dimmer", "capability.switchLevel", title: "Which dimmer?"
        input "brightness", "number", title: "On light level", description: "0-99"
        input "timeToOff", "number",  title: "Turn off in .. minutes", description: "10 minutes"
	}
    section("When I arrive?") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
    section("When I unlock the door?") {
        input "lock","capability.lock", multiple: false, required: false
    }
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", required: false
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
		input "phoneNumber", "phone", title: "Send a text message?", required: false
	}

}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
    subscribe(presence1, "presence", presenceHandler)
    subscribe(dimmer, "switch", switchHandler)
    subscribe(lock, "lock", lockHandler)
    state.sunIsDown = false;
}

def sunCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: getSunriseOffset(), sunsetOffset: getSunsetOffset())

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"
    log.debug "nowTime: $now"

	if(now.before(riseTime)){
		state.sunIsDown = true;
        log.debug("The sun is currently down");
	}else if(now.after(setTime)) {
		state.sunIsDown = true;
        log.debug("The sun is currently down");
	}else{
    	state.sunIsDown = false;
        log.debug("The sun is currently up");
    }
}

def presenceHandler(evt) {
	if (evt.value == "present") {
	    log.debug "someone has arrived"
        setDimmer();
	}else{
    	log.debug "someone has left"
    }
}

def switchHandler(evt){
	if (evt.value == "on" && timeToOff && timeToOff > 0){
    	runIn(timeToOff * 60, turnOffLight);
        log.debug("Schedule light to turn off in $timeToOff minutes.");
    }else if (evt.value == "off"){
    	unschedule(turnOffLight);
        log.debug("Light is off, remove scheduled jobs");
    }
}

def turnOffLight(){
	dimmer.setLevel(0);
    log.debug("Turning off ${dimmer.label} light");
    send("SmartLight Outside: ${dimmer.label} light turned off.");
}

def setDimmer(){
	sunCheck();
    if (state.sunIsDown){
		log.debug "sun is down"
        dimmer.setLevel(brightness)
    	log.debug("Setting ${dimmer.label} light to $brightness");
        send("SmartLight Outside: ${dimmer.label} light set to ${brightness}%.");
    }else{
    	log.debug("sun is up, do not turn on light")
        send("SmartLight Outside: ${dimmer.label} light will remain off, the sun is shining!");
    }
}

def lockHandler(evt){
	if (evt.value == "unlocked"){
    	   setDimmer();
    }
}

private send(msg) {
	if ( sendPushMessage == "Yes" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}

	if ( phoneNumber ) {
		log.debug( "sending text message" )
		sendSms( phoneNumber, msg )
	}

	log.debug msg
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}