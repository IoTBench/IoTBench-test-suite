/**
 *  Weather Monitor PWS
 *
 *  Author: Jim Lewallen
 *  Date: 2015-06-16
 */

definition(
    name: "Weather Monitor PWS",
    namespace: "jameslew",
    author: "jameslew@live.com",
    description: "Integrating a Personal Weather Station into SmartThings via Weather Underground.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section ("Weather threshholds to alert at..."){
    	input "highwindspeed", "number", title: "Avg Wind Speed (MPH)", required: false, defaultValue: 10
    	input "hightempf", "number", title: "High Temperature (Deg F)", required: false, defaultValue: 85
    	input "lowtempf", "number", title: "Low Temperature (Deg F)", required: false, defaultValue: 37
    	input "highrainfall", "number", title: "Rainfall Threshhold (Inches)", required: false, defaultValue: 1    
    }
	section ("In addition to push notifications, send text alerts to...") {
		input "phone1", "phone", title: "Phone Number 1", required: false
		input "phone2", "phone", title: "Phone Number 2", required: false
		input "phone3", "phone", title: "Phone Number 3", required: false
	}
	section ("Weather Underground Weather Station ID") {
		input "stationid", "text", title: "Station ID", required: true
	}
	section ("Custom Alert Messages") {
		input "warmmsg", "text", description: "Its hot at the weather station ", title: "Too Hot Message", required: false, defaultValue: "Its hot at the weather station "
		input "coldmsg", "text", description: "Its cold at the weather station ", title: "Too Cold Message", required: false, defaultValue: "Its cold at the weather station "
		input "windymsg", "text", description: "Its windy at the weather station ", title: "Windy Message", required: false, defaultValue: "Its windy at the weather station "
		input "wetmsg", "text", description: "Its really wet out ", title: "Wet Message", required: false, defaultValue: "Its really wet out "
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	setDefaultWeather()
	runEvery5Minutes("checkWeather") //Check at top and half-past of every hour
    schedule("0 0 5,16 * * ?", "setDefaultWeather") //Reset in the morning
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    setDefaultWeather()
	runEvery5Minutes("checkWeather") //Check at top and half-past of every hour
    schedule("0 0 5,16 * * ?", "setDefaultWeather") //Reset in the morning
}

def checkWeather() {
    def weather

    // If there is a zipcode defined, use it, otherwise SmartThings will determine your current location from the Hub.
    log.debug "WeatherStation: pws:{${stationid}}"
    weather = getWeatherFeature( "conditions", "pws:${stationid}" )

    // Check if the variable is populated, otherwise return.
    if (!weather) {
        log.debug "Something went wrong, no data found." 
        return false
    }
    
    def windspeed = weather.current_observation.wind_mph
    def tempf = weather.current_observation.temp_f
    def hourlyprecip = Float.parseFloat(weather.current_observation.precip_1hr_in)

    log.debug "Actual: ${windspeed}, ${tempf}, ${hourlyprecip}"
    log.debug "Threshholds: ${highwindspeed}, ${hightempf}, ${lowtempf}, ${hourlyprecip}"
    log.debug "State: ${state.lastHighWindSpeed}, ${state.lasthightempf}, ${state.lastlowtempf}, ${state.lasthourlyprecip}"
	log.debug "${settings.windymsg} ${settings.warmmsg} ${settings.coldmsg} ${settings.wetmsg}"
    
    if (windspeed > highwindspeed) {
        if (windspeed >= state.lastHighWindSpeed + 3.0 ) {
            state.lastHighWindSpeed = windspeed
            notifyPeople("${settings.windymsg} ${windspeed}")
        } else {log.debug "not enough wind speed change"}
    } else { state.lastHighWindSpeed = windspeed }

    if (tempf > hightempf) {
        if (tempf >= state.lasthightempf + 3.0 ) {
            state.lasthightempf = tempf
            notifyPeople("${settings.warmmsg} ${tempf}F")
        } else {log.debug "not enough high temp change"}
    } else { state.lasthightempf = tempf }

    if (tempf < lowtempf) {
        if (tempf <= state.lastlowtempf - 3.0 ) {
            state.lastlowtempf = tempf
            notifyPeople("${settings.coldmsg} ${tempf}F")
        } else { log.debug "not enough low temp change" }
    } else { state.lastlowtempf = tempf }

    if (hourlyprecip > highrainfall) {
        if (hourlyprecip  >= state.lasthourlyprecip + 1.0 ) {
            state.lasthourlyprecip = hourlyprecip 
            notifyPeople("${settings.wetmsg} ${hourlyprecip}in.")
        } else {log.debug "not enough precip change"}
    } else { state.lasthourlyprecip = hourlyprecip }
}

def setDefaultWeather() {
    state.lastHighWindSpeed = 0.0
    state.lasthightempf = 65.0
    state.lastlowtempf = 40.0
    state.lasthourlyprecip = 0.0
    log.debug "state: ${state.lastHighWindSpeed}, ${state.lasthightempf}, ${state.lastlowtempf}, ${state.lasthourlyprecip}"
}

private notifyPeople(message) {

    def t0 = new Date(now() - (8 * 60 * 60 * 1000))
	def h0 = t0.getHours()

	if (h0 >= 6 && h0 <= 22)
    {
        log.debug "Weather event to be reported: ${message}"
        send(message)
    } else {
    	log.debug "Swallowing notification during off hours: ${t0.getHours()}"
    }
}

private send(message) {
	sendPush (message)
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