/**
 *  Sonos Play mp3, speak text or play weather forecast
 *
 *  Author: SmartThings and foneguy2
 *  Date: 03-17-2014
 */
preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "contactClosed", "capability.contactSensor", title: "Contact Closes", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "mySwitchOff", "capability.switch", title: "Switch Turned Off", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
		input "smoke", "capability.smokeDetector", title: "Smoke Detected", required: false, multiple: true
		input "water", "capability.waterSensor", title: "Water Sensor Wet", required: false, multiple: true
		input "button1", "capability.button", title: "Button Press", required:false, multiple:true //remove from production
		input "triggerModes", "mode", title: "System Changes Mode", required: false, multiple: true
	}

	section("Perform this action"){
		input "actionType", "enum", title: "Action?", required: true, options: [
			"Weather Forecast",
			"Speak User Text",
			"Play",
			"Stop Playing",
			"Toggle Play/Pause",
			"Bell 1",
			"Bell 2",
			"Dogs Barking",
			"Fire Alarm",
			"The mail has arrived",
			"A door opened",
			"There is motion",
			"Smartthings detected a flood",
			"Smartthings detected smoke",
			"Someone is arriving",
			"Piano",
			"Lightsaber"]
	}
	
    section("Speak This Text"){
		input "message", "text", title: "Sonos to speak", required: false
    }	

	section {
		input "sonos", "capability.musicPlayer", title: "Sonos Device", required: true
	}

	section("Minimum time between actions (optional, defaults to every event)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}

	section("More options", expandable: false, expanded: false) {
		input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
		input "resumePlaying", "bool", title: "Resume playing music", required: false, defaultValue: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
	subscribe(acceleration, "acceleration.active", eventHandler)
	subscribe(motion, "motion.active", eventHandler)
	subscribe(mySwitch, "switch.on", eventHandler)
	subscribe(mySwitchOff, "switch.off", eventHandler)
	subscribe(arrivalPresence, "presence.present", eventHandler)
	subscribe(departurePresence, "presence.not present", eventHandler)
	subscribe(smoke, "smoke.detected", eventHandler)
	subscribe(smoke, "smoke.tested", eventHandler)
	subscribe(smoke, "carbonMonoxide.detected", eventHandler)
	subscribe(water, "water.wet", eventHandler)
	subscribe(button1, "button.pushed", eventHandler)
	if (triggerModes) {
		subscribe(location, modeChangeHandler)
	}
}

def eventHandler(evt) {
	if (frequency) {
		def lastTime = state.lastActionTimeStamp
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			sendMessage(evt)
		}
	}
	else {
		takeAction(evt)
	}
}

def modeChangeHandler(evt) {
	log.trace "modeChangeHandler $evt.name: $evt.value ($triggerModes)"
	if (evt.value in triggerModes) {
		eventHandler(evt)
	}
}

private takeAction(evt) {
	log.debug "takeAction($actionType)"
    def currentStatus = sonos.currentValue("status")
	def currentVolume = sonos.currentState("level")?.integerValue
	def currentTrack = sonos.currentState("trackData")?.jsonValue    
    
    def okToPlay = true

	if (actionType == "Play") {
		log.trace "sonos.on()"
		sonos.on()
        okToPlay = false
	}
	else if (actionType == "Stop Playing") {
		log.trace "sonos.off()"
		sonos.off()
        okToPlay = false
	}
	else if (actionType == "Toggle Play/Pause") {
		log.trace "$actionType"
        okToPlay = false		
		if (currentStatus == "playing") {
			sonos.pause()
		}
		else {
			sonos.play()
		}
	}

	else {

		if (volume != null) {
			sonos.stop()
			pause(500)
			sonos.setLevel(volume)
			pause(500)
		}

		log.trace "Playing $actionType"
        def length  = 3000
		
		switch ( actionType) {
			case "Weather Forecast":
				length += 12000			
                def data2 = getWeatherFeature("forecast")
				log.debug( "Forecast Today ${data2.forecast.txt_forecast.forecastday.fcttext[0]}")             
				sonos.playText("Forecast Today ${data2.forecast.txt_forecast.forecastday.fcttext[0]}")								
				break;
				
			case "Speak User Text":
			    length += 4000
				sonos.playText("$message")
				break;
				
			case "Bell 1":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3")
				break;
			case "Bell 2":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/bell2.mp3")
				break;
			case "Dogs Barking":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/dogs.mp3")
				length += 4000
				break;
			case "Fire Alarm":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/alarm.mp3")
				length += 9000
				break;
			case "The mail has arrived":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/the+mail+has+arrived.mp3")
				break;
			case "A door opened":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/a+door+opened.mp3")
				break;
			case "There is motion":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/there+is+motion.mp3")
				break;
			case "Smartthings detected a flood":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+a+flood.mp3")
				length += 1000
				break;
			case "Smartthings detected smoke":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/smartthings+detected+smoke.mp3")
				length += 1000
				break;
			case "Someone is arriving":
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/someone+is+arriving.mp3")
				break;
			case "Piano":
				length += 11000
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/piano2.mp3")
				break;
			case "Lightsaber":
				length += 9000
				sonos.playTrack("http://s3.amazonaws.com/smartapp-media/sonos/lightsaber.mp3")
				break;
			default:
				log.debug "Missing Sound Choice"
				break;
		}

		// Stop after allowed time
		pause(length)

		// Reset volume and resume
		if (volume && currentVolume) {
			log.trace "Restoring volume"
			sonos.stop()
			pause(500)
			sonos.setLevel(currentVolume)
			pause(500)
		}
        
        log.trace "$resumePlaying"
        log.trace "$currentTrack.status"
        log.trace "$okToPlay"        

        if ((resumePlaying == null || resumePlaying) && currentStatus == "playing" && okToPlay) {
            log.trace "Resuming play"
            sonos.playTrack(currentTrack)
        }
        else {
            log.trace "Restoring track"
            sonos.setTrack(currentTrack)
        }
	}

		if (frequency) {
			state.lastActionTimeStamp = now()
		}
		
}