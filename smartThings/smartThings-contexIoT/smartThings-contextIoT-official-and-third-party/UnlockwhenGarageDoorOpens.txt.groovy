/**
 *  Unlock when Garage Door Opens
 *
 *  Copyright 2014 chrisb
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
    name: "Unlock when Garage Door Opens",
    namespace: "chrisb",
    author: "chrisb",
    description: "App is designed to unlock an interior door when a Garage Door is opened, then optionally relock it a few minutes later only if the app unlocked the door.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Garage door sensor:") {
		input "doorSensor", "capability.contactSensor", title: "Which sensor?"	
		}
        // Ask the user which open/close sensor is on the garage door.
	section("Interior door lock:") {
	    input "interiorDoorLock", "capability.lock", title: "Which door lock?"
    	}
        // Ask the user which lock they want to use.
	section("Relock later?") {
    	input "lockLater", "enum", metadata: [values: ["Yes", "No"]], title: "Should I relock?" 
        input "reallyLockLater", "enum", metadata: [values: ["Yes", "No"]], title: "Relock even if originally unlocked?"
        // enum provides a list for the user to select from when installing.  Here we just have two options: Yes or no.  But you can
        // have many options if you so desire.  the "enum" type operates like a text variable.      
		input "lockDelay", "number", title: "How many minutes should I wait before locking?", required: false
		}
        // Ask the user if s/he wants the the program to relock the lock, even if it was unlocked previously.  Also
        // ask how many minutes we want to wait.
    section("Optional: Have Ubi speak when the door locks:") {
		input "behaviorToken", "text", title: "What is the Ubi Token?", required: false, autoCorrect:false
        }
        // Optionally ask for a ACB token from Ubi.  The user should create a new Behavior in Ubi with the trigger being
        // an http request.  The action should be an Ubi utterance of {variable}.  This will generate a token.  The user
        // will enter this here.  This is not required.
	}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()															// When installing, call the initialize procedure.  This connects the apps to
    }																		// the triggers we need to run.

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()															// When updating, make sure we unsubscribe from anything subscribed to before
    unschedule()															// as well as scheduled events.
	initialize()															// Just like when we install, when we update we also call initialize.
	}

def initialize() {
	subscribe(doorSensor, "contact.open", contactOpenHandler)
    }
	// We want to subscribe to when the garage door sensor (doorSensor) opens ("contact.open").  When it does, we'll run the
    // procedure that descides what to do (contactOpenHandler).


/*
*	The contactOpenHandler procedure (or method) does two things:
*	First: It checks if the door is currently locked.  If it is, it unlocks it.
*   Second: Using various If-statements we try to figure out if the users wants the app to relock the door.  If one of these "relock"
*    	conditions are true, then this procedure schedules the 'relock' procedure.
*/
def contactOpenHandler(evt) {
	log.debug "current lock status: ${interiorDoorLock.currentLock}"
	if (interiorDoorLock.currentLock == "locked") {							// If the door is currently locked then...
    	interiorDoorLock.unlock()											// ...unlock the door.
        if (lockLater == "Yes") {												// if the user wants to relock the door then...
	        def timeDelay = lockDelay * 60										// convert minutes to seconds because runIn uses seconds
 		    runIn (timeDelay, relock)	 										// schedule the relock procedure
            																	// -->> runIn (time, procedure)  Time is the number of seconds from now that
                                                                                // you to run the procedure.  The procedure is obviously what you want to run
            }
    } else {																// If the door isn't currently locked then...
        if (reallyLockLater == "Yes") {											// if the user wants to lock the door even if it wasn't locked then...
        	def timeDelay = lockDelay * 60										// convert minutes to seconds.
 		    runIn (timeDelay, relock)	 										// schedule the relock procedure
            }
        }
	}

/*
*	The relock procedure performs three actions:
*	First: it relocks the door.
*	Second: it builds a phrase that is used as a push notification to the user.
*	Third: if the user entered an Ubi token, it builds, and then pushes the Ubi notification.
*/
def relock() {
	interiorDoorLock.lock()													// Lock the door.
    def phrase = ""															// initialize phrase...
    def ubiPhrase = ""														// and ubiPhrase.
    phrase = "Locking the " + interiorDoorLock.displayName					// Build our phrase for the push notification.
    sendPush phrase															// Send the push notification
    if (behaviorToken) {													// If the user supplied an Ubi token (ie, if the variable 
    																		// "behaviorToken" is not empty) then...
    	ubiPhrase = phrase.replaceAll(' ', '%20')							// Convert the spaces in "phrase" to %20 so they'll work as an http request.
       	httpGet("https://portal.theubi.com/webapi/behaviour?access_token=${behaviorToken}&variable=${ubiPhrase}")
        }																	// send the http request to Ubi.
    }