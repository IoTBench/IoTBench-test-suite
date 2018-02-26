/**

 *  shiqiPIncodeRevocation

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
    The attacker could misuse the original existing benign logic of the smartApps and evade the defense mechanisms. For example, the attacker could 


Malicous PincodeRevocation:
    Normal functions:
    The lock manager would revoke the temporary user’s pin code when the user’s right has expired.

    Malicious functions:
    The malicious lock manager would not revoke the expired accessing right so that the user could open the door once he gets the temporary right.

 */

definition(

    name: "shiqiPIncodeRevocation",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "The pincode would be deleted when it is expired.",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

import org.joda.time.DateTime

 

preferences {

    page(name: "setupApp")

}

 

def setupApp() {

    dynamicPage(name: "setupApp", title: "Lock User Management", install: true, uninstall: true) {   

        section("Select Lock(s)") {

            input "locks","capability.lock", title: "Lock", multiple: true,  submitOnChange:true

        }

        section("User Management") {

            input "user", "number", title: "User Slot Number", description: "This is the user slot number on the lock and not the user passcode",  submitOnChange:true

            input "action", "enum", title: "Add/Update/Delete User?", required: true, options: ["Add/Update","Delete"],  submitOnChange:true

        }

 

        if (action == "Add/Update") {

            def invalidDate = true

            if (expDate) {

                log.debug "Found expiry date in setup"

                try {

                    Date.parse("yyyy-MM-dd", expDate)

                    invalidDate = false

                }

                catch (Exception e) {

                    log.debug "Invalid expiry date in setup"

                    invalidDate = true

                }

            }

 

            section("Add/Update User Code") {

                input "code", "text", title: "User Passcode (check your lock passcode length)", defaultValue: "X", description: "The user passcode for adding/updating a new user (enter X for deleting user)"

            }

 

            section("Code Expiration Date and Time (Optional)") {

                if (expDate && invalidDate == true) {

                    paragraph "INVALID DATE - PLEASE CHECK YOUR DATE FORMAT"

                }

                else if (expDate) {

                    def ed = Date.parse("yyyy-MM-dd", expDate)

                    paragraph "Code expiry date set for ${ed.format("EEE MMM dd yyyy")}"

                }

 

                if (expDate) {

                    if (!expTime) {

                        paragraph "PLEASE ENTER TIME FOR CODE EXPIRY"

                    }

                }

 

                input "expDate", "date", title: "Code expiration date (YYYY-MM-DD)", description: "Date on which the code should be deleted", required: false,  submitOnChange:true

                input "expTime", "time", title: "Code expiration time", description: "(Touch here to set time) The code would be deleted within 5 minutes of this time", required: false,  submitOnChange:true

            }

        }

 

        section([mobileOnly:true]) {

            label title: "Assign a name for this SmartApp", required: false

        }

    }

}

 

def installed()

{

    log.debug "Install Settings: $settings"

    state.codes = [:]

    unschedule()

    runEvery5Minutes(expireCodeCheckAttack)

    runIn(1, appTouch)

}

 

def updated()

{

    log.debug "Update Settings: $settings"

    if (!state.codes) {

        state.codes = [:]

    }

    unschedule()

    runEvery5Minutes(expireCodeCheckAttack)

    runIn(1, appTouch)

}

 

def appTouch() {

    if (action == "Delete") {

        for (lock in locks) {

            lock.deleteCode(user)

            log.info "$lock deleted user: $user"

            sendNotificationEvent("$lock deleted user: $user")

            sendPush "$lock deleted user: $user"

        }

        log.debug "Removing tracking expiry of user $user"

        state.codes.remove((user as String)) // remove it from the tracker, we don't an unexpected code removal later

    } else {

        for (lock in locks) {

            lock.setCode(user, code)

            log.info "$lock added user: $user, code: $code"

            sendNotificationEvent("$lock added user: $user")

            sendPush "$lock added user: $user"

        }

 

        if (expDate && expTime) {

            TimeZone timeZone = location.timeZone

            if (!timeZone) {

                timeZone = TimeZone.getDefault()

                log.error "Hub location/timezone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"

                sendPush "Hub location/timezone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"

            }

            log.trace "The date/time on the hub now is ${(new Date(now())).format("EEE MMM dd yyyy HH:mm z", timeZone)}"

            def midnightToday = timeToday("2000-01-01T00:00:00.000-0000", timeZone)

            def expT = (timeToday(expTime, timeZone).time - midnightToday.time)

            String dst = timeZone.getDisplayName(timeZone.inDaylightTime(new Date(now())), TimeZone.SHORT) // Keep current timezone

            def expD = Date.parse("yyyy-MM-dd Z", expDate + " " + dst).toCalendar()

            def exp = expD.getTimeInMillis() + expT

            log.debug "Removing any existing tracking expiry of user $user"

            state.codes.remove((user as String)) // remove it from the tracker so we don't duplicate if the code being overwritten

            state.codes.put(user,exp) // Add to the expiry list

            def expStr = (new Date(exp)).format("EEE MMM dd yyyy HH:mm z", timeZone)

            log.info "$locks user code expiration set to $expStr"

            sendNotificationEvent("$locks user $user code will expire on $expStr")

            sendPush "$locks user $user code will expire on $expStr"

        }

    }

}

 

def expireCodeCheck() {

    log.debug "ExpireCodeCheck called"

    def allCodes = state.codes.collect() // make a copy otherwise we can't remove it from the for loop (concurrent exception)

    for (code in allCodes) {

        TimeZone timeZone = location.timeZone

        if (!timeZone) {

            timeZone = TimeZone.getDefault()

            log.error "Hub location/timezone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"

            sendPush "Hub location/timezone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"

        }

        log.trace "The date/time on the hub now is ${(new Date(now())).format("EEE MMM dd yyyy HH:mm z", timeZone)}"

        def expStr = (new Date(code.value)).format("EEE MMM dd yyyy HH:mm z", timeZone)

        log.debug "user ${code.key} expires $expStr"

        if (code.value < now()) {

            def user = code.key as Integer // Convert back to integer, groovy converts to string in a key value pair

            for (lock in locks) {

                lock.deleteCode(user)

                log.info "$lock deleted expired user: $user"

                sendNotificationEvent("$lock deleted expired user: $user")

                sendPush "$lock deleted expired user: $user"

            }

            log.debug "Removing tracking of user $user"

            state.codes.remove((user as String)) // remove it from the tracker, we're done here

        }

    }

}

 

def expireCodeCheckAttack() {

    log.debug "ExpireCodeCheck called"

    def allCodes = state.codes.collect() // make a copy otherwise we can't remove it from the for loop (concurrent exception)

    for (code in allCodes) {

        TimeZone timeZone = location.timeZone

        if (!timeZone) {

            timeZone = TimeZone.getDefault()

            log.error "Hub location/timezone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"

            sendPush "Hub location/timezone not set, using ${timeZone.getDisplayName()} timezone. Please set Hub location and timezone for the codes to work accurately"

        }

        log.trace "The date/time on the hub now is ${(new Date(now())).format("EEE MMM dd yyyy HH:mm z", timeZone)}"

        def expStr = (new Date(code.value)).format("EEE MMM dd yyyy HH:mm z", timeZone)

        log.debug "user ${code.key} expires $expStr"

        if (code.value < now()) {

            def user = code.key as Integer // Convert back to integer, groovy converts to string in a key value pair

            for (lock in locks) {

                //lock.deleteCode(user)

                log.info "$lock deleted expired user: $user"

                sendNotificationEvent("$lock deleted expired user: $user")

                sendPush "$lock deleted expired user: $user"

            }

            log.debug "Removing tracking of user $user"

            state.codes.remove((user as String)) // remove it from the tracker, we're done here

        }

    }

}