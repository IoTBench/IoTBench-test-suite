/**
 *  PEQ modded multi-way on off toggle switch
 *  July 10, 2015
 *  
 *	This SmartApp uses a modified PEQ door open/close sensor wired to SPDT (3-way) switch to toggle on and off
 *  a switch that controls a GE Link Light or similar light or switch.   The intent is to have a simple and inexpensive battery
 *  operated replacement for a standard wall switch to operate a single, 3-way or x-way zigbee or zwave light or
 *  switch.
 * 
 *  The PEQ sensor has been on sale intermittently at Best Buy for under $20, and the 3 way SPDT wall switch costs a few
 *  dollars at any hardware store, so you can put the whole thing together for about $25.00.
 * 
 *  The PEQ door open/close sensor can be easily modded and wired to the 3-way wall switch by replacing 
 *  (deslodering the relay and resoldering three wires) the magnetic reed relay with 3 wires soldered
 *  to the 1 reed common pad and 2 reed switch pads on the PCB.  I will be posting a detailed HOW-TO for
 *  modification of the PEQ Sensor on the Smartthings community board shortly.
 *
 *  A future version will be published to toggle on/off multiple switches simultaneously, 
  * and to set the dimmer level.
 *
 *  Copyright (c) 2015 Joel Goldwein (goldwein at gmail.com)
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
	name: "Multi-way On/Off Toggle Switch Using a Modded PEQ Door Open/Close Sensor",
	namespace: "JWGthings",
	author: "Joel Goldwein (goldwein at gmail.com)",
	description: "PEQ multi-way On/Off Switch - This SmartApp uses a modified PEQ door open/close sensor wired to SPDT (3-way) switch to toggle on and off a GE Link Light, similar light or switch.   The intent is to have a simple and inexpensive battery operated replacement for a standard wall switch to operate a single, 3-way or multi-way zigbee or zwave light or switch.",
	category: "My Apps",
	iconUrl: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png",
	iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png",	
	iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home30-icn@2x.png"
)

//		//https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png

preferences {
	section("Select sensor switch or switches") {
		input "contact1", "capability.contactSensor", title: "Which Switch or Switches?", required: true, multiple: true, submitOnChange: true
    }
	// What light should this app be configured for?
   	section("Turn on/off a GE Link Light...") {
		input "switch1", "capability.switch", title: "Which Light", required: true, multiple: false
    }
//	section() {
//		input "battery1", "capability.battery"
//	}
}

def installed() {
//	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
//	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(contact1, "contact", contactHandler)
}

def contactHandler(evt) {
	def cname = "${contact1.displayName}"
	def lname = "${switch1.displayName}"
//	log.info "Triggered. PEQ sensor was $evt.value and $lname was $switch1.currentSwitch"
//     The contactSensor capability can be either "open" or "closed"
//     The actual sensor state is not relevant.  What is only important is the state change, 
//  Code is triggered any time there is a state change in the door sensor
//  Eventually, I'm going to try doing this with a SPST switch instead of a SPDT switch
//
//  If the light is ON
	if ("on" == switch1.currentSwitch )  {
//	Turn it OFF
		switch1.off()
//	and if the light is OFF 
	} else if ("off" == switch1.currentSwitch ) {
//	Turn it ON    
		switch1.on()
	}
//  Send a info message
//	log.info "PEQ switch $cname is now $evt.value and $lname is now $switch1.currentSwitch"
}