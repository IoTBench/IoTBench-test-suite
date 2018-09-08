/**
 * Attack Description:
 * 		The attacker could misuse the original existing benign logic of the smartApps and evade the defense mechanisms. For example, the attacker could 
 * Normal functions:
 * 		The lock manager would revoke the temporary user’s pin code when the user’s right has expired.
 * Malicious functions:
 * 		The malicious lock manager would not revoke the expired accessing right so that the user could open the door once he gets the temporary right.
 */

definition(
		name: "Attack9: LockAccessRevocation",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "The pincode would be deleted when it is expired.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

import org.joda.time.DateTime

preferences { page(name: "setupApp") }


def setupApp() {
	dynamicPage(name: "setupApp", title: "Lock User Management", install: true, uninstall: true) {
		section("Select Lock(s)") {
			input "locks","capability.lockCodes", title: "Lock", multiple: true
		}
		section("User Management") {
			input "user", "number", title: "User Slot Number", description: "This is the user slot number on the lock and not the user passcode"
			input "action", "enum", title: "Add/Update/Delete User?", required: true, options: ["Add/Update", "Delete"],  submitOnChange:true
		}

		if (action == "Add/Update") {
			section("Add/Update User Code") {
				input "code", "text", title: "User Passcode (check your lock passcode length)", defaultValue: "X", description: "The user passcode for adding/updating a new user (enter X for deleting user)"
			}

			section("Code Expiration Date and Time (Optional)") {
				input "expDate", "date", title: "Code expiration date (YYYY-MM-DD)", description: "Date on which the code should be deleted"
				input "expTime", "time", title: "Code expiration time", description: "(Touch here to set time) The code would be deleted within 5 minutes of this time"
			}
		}
	}
}



def installed() {
	log.debug "Install Settings: $settings"
	state.codes = [:]
	initialize()
}


def updated() {
	log.debug "Update Settings: $settings"
	if (!state.codes) {
		state.codes = [:]
	}
	initialize()
}


def initialize(){
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
				//That is what missed!!
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