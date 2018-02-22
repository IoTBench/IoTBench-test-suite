/**
 *  Copyright 2015 Jesse Newland
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
    name: "Hello Home / Mode Switches",
    namespace: "jnewland",
    author: "Jesse Newland",
    description: "Name on/off tiles the same as your Hello Home phrases or Modes",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
  subscribe(switches, "switch", switchHandler)
}

def switchHandler(evt) {
  def s = switches.find{ evt.deviceId == it.id }
  def phrase = location.helloHome.getPhrases().find { it.label == s.displayName }
  if (phrase) {
    location.helloHome.execute(phrase.label)
  }
  def mode = location.modes.find { it.name == s.displayName }
  if (mode) {
    setLocationMode(mode)
  }
}

preferences {
  page(name: selectSwitches)
}

def selectSwitches() {
    dynamicPage(name: "selectSwitches", title: "Switches", install: true) {
        section("Select switches named after Hello Home phrases") {
            input "switches", "capability.switch", title: "Switches", multiple: true
        }
    }
}
