/**
 *  Presence with filter
 *
 *  Author: mattbaker777@gmail.com
 *  Date: 2014-01-18
 */
preferences {
	section("Select a presence sensor") {
		input "presenceSensor", "capability.presenceSensor", title: "Presence sensor", required: false, multiple: true
	}
    section("Subscribe to the desired status change(s) (default is Both)") {
    	input "statusSubscription", "enum", title: "Status changes", required: false, metadata: [values: ["Depart only","Arrive only","Both"]]
    }
	section("Enter notification messages (leave blank to push default status messages)") {
        input "arrivalText", "text", title: "Arrival message", required: false
        input "departureText", "text", title: "Departure message", required: false
	}
    section("Send a push notification? (default is Yes)") {
    	input "sendPushPreference", "enum", title: "Push (optional)", required: false, metadata: [values: ["Yes","No"]]
    }
	section("Enter a phone number to send an SMS notification") {
		input "phoneNumber", "phone", title: "Phone number (optional)", required: false
	}
	section("Filter sensor dropouts by entering an allowed disconnect time (will delay departure notifications by same time)") {
		input "filterTime", "decimal", title: "Minutes (optional)", required: false
	}
}

def installed() {
	log.debug "[installed]"
	log.debug "...Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "[updated]"
	log.debug "...Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	log.debug "[subscribeToEvents]"
	if (presenceSensor) {
    	log.debug "...Subscribing to arrival and departure events"
		subscribe(presenceSensor, "presence.present", eventHandler)
		subscribe(presenceSensor, "presence.not present", eventHandler)
    } else {
    	log.debug "...No sensor selected.  Could not subscribe."
    }
	state.departNotificationCallbackRunning = false
}

def eventHandler(evt) {
	log.debug "[eventHandler]"
    log.debug "...statusSubscription = ${statusSubscription}"
    log.debug "...Event value = ${evt.value}"

	// If no filterTime, always send notification when subscribed to both status changes.
    // Otherwise, only send when subscribed to the specific status change.
    if (filterTime == null || filterTime <= 0) {
    	def sendNotification = (statusSubscription == "Both" || !statusSubscription) 
        sendNotification = sendNotification || (evt.value == "present" && statusSubscription == "Arrive only")  
        sendNotification = sendNotification || (evt.value == "not present" && statusSubscription == "Depart only")
        
        log.debug "...Filter NOT defined."
	    log.debug "......Sending notification = ${sendNotification}"
        
    	if (sendNotification) { sendMessage(evt, false) }

	// When filterTime is greater than zero, send notifications based on filterTime and previous status changes
	} else {
    	log.debug "...Filter defined."
		if (statusSubscription == "Arrive only") {
        	if (evt.value == "present") {
				log.debug "......Arrive and present. Checking last departure time."
				checkLastDepartTime(evt)
        
        	// Else run for evt.value == "not present"
        	} else {
            	// Set lastDepartTime but don't send a notification
                log.debug "......Arrive and NOT present.  Setting lastDepartTime to now."
            	setLastDepartTime(evt, now())
            }
        } else {
            if (statusSubscription == "Depart only") {
                // Do nothing for evt.value == "present"
                if (evt.value == "present") {
                    log.debug "......Depart and present...do nothing"
                } else {
                    log.debug "......Depart and NOT present. Launching callback method."
                    launchCallback(evt)
                }
            // Else run for statusSubscription == "Both" or null   
            } else {
                if (evt.value == "present") {
                    log.debug "......Both and present. Checking last departure time."
                    checkLastDepartTime(evt)
                
                // Else run for evt.value == "not present"
                } else {
                    // log departure time
                    log.debug "......Both and NOT present. Setting lastDepartureTime to now and launching callback method."
                    setLastDepartTime(evt, now())
                    launchCallback(evt)
                }
            }
        }
	}
}

private checkLastDepartTime(evt) {
	def lastDepartTime = state[evt.deviceId]
    
	log.debug "[checkLastDepartTime]"
    log.debug "...now = ${now()}"
    log.debug "...lastDepartTime = ${lastDepartTime}"
    log.debug "...deviceID = ${evt.deviceId}"
    
    // If no lastDepartTime or elapsed time is greater than filterTime, send the notification and clear lastDepartTime.
    // Otherwise, clear the departNotificationCallback
	if (lastDepartTime == null || now() - lastDepartTime >= filterTime * 60000) {
    	log.debug "...Sending message"
		sendMessage(evt, false)
    	setLastDepartTime(evt, null)
	} else {
    	log.debug "...Arrival within filterTime.  Cancelling callback.  Will not send notification."
        state.departNotificationCallbackRunning = false
    }
}

