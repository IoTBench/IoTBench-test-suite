/**
 *  Turn on when Arrival, Open or Motion - Sunset/Sunrise
 *  Borrowed code from: Sunrise/Sunset by dianoga7@3dgo.net
 *
 *  Author: Scott Jones
 *  Date: 26 Aug 13
 *
 *  Turn on a light when a door opens, arrival presence or motion. Optionally turn it off a set 
 *  number of minutes later.  Only turns on between sunset and sunrise.
 */

 preferences {
     section("When I arrive...(optional)"){
		input "presence", "capability.presenceSensor", title: "Who?", required: false, multiple: true
	}
	section("When this door opens...(optional)"){
		input "contact", "capability.contactSensor", title: "Where?", required: false, multiple: true
	}
	section("When there is motion...(optional)"){
    	input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
	}
    section("Turn on this light(s)..."){
		input "switches", "capability.switch", multiple: true
	}
    section("Turn off after how many minutes?"){
        input ('time', 'number', title: 'Enter 0 to not auto-off')
    }
	section("But only after dark for this area."){
    	input ('zip', 'number', title: 'Zip Code')
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(presence, "presence", eventHandler)
    subscribe (contact, "contact.open", eventHandler)
    subscribe (motion, "motion.active", eventHandler)
}

def eventHandler(evt) {
	def data = getWeatherFeature('astronomy', settings.zip as String)
    
    def sunsetTime = data.moon_phase.sunset.hour + ':' + data.moon_phase.sunset.minute
    def sunriseTime = data.moon_phase.sunrise.hour + ':' + data.moon_phase.sunrise.minute
    def currentTime = data.moon_phase.current_time.hour + ':' + data.moon_phase.current_time.minute
    
    def localData = getWeatherFeature('geolookup', settings.zip as String)
    
    def timezone = TimeZone.getTimeZone(localData.location.tz_long)
    
	log.debug( "Sunset today is at $sunsetTime" )
	log.debug( "Sunrise today is at $sunriseTime" )
	log.debug ("$evt.value: $evt, $settings")
 	def startTime = timeToday(sunsetTime, timezone)
    def endTime = timeToday(sunriseTime, timezone) 
	if (now() < startTime.time && now() > endTime.time) 
    {
    log.debug ("Too much light, not turning on" )
    }
    else {
		if (presence()) {
			if (evt.value == "present") {
            	log.trace "${presence.label ?: presence.name} has arrived at the ${location}"
				log.trace ("Turning on switches: $switches")
				switches.on()
            }
        }
		if (contact()) {
        	log.debug "${contact.label ?: contact.name} has opened at the ${location}"
        	log.trace ("Turning on switches: $switches")
            switches.on()
		}
		if (motion()) {
        	log.trace "${motion.label ?: motion.name} has motion at the ${location}"
            log.trace ("Turning on switches: $switches")
            switches.on()
        }
    }
    
    if (time == 0) /* if delay was set to 0 then we're doing nothing. */
    {
    log.debug ("Not gonna turn you off")
    }
    else /* is delay was anything other than 0, then we're doing to turn off the switch x-number of minutes later */
    {
    	log.trace ("Turning off soon...")
        def delay = time * 60000  /* delay is in milliseconds, so multiply time (minutes) * 60 (for seconds) * 1000 (for milliseconds) */
        switches.off(delay: delay)
    }
}