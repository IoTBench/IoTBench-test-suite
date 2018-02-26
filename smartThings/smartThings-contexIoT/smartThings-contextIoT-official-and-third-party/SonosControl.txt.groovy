definition(
    name: "Sonos Control",
    namespace: "Arno",
    author: "Arno",
    description: "Play, Pause, Mute, Umute your Sonos when certain actions take place in your home.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/sonos.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/sonos@2x.png"
)

preferences
{
	page(name: "mainPage", title: "Control your Sonos when something happens...", install: true, uninstall: true)
	page(name: "timeIntervalInput", title: "Only during a certain time")
    	{
		section
        	{
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
			}
		}
    page(name: "noMovement", title: "When movement Stops for X minute(s) here:")
    	{
        section
        	{
            input "motionSensors", "capability.motionSensor", title: "Where?", multiple: true, required: false
            input "minutesLater", "number", title: "Minutes?", required: true
            }
		}
	page(name: "movement", title: "When movement movement detected here:")
    	{
        section
        	{
            input "motion", "capability.motionSensor", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "contactOpen", title: "When contact opens here:")
    	{
        section
        	{
            input "contactOpened", "capability.contactSensor", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "contactClose", title: "When contact closes here:")
    	{
        section
        	{
            input "contactClosed", "capability.contactSensor", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "accelerationDetected", title: "When acceleration detected here:")
    	{
        section
        	{
            input "acceleration", "capability.accelerationSensor", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "switchTurnOn", title: "When switch turns on:")
    	{
        section
        	{
            input "switchOn", "capability.switch", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "switchTurnOff", title: "When switch turns on:")
    	{
        section
        	{
            input "switchOff", "capability.switch", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "arrival", title: "When this person arrives:")
    	{
        section
        	{
            input "arrivalPresence", "capability.presenceSensor", title: "Who?", multiple: true, required: false
            }
		}
	page(name: "departure", title: "When this person leaves:")
    	{
        section
        	{
            input "departurePresence", "capability.presenceSensor", title: "Who?", multiple: true, required: false
            }
		}
	page(name: "smokeDetected", title: "When smoke detected here:")
    	{
        section
        	{
            input "smoke", "capability.smokeDetector", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "waterDetected", title: "When humidity detected here:")
    	{
        section
        	{
            input "water", "capability.waterSensor", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "buttonPressed", title: "When button is pressed:")
    	{
        section
        	{
            input "button", "capability.button", title: "Where?", multiple: true, required: false
            }
		}
	page(name: "newMode", title: "When entering mode:")
    	{
        section
        	{
            input "triggerModes", "mode", title: "Mode(s)?", multiple: true, required: false
            }
		}
	page(name: "scheduledTime", title: "When time is:")
    	{
        section
        	{
            input "timeOfDay", "time", title: "When?", required: false
            }
		}
}

def mainPage()
{
	dynamicPage(name: "mainPage")
    	{
        /*def anythingSet = anythingSet()
		if (anythingSet)
        	{*/
        section ("When...")
        	{
            if (motionSensors != null && minutesLater != null)
                {
                def noMovementLabel = "Trigger action when ${motionSensors ?: "motion sensor(s)"} is/are inactive for ${minutesLater ?: "X"} minute(s)."
                href "noMovement", title: "Motion Stops:", description: noMovementLabel ?: "Tap to set", state: noMovementLabel ? "complete" : "incomplete"
                }
            else
                {
                def noMovementLabel = null
                href "noMovement", title: "Motion Stops:", description: noMovementLabel ?: "Tap to set", state: noMovementLabel ? "complete" : "incomplete"
                }
			if (motion != null)
            	{
                def movementLabel = "Trigger action when ${motion ?: "motion sensor(s)"} detects/detect motion."
                href "movement", title: "Motion Here:", description: movementLabel ?: "Tap to set", state: movementLabel ? "complete" : "incomplete"
                }
			else
            	{
                def movementLabel = null
                href "movement", title: "Motion Here:", description: movementLabel ?: "Tap to set", state: movementLabel ? "complete" : "incomplete"
                }
			if (contactOpened != null)
            	{
                def contactOpenLabel = "Trigger action when ${contactOpened ?: "contact(s)"} opens/open."
                href "contactOpen", title: "Contact Opens:", description: contactOpenLabel ?: "Tap to set", state: contactOpenLabel ? "complete" : "incomplete"
                }
			else
            	{
                def contactOpenLabel = null
                href "contactOpen", title: "Contact Opens:", description: contactOpenLabel ?: "Tap to set", state: contactOpenLabel ? "complete" : "incomplete"
                }
			if (contactClosed != null)
            	{
                def contactCloseLabel = "Trigger action when ${contactClosed ?: "contact(s)"} closes/close."
                href "contactClose", title: "Contact Closes:", description: contactCloseLabel ?: "Tap to set", state: contactCloseLabel ? "complete" : "incomplete"
                }
			else
            	{
                def contactCloseLabel = null
                href "contactClose", title: "Contact Closes:", description: contactCloseLabel ?: "Tap to set", state: contactCloseLabel ? "complete" : "incomplete"
                }
			if (acceleration != null)
            	{
                def accelerationLabel = "Trigger action when ${acceleration ?: "acceleration sensor(s)"} detects/detect acceleration."
                href "accelerationDetected", title: "Acceleration Detected:", description: accelerationLabel ?: "Tap to set", state: accelerationLabel ? "complete" : "incomplete"
                }
			else
            	{
                def accelerationLabel = null
                href "accelerationDetected", title: "Acceleration Detected:", description: accelerationLabel ?: "Tap to set", state: accelerationLabel ? "complete" : "incomplete"
                }
			if (switchOn != null)
            	{
                def switchOnLabel = "Trigger action when ${switchOn ?: "switch(s)"} turns/turn on."
                href "switchTurnOn", title: "Switch Turn On:", description: switchOnLabel ?: "Tap to set", state: switchOnLabel ? "complete" : "incomplete"
                }
			else
            	{
                def switchOnLabel = null
                href "switchTurnOn", title: "Switch Turn On:", description: switchOnLabel ?: "Tap to set", state: switchOnLabel ? "complete" : "incomplete"
                }
			if (switchOff != null)
            	{
                def switchOffLabel = "Trigger action when ${switchOff ?: "switch(s)"} turns/turn off."
                href "switchTurnOff", title: "Switch Turn Off:", description: switchOffLabel ?: "Tap to set", state: switchOffLabel ? "complete" : "incomplete"
                }
			else
            	{
                def switchOffLabel = null
                href "switchTurnOff", title: "Switch Turn Off:", description: switchOffLabel ?: "Tap to set", state: switchOffLabel ? "complete" : "incomplete"
                }
			if (arrivalPresence != null)
            	{
                def arrivalPresenceLabel = "Trigger action when ${arrivalPresence ?: "presence sensor(s)"} arrives/arrive."
                href "arrival", title: "Arrival Of:", description: arrivalPresenceLabel ?: "Tap to set", state: arrivalPresenceLabel ? "complete" : "incomplete"
                }
			else
            	{
                def arrivalPresenceLabel = null
                href "arrival", title: "Arrival Of:", description: arrivalPresenceLabel ?: "Tap to set", state: arrivalPresenceLabel ? "complete" : "incomplete"
                }
			if (departurePresence != null)
            	{
                def departurePresenceLabel = "Trigger action when ${departurePresence ?: "presence sensor(s)"} leaves/leave."
                href "departure", title: "Departure Of:", description: departurePresenceLabel ?: "Tap to set", state: departurePresenceLabel ? "complete" : "incomplete"
                }
			else
            	{
                def departurePresenceLabel = null
                href "departure", title: "Departure Of:", description: departurePresenceLabel ?: "Tap to set", state: departurePresenceLabel ? "complete" : "incomplete"
                }
			if (smoke != null)
            	{
                def smokeLabel = "Trigger action when ${smoke ?: "smoke sensor(s)"} detects/detect smoke."
                href "smokeDetected", title: "Smoke Detected:", description: smokeLabel ?: "Tap to set", state: smokeLabel ? "complete" : "incomplete"
                }
			else
            	{
                def smokeLabel = null
                href "smokeDetected", title: "Smoke Detected:", description: smokeLabel ?: "Tap to set", state: smokeLabel ? "complete" : "incomplete"
                }
			if (water != null)
            	{
                def waterLabel = "Trigger action when ${water ?: "humidity sensor(s)"} detects/detect humidity."
                href "waterDetected", title: "Water Detected:", description: waterLabel ?: "Tap to set", state: waterLabel ? "complete" : "incomplete"
                }
			else
            	{
                def waterLabel = null
                href "waterDetected", title: "Water Detected:", description: waterLabel ?: "Tap to set", state: waterLabel ? "complete" : "incomplete"
                }
			if (button != null)
            	{
                def buttonLabel = "Trigger action when ${button ?: "button(s)"} is/are pressed."
                href "buttonPressed", title: "Button Pressed:", description: buttonLabel ?: "Tap to set", state: buttonLabel ? "complete" : "incomplete"
                }
			else
            	{
                def buttonLabel = null
                href "buttonPressed", title: "Button Pressed:", description: buttonLabel ?: "Tap to set", state: buttonLabel ? "complete" : "incomplete"
                }
			if (triggerModes != null)
            	{
                def triggerModesLabel = "Trigger action when entering ${triggerModes ?: "mode(s)"}."
                href "newMode", title: "Entering Mode:", description: triggerModesLabel ?: "Tap to set", state: triggerModesLabel ? "complete" : "incomplete"
                }
			else
            	{
                def triggerModesLabel = null
                href "newMode", title: "Entering Mode:", description: triggerModesLabel ?: "Tap to set", state: triggerModesLabel ? "complete" : "incomplete"
                }
			if (timeOfDay != null)
            	{
                def timeOfDayLabel = "Trigger action when time is: $timeOfDay."
                href "scheduledTime", title: "At a Scheduled Time:", description: timeOfDayLabel ?: "Tap to set", state: timeOfDayLabel ? "complete" : "incomplete"
                }
			else
            	{
                def timeOfDay = null
                href "scheduledTime", title: "At a Scheduled Time:", description: timeOfDayLabel ?: "Tap to set", state: timeOfDayLabel ? "complete" : "incomplete"
                }
			}
		section ("Sonos Player:")
        	{
			input "sonos", "capability.musicPlayer", title: "Sonos music player", required: true, multiple: true
			}
        section ("Perform this action:")
        	{
			input "actionType", "enum", title: "Action?", required: true, multiple: false, defaultValue: "play", options:
                [
                "Play",
                "Stop Playing",
                "Toggle Play/Pause",
                "Skip to Next Track",
                "Play Previous Track",
                "Mute",
                "Unmute"
                ]
			}
		section ("More options", hideable: true, hidden: true)
        	{
			input "volume", "number", title: "Set the volume volume", description: "0-100%", required: false
			input "frequency", "decimal", title: "Minimum time between actions (defaults to every event)", description: "Minutes", required: false
			def timeLabel = timeIntervalLabel()
            href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : "incomplete"
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false, options:
            	[
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday", "Saturday",
                "Sunday"
            	]
			if (settings.modes)
            	{
            	input "modes", "mode", title: "Only when mode is", multiple: true, required: false
            	}
			input "oncePerDay", "bool", title: "Only once per day", required: false, defaultValue: false
			}
		section([mobileOnly:true])
        	{
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)"
			}
		}
}

private anythingSet()
{
	for (name in ["motion","contact","contactClosed","acceleration","mySwitch","mySwitchOff","arrivalPresence","departurePresence","smoke","water","button1","triggerModes","timeOfDay"])
    	{
		if (settings[name])
        	{
			return true
			}
		}
	return false
}

private ifUnset(Map options, String name, String capability)
{
	if (!settings[name])
    	{
		input(options, name, capability)
		}
}

private ifSet(Map options, String name, String capability)
{
	if (settings[name])
    	{
		input(options, name, capability)
		}
}

def installed()
{
	log.debug "Installed with settings: ${settings}."
	subscribeToEvents()
    initialize()
}

def updated()
{
	log.debug "Updated with settings: ${settings}."
	unsubscribe()
	unschedule()
	subscribeToEvents()
    initialize()
}

def subscribeToEvents()
{
	log.trace "subscribeToEvents()"
	subscribe(app, appTouchHandler)
    subscribe(motionSensors, "motion", motionHandler)
	subscribe(contactOpened, "contact.open", eventHandler)
    subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
    subscribe(motion, "motion.active", eventHandler)
	subscribe(switchOn, "switch.on", eventHandler)
	subscribe(switchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
	subscribe(button, "button.pushed", eventHandler)

	if (triggerModes)
    	{
		subscribe(location, modeChangeHandler)
		}

	if (timeOfDay)
    	{
		schedule(timeOfDay, scheduledTimeHandler)
		}
}

def initialize()
{
	unschedule (takeAction)
	if (motionSensors != null && minutesLater != null)
		{
        log.debug "Scheduling task in $minutesLater minute(s)..."
		def delay = minutesLater * 60
		runIn (delay, takeAction)
        }
}

def motionHandler(evt)
{
	if (evt.value == "active")
    	{
        log.debug "Unscheduling task..."
    	unschedule(takeAction)
        }
    else if (evt.value == "inactive")
    	{
        if (allOk)
            {
            def lastTime = state[frequencyKey(evt)]
            if (oncePerDayOk(lastTime))
                {
                if (frequency)
                    {
                    if (lastTime == null || now() - lastTime >= frequency * 60000)
                        {
						log.debug "Scheduling task in $minutesLater minute(s)..."
                        def delay = minutesLater * 60
                        runIn (delay, takeAction)
                        }
                    else
                        {
                        log.debug "Not taking action because $frequency minutes have not elapsed since last action"
                        }
                    }
                else
                    {
					log.debug "Scheduling task in $minutesLater minute(s)..."
                    def delay = minutesLater * 60
                    runIn (delay, takeAction)
                    }
                }
            else
                {
                log.debug "Not taking action because it was already taken today"
                }
            }
		}
}

def eventHandler(evt)
{
	if (allOk)
    	{
		def lastTime = state[frequencyKey(evt)]
		if (oncePerDayOk(lastTime))
        	{
			if (frequency)
            	{
				if (lastTime == null || now() - lastTime >= frequency * 60000)
                	{
					takeAction()
					}
				else
                	{
					log.debug "Not taking action because $frequency minutes have not elapsed since last action"
					}
				}
			else
            	{
				takeAction()
				}
			}
		else
        	{
			log.debug "Not taking action because it was already taken today"
			}
		}
}

def modeChangeHandler(evt)
{
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	if (evt.value in triggerModes)
    	{
		eventHandler(evt)
		}
}

def scheduledTimeHandler()
{
	eventHandler(null)
}

def appTouchHandler(evt)
{
	takeAction()
}

def takeAction()
{
	log.debug "Take action ($actionType)."
	def options = [:]
    if (volume)
    	{
    	sonos?.each
            {
            it.setLevel(volume as Integer)
            options.delay = 1000
            }
		}
	switch (actionType)
    	{
		case "Play":
        sonos?.each
        	{
			options ? it.on(options) : it.on()
            }            
			break
		case "Stop Playing":
        sonos?.each
            {
            options ? it.off(options) : it.off()
            }
			break
		case "Toggle Play/Pause":
			def currentStatus = sonos.currentValue("status")
			if (currentStatus == "playing")
            	{
                sonos?.each
                	{
					options ? it.pause(options) : it.pause()
                    }
				}
			else
            	{
                sonos?.each
                	{
					options ? it.play(options) : it.play()
                    }
				}
			break
		case "Skip to Next Track":
        sonos?.each
            {
			options ? it.nextTrack(options) : it.nextTrack()
            }
			break
		case "Play Previous Track":
        sonos?.each
        	{
			options ? it.previousTrack(options) : it.previousTrack()
            }
			break
		case "Mute":
        sonos?.each
        	{
       		options ? it.mute(options) : it.mute()
            }
			break
		case "Unmute":
        sonos?.each
        	{
       		options ? it.unmute(options) : it.unmute()
            }
			break
		default:
			log.error "Action type '$actionType' not defined"
	}
	if (frequency)
    	{
		state.lastActionTimeStamp = now()
		}
	initialize()
}

private frequencyKey(evt)
{
	"lastActionTimeStamp"
}

private dayString(Date date)
{
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	if (location.timeZone)
    	{
		df.setTimeZone(location.timeZone)
		}
	else
    	{
		df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
	df.format(date)
}

private oncePerDayOk(Long lastTime)
{
	def result = true
	if (oncePerDay)
    	{
		result = lastTime ? dayString(new Date()) != dayString(new Date(lastTime)) : true
		log.trace "oncePerDayOk = $result"
		}
	result
}

private getAllOk()
{
	modeOk && daysOk && timeOk
}

private getModeOk()
{
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk()
{
	def result = true
	if (days)
    	{
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone)
        	{
			df.setTimeZone(location.timeZone)
			}
		else
        	{
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
			}
		def day = df.format(new Date())
		result = days.contains(day)
		}
	log.trace "daysOk = $result"
	result
}

private getTimeOk()
{
	def result = true
	if (starting && ending)
    	{
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
		}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private timeIntervalLabel()
{
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}