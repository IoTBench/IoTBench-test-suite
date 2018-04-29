/**
* Auto change to Vacation Mode
* Copyright 2015 Ashutosh Jaiswal
* Licensed under the Apache License, Version 2.0 (the “License”); you may not use this file except
*in compliance with the License. You may obtain a copy of the License at:
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*/
definition(
name: “Auto change to Vacation Mode”,
namespace: “”,
author: “Ashutosh Jaiswal”,
description: “This app automatically changes mode from Away to Vacation Mode after it stays in Away mode for a period of time”,
category: “Safety & Security”,
iconUrl: “http://www.clker.com/cliparts/a/5/5/5/1315944954399165199Baggage%20Check.svg.hi.png”,
iconX2Url: “http://www.clker.com/cliparts/a/5/5/5/1315944954399165199Baggage%20Check.svg.hi.png”,
iconX3Url: “http://www.clker.com/cliparts/a/5/5/5/1315944954399165199Baggage%20Check.svg.hi.png”)

preferences {
section(“Choose Mode for Away…”) {
input “AwayMode”,“mode”, title: “Away Mode”
}
section(“Choose Mode for Vacation…”) {
input “VacationMode”,“mode”, title: “Vacation Mode”
}
section(“Change mode to Vacation mode after how much time in Away Mode?”){
input “Time”, “number”, title: “Hours?”
}

}

def installed() {
log.debug “Installed with settings: ${settings}”

initialize()
}

def updated() {
log.debug “Updated with settings: ${settings}”

unsubscribe()
initialize()
}

def initialize() {
subscribe(location, modeChangeHandler)
}

def modeChangeHandler(evt) {
log.debug “Mode change to: ${evt.value}”

 if (AwayMode == evt.value) {
	def delay = Time*60*60  
	runIn(delay, changemode)
}
else {
	log.debug("Mode did not change to - Away mode")
	}
}

def changemode(){
if (AwayMode == location.mode) {
sendPush "Mode has changed to ‘${VacationMode}’ since you were in Away mode for more than ‘${Time}’ hours"
setLocationMode(VacationMode)
}
else {
log.debug “Cannot change to Vacation mode since mode was changed in between”
}
}