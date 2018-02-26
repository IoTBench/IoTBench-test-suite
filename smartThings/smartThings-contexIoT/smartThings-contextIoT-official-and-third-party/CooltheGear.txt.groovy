/**
 *  Cool the Gear
 *
 *  Copyright 2014 Scottin Pollock
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Cool the Gear",
    namespace: "soletc.com",
    author: "Scottin Pollock",
    description: "Turns on switch based on temperature sensor for a predetermined time, not to repeat for another predetermined time.",
    category: "My Apps",
    iconUrl: "http://solutionsetcetera.com/stuff/STIcons/fan.png",
    iconX2Url: "http://solutionsetcetera.com/stuff/STIcons/fan@2x.png")


preferences {
	section("When heat is sensed...") {
		input "temperatureSensor1", "capability.temperatureMeasurement", title: "Where?", required: true
        input "temperature1", "number", title: "Degrees or higher...", required: true
	}
    
    section("Run fan for...") {
		input "runTime", "number", title: "how many minutes.", required: true
	}
    
    section("Don't restart fan for at least...") {
		input "timeout", "number", title: "how many minutes. (optional)", required: false
	}
    
    section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
	}
    
	section("Send these commands to HAM Bridge"){
		input "HAMBon", "text", title: "Command for ON", required: true
		input "HAMBoff", "text", title: "Command for OFF", required: true
	}
	section("Server address and port number"){
		input "server", "text", title: "Server IP", description: "Your HAM Bridger Server IP", required: true
		input "port", "number", title: "Port", description: "Port Number", required: true
	}
    
}
def installed() {
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
   	subscribe(app, appTouch)
}

def updated() {
	unsubscribe()
	subscribe(temperatureSensor1, "temperature", temperatureHandler)
   	subscribe(app, appTouch)
}

def temperatureHandler(evt) {
    def tooWarm = temperature1
	if (evt.doubleValue >= tooWarm) {
		 if (state.delay != true) {
            fanOn()
          } 
    }
}

def resetTimeout() {
	state.delay = false
	log.debug "resetting timeout"
    def tooHot = checkTemp()
    if (tooHot >= temperature1) {
       	fanOn()
        }
}

def fanOn() {
	log.debug "turning fan on"
    runIn(runTime*60, fanOff)
	def ip = "${settings.server}:${settings.port}"
	def deviceNetworkId = "1234"
	sendHubCommand(new physicalgraph.device.HubAction("""GET /?${settings.HAMBon} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
	send("Cooling Router")
}

def fanOff() {
	log.debug "turning fan off"
    if (timeout) {
        	state.delay = true
            runIn(timeout*60, resetTimeout)
        }
    def ip = "${settings.server}:${settings.port}"
	def deviceNetworkId = "1234"
	sendHubCommand(new physicalgraph.device.HubAction("""GET /?${settings.HAMBoff} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
	send("Router fan off")
}

def appTouch(evt) {
	fanOn()
}

def checkTemp(evt) {
	def latestValue = temperatureSensor1.latestValue("temperature")
}

private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}
}
