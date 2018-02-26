/**
 *  SwitchSensor
 *
 *  Copyright 2014 Alan
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
    name: "SwitchSensor",
    namespace: "FirstTry",
    author: "Alan",
    description: "Try SwitchSensor",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Which sensor do you want to track?") {
		// TODO: put inputs here
        input "contact1", "capability.contactSensor", title: "Select one", multiple:true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    subscribe(contact1, "contact", contactHandler)
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(contact1, "contact", contactHandler)    
}

// TODO: implement event handlers
def contactHandler(evt){
	if (evt.value == "open"){
    	httpPost("https://www.google-analytics.com/collect","v=1&tid=UA-38046499-1&cid=1&t=event&ec=Door-"+evt.displayName+"&ea=Open")
    
    }

}





