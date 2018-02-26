/**
 *  Foscam Presence Alarm
 *
 *  Author: skp19
 *
 */
definition(
    name: "Foscam Presence Alarm",
    namespace: "skp19",
    author: "skp19",
    description: "Enables/disables Foscam alarm when people arrive or leave",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Arm/Disarm these cameras") {
		input "cameras", "capability.imageCapture", multiple: true
	}
    section("When people arrive/depart") {
    	input "presence", "capability.presenceSensor", title: "Who?", multiple: true
    }
    section("Only between these times...") {
    	input "startTime", "time", title: "Start Time", required: false
        input "endTime", "time", title: "End Time", required: false
    }
    section("Options") {
        input "notify", "bool", title: "Notification?"
        input "buttonMode", "enum", title: "Button Function", metadata: [values: ["Enable Alarm", "Disable Alarm"]]
    }
}

def installed() {
	subscribe(presence, "presence", checkTime)
    subscribe(app, toggleAlarm)
}

def updated() {
	unsubscribe()
	subscribe(presence, "presence", checkTime)
    subscribe(app, toggleAlarm)
}

def presenceAlarm(evt) {
	if (evt.value == "present") {
    	log.debug "${presence.label ?: presence.name} has arrived at ${location}"
    	cameras?.alarmOff()
        sendMessage("Foscam alarm disabled")
    }
    else {
    	def nobodyHome = presence.find{it.currentPresence == "present"} == null
        if (nobodyHome) {
        	log.debug "Everyone has left ${location}"
        	cameras?.alarmOn()
            sendMessage("Foscam alarm enabled")
        }
    }
}

def sendMessage(msg) {
	if (notify) {
		sendPush msg
	}
}

def toggleAlarm(evt) {
	if (buttonMode == "Enable Alarm") {
    	log.debug "Alarms Enabled"
    	cameras?.alarmOn()
    }
    else {
        log.debug "Alarms Disabled"
    	cameras?.alarmOff()
    }
}

def checkTime(evt) {
    if(startTime && endTime) {
        def currentTime = new Date()
    	def startUTC = timeToday(startTime)
    	def endUTC = timeToday(endTime)
	    if((currentTime > startUTC && currentTime < endUTC && startUTC < endUTC) || (currentTime > startUTC && startUTC > endUTC) || (currentTime < endUTC && endUTC < startUTC)) {
    		presenceAlarm(evt)
    	}
    }
    else {
    	presenceAlarm(evt)
    }
}