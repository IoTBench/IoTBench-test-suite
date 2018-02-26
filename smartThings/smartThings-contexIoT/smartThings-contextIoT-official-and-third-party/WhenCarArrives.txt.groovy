/**
 *  Garage Door Opener
 *
 *  Opens garage door if not open and presence sensor changes to present.
 *
 *  This is an improvement on the SmartThings app that will trigger 
 *    the garage door switch regardless of the state of the door.
 *    Yes... the SmartThings garage door app will close the door on your
 *    car while you are pulling into the garage if the presence sensor
 *    is a little slow and the door is already open.
 *
 *  Copyright 2014 Matthew Nichols
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
**/


// Automatically generated. Make future change here.
definition(
    name: "When Car Arrives",
    namespace: "name.nichols.matt.smartapps",
    author: "Matthew Nichols",
    description: "Opens specified garage door and turns on lights when presence detected and the door is closed.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When any of these cars arrive...") {
		input "cars", "capability.presenceSensor", multiple: true
	}
	section("Open this garage door...") {
		input "garageDoorContact", "capability.contactSensor", multiple: false
	}
	section("With this relay...") {
		input "garageDoorSwitch", "capability.momentary", multiple: false
	}
	section("Turn on these lights...") {
		input "lightSwitches", "capability.switch", multiple: true
	}
	section("Wait this many minutes between presence changes to act (default: 10)...") {
		input "falseAlarmThreshold", "number", required: false, title: "Minutes?"
	}
}

def installed() {
	subscribe(cars, "presence", presence)
}

def updated() {
	unsubscribe()
	subscribe(cars, "presence", presence)
}

def presence(evt)
{
	log.debug("${evt.name}: ${evt.value}")
	def threshold = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? (falseAlarmThreshold * 60 * 1000) as Long : 10 * 60 * 1000L
	
	def t0 = new Date(now() - threshold)
	if (evt.value == "present") {

		def car = getCar(evt)
		def recentNotPresent = car.statesSince("presence", t0).find{it.value == "not present"}
		if (recentNotPresent) {
			log.debug "skipping open for ${car.displayName} because last departure was only ${now() - recentNotPresent.date.time} msec ago"
		}
		else {
			openDoor()
		}
	}
}

private openDoor()
{
	lightSwitches.on()
	if (garageDoorContact.currentContact == "closed") {
		log.debug "opening door"
		garageDoorSwitch.push()
	}
	else {
		log.debug "door already open"
	}
}

private getCar(evt)
{
	cars.find{evt.deviceId == it.id}
}

