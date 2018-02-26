//Based on Groovestream wrapper by Jason Steele

definition(
		name: "StatHat QuickStart",
		namespace: "Pursual",
		author: "Tony Gutierrez",
		description: "Log to StatHat",
		category: "Convenience",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Log devices...") {
		input "contacts", "capability.contactSensor", title: "Doors open/close", required: false, multiple: true
		input "locks", "capability.lock", title: "Locks", required: false, multiple: true
		input "temperatures", "capability.temperatureMeasurement", title: "Temperatures", required:false, multiple: true
        input "humids", "capability.relativeHumidityMeasurement", title: "Humidities", required:false, multiple: true
        input "batteries", "capability.battery", title: "Battery Levels", required:false, multiple: true
	}

	section ("StatHat EZKey...") {
		input "ezkey", "text", title: "Ez key"
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
	subscribe(temperatures, "temperature", handleStringEvent)
	subscribe(contacts, "contact", handleContactEvent)
    subscribe(locks, "lock", handleLockEvent)
    subscribe(humids, "humidity", handleStringEvent)
    subscribe(batteries, "battery", handleStringEvent)
}

def handleStringEvent(evt) {
	statPut(evt, {it.toString()})
}

def handleContactEvent(evt) {
	statPut(evt, {it == "open" ? 1 : 0})
}

def handleLockEvent(evt) {
	statPut(evt, {it == "locked" ? 1 : 0})
}

private statPut(evt, Closure convert) {
	def type = evt.displayName.trim() + " - " + evt.name
    def value = convert(evt.value)
    
    httpPostJson(
        uri: "http://api.stathat.com/ez",
        body: "{\"ezkey\":\"${settings.ezkey}\", \"data\":[{\"stat\":\"${type}\", \"value\":${value}}]}",
        ) {response -> log.debug (response.data)}
}