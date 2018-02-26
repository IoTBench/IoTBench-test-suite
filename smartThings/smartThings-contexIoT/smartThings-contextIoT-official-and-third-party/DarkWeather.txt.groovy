/**
 *  Dark Weather
 *
 *	This app monitors weather conditions for rain or a dark sky and will set the 
 *	house into a desired mode in order for the lights to work during the day.
 *
 *  Author: Aaron Crocco
 */
 
 
// Automatically generated. Make future change here.
definition(
    name: "Dark Weather",
    namespace: "MacStainless",
    author: "Aaron Crocco",
    description: "Monitors weather for rain and changes house into night mode during the day. When the rain is done, house will change back to daytime mode and can turn the lights off. If the rain stops after sundown, the house will remain on night mode.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true)

preferences {
	section("Choose a weather sensor... "){
		input "sensor", "capability.waterSensor", title: "Sensor?"
	}
    section("When it rains change mode to...") {
		input "rainMode", "mode", title: "Mode?"
	}
    section("When it's all clear change mode to...") {
		input "clearMode", "mode", title: "Mode?"
	}
    section("and (optionally) turn off these lights...") {
    	input "switches", "capability.switch", multiple: true, required: false
    }
 } 
 

def installed() {
    log.debug "Water installed: $sensor.currentWater"
    subscribe (sensor, "water", waterHandler)   
    initialize()
}

def updated() {
	unsubscribe()
    log.debug "Water updated: $sensor.currentWater"
    subscribe (sensor, "water", waterHandler)
    initialize()
}

def initialize() {

	//Schedule a check of the weather every minute
    def freq = 5
	schedule("0 0/$freq * * * ?", checkWeather)
}


def waterHandler(evt) {

	//When water sensor changes state, call weatherModeChange
	log.debug "Water Sensor triggered!"
    weatherModeChange(evt)

}


def checkWeather() {

	//Gets the current weather conditions
    def weather = getWeatherFeature( "conditions" )
	def currentConditions = weather.current_observation.icon
    def wet="wet"
    def dry="dry"
    def rain="rain"
    def snow="snow"
    def clear="clear"	//This is a dummy parameter to pass for weatherModeChange when the sky is clear.

	log.debug "It's currently $currentConditions"
	   
	if ((currentConditions=="rain")||(currentConditions=="tstorms")||(currentConditions=="snow")||(currentConditions=="sleet")) {
		if (location.mode != rainMode) {
    		log.debug "conditions require light"
			if ((currentConditions=="snow")||(currentConditions=="sleet")) {
            	weatherModeChange(wet,snow)
			}
            else { weatherModeChange(wet,rain) }
		}
 	}
    
	else if (location.mode == rainMode) {
    	log.debug "conditions do not need light"
       	weatherModeChange(dry,clear)
	}
}


def weatherModeChange(evt,reason) {

	log.debug "Weather mode change has started. Current event value is $evt"
        
	if (evt =="wet") {
    	log.debug "Wet / rain!"     
        if (location.mode != rainMode) {
    		setLocationMode(rainMode)
            log.debug "Mode changed."
            if (reason=="rain") {
        		sendPush("It's raining & dark out! Changing mode to $rainMode.")
                log.debug "Rain message sent."
			}
			if (reason=="snow") {
        		sendPush("It's snowing & dark out! Changing mode to $rainMode.")
                log.debug "Rain message sent."
			}
		}
	}
	else if (evt == "dry" ) {
    	log.debug "Dry!"  
        
        //Check current time to see if it's after sundown.
        def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
		def now = new Date()
		def setTime = s.sunset
		log.debug "Sunset is at $setTime. Current time is $now"
        
        
        if (setTime.after(now)) {	//Executes only if it's before sundown.
		
         	if (location.mode != clearMode) {
    			setLocationMode(clearMode)
                switches?.off()
        		sendPush("The sky is clear! Turning lights off and changing mode to $clearMode.")
				log.debug "Mode changed, dry message sent."
			}        
        }
	}
}
