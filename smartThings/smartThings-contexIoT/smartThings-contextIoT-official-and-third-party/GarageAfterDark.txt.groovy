/**
 *  Garage After Dark
 *
 *  Author: Scotin Pollock
 *
 *  Date: 2014-06-15
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
 
definition(
    name: "Garage After Dark",
    namespace: "soletc.com",
    author: "Scotin Pollock",
    description: "Closes Garage and sends notification when garage door is open during sunset times.",
    category: "My Apps",
    iconUrl: "http://solutionsetcetera.com/stuff/STIcons/GDO.png",
    iconX2Url: "http://solutionsetcetera.com/stuff/STIcons/GDO@2x.png"
)

preferences {
    section("Close if garage door is open...") {
		input "contact1", "capability.contactSensor", title: "Which Sensor?"
		input "theDoor", "capability.switch", title: "Which Door?"
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, metadata: [values: ["Before","After"]]
	}
	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipCode", "text", title: "Zip Code", required: false
	}
	section( "Notifications" ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
	}
}

def installed() {
	initialize()
}

def updated() {
	unschedule()
	initialize()
}

def initialize() {
	scheduleAstroCheck()
	astroCheck()
}

def scheduleAstroCheck() {
	def min = Math.round(Math.floor(Math.random() * 60))
	def exp = "0 $min * * * ?"
    log.debug "$exp"
	schedule(exp, astroCheck) // check every hour since location can change without event?
    state.hasRandomSchedule = true
}

def astroCheck() {
	if (!state.hasRandomSchedule && state.riseTime) {
    	log.info "Rescheduling random astro check"
        unschedule("astroCheck")
    	scheduleAstroCheck()
    }
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)

	def now = new Date()
	def setTime = s.sunset
	log.debug "setTime: $setTime"
	if (state.setTime != setTime.time) {
		state.setTime = setTime.time

		unschedule("sunsetHandler")

		if (setTime.after(now)) {
			log.info "scheduling sunset handler for $setTime"
			runOnce(setTime, sunsetHandler)
		}
	}
}

def sunsetHandler() {
	log.info "Executing sunset handler"
    def Gdoor = checkGarage()
       if (Gdoor == "open") {
       	send("You left the garage door $Gdoor, closing now")
        theDoor.off()
        }
	unschedule("sunsetHandler") // Temporary work-around for scheduling bug
}

private send(msg) {
	if ( sendPushMessage != "No" ) {
		log.debug( "sending push message" )
		sendPush( message )
	}

	log.debug message
}

def checkGarage(evt) {
	def latestValue = contact1.currentContact
}


private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}