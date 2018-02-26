/**
 *  Temperature Change Specified
 *
 *  Author: dbrockjr
 */
preferences {
	section("Select a temperature sensor"){
		input "sensor", "capability.temperatureMeasurement", title: "Sensor"
	}
    section("Set the LOW temperature"){
		input "setLow", "decimal", title: "Low Temp"
	}
    section("Set the HIGH temperature"){
		input "setHigh", "decimal", title: "High Temp"
	}
    section("Text me at (optional)") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed(){
	go()
}

def updated(){
	unsubscribe()
    go()
}

def go(){
	if(sensor && setHigh && setLow){
    	subscribe(sensor, "temperature", temperatureHandler)
    }
}

def temperatureHandler(evt) {
	if(setLow > setHigh){
    	def temp = setLow
        setLow = setHigh
        setHigh = temp
    }
	def currentTemp = evt.doubleValue
    def msg = "ALERT: $sensor.label has detected a temp of $currentTemp"
	if (currentTemp < setLow || currentTemp > setHigh) {
        sendPush(msg)
		if(phone){
        	sendSms(phone, msg)
        }
    }
}

