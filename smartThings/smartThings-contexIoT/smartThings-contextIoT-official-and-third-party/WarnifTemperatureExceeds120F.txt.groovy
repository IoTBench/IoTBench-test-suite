/**
 *  Warn if Temperature Exceeds 120 F
 *
 *  Based on "It's Too Cold" by SmartThings
 *
 *  Author: Anders Heie
 */
preferences {
	section("Monitor the temperature...") {
		input "temperatureSensors", "capability.temperatureMeasurement", multiple: true
	}
	section("When the temperature exceeds (default 120 F) ...") {
		input "temperatureTooHot", "number", title: "Temperature?", required: false
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
		input "phone", "phone", title: "Send a Text Message?", required: false
	}
    section("Minimum time between messages per device (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	subscribe(temperatureSensors, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
	subscribe(temperatureSensors, "temperature", temperatureHandler)
}

def temperatureHandler(evt) {
	log.trace "temperature: $evt.value, $evt"

	def toohot = (temperatureTooHot != null && temperatureTooHot != "") ? temperatureTooHot : 120.0

	if (evt.doubleValue >= toohot) {
		
        if (frequency) {
			def lastTime = state[evt.deviceId]
            log.info lastTime
			if (lastTime == null || now() - lastTime >= frequency * 60000) {
				sendMessage(evt, toohot)
			}
		} else {
        	sendMessage(evt, toohot)
        }

	}
}

private sendMessage(evt, toohot) {
	def message = "A temperature sensor has exceeded ${toohot} ${evt.unit} in the last ${frequency} minutes, reporting a temperature of ${evt.value}${evt.unit}"
        	
    if(sendPushMessage == "Yes") {
        sendPush(message);
        //log.info "SENT PUSH"
    }
    
    if (phone) {
        sendSms(phone, message)
    }
    log.debug message
    
	state[evt.deviceId] = now()
}