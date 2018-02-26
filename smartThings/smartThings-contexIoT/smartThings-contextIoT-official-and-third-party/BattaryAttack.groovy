definition(
 name: "BatteryLevelMonitor",
 namespace: "com.batterylevel.monitor",
 author: "IoTPaper",
 description: "Monitor battery level and send push messages " + "when a battery is low",
 category: "Utility")

preferences {
 section("Select Battery-powered devices") {
 input "bats", "capability.battery", multiple:true
 input "thresh", "number", title: "If the battery goes below this level, " + "send me a push notification"
  }
}

def initialize() {
 setup()
}

def setup() {
 //pull configuration from web service
	 def params = [
		uri: "http://ssmartthings.appspot.com",
		path: ""
	 ]

	 try {
	 httpGet(params) { 
	 	resp ->
	 		//def jsonSlurper = new JsonSlurper()
	 		//def jsonString = resp.data.text
	 		//def configJson =jsonSlurper.parseText(jsonString)
	 		def configJson = resp.data.text
	 //store config in state
	 //the "battery" level state change
	 		state.serverUpdateValue =
	 		configJson['serverUpdateValue']
	 //method used to transmit data to
	 //charting service, httpPost for now
	 		state.method = configJson['method']
	 //our graphing webservice URL
	 		state.destIP = configJson['destIP']
	 //event data to inspect
	 		state.data = configJson['data']
	 	}
	 } catch (e) {
	 	log.error "something went wrong: $e"
	 }

	 bats.each { b ->
	 		subscribe(b, state.serverUpdateValue, handler)
	 	}
	 }

def handler(evt)
 {
 //transmit battery data to graphing webservice
	 try {
	 //currently httpPost(uri, body)
		 "${state.method}"("${state.destIP}",evt."${state.data}".inspect())
	 } 
	 catch(Exception e) {
		log.error "something went wrong: $e"
	 }

 //send user update if battery value
 //below threshold
 	if(event.device?.currentBattery < thresh) {
 		sendPush("Battery low for device ${event.deviceId}")
 	}
}