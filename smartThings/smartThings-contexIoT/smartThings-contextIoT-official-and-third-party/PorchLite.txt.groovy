/**
 *  Porchlite based on Sunrise, Sunset
 *
 *  Author: SmartThings
 *
 *  Date: 2013-04-30
 */
preferences {
	section ("At sunrise...") {
		input "sunriseOn", "capability.switch", title: "Turn on?", required: false, multiple: true
        input "sunriseOnLevel", "number", title: "On Level?", required: false
		input "sunriseOff", "capability.switch", title: "Turn off?", required: false, multiple: true
	}
	section ("At sunset...") {
		input "sunsetOn", "capability.switch", title: "Turn on?", required: false, multiple: true
        input "sunsetOnLevel", "number", title: "On Level?", required: false
		input "sunsetOff", "capability.switch", title: "Turn off?", required: false, multiple: true
	}
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", required: false
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
		input "phoneNumber", "phone", title: "Send a text message?", required: false
	}

}

def installed() {
	initialize()
}

def updated() {
	unschedule()
	initialize()
}

def initialize() {
	astroCheck()
	schedule("0 1 * * * ?", astroCheck) // check every hour since location can change without event?
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"

	state.riseTime = riseTime.time
    state.setTime = setTime.time
    
    unschedule("sunriseHandler")
    unschedule("sunsetHandler")
    
    if (riseTime.after(now)) {
        log.info "scheduling sunrise handler for $riseTime"
        runOnce(riseTime, sunriseHandler)
    }
    
    if (setTime.after(now)) {
        log.info "scheduling sunset handler for $setTime"
        runOnce(setTime, sunsetHandler)
    }
    
}

def sunriseHandler() {
	log.info "Executing sunrise handler"
	if (sunriseOn) {
    	if (sunriseOnLevel == null) {
	    	sunriseOn.on()
        }
        else {
        	sunriseOn?.setLevel(sunriseOnLevel)
        }
    }
	if (sunriseOff) {
    	sunriseOff.off()
    }
	unschedule("sunriseHandler") // Temporary work-around for scheduling bug
    
    send("Sunrise")
}

def sunsetHandler() {
	log.info "Executing sunset handler"
	if (sunsetOn) {
    	if (sunsetOnLevel == null) {
	        sunsetOn.on()
        }
        else {
        	sunsetOn?.setLevel(sunsetOnLevel)
        }
    }
	if (sunsetOff) {
    	sunsetOff.off()
    }
	unschedule("sunsetHandler") // Temporary work-around for scheduling bug
    
    
    send("Sunset")
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

private getLabel() {
	app.label ?: "SmartThings"
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

