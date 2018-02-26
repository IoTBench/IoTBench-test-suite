/**
 *  Whole House Fan
 *
 *  Copyright 2014 Ryan Bennett
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
    name: "Whole House Fan with Open Windows",
    namespace: "detriment",
    author: "Ryan Bennett",
    description: "Toggle a whole house fan (switch) when: Outside is cooler than inside + offset, Inside is above x temp, Thermostat is off, windows/doors open. Stolen almost entirely from original Whole House Fan app by Brian Steere.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Developers/whole-house-fan%402x.png"
)


preferences {
	section("Outdoor") {
		input "outTemp", "capability.temperatureMeasurement", title: "Outdoor Thermometer"
        input "offsetTemp", "number", title: "Outdoor Temperature Offset"  
	}
    
    section("Indoor") {
    	input "thermostat", "capability.thermostat", title: "Thermostat" 
    	input "inTemp", "capability.temperatureMeasurement", title: "Indoor Thermometer" 
        input "minTemp", "number", title: "Minimum Indoor Temperature" 
        input "windows", "capability.contactSensor", title: "Open Windows", multiple: true, required: false
        input "fans", "capability.switch", title: "Vent Fan", multiple: true
    }  
}


def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	state.fanRunning = false;
    
    subscribe(outTemp, "temperature", "checkThings");
    subscribe(inTemp, "temperature", "checkThings");
    subscribe(thermostat, "thermostatOperatingState", "checkThings");    
    subscribe(windows, "contact", "checkThings"); 
    
}

def checkThings(evt) {
    def outsideTemp = settings.outTemp.currentValue('temperature')
    def insideTemp = settings.inTemp.currentValue('temperature')
    def thermostatMode = settings.thermostat.currentValue('thermostatOperatingState')
	def openWindow = !windows || !windows.latestValue("contact").contains("open")
   
        
    log.debug "Inside: $insideTemp, Outside: $outsideTemp, Thermostat: $thermostatMode, Windows: $openWindow $windows"
    
    def shouldRun = true;
    
//    if(thermostatMode != 'idle') {
//   	log.debug "Not running due to thermostat mode"
//    	shouldRun = false;
//    }
    

	if(insideTemp <= outsideTemp + offsetTemp) {
    	log.debug "Not running due to insideTemp < outdoorTemp"
    	shouldRun = false;
    }
    
    if(insideTemp <= settings.minTemp) {
    	log.debug "Not running due to insideTemp < minTemp"
    	shouldRun = false;
    }

if (windows != null) {
if(openWindow) {
    	log.debug "Not running due to windows closed"
    	shouldRun = false;
    }}
    
    
    if(shouldRun && !state.fanRunning) {
    	fans.on();
        state.fanRunning = true;
        
    	log.debug "Adjusting thermostat settings."
		state.thermostatMode = thermostat.currentValue("thermostatMode")
        state.changed = true
    	thermostat.off()
    	log.debug "State: $state"
                
        
    } else if(!shouldRun && state.fanRunning) {
    	fans.off();
        state.fanRunning = false;
        
    log.debug "Setting thermostat to $state.thermostatMode"
    thermostat.setThermostatMode(state.thermostatMode)
    state.changed = false
        
    }
}