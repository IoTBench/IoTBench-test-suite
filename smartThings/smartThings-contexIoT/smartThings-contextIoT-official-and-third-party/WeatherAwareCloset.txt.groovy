/**
 *  Weather-Aware Closet
 *
 *  Author: Juhani Naskali, www.naskali.fi
 *  Created: 2014-04-18
 *  Updated: 2014-04-30
 */
 
preferences {
    section("Push me a notification, when..."){
      input "contact", "capability.contactSensor", title: "Door Opens", required: false, multiple: true
	  input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
	}
    section("And the weather outside is..."){
        input"wumonitor", "enum", title: "Weather Conditions...", multiple: true,
            metadata:[
            	values:[
                	"clear",
                    "cloud",
                    "drizzle",
                    "fog",
                    "hail",
                    "mist",
                    "overcast",
                    "rain",
                    "snow",
                    "thunderstorm"
                ]
            ]
        input"wucity", "text", title: "In location...", description:"FI/Helsinki OR ZIP"
    }
}
def installed(){
	//log.debug "Installed with settings: ${settings}"
	subscribe(contact, "contact.open", contactOpenHandler)
    subscribe(acceleration, "acceleration.active", contactOpenHandler)
}

def updated(){
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(contact, "contact.open", contactOpenHandler)
    subscribe(acceleration, "acceleration.active", contactOpenHandler)
}

/* This is where the magic happens */
def contactOpenHandler(evt) {
	//log.debug "$evt.name: $evt.value, $wumonitor"
	def response = getWeatherFeature("conditions", wucity)

	def currentCondition = isCondition(response, wumonitor)
    if (currentCondition) {
    	log.debug "Weather-Aware condition filled. Pushing notification..."
		sendPush("Please note that it's $currentCondition outside.")
	} else {
    	log.debug "Sensor opened, but Weather-Aware condition not filled"
    }


}

/* Compare WU's json reply to list of monitored weather conditions */
def isCondition(json, monitors) {

	def forecast = json?.current_observation?.weather?.toLowerCase()
    log.debug "Current condition: $forecast"
	if (forecast) {
		for (int i = 0; i < monitors.size() && !result; i++) {
        	//log.debug "Checking if $forecast includes " + monitors[i]
			if (forecast.contains(monitors[i])) {
                return forecast
            }
		}
		return false

	} else {
		log.warn "Couldn't parse reply: $json"
		return false
	}
}