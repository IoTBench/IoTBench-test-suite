/**
 *  Sleepy Time
 *
 *  Copyright 2014 Physical Graph Corporation
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
    name: "Jawbone Control",
    namespace: "sriquier",
    author: "Scott Riquier",
    description: "Use Jawbone sleep mode events to automatically execute Hello, Home phrases. Automatially put the house to bed or wake it up in the morning by pushing the button on your UP.",
    category: "Mode Magic",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/jawbone-up@2x.png"
)

preferences {
	page(name: "selectPhrases")
}

def selectPhrases() {
	def configured = (settings.sleepPhrase && settings.wakePhrase && settings.jawbone)
    dynamicPage(name: "selectPhrases", title: "Configure Your Jawbone Phrases.", install: configured, uninstall: true) {		
		section("Select your Jawbone UP") {
			input "jawbone", "device.jawboneUser", title: "Jawbone UP", required: true, multiple: false,  refreshAfterSelection:true
		}
        
		def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) {
        	phrases.sort()
			section("Hello Home Actions") {
				log.trace phrases
				input "sleepPhrase", "enum", title: "Enter Sleep Mode (Bedtime) Phrase", required: true, options: phrases,  refreshAfterSelection:true
				input "wakePhrase", "enum", title: "Exit Sleep Mode (Waking Up) Phrase", required: true, options: phrases,  refreshAfterSelection:true
				input "ignoreMode", "mode", title: "Ignore Mode", required: false
			}
		}
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
    
    log.debug "Subscribing to sleeping events."
    
   	subscribe (jawbone, "sleeping", jawboneHandler)
    
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    
    log.debug "Subscribing to sleeping events."
        
   	subscribe (jawbone, "sleeping", jawboneHandler)
    
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

def jawboneHandler(evt) {

	log.debug "In Jawbone Event Handler, Event Name = ${evt.name}, Value = ${evt.value}, Current Mode = ${location.mode}, Sleep Phrase = ${sleepPhrase}, Wake Phrase = ${wakePhrase}, Ignore Mode = ${ignoreMode}"

	if (location.mode == ignoreMode) {
    	log.debug "Jawbone Control - Jawbone event ignored since system is in an ignored mode."
            sendNotificationEvent("Jawbone Control not performing any tasks since you requested sleep changes to be ignored in ${ignoreMode} mode.")
        }
    else {
        if (evt.value == "sleeping") {
            sendNotificationEvent("Jawbone Control performing \"${sleepPhrase}\" for you as requested.")
            location.helloHome.execute(settings.sleepPhrase)
        }
        else {
            sendNotificationEvent("Jawbone Control performing \"${wakePhrase}\" for you as requested.")
            location.helloHome.execute(settings.wakePhrase)
        }
    }
        
}