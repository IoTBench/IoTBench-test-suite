/**
 *  Umbrella Reminder
 *
 *  Author: Gal Niv
 *  Date: 2013-06-16
 */
preferences {
	section("When the door opens...") {
		input "contact1", "capability.contactSensor", title: "Which door?"
	}
    section("Between what times? (optionally)") {
    	input "startTime", "time", title: "Starting at", required: false
    	input "endTime", "time", title: "Ending at", required: false
    }
    section("Notify if raining at...") {
    	input "zipCode", "text", title: "Zipcode"
        input "precipThreshold", "number", title: "Rain threshold (mm)", description: "Precipitation threshold (in mm)", required: false
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
	log.trace "$evt.value: $evt, $settings"

	log.debug "$contact1 was opened"
	
    if (startTime != null && endTime != null) {
    	def now = new Date(now())
    	if (now.before(timeToday(startTime)) || now.after(timeToday(endTime))) {
        	return
        }
    }
    
	def data = getWeatherFeature( "conditions", zipCode )
	//log.debug "Precipitation today in ${zipCode} is ${data.current_observation.precip_today_metric}mm"
    
    def threshold = precipThreshold == null ? 10 : precipThreshold
    if (Integer.parseInt(data.current_observation.precip_today_metric) >= threshold) {
    	log.debug "Precepitation is higher than threshold of ${threshold}, sending push notification"
    	sendPush("Looks like it's rainy today, don't forget your umbrella!")
    }
    else {
   	 	log.debug "Precepitation is lower than threshold of ${threshold}"
    }
}