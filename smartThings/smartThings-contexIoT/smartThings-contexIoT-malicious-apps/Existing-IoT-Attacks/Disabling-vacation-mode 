
/**

 *  shiqiVacationMode

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
    Even some of the logic in benign smartApps could be misused to do something dangerous as long as the attacker stole the Oauth token.



Malicous VacationMode:
    Normal functions:
    The vacation mode smartApp could detect whether there is anybody home. If there is  nobody home and the user turn on the switch of the vacation mode, then the smartApp could control the light to simulate occupancy. When the sunrise, it would turn on the light. While the sunset, it would turn off the light.

    Malicious functions:
    Attacker with the legitimate token could control the vacation mode. If the user is out of home and has set on the vacation mode to let the light open when sunset. The user could send a put request to the smartApp’s path to trigger it set off the vacation mode.

 */

definition(

    name: "shiqiVacationMode",

    namespace: "wsq",

    author: "Yunhan Jia &amp; Shiqi Wang",

    description: "The smartApp could detect the presence of the user and changes the mode of the user. At the same time, when the user leaves home, it could change to the vacation mode to open the light at certain time.",

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

    section("vacation mode") {

          input "myswitch","capability.switch", title:"vacation mode?"

              input "light", "capability.switch", title: "light?"

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

    subscribe(myswitch,"switch", switchHandler)

    subscribe(location, "sunset", sunsetHandler)

    subscribe(location, "sunrise", sunriseHandler)

}

 

def updated() {

       log.debug "Updated with settings: ${settings}"

       log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"

       unsubscribe()

       subscribe(people, "presence", presence)

    subscribe(myswitch,"switch", switchHandler)

    subscribe(location, "sunset", sunsetHandler)

    subscribe(location, "sunrise", sunriseHandler)

}

 

def switchHandler(evt) {

       if(evt.value == "on") {

          state.vacation = true

        log.debug "vacation mode set"

    }

    else {

          state.vacation = false

        log.debug "vacation mode cancel"

    }

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

            state.home = true

              }

       }

       else {

              log.debug "canceling"

              unschedule("takeAction")

        state.home = true

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

    state.home = false

       setLocationMode(newMode)

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

 

def sunsetHandler(evt) {

       if(state.vacation&&!state.home) {//not home and vacation mode on

          log.debug "sunset&vacation:close the light!"

        light.off()

    }

}

 

def sunriseHandler(evt) {

       if(state.vacation&&!state.home) {

          log.debug "sunrise&vacation:open the light!"

        light.on()

    }

}