/**
 *  Test
 *
 *  Copyright 2014 Kevin Lewis
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
    name: "Left Unlocked",
    namespace: "coolkev",
    author: "Kevin Lewis",
    description: "Locks a door if left unlocked for a period of time.",
    category: "Safety & Security",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Home.home3-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Home.home3-icn?displaySize=2x")

preferences {

	section("Monitor this lock") {
		input "lock1","capability.lock", multiple: false
	}
	section("Door contact sensor") {
		input "contact1","capability.contactSensor", multiple: false
	}

	section("And notify me if it's open for more than this many minutes (default 10)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
}

def installed() {
	log.trace "installed()"
	
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(lock1, "lock", handleLock)
}

def handleLock(evt)
{
	log.trace "handleLock($evt.name: $evt.value)"
    
    if (evt.value=="unlocked") {
	
		def delaySeconds = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 600
		runIn(delaySeconds, doorUnlockedTooLong)
        
	}
    else {
    	unschedule( doorUnlockedTooLong )
    }
}

def doorUnlockedTooLong() {
	
    if (lock1.currentLock != "locked") {

        def delayMinutes = ((openThreshold != null && openThreshold != "") ? openThreshold : 10)

        if (contact1.currentContact=="closed") {
            sendPush "Door has been unlocked for $delayMinutes minutes. Locking automatically..."
            lock1.lock()
        }
        else
        {    
            sendPush "Door has been unlocked for $delayMinutes minutes. Cannot lock because the door is open. Will try again in $delayMinutes minutes."    
            runIn(delayMinutes*60, doorUnlockedTooLong)
        }
   }
}
