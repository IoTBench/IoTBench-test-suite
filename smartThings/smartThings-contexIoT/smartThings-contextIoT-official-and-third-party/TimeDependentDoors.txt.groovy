definition(
    name: "Time Dependent Doors",
    namespace: "jrichard",
    author: "Jacob Richard",
    description: "Time dependent door actions for Brad",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When I arrive and leave..."){
		input "person", "capability.presenceSensor", title: "Who?", multiple: false
	}
    section("Unlock Which Door at Night..."){
		input "nightdoor", "capability.lock", multiple: false
	}
    section("Turn on Which Light at Night..."){
		input "nightlights", "capability.switch", multiple: true
	}        
    section("Unlock Which Door During the Day..."){
		input "daydoor", "capability.lock", multiple: false
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
	subscribe(person, "presence", presenceHandler)
}

def presenceHandler(evt) {
    def data = getWeatherFeature( "astronomy", zipcode )
    def hour = data.moon_phase.current_time.hour.toInteger()
    if (evt.value == 'present') {
        if (hour > 6) {
            daydoor.unlock()
        }
        else if (hour < 7) {
            nightdoor.unlock()
            nightlights.on()
        }
    }
}


