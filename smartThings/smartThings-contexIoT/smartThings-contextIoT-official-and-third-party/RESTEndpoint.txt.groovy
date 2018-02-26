/**
 *  RESTEndpoint
 *
 *  Copyright 2014 Deon Poncini
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
    name: "RESTEndpoint",
    namespace: "",
    author: "Deon Poncini",
    description: "Restful Endpoint",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true
)


preferences {
	section("Allow endpoint to control these things...") {
		input "switches", "capability.switch", title: "Which switches?", multiple: true
	}
}

mappings {
	
    path("/switches"){
    	action: [
        		GET: "listSwitches"
        ]
   	}
    
    path("/switches/:id"){
    	action: [
        		GET: "showSwitch"
        ]
    }
    
    path("/switches/:id/:command"){
    	action: [
        		GET: "updateSwitch"
        ]
    }
}

def installed() {}

def updated() {}

def listSwitches() {
		switches.collect{device(it,"switch")}
}

def showSwitch() {
		show(switches, "switch")
}

void updateSwitch() {
	update(switches)
}

def deviceHandler(evt) {}

private void update(devices) {
	log.debug "update request: params: ${params}, devices: $devices.id"
    
    def command = params.command
    
    if (command) {
    	def device = devices.find{it.id == params.id }
        if (!device) {
        	httpError(404, "Device not found")
        } else {
        	if (command == "toggle") {
            	if (device.currentValue('switch') == "on") {
					device.off();
                } else {
               		device.on();
                }
            } else {
            	device."$command"()
            }
        }
    }
}

private show(devices, type) {
	def device = devices.find{it.id == params.id}
    if (!device){
    	httpError(404,"Device not found")
    } else {
    	def attributeName = type == "motionSensor" ? "motion" : type
        def s = device.currentState(attributeName)
        [id: device.id, label: device.displayName, value: s?.value, unitTime: s?.date?.time, type: type]
    }
}

private device(it, type) {
	it ? [id: it.id, label: it.label, type: type] : null
}









