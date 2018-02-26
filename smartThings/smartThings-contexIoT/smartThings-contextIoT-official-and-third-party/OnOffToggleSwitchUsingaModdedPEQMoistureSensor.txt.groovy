/**
 *  PEQ moisture sensor modded on off toggle switch
 *  July 17, 2015
 *  
 *	This SmartApp uses a modified PEQ water sensor wired to SPST switch to toggle on and off
 *  a GE Link Light or similar light or switch.   The intent is to have a simple and inexpensive battery
 *  operated replacement for a standard wall switch to operate a single, zigbee or zwave light or
 *  set of lights.
 * 
 *  The PEQ sensor has been on sale intermittently at Best Buy for under $20, and the SPST wall switch costs a few
 *  dollars at any hardware store, so you can put the whole thing together for about $25.00.
 * 
 *  The PEQ moisture sensor can be easily modded and wired to the wall switch by desoldering the moisture sensor 
 *  and resoldering two wires through the two vias on the PCB.  I will be posting a detailed HOW-TO for
 *  modification of the PEQ Sensor on the Smartthings community board shortly.
 *
 *  A different version has been published to toggle on/off multiple switches simultaneously. 
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
	name: "On/Off Toggle Switch Using a Modded PEQ Moisture Sensor",
	namespace: "JWGthings",
	author: "Joel Goldwein (goldwein at gmail.com)",
	description: "PEQ On/Off Switch - This SmartApp uses a modified PEQ moisture sensor wired to a SPST switch to toggle on and off a GE Link Light, similar light or switch.   The intent is to have a simple and inexpensive battery operated replacement for a standard wall switch to operate a single zigbee or zwave light or switch.",
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
	log.debug "JWG: 1. $lname was $switch1.currentSwitch"
//  The contactSensor capability can be either "open" or "closed"
//  Code is triggered any time there is a state change in the door sensor
//  If the sensor is on
//	if("open" == evt.value && "off" == switch1.currentSwitch )  {
	if("closed" == evt.value)  {
//	Turn it ON
		switch1.on()
//	and if the sensor is off 
//	} else if ("closed" == evt.value && "on" == switch1.currentSwitch ) {
	} else if ("open" == evt.value) {
//	Turn it OFF    
		switch1.off()
	}
   	log.debug "JWG 2: PEQ sensor was $evt.value and $lname is $switch1.currentSwitch"
//  Send a info message
//	log.info "PEQ switch $cname is now $evt.value and $lname is now $switch1.currentSwitch"
//if("open" == evt.value && "on" == switch1.currentSwitch )  {
//		switch1.off()
//	} else if ("open" == evt.value && "off" == switch1.currentSwitch ) {		                    
//		switch1.on()
//    }    
//    else if ("closed" == evt.value && "on" == switch1.currentSwitch )  {
//		switch1.off()
//	} else if ("closed" == evt.value && "off" == switch1.currentSwitch ) {		                    
//		switch1.on()
//    }    
//	// I'm going to try doing this with a SPST switch
//	else if (null == evt.value && "on" == switch1.currentSwitch )  {
//		switch1.off()
//	} else if (null == evt.value && "off" == switch1.currentSwitch ) {		                    
//		switch1.on()
//    }

}