/**
 *  Lock Door after X minutes
 *
 *  Copyright 2015 Eric Moritz
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
    name: "Lock Door after X minutes",
    namespace: "ericmoritz",
    author: "Eric Moritz",
    description: "Lock a door after it is unlocked",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Lock automatically") {
		input "whichLock", "capability.lock", title: "Which lock?"
        input "afterWhen", "decimal", title: "Lock after X minutes?"
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
  state.scheduled = false
  subscribe whichLock, "lock.unlocked", onUnlock
  // Poll every x minutes to check if the lock is unlocked
  pollForUnlocked()
}

def onUnlock(evt) {
  log.debug("Lock was unlocked, schedule locking")
  scheduleLock()
}

def pollForUnlocked() {
 // Here we lock the door if our state was screwed up.
 if(!state.scheduled && whichLock.currentLock == "unlocked") {
   log.debug("We noticed the lock was unlocked and we haven't scheduled it to lock... scheduling lock...")
   scheduleLock()
 }
 schedulePoll()
}

def schedulePoll() {
 runIn(60, pollForUnlocked)
}

def scheduleLock() {
  if(!state.scheduled) {
	state.scheduled = true
	runIn(afterWhen * 60, lock)
  }
}

def lock() {
  whichLock.lock()
  state.scheduled = false

}