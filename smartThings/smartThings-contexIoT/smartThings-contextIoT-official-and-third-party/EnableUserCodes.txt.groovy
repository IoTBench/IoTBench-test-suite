/**
 *  Enable User Codes
 *
 *  Copyright 2014 Nick Skeba
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Enable User Codes",
    namespace: "",
    author: "Nick Skeba",
    description: "User Codes",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


/**
 *  Change Lock Codes
 *
 *  Author: bigpunk6
 */

preferences {
    section("What Lock") {
                input "lock1","capability.lock", title: "Lock"
    }
    section("User") {
        input "user1", "decimal", title: "User (From 1 to 30) "
        input "code1", "decimal", title: "Code (4 to 8 digits)"
        input "delete1", "enum", title: "Delete User", required: false, metadata: [values: ["Yes","No"]]
    }
}

def installed()
{
        subscribe(app, appTouch)
        subscribe(lock1, "usercode", usercodeget)
}

def updated()
{
        unsubscribe()
        subscribe(app, appTouch)
        subscribe(lock1, "usercode", usercodeget)
}

def appTouch(evt) {
    log.debug "Current Code for user $user1: $lock1.currentUsercode"
    log.debug "user: $user1, code: $code1"
    def idstatus1 = 1
    if (delete1 == "Yes") {
        idstatus1 = 0
    } else {
        idstatus1 = 1
    }
    lock1.usercodechange(user1, code1, idstatus1)
}

def usercodeget(evt){
    log.debug "Current Code for user $user1: $lock1.currentUsercode"
}