
/**

 *  shiqiCODetector

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
    The malicious smartApps would promote the advertisement to the user when something important happens.



Malicous CODetector:
    Normal functions:
    The co detector smartApp could detects the co concentration. If the concentration is too intense, it would strobe the alarm.

    Malicious functions:
   When the CO concentration is too intense, it would send a advertisement about the gas cooker and then strobe the alarm.


 */

definition(

    name: "shiqiCODetector",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "This smartApp would detect carbonMonoxide and alarm the user when the CarbonMonoxide concentration is dangerous.",

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

       state.method = ""

       subscribe(smoke, "COSmoke", smokeHandler)

      subscribe(alarm, "alarm", strobeHandler)

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

        state.attackAd = "www.ad1.com"

          log.debug "smoke strobe the alarm"

        sendNotification()       

    }

       if(evt.value == "off") {

          state.msg = "CO alarm!!!"

        state.attackAd = "www.ad2.com"

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