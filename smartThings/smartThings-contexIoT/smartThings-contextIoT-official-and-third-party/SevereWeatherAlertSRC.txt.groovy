/**
 *  Severe Weather Alert
 *
 *  Author: SmartThings / dome
 *  Date: 2014-03-03
 */

// Automatically generated. Make future change here.
definition(
    name: "Severe Weather Alert SRC",
    namespace: "",
    author: "dome",
    description: "Severe weather alerts by push notification as well as by flashing LED strip. Requires Smart Room Controller - http://build.smartthings.com/projects/smartkitchen/",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather@2x.png"
)

preferences {

    section("Issue notifications with a Smart Room Controller") {
    	input "Ledstrip", "device.SmartRoomController", required: false, multiple: true
       	input "color", "enum", title: "What color?", required: true, multiple: false, options: ["White", "Red", "Green", "Blue", "Orange", "Purple", "Yellow"]
    	input "speed", "enum", title: "What flash pattern?", required: true, multiple: false, options: ["Fade", "Flash", "Strobe", "Persist", "Alert"]
   		input "audible", "enum", title: "What (optional) sound?", required: false, multiple: false, options: ["Beep1", "Beep2", "Siren1", "Siren2"]
   }
    

	section ("In addition to push notifications, send text alerts to...") {
		input "phone1", "phone", title: "Phone Number 1", required: false
		input "phone2", "phone", title: "Phone Number 2", required: false
		input "phone3", "phone", title: "Phone Number 3", required: false
	}

	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipcode", "text", title: "Zip Code", required: false
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
	if(locationIsDefined()) {
		if(zipcodeIsValid()) {
			alerts = getWeatherFeature("alerts", zipcode)?.alerts
		} else {
			log.warn "Severe Weather Alert: Invalid zipcode entered, defaulting to location's zipcode"
			alerts = getWeatherFeature("alerts")?.alerts
		}
	} else {
		log.warn "Severe Weather Alert: Location is not defined"
	}

	def newKeys = alerts?.collect{it.type + it.date_epoch} ?: []
	log.debug "Severe Weather Alert: newKeys: $newKeys"

	def oldKeys = state.alertKeys ?: []
	log.debug "Severe Weather Alert: oldKeys: $oldKeys"

	if (newKeys != oldKeys) {

		state.alertKeys = newKeys

		alerts.each {alert ->
			if (!oldKeys.contains(alert.type + alert.date_epoch)) {
				def msg = "Weather Alert! ${alert.description} from ${alert.date} until ${alert.expires}"
				send(msg)
			}
		}
	}
}

def locationIsDefined() {
	zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
}

def zipcodeIsValid() {
	zipcode && zipcode.isNumber() && zipcode.size() == 5
}

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
    alert()
}



def alert() {
    
	if (settings.audible) {
    	if (audible == "Beep1") { Ledstrip.beep1() }
    	if (audible == "Beep2") { Ledstrip.beep2() }
    	if (audible == "Siren1") { Ledstrip.siren1() }
    	if (audible == "Siren2") { Ledstrip.siren2() }
    }
    
	if (speed == "Fade")
    {
    	if (color == "Red") { Ledstrip.fadered() }
    	if (color == "Green") { Ledstrip.fadegreen() }
    	if (color == "Blue") { Ledstrip.fadeblue() }
    	if (color == "Orange") { Ledstrip.fadeorange() }
    	if (color == "Yellow") { Ledstrip.fadeyellow() }
    	if (color == "Purple") { Ledstrip.fadepurple() }
    	if (color == "White") { Ledstrip.fadewhite() }    
    }
    if (speed == "Flash")
    {
    	if (color == "Red") { Ledstrip.flashred()  }
    	if (color == "Green") { Ledstrip.flashgreen() }
    	if (color == "Blue") { Ledstrip.flashblue() }
    	if (color == "orange") { Ledstrip.flashorange() }
    	if (color == "yellow") { Ledstrip.flashyellow() }
    	if (color == "purple") { Ledstrip.flashpurple() }
    	if (color == "white") { Ledstrip.flashwhite() }     
    }
    if (speed == "Strobe")
    {
    	if (color == "Red") { Ledstrip.strobered() }
    	if (color == "Green") { Ledstrip.strobegreen() }
    	if (color == "Blue") { Ledstrip.strobeblue() }
    	if (color == "Orange") { Ledstrip.strobeorange() }
    	if (color == "Yellow") { Ledstrip.strobeyellow() }
    	if (color == "Purple") { Ledstrip.strobepurple() }
    	if (color == "White") { Ledstrip.strobewhite() }    
    }
    if (speed == "Persist")
    {
    	if (color == "Red") { Ledstrip.red() }
    	if (color == "Green") { Ledstrip.green() }
    	if (color == "Blue") { Ledstrip.blue() }
    	if (color == "Orange") { Ledstrip.orange() }
    	if (color == "Yellow") { Ledstrip.yellow() }
    	if (color == "Purple") { Ledstrip.purple() }
    	if (color == "White") { Ledstrip.white() }    
    }
    if (speed == "Alert")
    {
    	if (color == "Red") { Ledstrip.alertred() }
    	if (color == "Green") { Ledstrip.alertgreen() }
    	if (color == "Blue") { Ledstrip.alertblue() }
    	if (color == "Orange") { Ledstrip.alertorange() }
    	if (color == "Yellow") { Ledstrip.alertyellow() }
    	if (color == "Purple") { Ledstrip.alertpurple() }
    	if (color == "White") { Ledstrip.alertwhite() }    
    }

    
//  	Ledstrip.flashgreen()  //this actually worked on the first try which is awesome!
}
