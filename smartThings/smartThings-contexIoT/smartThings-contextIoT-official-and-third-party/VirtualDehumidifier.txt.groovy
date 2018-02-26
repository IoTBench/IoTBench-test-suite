definition(
    name: "Virtual Dehumidifier",
    namespace: "My Apps",
    author: "Barry Burke",
    category: "Green Living",
    description: "Turns on a humidifier when humidity gets too high, back off when it reaches the target again.",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
	section("Humidity") {
		input "humiditySensor", "capability.relativeHumidityMeasurement", title: "Which Sensor?"
		input "desiredHumidity", "number", title: "Desired Humidity?"
        input "dehumidifierSwitch", "capability.switch", title: "Which Switch?"
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
    initialize()
}

def initialize() {
	subscribe(humiditySensor, "humidity", humidityHandler)
    log.debug "Initialized... current humidity is ${humiditySensor.latestValue("humidity")}%, max humidity is ${desiredHumidity*1.05}, dehumidifier is ${dehumidifierSwitch.latestValue( "switch" )}"
    dehumidifierSwitch.poll() 			// Update power display
}


def humidityHandler(evt) {
	log.debug "Humidity: $evt.value, $evt"
    
	if (Double.parseDouble(evt.value.replace("%", "")) <= desiredHumidity) {
    	if ( dehumidifierSwitch.latestValue( "switch" ) != "off" ) {
        	log.debug "Turning dehumidifier off"
        	dehumidifierSwitch.off()
        }
    }
    else if (Double.parseDouble(evt.value.replace("%", "")) > desiredHumidity ) {
    	if ( dehumidifierSwitch.latestValue( "switch" ) != "on" ) {
        	log.debug "Turning dehumidifier on"
            dehumidifierSwitch.on()
        }  
    }
    else {
    	log.debug "Current humidity is ${evt.value}"
    }
    dehumidifierSwitch.poll()				// every time the humidity changes, poll the switch for power updates
}