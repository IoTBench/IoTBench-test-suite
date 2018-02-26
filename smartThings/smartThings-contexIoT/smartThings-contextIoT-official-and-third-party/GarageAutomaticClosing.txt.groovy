definition(
    name: "Garage Automatic Closing",
    namespace: "Arno",
    author: "Arno",
    description: "Closes Garage Door at Sunset, Specific Time or Mode Change. Will notify on failures and attempt to close the Garage Door again.",
    category: "Safety & Security",
    iconUrl: "http://solutionsetcetera.com/stuff/STIcons/GDO.png",
    iconX2Url: "http://solutionsetcetera.com/stuff/STIcons/GDO@2x.png"
)

preferences
{
    section("Garage Door Controls:")
    	{
		input "doorSensor", "capability.contactSensor", title: "Which Sensor?", required: true, multiple: false
		input "doorSwitch", "capability.momentary", title: "Which Door?", required: true, multiple: false
		}
	section ("Close at Sunset?")
    	{
    	input "closeSunsetAsk", "bool", title: "Garage door closing at sunset?", required: true
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false, description: "Optional"
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]], description: "Optional"
		input "zipCode", "text", title: "Zip Code", required: false, description: "Optional"
		}
    section ("Close at Sepecific Time?")
		{
		input "closeTimeAsk", "bool", title: "Garage door closing at specific time?", required: true
		input "closeTimeSet", "time", title: "When?", required: false
		}
	section ("Close when Entering Specific Mode?")
		{
		input "closeModeAsk", "bool", title: "Garage door closing when entering specific mode", required: true
		input "closeModeSet", "mode", title: "Mode?", required: false, multiple: true
		}
	section( "Notifications" )
    	{
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: true
		}
}

def installed()
{
	log.debug "Installed with settings: ${settings}"
    unsubscribe()
    subscribeToEvents()
    initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	unschedule()
    subscribeToEvents()
	initialize()    
}

def subscribeToEvents()
{
	if (closeModeSet)
    	{
		subscribe(location, modeChangeHandler)
		}
}

def initialize()
{
	if (closeSunsetAsk == true)
		{
        state.sunsetClose = "OK"
        scheduleAstroCheck()
		astroCheck()
        log.debug "Garage door scheduled to close at Sunset."
        }

	if (closeTimeAsk == true && closeTimeSet != null && closeTimeSet != "")
    	{
        state.timeClose = "OK"
        schedule(closeTimeSet, "closeTime")
        log.debug "Garage door scheduled to close at $closeTimeSet."
        }
	if (closeModeAsk == true && closeModeSet != null && closeModeSet != "")
    	{
        state.modeClose = "OK"
       	log.debug "Garage door scheduled to close when entering one of these modes: $closeModeSet."
        }
}

def scheduleAstroCheck()
{
	def min = Math.round(Math.floor(Math.random() * 60))
	def exp = "0 $min * * * ?"
    log.debug "$exp"
	schedule(exp, astroCheck)
    state.hasRandomSchedule = true
}

def astroCheck()
{
	if (!state.hasRandomSchedule && state.riseTime)
    {
    	log.info "Rescheduling random astro check"
        unschedule("astroCheck")
    	scheduleAstroCheck()
    }
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def setTime = s.sunset
	log.debug "setTime: $setTime"
	if (state.setTime != setTime.time)
    	{
		state.setTime = setTime.time
		unschedule("sunsetHandler")
		if (setTime.after(now))
        	{
			log.info "scheduling sunset handler for $setTime"
			runOnce(setTime, sunsetHandler)
			}
		}
}

def sunsetHandler()
{
	if (state.sunsetClose == "OK")
    	{
		log.debug "Closing Garage Door at Sunset..."
    	if (doorSensor.currentContact == "open")
    		{
			if (sendPushMessage == "Yes")
        		{
    			sendPush "$doorSensor is closing at Sunset..."
            	}
			closeDoor()
    		unschedule("sunsetHandler")
			}
    	else
    		{
			log.debug "$doorSensor was already closed."
			}
		}
}

def closeTime()
{
	if (state.timeClose == "OK")
    	{
		log.debug "Closing Garage Door at $closeTimeSet..."
		if (doorSensor.currentContact == "open")
    		{
        	if (sendPushMessage == "Yes")
        		{
    			sendPush "$doorSensor is closing at scheduled time ${closeTimeSet}..."
            	}
			closeDoor()
    		unschedule("closeTime")
        	}
		else
    		{
			log.debug "$doorSensor was already closed."
			}
		}
}

def modeChangeHandler(evt)
{
	log.debug "Closing Garage Door when entering mode $evt.value..."
	if (evt.value in closeModeSet)
    	{
        state.triggeredMode = "$evt.value"
        log.debug "Mode changed to ${state.triggeredMode}."
        closeMode()
		}
}

def closeMode()
{
	if (state.modeClose == "OK")
    	{
		log.debug "Closing Garage Door when entering mode ${state.triggeredMode}..."
    	if (doorSensor.currentContact == "open")
    		{
    		if (sendPushMessage == "Yes")
        		{
    			sendPush "$doorSensor is closing when entering mode ${state.triggeredMode}..."
            	}
            closeDoor()
            unschedule("closeMode")
        	}
		else
    		{
			log.debug "$doorSensor was already closed."
			}
		}
}

def closeDoor()
{
	log.trace "Closing Garage Door..."
	if (doorSensor.currentContact == "open")
    	{
		log.debug "$doorSensor was open."
        doorSwitch.push()
		verifyClosing()
		}
    else
    	{
		log.debug "$doorSensor was closed."
		}
}

def verifyClosing()
{
	log.debug "Verify Closing..."
    if (doorSensor.currentContact == "closed")
    	{
        unschedule("problemClosing")
		if (sendPushMessage != "No")
			{
            log.debug "Sending push message..."
			sendPush "$doorSensor is closed."
			}
		}
	else
    	{
    	log.debug "$doorSensor is still open."
        runIn(10, confirmClosing)
        runIn(60, problemClosing)
    	}
}

def confirmClosing()
{
	log.debug "Confirm Closing..."
	if (doorSensor.currentContact == "closed")
    	{
        log.debug "Verifying closing..."
        verifyClosing()
        }
	else
    	{
        verifyClosing()
        }
}

def problemClosing()
{
	log.debug "Problem closing Garage Door..."
	if (doorSensor.currentContact == "open")
    	{
        log.debug "Sending push message..."
        sendPush "$doorSensor is still open, attempting to close garage door again!"
        closeDoor()
        }
	else
    	{
        verifyClosing()
        }
}

private getSunsetOffset()
{
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}