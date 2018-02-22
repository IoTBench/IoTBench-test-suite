/**
 *  Virtual Dimmer to Color Temp Adjustments
 *
 *  Copyright 2015 Scott Gibson
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
 *  Many thanks to Eric Roberts for his virtual switch creator, which served as the template for creating child devices in this SmartApp!
 *
 */
definition(
    name: "Color Temp via Virtual Dimmer",
    namespace: "sticks18",
    author: "Scott Gibson",
    description: "Creates a virtual dimmer switch that will convert level settings to color temp adjustments in selected Color Temp bulbs.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page(name: "basicInfo", title: "Name your virtual dimmer and select color temp lights", install: true, uninstall: true){
		section("Create Virtual Dimmer Switch to be used as a dimmer when you want color temp adjustments") {
			input "switchLabel", "text", title: "Virtual Dimmer Label", multiple: false, required: true
		}
		section("Choose your Color Temp bulbs") {
			paragraph "This SmartApp will allow you to set the level of the Virtual Dimmer as part of your automations using the much more common Set Level option, and have that level converted to a color temperature adjustment in the selected bulbs. The conversion will take 0-100 and convert to 2700-6500k via colorTemp = (level*38)+2700. Some common conversions: 0 level = 2700k (Soft White), 10 level = 3080k (Warm White), 20 level = 3460k (Cool White), 40 level = 4220k (Bright White), 60 level = 4980k (Natural), 100 level = 6500k (Daylight).", title: "How to use...", required: true
			input "cLights", "capability.color temperature", title: "Select Color Temp Lights", required: true, multiple: true
  		}
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
       
    def deviceId = app.id + "vDimmerForCTemp"
    log.debug(deviceId)
    def existing = getChildDevice(deviceId)
    log.debug existing
    if (!existing) {
        log.debug "Add child"
        def childDevice = addChildDevice("sticks18", "Color Temp Virtual Dimmer", deviceId, null, [label: switchLabel])
    }
    
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

// Child device methods after this point. Instead of subscribing to child, have child directly call parent

def setLevel(childDevice, value) {
	
    def degrees = Math.round((value * 38) + 2700)
  	if (degrees == 6462) { degrees = degrees + 38 }
    
    log.debug "Converting dimmer level ${value} to color temp ${degrees}..."
    childDevice.updateTemp(degrees)
    cLights?.setColorTemperature(degrees)
    


}
