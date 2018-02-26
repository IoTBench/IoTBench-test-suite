/**
 *  Smart Humidifier
 *
 *  Copyright 2014 Sheikh Dawood
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
    name: "Smart Dehumidifier",
    namespace: "Sheikhsphere",
    author: "Sheikh Dawood",
    description: "Turn on/off dehumidifier based on relative humidity from a sensor.",
    category: "Convenience",
   iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather7-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Weather.weather7-icn?displaySize=2x"
)


preferences {
	section("Devices to sense and control humidity:") {
		input "humiditySensor1", "capability.relativeHumidityMeasurement", title: "Humidity Sensor"
        input "switch1", "capability.switch", title: " Dehumidifier Switch"
	}
	section("Settings:") {
		input "humidityHigh", "number", title: "Turn on when the humidity rises above (%):"
        input "humidityLow", "number", title: "Turn off when the humidity drops below (%):"
        input "delay", "number", title: "Polling delay (minutes):", required: true, defaultValue: 30
	}
    section( "Notifications" ) {
        input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
        input "phone1", "phone", title: "Send a Text Message?", required: false
    }
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
	state.lastSwitchStatus = null
	state.lastHumidity = null
	subscribe(humiditySensor1, "humidity", humidityHandler)
    subscribe(switch1, "switch", switchHandler)
	scheduleChecks()
    statusCheck()
}

def scheduleChecks() {
	def sec = Math.round(Math.floor(Math.random() * 60))
	def exp1 = "$sec */${delay} * * * ?"
    //log.debug "$exp"
	schedule(exp1, statusCheck)
}

def statusCheck() {
	log.debug "state.lastHumidity: $state.lastHumidity, humidityHigh: $humidityHigh, humidityLow: $humidityLow"
    //send ("state.lastHumidity: $state.lastHumidity, humidityHigh: $humidityHigh, humidityLow: $humidityLow")
    switch1?.poll()
    if (state.lastHumidity) {
        if (state.lastHumidity >= humidityHigh) {
            if (state.lastSwitchStatus != "on") {
                //log.debug "Humidity Rose Above $humidityHigh1:  sending SMS to $phone1 and deactivating $mySwitch"
                //send("${humiditySensor1.label} sensed high humidity level of ${state.lastHumidity}, turning on ${switch1.label}")
                //send("*${switch1.label} is supposed to be on")
                switch1?.on()
                state.lastSwitchStatus = "on"
            }
        }
        else if (state.lastHumidity <= humidityLow) {
            if (state.lastSwitchStatus != "off") {
                //log.debug "Humidity Dropped Below $humidityLow1:  sending SMS to $phone1 and activating $mySwitch"
                //send("${humiditySensor1.label} sensed low humidity level of ${state.lastHumidity}, turning off ${switch1.label}")
                //send("*${switch1.label} is supposed to be off")
                switch1?.off()
                state.lastSwitchStatus = "off"
            }
        }
    }
}

def switchHandler(evt) {
    log.debug "$evt.name: $evt.value: $evt.displayName"
    //send ("$evt.name: $evt.value: $evt.displayName")
    //if (evt.name=="switch") 
    state.lastSwitchStatus = evt.value
}


def humidityHandler(evt) {
	log.trace "humidity: $evt.value"
    log.trace "set high point: $humidityHigh"
    log.trace "set low point: $humidityLow"
    //send ("$state.lastSwitchStatus $evt.name: $evt.value: $evt.displayName")

	def currentHumidity = Double.parseDouble(evt.value.replace("%", ""))
	//def humidityHigh1 = humidityHigh 
    //def humidityLow1 = humidityLow 
	//def mySwitch = settings.switch1
    
    state.lastHumidity = currentHumidity

	if (currentHumidity >= humidityHigh) {
		log.debug "Checking how long the humidity sensor has been reporting > $humidityHigh1"

		// Don't send a continuous stream of text messages
		//def deltaMinutes = 10 
		//def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		//def recentEvents = humiditySensor1.eventsSince(timeAgo)
		//log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		//def alreadySentSms1 = recentEvents.count { Double.parseDouble(it.value.replace("%", "")) > humidityHigh1 } > 1

		//if (alreadySentSms1) {
		//	log.debug "Notification already sent within the last $deltaMinutes minutes"

		//} else {
         	if (state.lastSwitchStatus != "on") {
                log.debug "Humidity Rose Above $humidityHigh1:  sending SMS to $phone1 and deactivating $mySwitch"
                //send("${evt.displayName} sensed high humidity level of ${evt.value}, turning on ${switch1.label}")
                send("It's too humid! Turning on ${switch1.label}")
                switch1?.on()
                state.lastSwitchStatus = "on"
            }
		//}
	}
    else if (currentHumidity <= humidityLow) {
		log.debug "Checking how long the humidity sensor has been reporting < $humidityLow1"

		// Don't send a continuous stream of text messages
		//def deltaMinutes = 10 
		//def timeAgo = new Date(now() - (1000 * 60 * deltaMinutes).toLong())
		//def recentEvents = humiditySensor1.eventsSince(timeAgo)
		//log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaMinutes minutes"
		//def alreadySentSms2 = recentEvents.count { Double.parseDouble(it.value.replace("%", "")) < humidityLow1 } > 1

		//if (alreadySentSms2) {
		//	log.debug "Notification already sent within the last $deltaMinutes minutes"

		//} else {
        	if (state.lastSwitchStatus != "off") {
                log.debug "Humidity Dropped Below $humidityLow1:  sending SMS to $phone1 and activating $mySwitch"
                //send("${evt.displayName} sensed low humidity level of ${evt.value}, turning off ${switch1.label}")
                send("It's too dry! Turning off ${switch1.label}")
                switch1?.off()
                state.lastSwitchStatus = "off"
            }
		//}
	}
    else {
    	//log.debug "Humidity remained in threshold:  sending SMS to $phone1 and activating $mySwitch"
		//send("${humiditySensor1.label} sensed humidity level of ${evt.value} is within threshold, keeping on ${switch1.label}")
    	//switch1?.on()
    }
}

private send(msg) {
    if ( sendPushMessage == "Yes" ) {
        log.debug( "sending push message" )
        sendPush( msg )
    }

    if ( phone1 ) {
        log.debug( "sending text message to $phone 1" )
        sendSms( phone1, msg )
    }

    log.debug msg
}