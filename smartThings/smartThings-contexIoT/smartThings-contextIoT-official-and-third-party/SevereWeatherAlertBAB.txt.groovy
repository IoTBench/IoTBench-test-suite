/**
 *  Severe Weather Alert
 *
 *  Author: SmartThings
 *  Date: 2013-03-04
 */
definition(
    name: "Severe Weather Alert BAB",
    namespace: "Safety & Security",
    author: "SmartThings",
    description: "Get a push notification when severe weather is in your area.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather@2x.png"
)

preferences {
	section ("In addition to push notifications, send text alerts to...") {
		input "phone1", "phone", title: "Phone Number 1", required: false
		input "phone2", "phone", title: "Phone Number 2", required: false
		input "phone3", "phone", title: "Phone Number 3", required: false
	}

	section ("Location code (optional, defaults to location coordinates)...") {
		input "locationCode", "text", title: "Location Code", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule("0 */10 * * * ?", "checkForSevereWeather") //Check at top and half-past of every hour
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	schedule("0 */10 * * * ?", "checkForSevereWeather") //Check at top and half-past of every hour
}

def checkForSevereWeather() {
	def alerts
//	if(locationIsDefined()) {
		if(locationCode != "") {
			alerts = getWeatherFeature("alerts", locationCode)?.alerts
		} else {
			log.warn "Defaulting to location's zipcode"
			alerts = getWeatherFeature("alerts")?.alerts
		}
//	} else {
//		log.warn "Severe Weather Alert: Location is not defined"
//	}

	def newKeys = alerts?.collect{it.type + it.date_epoch} ?: []
	log.debug "Severe Weather Alert: newKeys: $newKeys"

	def oldKeys = state.alertKeys ?: []
	log.debug "Severe Weather Alert: oldKeys: $oldKeys"

	if (newKeys != oldKeys) {

		state.alertKeys = newKeys

		alerts.each {alert ->
			if (!oldKeys.contains(alert.type + alert.date_epoch) && descriptionFilter(alert.description)) {
				def msg = "${locationCode} Weather Alert! ${alert.description} from ${alert.date} until ${alert.expires}"
				send(msg)
			}
		}
	}
}

def descriptionFilter(String description) {
	def filterList = ["Special", "Statement", "Test"]
	def passesFilter = true
	filterList.each() { word ->
		if(description.contains(word)) { passesFilter = false }
	}
	passesFilter
}

//def locationIsDefined() {
//	zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
//}

//def zipcodeIsValid() {
//	zipcode && zipcode.isNumber() && zipcode.size() == 5
//}

private send(message) {
	sendPush message
	if (settings.phone1) {
		sendSms phone1, message
	}
	if (settings.phone2) {
		sendSms phone2, message
	}
	if (settings.phone3) {
		sendSms phone3, message
	}
}