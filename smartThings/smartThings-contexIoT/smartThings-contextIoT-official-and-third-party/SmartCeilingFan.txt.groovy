/**
 *  Smart Ceiling Fans
 *
 *  Copyright 2014 Barry A. Burke
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
    name: "Smart Ceiling Fan",
    namespace: "Convenience",
    author: "Barry A. Burke",
    description: "Monitor temperature and humidity, and if it gets too warm or muggy, crank up the old ceiling fan. Activates a dimmer-controlled fan (e.g. Leviton 3-way) on motion, optionally on door open, stops on inactivity for defined number of minutes, or on door close. Uses specific Temperature and Humidity monitors (e.g., Aeon Multisensor, Thermostat, SmartWeather Station, etc.), optionally calculates & uses Heat Index. Temp/heat index defines settable fan speeds (hotter = faster).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Turn on when..."){
		input "motion1", "capability.motionSensor", title: "There is motion here", required: true
        input "contact1", "capability.contactSensor", title: "Or this door opens", required: false
        input "minutes1", "number", title: "Off after (minutes)?"
    }    
	section("Control which fan..."){
		input "theFan", "capability.switchLevel", title: "Fan dimmer", multiple: false
        input "fanSlow", "number", title: "Slow speed?"
        input "fanMedium", "number", title: "Medium speed?"
        input "fanFast", "number", title: "Fast speed?"
	}	
    
    section("Use these sensors... "){
		input "tempSensor", "capability.temperatureMeasurement", title: "Temperature Sensor", required: true
        input "humidSensor", "capability.relativeHumidityMeasurement", title: "Humidity Sensor", required: true
       	input "humidBoost", "bool", title: "Use Heat Index?", required: true
    }
    section("Use these temp settings...") {
		input "tempSlow", "number", title: "Temp Slow?"
        input "tempMedium", "number", title: "Temp Medium?"
        input "tempFast", "number", title: "Temp Fast?"
	}

//	section("Your Zipcode..."){
//		input "zipcode", "text", title: "Zipcode?"

}

def installed()
{
	log.debug "Installed with settings: ${settings}"
    
    initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}"
    
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	log.debug "Initializing..."

    state.lastTemp = 0
    state.lastHumid = 0
	state.inactiveAt = null  
    state.turnedOff = false    // assume we haven't been turned off yet
    
    subscribe(motion1, "motion", motionHandler)
    subscribe(contact1, "contact", motionHandler)  // let's try being tricky - common event handler
    subscribe(theFan, "switch", fanHandler)

	schedule("0 */5 * * * ?", "scheduleCheck")		// Only every 5 minutes
}

def motionHandler(evt) {	
    log.debug "$evt.name: $evt.value"
    
    if ( state.turnedOff ) return		// Manually turned off after we turned on  						
    
    if ((evt.value == "active") || (evt.value == "open") || (evt.value == "justChecking")) {
		def nowTemp = Math.round(tempSensor.latestValue( "temperature" ))
        def nowHumid = humidSensor.latestValue( "humidity" )
        
        if ((nowTemp == state.lastTemp) && (humidBoost && (nowHumid == state.lastHumid))) return // No changes, bail
        
        state.lastTemp = nowTemp
        state.lastHumid = nowHumid

        if (humidBoost && (nowTemp > 73)) {		//Heat Index not accurate below 80 degrees F, but we fudge it here
            def heatIndex = Math.round( -42.379 + (2.04901523*nowTemp) + (10.14333127*nowHumid) - (0.22475541*(nowTemp*nowHumid)) - (0.00683783*(nowTemp**2)) - (0.05481717*(nowHumid*nowHumid)) + (0.00122874*((nowTemp**2)*nowHumid)) + (0.00085282*(nowTemp*(nowHumid**2))) - (0.00000199*((nowTemp*nowHumid)**2)))
			
            log.debug "Temp: ${nowTemp}, Humid: ${nowHumid}, HeatIndex: ${heatIndex}"

			if ( heatIndex > nowTemp) nowTemp = heatIndex
        }
          
        def fanSpeed = theFan.latestValue( "level" ) // in case someone changed it manually (boy, are we going to mess them up :o)
        
//        state.turnedOff = false
        
        if ((nowTemp < tempSlow) && (fanSpeed != 0)) {
        	log.debug "It has cooled down, turning off the fan"
        	theFan.off()
            state.inactiveAt = null
            state.turnedOff = true
        }
        else if ((nowTemp < tempMedium) && (fanSpeed != fanSlow)) {
        	log.debug "Turning fan on Slow"
        	theFan.setLevel(fanSlow)
        }
        else if ((nowTemp < tempFast) && (fanSpeed != fanMedium)) {
        	log.debug "Turning fan on Medium"
        	theFan.setLevel(fanMedium)
        }
        else if (fanSpeed != fanFast) {
        	log.debug "Turning fan on Fast"
        	theFan.setLevel(fanFast)
        }
        if ( evt.value != "justChecking" ) state.inactiveAt = null
    }
    else if (evt.value == "closed") {
       	log.debug "Turning off the fan (door closed)"
        
    	theFan.off()
        state.lastTemp = 0
        state.lastHumid = 0
   		state.inactiveAt = null
        state.turnedOff = true
    }
    else if (evt.value == "inactive") {
		if (!state.inactiveAt) {
			state.inactiveAt = now()
		}
  	}
}

def fanHandler( evt ) {
	if ( evt.value == "off" ) {
    	log.debug "Somebody turned off the fan"
        
        state.lastTemp = 0
        state.lastHumid = 0
        state.inactiveAt = now()			// start the timer - don't re-enable until we timeout again
        state.turnedOff = true
    }
    else if (evt.value == "on") {
    	log.debug "Somebody turned the fan back on"
    	state.turnedOff = false
        state.inactiveAt = null			// obviously, somebody wants the fan on...
        state.lastTemp = 0					// let's figure out how fast we need to be
        state.lastHumid = 0
    }
}

def scheduleCheck() {
	log.debug "schedule check, ts = ${state.inactiveAt}"
	if (state.inactiveAt) {
		def elapsed = now() - state.inactiveAt
		def threshold = 1000 * 60 * minutes1
		if (elapsed >= threshold) {
			log.debug "Turning off the fan (idle)"
			theFan.off()
            state.lastTemp = 0
        	state.lastHumid = 0
			state.inactiveAt = null
            state.turnedOff = false		// This will reset things so we can turn it back on again...
		}
		else {
			log.debug "${elapsed / 60000} minutes since motion stopped"
            def evt = [ name: "hack", value: "justChecking" ]  			// Been idle for 5 minutes, let's do a temperature check
	        motionHandler( evt )         								// just in case things have changed
		}
	}
}