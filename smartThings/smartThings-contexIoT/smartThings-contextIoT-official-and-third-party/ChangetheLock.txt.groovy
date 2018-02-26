/**
 *  Change the Lock
 *
 *  Copyright 2015 Aaron Parecki
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
    name: "Change the Lock",
    namespace: "pk.aaron",
    author: "Aaron Parecki",
    description: "Updates the lock code",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    oauth: true)

preferences {
	section("Choose a Door Lock") {
		input "doorlock", "capability.lockCodes"
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
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(doorlock, "codeReport", codeReportEvent);
    subscribe(doorlock, "codeChanged", codeChangedEvent);
}

mappings {
  path("/lock/code") {
    action: [
      GET: "listCode",
      POST: "updateCode"
    ]
  }
}

def listCode() {
  doorlock.requestCode(params.num.toInteger())
  return ["num":params.num.toInteger()]
}

def updateCode() {
  log.debug "update | request: params: ${params}"
  doorlock.setCode(params.num.toInteger(), params.code)
  return ["num":params.num.toInteger(), "code": params.code.toInteger()]
}

def codeReportEvent(evt) {
  log.debug "Got the code report event"
  log.debug evt.jsonData
  sendPostRequest([
    type: "CodeReport",
    locationId: evt.locationId,
    locationName: location.name,
    descriptionText: evt.descriptionText,
    num: evt.value,
    code: evt.jsonData.code
  ])
}

def codeChangedEvent(evt) {
  log.debug "Code changed"
  log.debug evt.value
  sendPostRequest([
    type: "CodeChanged",
    num: evt.value
  ])
}

def sendPostRequest(data) {
  httpPostJson([
    uri: "http://door.launchpad4.com/notify.php",
    body: data
  ])
}
