/**
 *  Watering Timer
 *
 *  Author: tnasir@gmail.com
 *  Date: 2013-10-17
 */
preferences {
	section("Turn on a switch"){
		input "switches", "capability.switch", multiple: true
	}
    section("Run every minutes?"){
        input ('RunCron', 'number', title: 'Enter 2 to 60 min')
    }
    section("Turn off after how many minutes?"){
        input ('time', 'number', title: 'Enter 0 to not auto-off')
    }
    section("But during the day for this area."){
    	input ('zip', 'number', title: 'Zip Code')
	}
}

def installed() {
	log.debug "Installed with settings: ${settings} v1.5"
	initialize()
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    initialize()
}

def initialize() {
	def min = RunCron
    log.debug "$min Mins"
    if (min >= 2 && min <= 60)
    {
        log.trace "Scheduled for every $min min"
		schedule("0 0/$min * * * ?", WaterPlants) // check every hour since location can change without event?
	}
    else
    {
    	log.trace "Run Min were less then 2 min or more then 60min"
    }
}

def WaterPlants()
{
	log.trace "Calling Watering"
    
    def data = getWeatherFeature('astronomy', settings.zip as String)
    
    def sunsetTime = data.moon_phase.sunset.hour + ':' + data.moon_phase.sunset.minute
    def sunriseTime = data.moon_phase.sunrise.hour + ':' + data.moon_phase.sunrise.minute
    def currentTime = data.moon_phase.current_time.hour + ':' + data.moon_phase.current_time.minute
    
    def localData = getWeatherFeature('geolookup', settings.zip as String)
    
    def timezone = TimeZone.getTimeZone(localData.location.tz_long)
    
	log.debug( "Sunset today is at $sunsetTime" )
	log.debug( "Sunrise today is at $sunriseTime" )
	
 	def startTime = timeToday(sunsetTime, timezone)
    def endTime = timeToday(sunriseTime, timezone) 
	if (now() < startTime.time && now() > endTime.time)  
    {
    	log.debug ("Turn the Switches on" )
        switches.on() //turning on the switches
        if (time == 0) /* if delay was set to 0 then we're doing nothing. */
        {
            log.debug ("Not gonna turn you off")
        }
        else /* is delay was anything other than 0, then we're doing to turn off the switch x-number of minutes later */
        {
            def delay = time * 60  /* 6000 delay is in milliseconds, so multiply time (minutes) * 60 (for seconds) * 1000 (for milliseconds) */
            log.trace ("Turning off after $delay seconds")
            //switches.off(delay: delay)
            runIn(delay,turnAllOff)
        }
    }
    else {
		log.trace "After SunSet Not doing anything."
    }	
}

def turnAllOff(){
	switches.off()
}