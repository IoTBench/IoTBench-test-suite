/**
 *  ThermoTimer
 *
 *  Author: anthony@theransoms.co.za
 *  Date: 2013-08-25
 */
preferences {
	section("Which Thermostat") {
		input "switch1", "capability.switch"
	}
	section("Monitor the temperature...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	}
	section("What is the desired room temperature") {
		input "temperature1", "number", title: "Temperature?"
	}
    section("Sunday") { }
    	section("Heating on periods") {
			input name: "startTime1", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime1", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime2", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime2", title: "Turn Off Period 2?", type: "time", required: false
        }
    section("Monday") { }
    	section("Heating on periods") {
			input name: "startTime3", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime3", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime4", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime4", title: "Turn Off Period 2?", type: "time", required: false
        }
        section("Tuesday") { }
    	section("Heating on periods") {
			input name: "startTime5", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime5", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime6", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime6", title: "Turn Off Period 2?", type: "time", required: false
        }
        section("Wednesday") { }
    	section("Heating on periods") {
			input name: "startTime7", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime7", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime8", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime8", title: "Turn Off Period 2?", type: "time", required: false
        }
        section("Thursday") { }
    	section("Heating on periods") {
			input name: "startTime9", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime9", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime10", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime10", title: "Turn Off Period 2?", type: "time", required: false
        }
        section("Friday") { }
    	section("Heating on periods") {
			input name: "startTime11", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime11", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime12", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime12", title: "Turn Off Period 2?", type: "time", required: false
        }
        section("Saturday") { }
    	section("Heating on periods") {
			input name: "startTime13", title: "Turn On Period 1?", type: "time", required: false
			input name: "stopTime13", title: "Turn Off Period 1?", type: "time", required: false
            input name: "startTime14", title: "Turn On Period 2?", type: "time", required: false
			input name: "stopTime14", title: "Turn Off Period 2?", type: "time", required: false
        }
}

def installed() {
	log.debug "installed, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	initialize()
}

def updated() {
	log.debug "updated, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	unsubscribe()
    unschedule()
	initialize()
}

def uninstalled() {
	log.debug "uninstalled, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	unsubscribe()
    unschedule()
}

def initialize() {
	//log.trace "startTime3: $startTime3, stopTime3: $stopTime3"
    //log.trace "startTime4: $startTime4, stopTime4: $stopTime4"

    schedule("0 0/5 * * * ?", "scheduleCheck")
    //scheduleCheck()
}


def scheduleCheck()
{
	Calendar cal = Calendar.getInstance()
    
	log.trace "scheduledCheck"
    def day = cal.get(Calendar.DAY_OF_WEEK)
    log.trace "$day"
    if (day == 1) {
        Date d = new Date(now())
        log.trace "$timeOfDayStart"
        log.trace "$d"
        if (check(startTime1, stopTime1) || check(startTime2, stopTime2)) {
    		if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
    else if (day == 2) {
        Date d = new Date(now())
        log.trace "$d"
        if (check(startTime3, stopTime3) || check(startTime4, stopTime4)) {   
            if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
    else if (day == 3) {
        Date d = new Date(now())
        log.trace "$timeOfDayStart"
        log.trace "$d"
        if (check(startTime5, stopTime5) || check(startTime6, stopTime6)) {
        	if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
    else if (day == 4) {
        Date d = new Date(now())
        log.trace "$timeOfDayStart"
        log.trace "$d"
        if (check(startTime7, stopTime7) || check(startTime8, stopTime8)) {
        	if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
    else if (day == 5) {
        Date d = new Date(now())
        log.trace "$timeOfDayStart"
        log.trace "$d"
        if (check(startTime9, stopTime9) || check(startTime10, stopTime10)) {
        	if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
    else if (day == 6) {
        Date d = new Date(now())
        log.trace "$timeOfDayStart"
        log.trace "$d"
        if (check(startTime11, stopTime11) || check(startTime12, stopTime12)) {
        	if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
    else if (day == 7) {
        Date d = new Date(now())
        log.trace "$timeOfDayStart"
        log.trace "$d"
        if (check(startTime13, stopTime13) || check(startTime14, stopTime14)) {
    		if (temperatureSensor1.currentTemperature < temperature1) {
            	switchOn()
           	}
            else {
            	switchOff()
            }
        }
        else {
        	switchOff()
        }
    }
}

def check(strt, end)
{
	Date c = new Date(now())
    if ((strt == "") || (end == "")) {
    	return false
    }
	else if (c >= timeToday(strt.replaceAll('0-200', '0200')) && c < timeToday(end.replaceAll('0-200', '0200'))) {
    	log.trace "method check"
    	return true
    }
	return false
}
def switchOn()
{
	def currPos = switch1.currentSwitch
	switch1.on()
    log.trace "switched On"
    if (currPos == "off") {
    	sendPush("Thermo On")
    }
}

def switchOff()
{
	def currPos = switch1.currentSwitch
	switch1.off()
    log.trace "switched Off"
    if (currPos == "on") {
    	sendPush("Thermo Off")
    }
}