/**
 *  When It's Going To Rain
 *
 *  Author: SmartThings
 */
 preferences {
	 section("Zip code..."){
		 input "zipcode", "text", title: "Zipcode?"
	 }
	 // TODO: would be nice to cron this so we could check every hour or so
	 section("Check at..."){
		 input "time", "time", title: "When?"
	 }
	 section("Things to check..."){
		 input "sensors", "capability.contactSensor", multiple: true
	 }
	 section("Text me if I anything is open..."){
		 input "phone", "phone", title: "Phone number?"
	 }
 }
 

def installed() {
	log.debug "Installed: $settings"
	schedule(time, "scheduleCheck")
}

def updated() {
	log.debug "Updated: $settings"
	unschedule()
	schedule(time, "scheduleCheck")
}

def scheduleCheck() {
	def response = getWeatherFeature("forecast", zipcode)
	if (isStormy(response)) {
		sensors.each {
			log.debug it?.latestValue
		}
		def open = sensors.findAll { it?.latestValue == 'open' }
		if (open) {
			sendSms(phone, "A storm is a coming and the following things are open: ${open.join(', ')}")
		}
	}
}

private isStormy(json) {
	def STORMY = ['rain', 'snow', 'showers', 'sprinkles', 'precipitation']

	def forecast = json?.forecast?.txt_forecast?.forecastday?.first()
	if (forecast) {
		def text = forecast?.fcttext?.toLowerCase()
		if (text) {
			def result = false
			for (int i = 0; i < STORMY.size() && !result; i++) {
				result = text.contains(STORMY[i])
			}
			return result
		} else {
			return false
		}
	} else {
		log.warn "Did not get a forecast: $json"
		return false
	}
}
