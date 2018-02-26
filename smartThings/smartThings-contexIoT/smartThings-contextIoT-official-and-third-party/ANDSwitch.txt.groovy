/**
*  AND Switch
*
*  Copyright 2015 Dis McCarthy
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
    name: "AND Switch",
    namespace: "disconn3ct",
    author: "Dis McCarthy",
    description: "Allow 2 switches to trigger a 3rd, only if both are in the defined states.",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section ("First Input Switch") {
        input "switch1", "capability.switch"
        input (name: "switch1state", type: "enum", title: "State to Match", options: ["On","Off"])
    }
    section ("Second Input Switch") {
        input "switch2", "capability.switch"
        input (name: "switch2state", type: "enum", title: "State to Match", options: ["On","Off"])
    }
    section ("Output Switch and Matching State") {
        input "switchOut", "capability.switch"
        input (name: "switchOutState", type: "enum", title: "State When Matching", options: ["On","Off"])
        input (name: "switch2Reset", type: "enum", title: "Reset when unmatched?", options: ["Yes", "No"])

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
    subscribe(switch1, "switch", switchHandler)
    subscribe(switch2, "switch", switchHandler)
}

def switchHandler(evt) {
    log.debug "$evt.device is $evt.value"

    def sw1cur = switch1.currentValue("switch")
    def sw2cur = switch2.currentValue("switch")

    log.debug "Matching $switch1state to $sw1cur"
    log.debug "Matching $switch2state to $sw2cur"

    if ("$sw1cur" == switch1state.toLowerCase() && "$sw2cur" == switch2state.toLowerCase()) {
        log.debug "Switch match."
        if (switchOutState == "On") {
            log.debug "SwitchOut On"
            switchOut.on()
        } else if (switch2Reset == "Yes") {
            log.debug "SwitchOut Off"
            switchOut.off()
        }
    } else if (switch2Reset == "Yes") {
        log.debug "Switch No-Match"
        if (switchOutState == "On") {
            log.debug "SwitchOut Off"
            switchOut.off()
        } else {
            log.debug "SwitchOut On"
            switchOut.on()
        }
    }
}
