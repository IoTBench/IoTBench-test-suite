/**
 *  Alert me the temp
 *
 *  Author: christianson.matt@gmail.com
 *  Date: 2014-04-18
 */
preferences {
	section("Alert me at:") {
		input "time1", "time", title: "When?"
		input "dayOfWeek", "enum", 
			title: "Which day of the week?",
			multiple: false,
			metadata: [ 
            	values: [
					'All Week',
                    'Monday to Friday',
					'Saturday & Sunday',
					'Monday','Tuesday','Wednesday','Thursday','Friday', 'Saturday', 'Sunday'			
                ]
            ]

	}
    section("Which sensor?") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("Text me at (optional, sends a push notification if not specified)...") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(time1, "scheduleCheck")
}

def updated() {
	unschedule()
	log.debug "Updated with settings: ${settings}"
	schedule(time1, "scheduleCheck")
}

def scheduleCheck() {
	log.trace "day of week: ${dayOfWeek}"
    def isTodaySelected = isTodaySelected(Calendar.instance.DAY_OF_WEEK)
	log.trace "isTodaySelected: ${isTodaySelected}"
    if (isTodaySelected) {
    	def currentTemp = temperatureSensor1.latestValue("temperature")
		log.trace "sending the temp: ${currentTemp}"
   		def msg = "${temperatureSensor1.label} is currently at ${currentTemp} degrees"
	    sendTextMessage(msg)
    }

}

def isTodaySelected(today) {
	def todaysDayOfWeekInt = Calendar.instance.get(Calendar.DAY_OF_WEEK)

	def dayOfWeekIntArr = (dayOfWeek == 'All Week') ? 1..7 :
    	(dayOfWeek == 'Monday to Friday') ? 2..6 :
	    (dayOfWeek == 'Saturday & Sunday') ? [1,7] :
	    [Calendar."${dayOfWeek.toUpperCase()}"]

	return todaysDayOfWeekInt in dayOfWeekIntArr
}

def sendTextMessage(msg) {
	if (phone) {
		sendSms(phone, msg)
	}
	else {
		sendPush msg
	}
}
