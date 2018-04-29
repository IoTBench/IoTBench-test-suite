/**
 * Christmas Tree Water Monitor
 *
 * Copyright 2016 Steve White
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 *
 */

definition(
	name: "Christmas Tree Water Monitor",
	namespace: "shackrat",
	author: "Steve White",
	description: "Notifies when christmas tree water level drops to prevent it from drying out.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png",
	iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png"
)


preferences
{
	page (name: "mainPage", title: "Create a new Christmas Tree Monitor")
	page (name: "runAtTime", title: "Christmas Tree Monitor")
	page (name: "namePage", title: "Christmas Tree Monitor")
}



def installed()
{
	log.info "IrisUsers.com Christmas Tree Monitor Installed"
	unschedule()
	initialize()
}


def updated()
{
	log.info "IrisUsers.com Christmas Tree Monitor Updated"
	unsubscribe()
	unschedule()
	initialize()
}


def initialize()
{
	log.info "IrisUsers.com Christmas Tree Monitor Initialized"

	subscribe(leakDetector, "water", handleWaterEvent)
	if (alwaysOff == true)
	{
    log.trace "Subscribing to switch"
		subscribe(treeSwitch, "switch.on", handleTreeOffEvent)
	}
}


def handleWaterEvent(evt)
{
	if (!allowedToRun)
    {
    	log.trace "${app.name}: water event not allowed."
    	return
	}
   
	if (evt.value == "dry")
	{
		log.info "Detected ${evt.device} is DRY!!!" + (treeSwitch ? ", turning tree OFF in ${treeOffAfter} minutes" : "") +  (sendPush ? ", sending notification in ${pushAfter} minutes" : "")
		if (treeSwitch)
        {
			if (treeOffAfter > 0)
			{
				runIn(treeOffAfter * 60, treeOff)
			}
			else
			{
				treeOff()
			}
        }
        
		if (sendPush)
		{
            runIn(pushAfter * 60, doPush)
		}
	}
    
	else
	{     
		log.info "Detected ${evt.device} water level ok."
		unschedule()
	}
}


def handleTreeOffEvent(evt)
{
	if (leakDetector.currentValue("water") == "dry")
	{
		treeOff()
	}
}


def treeOff()
{
	treeSwitch.off()
}

def doPush()
{
	sendPush("${leakDetector} has been DRY longer than ${pushAfter} minutes!")
}



// App allowed to run?
private getAllowedToRun()
{
	allowedMode && allowedDays && allowedTime
}



def mainPage()
{
	dynamicPage(name: "mainPage", title: "Create a new Christmas Tree Monitor", uninstall: true, install: false, nextPage: "namePage")
    {
		section("Water Sensor")
		{
			input "leakDetector", "capability.waterSensor", title: "Select a leak sensor to monitor:", required: true, multiple: false
		}

		section("Control")
		{
			input "treeSwitch", "capability.switch", title: "Turn off the Christmas tree when dry?", required: true, multiple: false, submitOnChange: true
			if (treeSwitch)
        	{
				input "treeOffAfter", "number", title: "... after (n) minutes", required: false, default: 0
				input "alwaysOff", "bool", title: "Keep tree off while dry?", required: false, default: false
			}
		}
        
		section("Alerts")
		{
			input "sendPush", "bool", title: "Send a push notification?", defaultValue: false, submitOnChange: true
			if (sendPush == true)
        	{
        		input "pushAfter", "number", title: "... when dry longer than (n) minutes:", required: false, default: 0
			}
		}
    
    	section("Options")
    	{
			def timeLabel = formatTimeLabel()

			href "runAtTime", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null
			input "runDays", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}
	}
}



def runAtTime()
{
	dynamicPage(name:"runAtTime", title: "Only during a certain time", uninstall: false)
	{
		section()
		{
			input "startingAt", "enum", title: "Starting at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if (startingAt in [null, "A specific time"])
				input "startingTime", "time", title: "Start time", required: false
			else
            {
				if (startingAt == "Sunrise")
					input "startSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if (startingAt == "Sunset")
					input "startSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
		
		section()
		{
			input "endingAt", "enum", title: "Ending at", options: ["A specific time", "Sunrise", "Sunset"], defaultValue: "A specific time", submitOnChange: true
			if (endingAt in [null, "A specific time"])
				input "endingTime", "time", title: "End time", required: false
			else
			{
				if (endingAt == "Sunrise")
					input "endSunriseOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
				else if (endingAt == "Sunset")
					input "endSunsetOffset", "number", range: "*..*", title: "Offset in minutes (+/-)", required: false
			}
		}
	}
}



// page for allowing the user to give the automation a custom name
def namePage()
{
	def l = "${leakDetector} Automation"
	if (!overrideLabel)
	{
		// if the user selects to not change the label, give a default label
		log.debug "will set default label of $l"
		app.updateLabel(l)
	}

	dynamicPage(name: "namePage", uninstall: true, install: true)
	{
		if (overrideLabel)
		{
			section("Automation name")
			{
				label title: "Enter custom name", defaultValue: l, required: false
			}
		}
		else
		{
			section("Automation name")
			{
				paragraph app.label
			}
		}
		section
		{
			input "overrideLabel", "bool", title: "Edit automation name", defaultValue: "false", required: "false", submitOnChange: true
		}
	}
}


private formatTimeLabel()
{
	def result = ""
    
	if (startingAt == "Sunrise") result = startingAt + offset(startSunriseOffset)
	else if (startingAt == "Sunset") result = startingAt + offset(startSunsetOffset)
	else if (startingTime) result = hhmm(startingTime)
    
    result += " to "
    
 	if (endingAt == "Sunrise") result += endingAt + offset(endSunriseOffset)
	else if (endingAt == "Sunset") result += endingAt + offset(endSunsetOffset)
	else if (endingTime) result += hhmm(endingTime, "h:mm a z")   
}


private getAllowedDays()
{
	def result = true
	if (runDays)
    {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) df.setTimeZone(location.timeZone)
		else df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		def day = df.format(new Date())
		result = days.contains(day)
	}
	return result
}


private getAllowedTime()
{
	def result = true
	if ((startingTime && endingTime) ||
	(startingTime && endingAt in ["Sunrise", "Sunset"]) ||
	(startingAt in ["Sunrise", "Sunset"] && endingTime) ||
	(startingAt in ["Sunrise", "Sunset"] && endingAt in ["Sunrise", "Sunset"]))
	{
		def currTime = now()
		def start = null
		def stop = null

		def startSunTime = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: startSunriseOffset, sunsetOffset: startSunsetOffset)
		if (startingAt == "Sunrise") start = startSunTime.sunrise.time
		else if (startingAt == "Sunset") start = startSunTime.sunset.time
		else if (startingTime) start = timeToday(startingTime,location.timeZone).time

		endSunTime = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: endSunriseOffset, sunsetOffset: endSunsetOffset)
		if (endingAt == "Sunrise") stop = endSunTime.sunrise.time
		else if (endingAt == "Sunset") stop = endSunTime.sunset.time
		else if (endingTime) stop = timeToday(endingTime,location.timeZone).time
		
        result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	return result
}


private getAllowedMode()
{
	def result = !modes || modes.contains(location.mode)
	log.trace "AllowedMode = $result"
	return result
}


private offset(value)
{
    def result = value ? ((value > 0 ? "+" : "") + value + " min") : ""
}


private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}