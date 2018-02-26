/**
 *  LittleLight
 *
 *  Copyright 2014 Chris B
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "LittleLight",
    namespace: "sirhcb",
    author: "Chris B",
    description: "This Little Light of Mine: Turn on a light after sunset if it's been accidentally turned off.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	
     section("When this switch is turned off..."){
		input "switch1", "capability.switch", title: "Which?"
     }
	 section("Turn it back on after how many minutes?") {
        input ('time', 'number', title: 'Enter 0 to turn on immedately')
     }
     section("But only after dark for this area.") {
    	input ('zip', 'number', title: 'Zip Code')
     }        
     section("Automatically turn....") {
     	input ('onAtSet', 'bool', title: 'On at Sunset?')
        input ('offAtRise', 'bool', title: 'Off at Sunrise?')
     }   
}


def installed() {
	initialize()				// Run the initialization procedure
}

def updated() {
	unsubscribe()				// Unsubscribe to any subscriptions
//	unschedule()				// Normally we'd also unschedule becuase astrocheck is a schedule procedure, but unschedule handled in astroCheck method/procedure already
	initialize()				// Run the initialization procedure
}

def initialize() {
	subscribe(switch1, "switch.off", switchOffHandler)

	subscribe(location, "position", locationPositionChange)
	subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
	subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
	
	astroCheck()
}



def switchOffHandler(evt) {

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
    	log.debug ("Too much light, not turning back on" )
    	}
    else {       
    	log.trace ("Turning back on soon...")
        def delay = time * 60  /* runIn uses seconds, so multiply time (minutes) * 60 (for seconds)  */
        runIn (delay, switchOn)
    }
}

def switchOn (){
	switch1.on()
}



def locationPositionChange(evt) {
	log.trace "locationChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	log.trace "sunriseSunsetTimeHandler()"
	astroCheck()
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def riseTime = s.sunrise
	def setTime = s.sunset
	log.debug "riseTime: $riseTime"
	log.debug "setTime: $setTime"

	if (state.riseTime != riseTime.time) {
		unschedule("sunriseHandler")

		if(riseTime.before(now)) {
			riseTime = riseTime.next()
		}

		state.riseTime = riseTime.time

		log.info "scheduling sunrise handler for $riseTime"
		schedule(riseTime, sunriseHandler)
	}

	if (state.setTime != setTime.time) {
		unschedule("sunsetHandler")

	    if(setTime.before(now)) {
	        setTime = setTime.next()
	    }

		state.setTime = setTime.time

		log.info "scheduling sunset handler for $setTime"
	    schedule(setTime, sunsetHandler)
	}
}

def sunriseHandler() {
	log.info "Executing sunrise handler"
	if (offAtRise) {
		runIn (60,switch1.off)					// A slight delay in put in to prevent a possible loop of the switch turning off and then back on repeatedly.
	}
}

def sunsetHandler() {
	log.info "Executing sunset handler"
	if (onAtSet) {
		switch1.on()
	}
}