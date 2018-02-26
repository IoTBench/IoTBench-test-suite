/**
 *  Porch Fans - On...It is Hot with Calm Wind!
 *  This will turn on switch(es) with motion if the temperature is above a user
 *  value and the wind is below a user value. The temperature is received from a 
 *  multi-sensor and the wind speed comes from weather underground for a user input zipcode.
 *
 *  Author: scottjones67@gmail.com
 *  Date: 2013-07-05
 */

preferences {
	section("Turn on when there's motion..."){
		input "motion1", "capability.motionSensor", title: "Where?"
    }    
	section("Choose a temperature sensor... "){
		input "sensor1", "capability.temperatureMeasurement", title: "Temp Sensor"
    }
    section("When the temperature is above...") {
		input "temperature1", "number", title: "Temp?"
	}
	section("And windspeed is below..."){
		input "wind1", "number", title: "Wind (mph)?"
    }
	section("And off when there's been no motion for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off switches..."){
		input "switches", "capability.switch", multiple: true
	}
	section("Your Zipcode..."){
		input "zipcode", "text", title: "Zipcode?"
	}

}

def installed()
{
	subscribe(motion1, "motion", motionHandler)
	schedule("0 * * * * ?", "scheduleCheck")
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion", motionHandler)
	unschedule()
	schedule("0 * * * * ?", "scheduleCheck")
}

def motionHandler(evt) {	
    log.debug "$evt.name: $evt.value"
	def data = getWeatherFeature( "conditions", zipcode )
	log.debug( "Current windspeed in ${zipcode} is ${data.current_observation.wind_mph} mph")      
    if (evt.value == "active") {
		def lastTemp = sensor1.currentTemperature
		if (lastTemp >= temperature1){
			def lastWindspeed = data.current_observation.wind_mph
			if (lastWindspeed <= wind1){
				log.debug "turning on lights"
				switches.on()
				state.inactiveAt = null
                }
            }
		} 
    else if (evt.value == "inactive") {
		if (!state.inactiveAt) {
			state.inactiveAt = now()
		}
  	}
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	if (state.inactiveAt) {
		def elapsed = now() - state.inactiveAt
		def threshold = 1000 * 60 * minutes1
		if (elapsed >= threshold) {
			log.debug "turning off lights"
			switches.off()
			state.inactiveAt = null
		}
	else {
			log.debug "${elapsed / 1000} sec since motion stopped"
		}
	}
}