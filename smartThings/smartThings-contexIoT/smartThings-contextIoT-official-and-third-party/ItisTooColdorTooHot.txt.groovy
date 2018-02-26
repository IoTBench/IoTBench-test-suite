/**
 *  It's Too Cold or Too Hot
 *
 *  Author: SmartThings and Martin Tremblay
 */
preferences {
	section("Temperature") {
		input "temperatureSensor1", "capability.temperatureMeasurement", title: "Which Sensor?"
		input "minTemp", "number", title: "Minimum Temperature"
		input "maxTemp", "number", title: "Maximum Temperature"
	}
	section("Notification  (optional)") {
		input "interval1", "number", title: "Interval?", description: "In minutes"
		input "phone1", "phone", title: "Phone number?", description: "Push if not set", required: false
	}
	section("Power outlet(s)  (optional)") {
		input "switch1", "capability.switch", title: "Which outlet(s)?", multiple: true, required: false
	}
}

def installed() {
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
    state.alarmActiveCurrent = null
    state.alarmActiveLast = null
    state.nextNotif = null
}

def updated() {
	unsubscribe()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
    state.alarmActiveCurrent = null
    state.alarmActiveLast = null
    state.nextNotif = null
}

def temperatureHandler(evt) {
	log.debug "Temperature: $evt.value, $evt"

	def textAlert = null
    
	if (evt.doubleValue < minTemp) {
    	textAlert = "${temperatureSensor1.label} is below the minimum threshold ($minTemp), reporting a temperature of ${evt.value}${evt.unit}."
        state.alarmActiveCurrent = true
    } else if (evt.doubleValue > maxTemp) {
    	textAlert = "${temperatureSensor1.label} is above the maximum threshold ($maxTemp), reporting a temperature of ${evt.value}${evt.unit}."
        state.alarmActiveCurrent = true
	} else {
        state.alarmActiveCurrent = false
    }
   	log.debug "alarmActiveLast: $state.alarmActiveLast | alarmActiveCurrent: $state.alarmActiveCurrent | nextNotif: $state.nextNotif"

	if (state.alarmActiveCurrent) {
    	log.debug "$textAlert"
		if (!state.alarmActiveLast) {
        	// This is a new alarm, notify and turn on outlets (if defined)
            if (phone1) {
            	log.trace "Sending SMS to $phone1"
				sendSms(phone1, "$textAlert")
            } else {
            	log.trace "Sending push notification"
				sendPush("$textAlert")
            }
			if (switch1) {
				log.trace "Turning on outlets: $switch1"
                switch1.on()
            }
			// Set alarm mode. Not sure why but we need to use epoch time to re-use it later on when we re-enter the method
            state.nextNotif = new Date(0)
            state.nextNotif = (now() + (1000 * 60 * interval1))
       		state.alarmActiveLast = true
		} else {
        	// This is an existing alarm, do we need to send another notification ?
			log.debug "Checking the nofification interval: $interval1 minutes"
        	def currentDateTime = new Date(0)
            currentDateTime = now()
			if ( state.nextNotif < currentDateTime ) {
        	   	log.debug "Current time is $currentDateTime"
            	if (phone1) {
            		log.trace "Sending SMS to $phone1"
					sendSms(phone1, "$textAlert")
            	} else {
					log.trace "Sending push notification"
					sendPush("$textAlert")
            	}
				state.nextNotif = new Date(0)
		        state.nextNotif = (now() + (1000 * 60 * interval1))
           	}
		}
	}
    else if (state.alarmActiveLast) {
    	log.debug "We recover from an alarm"
        if (phone1) {
           	log.trace "Sending SMS to $phone1"
			sendSms(phone1, "${temperatureSensor1.label} is ok now, reporting a temperature of ${evt.value}${evt.unit}.")
        } else {
			log.trace "Sending push notification"
			sendPush("${temperatureSensor1.label} is ok now, reporting a temperature of ${evt.value}${evt.unit}.")
        }
		if (switch1) {
			log.trace "Turning off outlets: $switch1"
            switch1.off()
        }
		// Reset the alarm and notification state
		state.alarmActiveLast = false
        state.nextNotif = null

    }
}