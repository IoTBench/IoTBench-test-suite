/**
 *  I'm Back - Change to Home Mode on Code Unlock
 *
 *  Copyright 2014 Barry A. Burke
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
    name: "Home on Code Unlock",
    namespace: "smartthings",
    author: "Barry A. Burke",
    description: "Set mode = Home when specific lock is unlocked with a specific code (by a specified person).",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

import groovy.json.JsonSlurper

preferences {
	page(name: "selectPhrases")
}

def selectPhrases() {
	def configured = (settings.lock1 && settings.lockCode1 && settings.homePhrase)
    dynamicPage(name: "selectPhrases", title: "Configure your code and phrases.", install: configured, uninstall: true) {	
    
		section("What Lock?") {
			input "lock1","capability.lock", title: "Lock"
    	}

		section("Which user?") {
			input name: "lockCode1", title: "User code (1-30)", type: "number", multiple: false
            input name: "userName", title: "User identifier", type: string, multiple: false
		}
        
        section("Return to away if none of these are present") {
        	input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
        }
            
    	def phrases = location.helloHome?.getPhrases()*.label
    	if (phrases) {
       		phrases.sort()
            log.trace phrases
			section("Hello Home actions...") {
				input "homePhrase", "enum", title: "Home Mode Phrase (I'm Back!)", required: true, options: phrases, refreshAfterSelection:true
            	input "awayPhrase", "enum", title: "Away Mode Phrase (Goodbye!)", required:true, options: phrases, refreshAfterSelection:true
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

def initialize()
{
    log.debug "Settings: ${settings}"
//    subscribe(lock1, "lock", doorHandler, [filterEvents: false])
	subscribe(presence1, "presence", presence)
    subscribe(lock1, "lock", doorHandler)
    state.unlockSetHome = false
}


def doorHandler(evt)
{
//    log.debug "The ${lock1.displayName} lock is ${lock1.latestValue("lock")}."

	if (evt.name == "lock") {
    	if (evt.value == "unlocked") {
        	if (!state.unlockSetHome) { 										// only if we aren't already unlocked
	    		if ((evt.data != "") && (evt.data != null)) {					// ...and only if we have extended data
	    			def data = new JsonSlurper().parseText(evt.data)
            		if ((data.usedCode != "") && (data.usedCode != null)) {		// ...and only iuf we have usedCode data
		   				if (data.usedCode == lockCode1) { 						// ...and only if unlocked with the lockCode1
		        			log.debug "${lock1.displayName} unlocked with code ${data.usedCode} - ${userName} is Home!"
			        		if (location.mode != "Home") {  					// Only if we aren't already in Home mode
    	    					sendNotificationEvent("Running \"${homePhrase}\" because ${userName} unlocked ${lock1.displayName}.")
                            	state.unlockSetHome = true						// do this first, in case I'm Back unlocks the door too
								location.helloHome.execute(settings.homePhrase)	// Wake up the house - we're HOME!!!
                            }
                        }
                    }
                }
            }
        }
        else if (evt.value == "locked") {
        	if (state.unlockSetHome) {							// Should assure that only this instance runs Goodbye!
            	if (presence1.find{it.currentPresence == "present"} == null) {
            		if (location.mode != "Away") {
                		sendNotificationEvent("Running \"${awayPhrase}\" because ${userName} locked ${lock1.displayName} and nobody else is at home.")
                    	state.unlockSetHome = false								// do this first, in case Goodbye! action locks the door too.
                    	location.helloHome.execute(settings.awayPhrase)
                    }
                }
            }
        }
    }
}
