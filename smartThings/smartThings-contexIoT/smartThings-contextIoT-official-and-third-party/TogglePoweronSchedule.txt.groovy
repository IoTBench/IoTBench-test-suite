/**
 *  Toggle Power on Schedule
 *
 *  Copyright 2015 Dav Glass
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
    name: "Toggle Power on Schedule",
    namespace: "davglass",
    author: "Dav Glass",
    description: "Toggle the power of an outlet on a set schedule",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/GreenLiving/Cat-GreenLiving@2x.png")


preferences {
	section("App Information") {
        paragraph "Set the number of hours you want to toggle and this app will toggle a set of outlets at that time. Tapping the app will toggle it immediately."
    }
    section("Outlets to toggle:") {
        input "outlets", "capability.switch", title: "Outlets", required: true, multiple: true
    }
    section("Hours to leave on/off:") {
    	paragraph "crontab example: 0 0 0/4 * * ?"
        input "hours", "number", title: "Hours", required: true, defaultValue: 4
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
	def cron = "0 0 0/${hours} * * ?"
    log.debug "Setting up cron with: ${cron}"
	schedule(cron, toggleSwitches);
    subscribe(app, toggleSwitches);
}

def toggleSwitches(evt) {
	outlets.each {
    	def currentValue = it.currentValue("switch");
    	def msg = "${it.displayName} is ${currentValue}, switching it "
    	if (currentValue == "on") {
        	msg += "off"
        	it.off();
        } else {
        	msg += "on"
        	it.on();
        }
        log.debug msg;
        sendNotificationEvent(msg)
    }
}
