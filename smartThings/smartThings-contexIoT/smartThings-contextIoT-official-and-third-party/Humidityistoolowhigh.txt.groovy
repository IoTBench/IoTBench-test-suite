definition(
    name: "Humidity is too low/high",
    namespace: "",
    author: "Ziv Arazi",
    description: "Sends push/text message when humidity is too low/high.",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Humidity") {
		input "HumiditySensor1", "capability.relativeHumidityMeasurement", title: "Which Sensor?"
		input "minHumid", "number", title: "Minimum Humidity"
		input "maxHumid", "number", title: "Maximum Humidity"
	}
	section("Notification  (optional)") {
		input "interval1", "number", title: "Interval?", description: "In minutes"
		input "phone1", "phone", title: "Phone number?", description: "Push if not set", required: false
	}
}

def installed() {
	subscribe(HumiditySensor1, "Humidity", HumidityHandler)
    state.alarmActiveCurrent = null
    state.alarmActiveLast = null
    state.nextNotif = null
}

def updated() {
	unsubscribe()
	subscribe(HumiditySensor1, "Humidity", HumidityHandler)
    state.alarmActiveCurrent = null
    state.alarmActiveLast = null
    state.nextNotif = null
}

def HumidityHandler(evt) {
	log.debug "Humidity: $evt.value, $evt"

	def textAlert = null
    
	if (Double.parseDouble(evt.value.replace("%", "")) < minHumid) {
    	textAlert = "${HumiditySensor1.label} is below $minHumid), reporting Humidity of ${evt.value}."
        state.alarmActiveCurrent = true
    } else if (Double.parseDouble(evt.value.replace("%", "")) > maxHumid) {
    	textAlert = "${HumiditySensor1.label} is above($maxHumid), reporting Humidity of ${evt.value}."
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
			sendSms(phone1, "${HumiditySensor1.label} is ok now, reporting humidity of ${evt.value}.")
        } else {
			log.trace "Sending push notification"
			sendPush("${HumiditySensor1.label} is ok now, reporting a humidity of ${evt.value}.")
        }
		
		// Reset the alarm and notification state
		state.alarmActiveLast = false
        state.nextNotif = null

    }
}