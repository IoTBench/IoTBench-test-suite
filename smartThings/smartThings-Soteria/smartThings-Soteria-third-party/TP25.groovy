/**
 *  Sunrise/Sunset
 *
 *  Author: dianoga7@3dgo.net
 *  Date: 2013-06-26
 *  Code: https://github.com/smartthings-users/smartapp.sunrise-sunset
 *
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this 
 * software and associated documentation files (the "Software"), to deal in the Software 
 * without restriction, including without limitation the rights to use, copy, modify, 
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following 
 * conditions: The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
}

def sunset() {
    changeMode(settings.sunset_mode)
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