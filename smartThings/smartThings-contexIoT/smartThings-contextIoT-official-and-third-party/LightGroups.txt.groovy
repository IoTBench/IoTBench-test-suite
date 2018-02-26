/**
 *  Light Group
 *
 *  Copyright 2015 Michael Melancon
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
    name: "Light Groups",
    namespace: "melancon",
    author: "Michael Melancon",
    description: "Allows for creating a virtual child device that controls multiple switches, dimmers, smart bulbs.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/lightsAndSwitches.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/lightsAndSwitches@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Categories/lightsAndSwitches@2x.png")


preferences {
	section("Which switches should be in this light group") {
		input "switches", "capability.switch", multiple: true, required: false
	}
	section("Which dimmers should be in this light group") {
		input "dimmers", "capability.switchLevel", multiple: true, required: false
	}
	section("Which bulbs with color control should be in this light group") {
		input "colorControls", "capability.colorControl", multiple: true, required: false
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
    def master = getChildDevice("LG0001")
    if (!master)
    	master = addChildDevice("melancon", "Light Group", "LG0001", null)
	subscribe(master, "switch.on", onHandler)
	subscribe(master, "switch.off", offHandler)
	subscribe(master, "level", dimHandler)   
    subscribe(master, "hue", hueHandler)
    subscribe(master, "saturation", saturationHandler)
    subscribe(master, "color", colorHandler)
}

def onHandler(evt) {
	log.debug evt.value
	log.debug switches
	switches?.on()
}

def offHandler(evt) {
	log.debug evt.value
	log.debug switches
	switches?.off()
}

def dimHandler(evt) {
	log.debug "Dim level: ${evt.value}"
   	log.debug dimmers

	dimmers?.setLevel(evt.numericValue)
}

def colorHandler(evt) {
	log.debug "Color: ${evt.value}"
    log.debug colorControls
    colorControls?.setColor(evt.value)
}

def hueHandler(evt) {
	log.debug "Hue: ${evt.value}"
   	log.debug colorControls
	colorControls?.setHue(evt.numericValue)
}


def saturationHandler(evt) {
	log.debug "Saturation: ${evt.value}"
   	log.debug colorControls
	colorControls?.setSaturation(evt.numericValue)
}
