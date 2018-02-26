/**
 *  Switch Mode to Day or Night
 *
 *  Author: juano23@gmail.com

 *  Date: 2013-08-20
 */
 
preferences {
    section("Sunrise") {
        input ('sunrise_mode', 'mode', title: 'Change to mode: ')
    }
    
    section("SunriseOffset") {
        input ('sunrise_offset', 'number', title: 'Sunrise offset (minutes)', value: 0, required: true)
    }
    
	section("When mode change, turn off...") {
		input "switches", "capability.switch", multiple: true
	}

	section("Sunset") {
        input ('sunset_mode', 'mode', title: 'Change to mode: ')
    }
    
    section("SunsetOffset") {
        input ('sunset_offset', 'decimal', title: 'Sunset offset (minutes)', value: 0, required: true)
    }

	section("When mode change, turn on...") {
		input "switches2", "capability.switch", multiple: true
	}

	section("Miscellaneous") {
        input ('zip', 'number', title: 'Zip Code', required: false)
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    unsubscribe()
    initialize()
}

def initialize() {
    def data = getWeatherFeature('astronomy', settings.zip as String)
    def localData = getWeatherFeature('geolookup', settings.zip as String)    
    def timezone = TimeZone.getTimeZone(localData.location.tz_long)
    
    def rawsunriseTime = data.moon_phase.sunrise.hour + ':' + data.moon_phase.sunrise.minute
    def rawsunsetTime  = data.moon_phase.sunset.hour + ':' + data.moon_phase.sunset.minute
	def currentTime = data.moon_phase.current_time.hour + ':' + data.moon_phase.current_time.minute
    
    def sunriseTime = new Date(timeToday(rawsunriseTime, timezone).time + timeOffset(sunrise_offset))
	def sunsetTime = new Date(timeToday(rawsunsetTime, timezone).time - timeOffset(sunset_offset))

    log.debug( "Sunrise today is at $sunriseTime" )
    log.debug( "Sunset today is at $sunsetTime" )
    
    unschedule()    
    schedule(sunriseTime, sunrise)
    schedule(sunsetTime, sunset)
    schedule(timeTodayAfter(new Date(), '01:00', timezone), initialize)  
}

def sunrise() {
    changeMode(settings.sunrise_mode)
    switches?.off()
}

def sunset() {
    changeMode(settings.sunset_mode)
    switches2?.on()
}

def changeMode(newMode) {
    if (location.mode != newMode) {
        if (location.modes?.find{it.name == newMode}) {
            setLocationMode(newMode)
            log.debug "${label} has changed the mode to '${newMode}'"
        }
        else {
            log.debug "${label} tried to change to undefined mode '${newMode}'"
        }
    }
}