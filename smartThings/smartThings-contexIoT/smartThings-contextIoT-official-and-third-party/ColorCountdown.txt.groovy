/**
 *  Color Countdown
 *
 *  Copyright 2014 Amie Williams
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
    name: "Color Countdown",
    namespace: "jaamkie",
    author: "Amie Williams",
    description: "Timer that counts down by changing light color, blinking when time expires. ",
    category: "Fun & Social",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    section("Set Timer ...") {
       input "minutes", "number", title: "Minutes"
    }
	section("Light...") {
		input "bulb", "capability.colorControl", title: "Color-Control Bulb"
	}
    // add section to pick color scheme (wheel - pick start / end)
    // add section for additional actions upon target date / time
    // (phone notification; text message; sonos sound; blink lights; buzz fitness tracker)
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
    state.hueStart = 30 // to be in settings; for now, start at green (30s)
    state.hueEnd = 100 // in settings; go to red (100)
    
    // figure out the time slices
	state.startTime = now()
    state.endTime = now() + settings.minutes * 1000 * 60
    state.intervalSeconds = 5 
    
    state.hueInterval = (state.hueEnd - state.hueStart) / settings.minutes / (60 / state.intervalSeconds)

	// initialize things
	state.counter = 0

	// turn bulb on and set it to starting color
    storeBulbState()
    setColor()
    settings.bulb.setSaturation(100)
	turnOn()
    
	runIn(state.intervalSeconds, "changeColor", [overwrite: false])
    log.debug "Time started"
}

def storeBulbState() {
	logColor()
	state.on = settings.bulb.currentValue("switch")
    state.level = settings.bulb.currentValue("level")
    state.saturation = settings.bulb.currentValue("saturation")
    state.hue = settings.bulb.currentValue("hue")
}

def resetBulbFromState() {
	logColor()
	settings.bulb.setLevel(state.level)
    settings.bulb.setSaturation(state.saturation)
    settings.bulb.setHue(state.hue)
    if("on" == state.on) {
    	settings.bulb.on()
    } else {
    	settings.bulb.off()
    }
    logColor()
}

def changeColor() {
    if (state.endTime <= now()) {
    	timesUp() 
    } else {
	    runIn( state.intervalSeconds, "changeColor", [overwrite: false])
    	state.counter = state.counter + 1
        setColor()
    }
}

def setColor() {
   settings.bulb.setHue( state.hueStart + state.hueInterval * state.counter )
//   logColor()
}

def timesUp() {
    log.debug "Time is Up!"
    sendNotification("Time is Up!")
    doubleBlink()
    runIn(30, "resetBulbFromState", [overwrite: false]) // to ensure blink is done first
}

def blink() {
	turnOn()
    runIn(1, "turnOff", [overwrite: false])
    runIn(2, "turnOn", [overwrite: false])
}

def doubleBlink() {
	turnOn()
    runIn(1, "turnOff", [overwrite: false])
    runIn(2, "turnOn", [overwrite: false])
    runIn(3, "turnOff", [overwrite: false])
    runIn(4, "turnOn", [overwrite: false])
}

def turnOn() {
	settings.bulb.setLevel(100)
}
def turnOff() {
	settings.bulb.setLevel(0)
}

def logColor() {
	log.debug "Hue: "+settings.bulb.currentValue("hue")+" Sat: "+settings.bulb.currentValue("saturation")+" Level: "+settings.bulb.currentValue("level")
}

