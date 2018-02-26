/**
 *  Testing
 *
 *  Copyright 2015 Josiah Spence
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
    name: "Hue Notify If Door Open at a Set Time",
    namespace: "",
    author: "Josiah Spence",
    description: "Turns a specific Hue bulb a specified color if a door is open at a certain time.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
)

preferences {
	section("Choose your Garage Door Sensor...") {
		input "garageDoorStatus", "capability.contactSensor", title: "Where?"
	}
	section("If Open, Close My Garage Door at...") {
		input "time1", "time", title: "When?"
	}
    // Hue Bulbs //
    section("Color Settings for Hue Bulbs...") {
        input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
        input "color", "enum", title: "Hue Color?", required: false, multiple:false, options: [
            "Soft White":"Soft White - Default",
            "White":"White - Concentrate",
            "Daylight":"Daylight - Energize",
            "Warm White":"Warm White - Relax",
            "Red":"Red",
            "Green":"Green",
            "Blue":"Blue",
            "Yellow":"Yellow",
            "Orange":"Orange",
            "Purple":"Purple",
            "Pink":"Pink"]
        input "lightLevel", "enum", title: "Light Level?", required: false, options: [10:"10%",20:"20%",30:"30%",40:"40%",50:"50%",60:"60%",70:"70%",80:"80%",90:"90%",100:"100%"]
    }
}

def installed()
{
	log.debug "Installed with settings: ${settings}"
	schedule(time1, "scheduleCheck")
}

def updated()
{
	def now = new Date()
	log.debug "Current Time is $now"
	unsubscribe() //TODO no longer subscribe like we used to - clean this up after all apps updated
	unschedule()
	schedule(time1, "scheduleCheck")
}

def scheduleCheck()
{
	log.trace "scheduledCheck"
    def currentState = garageDoorStatus.contactState
	if (currentState?.value == "open") {
		log.debug "Door was open - Notifying."
        
        def hueColor = 0
        def saturation = 100

        switch(color) {
            case "White":
                hueColor = 52
                saturation = 19
                break;
            case "Daylight":
                hueColor = 53
                saturation = 91
                break;
            case "Soft White":
                hueColor = 23
                saturation = 56
                break;
            case "Warm White":
                hueColor = 20
                saturation = 80 //83
                break;
            case "Blue":
                hueColor = 70
                break;
            case "Green":
                hueColor = 39
                break;
            case "Yellow":
                hueColor = 25
                break;
            case "Orange":
                hueColor = 10
                break;
            case "Purple":
                hueColor = 75
                break;
            case "Pink":
                hueColor = 83
                break;
            case "Red":
                hueColor = 100
                break;
        }

        state.previous = [:]

        hues.each {
            state.previous[it.id] = [
                "switch" : it.currentValue("switch"),
                "level" : it.currentValue("level"),
                "hue" : it.currentValue("hue"),
                "saturation" : it.currentValue("saturation")
            ]
        }

		def previousHueColor = state.hueColor as Integer

        log.debug "current values = $previousHueColor"

        def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]

        log.debug "new hue value = $newValue"

        hues*.setColor(newValue)
        
        def fiveMinuteDelay = 60 *5
        
        runIn(fiveMinuteDelay, endNotification)
        
        
	} else {
		log.debug "Door was not open. No Action."
	}
}

def endNotification() {
    
    hues.each {
		it.setColor(state.previous[it.id])
	}
}