private launchCallback(evt) {
	log.debug "[launchCallback]"
    log.debug "...departNotificationCallbackRunning = ${state.departNotificationCallbackRunning}"
    // If no departNotificationCallback, launch callback.  Otherwise, do nothing.
    if (!state.departNotificationCallbackRunning || state.departNotificationCallbackRunning == null) {
    	def callbackTime = 60 * filterTime
    	log.debug "...departNotificationCallback not already running. Launching callback with time = ${callbackTime} sec"
		state.departNotificationCallbackRunning = true
        log.debug "...setting departNotificationCallbackRunning = ${state.departNotificationCallbackRunning}"
        
        // Cannot store evt for later.  Storing required hash components for sendMessage methods
        state.callbackEvt = [value: evt.value, name: evt.name, linkText: evt.linkText, descriptionText:evt.descriptionText]
        log.debug "...setting callbackEvt = ${state.callbackEvt}"
        
       	runIn(callbackTime, "departNotificationCallback", [overwrite: true])
	} else {
    	log.debug "...departNotificationCallback already running.  Doing nothing."
    }
}

private departNotificationCallback() {
	log.debug "[departNotificationCallback]"
    log.debug "...departNotificationCallbackRunning = ${state.departNotificationCallbackRunning}"
    log.debug "...callbackEvt = ${state.callbackEvt}"
	// If the callback hasn't been cancelled, reset the callback and send the departed message
	if (state.departNotificationCallbackRunning) {
    	log.debug "...Sending message."
    	state.departNotificationCallbackRunning = false
    	sendMessage(state.callbackEvt, true)
    } else {
    	log.debug "...Arrival within filterTime.  Will not send notification."
    }
}

private setLastDepartTime(evt, time) {
	log.debug "[setLastDepartTime]"
    log.debug "...Time = ${time}"
    log.debug "...Device ID = ${evt.deviceId}"
	state[evt.deviceId] = time
    log.debug "...Set lastDepartTime = ${state[evt.deviceId]} for device: ${evt.linkText}"
}

private sendMessage(evt, delayed) {
	log.debug "[sendMessage]"
    def msg
    // Assign msg based on presence and custom or default message
    if (evt.value == "present") {
    	msg = arrivalText ?: defaultText(evt)
    } else {
    	msg = departureText ?: defaultText(evt)
        if (delayed) { msg = msg + " (${filterTime} minutes ago)" }
    }
    
	log.debug "$evt.name:${evt.value}, sendPush:${sendPushPreference}, phone:${phoneNumber}, '${msg}'"

	// Push notification
	if (sendPushPreference == "Yes" || sendPushPreference == null) {
		log.debug "...sending push"
		sendPush(msg)
	}
    
    // SMS notification
    if (phoneNumber) {
        log.debug "...sending SMS"
        sendSms(phoneNumber, msg)
    } 
    
    // Send a push notification if nothing would otherwise be sent.
    if (phoneNumber == null && sendPushPreference == "No" ) {
        log.debug "...Push is off but no phone number entered for SMS. Sending push."
        sendPush("Push notification is turned off but no phone number was provided for SMS.  Pushing notification instead: " + msg)
    }
}

private defaultText(evt) {
	if (evt.name == "presence") {
		if (evt.value == "present") {
			if (includeArticle) {
				"$evt.linkText has arrived at the $location.name"
			} else {
				"$evt.linkText has arrived at $location.name"
			}
		} else {
			if (includeArticle) {
				"$evt.linkText has left the $location.name"
			} else {
				"$evt.linkText has left $location.name"
			}
		}
	} else {
		evt.descriptionText
	}
}

private getIncludeArticle() {
	def name = location.name.toLowerCase()
	def segs = name.split(" ")
	!(["work","home"].contains(name) || (segs.size() > 1 && (["the","my","a","an"].contains(segs[0]) || segs[0].endsWith("'s"))))
}
