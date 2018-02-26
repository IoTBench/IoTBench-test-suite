/**
 *  Turn on when Open, only sunset to rise
 *  Borrowed code from: Sunrise/Sunset by dianoga7@3dgo.net
 *
 *  Author: chrisb
 *  Date: 2013-07-18
 */

 preferences {
     section("When this door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?"
     }
     section("Turn on this light(s)..."){
		input "switches", "capability.switch", multiple: true
     }
     section("But only after dark for this area.") {
    	input ('zip', 'number', title: 'Zip Code')
     }
}

def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {

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
         log.trace ("Turning on switches: $switches")
	     switches.on()
    }
}
