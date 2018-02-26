/**
 *  Turn on when Open, only sunset to rise
 *  Borrowed code from: Sunrise/Sunset by dianoga7@3dgo.net
 *
 *  Author: chrisb
 *  Date: 2013-07-18
 *
 *  Modified by Jayden Phillips
 * April 2014
 *
 *  Turn on a light when a door opens, then then optionally turn it off a set 
 *  number of minutes later.  Only turns on between sunset and sunrise.
 *
 *  Update: 2014-02-20
 *	Due to apparent changes to the delay: command, I've modified the code to use runIn instead.
 *	This should work properly, but has not been tested yet.
 */

 preferences {
     section("When this door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?", multiple: true
     }
     section("Turn on this light(s)..."){
		input "switches", "capability.switch", multiple: true
     }
     section("Turn off after how many minutes?") {
        input ('time', 'number', title: 'Enter 0 to not auto-off')
     }
     	section("Using either on this light sensor (optional) or the local sunrise and sunset"){
		input "lightSensor", "capability.illuminanceMeasurement", required: false
	}
     section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Zip code (optional, defaults to location coordinates when location services are enabled)...") {
		input "zipCode", "text", required: false
	}
	 
}

def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
	if (lightSensor) {
		subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
	}
	else {
		astroCheck()
		schedule("0 1 * * * ?", astroCheck) // check every hour since location can change without event?
	}
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.openening", contactOpenHandler)
    subscribe(contact1, "contact.open", contactOpenHandler)
	if (lightSensor) {
		subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
	}
	else {
		astroCheck()
		schedule("0 1 * * * ?", astroCheck) // check every hour since location can change without event?
	}
}

def contactOpenHandler(evt) {


    
    
	if (enabled()) 
    {
    	 log.trace ("Turning on switches: $switches")
	     switches.on()

    }
    else {
		 log.debug ("Too much light, not turning on" )
    }
    if (time == 0) /* if delay was set to 0 then we're doing nothing. */
    {
    	log.debug ("Not gonna turn you off")
    }
    else /* is delay was anything other than 0, then we're doing to turn off the switch x-number of minutes later */
    {
    	log.trace ("Turning off soon...")
        def delay = time * 60  /* runIn uses seconds, so multiply time (minutes) * 60 (for seconds)  */
        //switches.off(delay: delay)
        runIn (delay, switchOff)
    }
}

def switchOff (){
	switches.off()
}

def illuminanceHandler(evt) {
	log.debug "$evt.name: $evt.value, lastStatus: $state.lastStatus, motionStopTime: $state.motionStopTime"
	def lastStatus = state.lastStatus
	if (lastStatus != "off" && evt.integerValue > 50) {
		lights.off()
		state.lastStatus = "off"
	}
	else if (state.motionStopTime) {
		if (lastStatus != "off") {
			def elapsed = now() - state.motionStopTime
			if (elapsed >= (delayMinutes ?: 0) * 60000L) {
				lights.off()
				state.lastStatus = "off"
			}
		}
	}
	else if (lastStatus != "on" && evt.value < 30){
		lights.on()
		state.lastStatus = "on"
	}
}


def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}

private enabled() {
	def result
	if (lightSensor) {
		result = lightSensor.currentIlluminance < 30
	}
	else {
		def t = now()
		result = t < state.riseTime || t > state.setTime
	}
	result
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

