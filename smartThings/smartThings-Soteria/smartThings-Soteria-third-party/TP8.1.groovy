/**
*
* Notes: context: mentioned about designed for coops. 
*https://community.smartthings.com/t/coopboss-is-certified-chicken-coop-door-controller-for-smartthings/49873?page=2
*/

preferences {
    section("Sunrise") {
        input ('sunrise_mode', 'mode', title: 'Change to mode: ')
    }
    
    section("Sunset") {
        input ('sunset_mode', 'mode', title: 'Change to mode: ')
    }
    
    section("Miscellaneous") {
        input ('zip', 'number', title: 'Zip Code', required: false)
    }
    section("Make sure this is locked") {
        input "lock","capability.lock"
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
    
    def sunsetTime = data.moon_phase.sunset.hour + ':' + data.moon_phase.sunset.minute
    def sunriseTime = data.moon_phase.sunrise.hour + ':' + data.moon_phase.sunrise.minute
    def currentTime = data.moon_phase.current_time.hour + ':' + data.moon_phase.current_time.minute
    
    def localData = getWeatherFeature('geolookup', settings.zip as String)
    
    def timezone = TimeZone.getTimeZone(localData.location.tz_long)
    
    log.debug( "Sunset today is at $sunsetTime" )
    log.debug( "Sunrise today is at $sunriseTime" )
    
    unschedule()    
    schedule(timeToday(sunriseTime, timezone), sunrise)
    schedule(timeToday(sunsetTime, timezone), sunset)
    schedule(timeTodayAfter(new Date(), '01:00', timezone), initialize)
    
}

def sunrise() {
    changeMode(settings.sunrise_mode)
    lock.unlock()
}

def sunset() {
    changeMode(settings.sunset_mode)
    lock.lock()
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