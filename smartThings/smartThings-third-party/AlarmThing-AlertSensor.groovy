/**
 *  AlarmThing Alert Sensor
 *
 *  Copyright 2014 ObyCode
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
    name: "AlarmThing Sensor Alert",
    namespace: "com.obycode",
    author: "ObyCode",
    description: "Alert me when there is activity on one or more of my alarm's sensors.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences {
    page(name: "selectAlarm")
    page(name: "selectSensors")
    page(name: "selectStates")
}

def selectAlarm() {
    dynamicPage(name: "selectAlarm", title: "Configure Alarm", nextPage:"selectSensors", uninstall: true) {
        section("When there is activity on this alarm...") {
            input "theAlarm", "capability.alarm", multiple: false, required: true
        }
    }
}

def selectSensors() {
    dynamicPage(name: "selectSensors", title: "Configure Sensors", uninstall: true, nextPage:"selectStates") {
        def sensors = theAlarm.supportedAttributes*.name
        if (sensors) {
            section("On these sensors...") {
                input "theSensors", "enum", required: true, multiple:true, metadata:[values:sensors], refreshAfterSelection:true
            }
        }
        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
        }
    }
}

def selectStates() {
    dynamicPage(name: "selectStates", title: "Which states should trigger a notification?", uninstall: true, install: true) {
        theSensors.each() {
            def sensor = it
            def states = []
            // TODO: Cannot figure out how to get these possible states, so have to guess them based on the current value
            switch(theAlarm.currentValue("$it")) {
            case "active":
            case "inactive":
                states = ["active", "inactive"]
                break
            case "on":
            case "off":
                states = ["on", "off"]
                break
            case "detected":
            case "clear":
            case "tested":
                states = ["detected", "clear", "tested"]
                break
            case "closed":
            case "open":
                states =  ["closed", "open"]
                break
            default:
                log.debug "value not handled: ${theAlarm.currentValue("$sensor")}"
            }           
            if (states) {
                section() {
                    input "${sensor}States", "enum", title:"For $sensor...", required: true, multiple:true, metadata:[values:states], refreshAfterSelection:true
                }
            }
        }
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
    theSensors.each() {
        def sensor = it
        settings."${it}States".each() {
            subscribe(theAlarm, "${sensor}.$it", sensorTriggered)
        }
    }
}

def sensorTriggered(evt) {
    sendPush("Alarm: ${evt.name} is ${evt.value}")
    log.debug "Alarm: ${evt.name} is ${evt.value}"
}
