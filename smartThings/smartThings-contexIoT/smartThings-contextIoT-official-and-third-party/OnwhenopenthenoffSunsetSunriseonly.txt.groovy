/**
 *  Turn on when Open, only sunset to rise
 *  Borrowed code from: Sunrise/Sunset by dianoga7@3dgo.net
 *
 *  Author: chrisb
 *  Date: 2013-07-18
 *
 *  Turn on a light when a door opens, then then optionally turn it off a set 
 *  number of minutes later.  Only turns on between sunset and sunrise.
 *
 *  Update: 2014-02-20
 *	Due to apparent changes to the delay: command, I've modified the code to use runIn instead.
 *	This should work properly, but has not been tested yet.
 */

 
// Automatically generated. Make future change here.
definition(
    name: "On when open, then off Sunset/Sunrise only",
    namespace: "",
    author: "seateabee@gmail.com",
    description: "Turn on a light when a door opens, then then optionally turn it off a set number of minutes later.  Only turns on between sunset and sunrise.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

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
	 section("Turn off after how many minutes?") {
        input ('time', 'number', title: 'Enter 0 to not auto-off')
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