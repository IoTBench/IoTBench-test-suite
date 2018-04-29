/**
 * I use one every year for my tree, it reports dry just fine and no issues with the LED.
 * I use it for my fish tank to tell me when to refill the automatic fill tank (Dry sensor) until the tree goes up then use it for that. 
 * With the app just named “no water” but was posted here 2 years ago for just the Christmas tree low water purpose.
 */

definition(
name: “Dry Alert!”,
namespace: “smartthings”,
author: “SmartThings”,
description: “Get a push notification or text message when water is NOT detected.”,
category: “Safety & Security”,
iconUrl: “https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture.png 1”,
iconX2Url: “https://s3.amazonaws.com/smartapp-icons/Meta/water_moisture@2x.png 1”
)

preferences {
	section(“When there’s NO water detected…”) {
		input “alarm”, “capability.waterSensor”, title: “Where?”}
	
	section(“Send a notification to…”) {
		input(“recipients”, “contact”, title: “Recipients”, description: “Send notifications to”) {
		input “phone”, “phone”, title: “Phone number?”, required: false}
	}
}


def installed() {
	subscribe(alarm, “water.dry”, waterWetHandler)
}

def updated() {
	unsubscribe()
	subscribe(alarm, “water.dry”, waterWetHandler)
}

def waterWetHandler(evt) {
	def deltaSeconds = 60
	
	def timeAgo = new Date(now() - (1000 * deltaSeconds))
	def recentEvents = alarm.eventsSince(timeAgo)
	log.debug "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"

	def alreadySentSms = recentEvents.count { it.value && it.value == "dry" } > 1

	if (alreadySentSms) {
		log.debug "SMS already sent within the last $deltaSeconds seconds"
	} else {
		def msg = "${alarm.displayName} is dry!"
		log.debug "$alarm is dry, texting phone number"

	if (location.contactBookEnabled) {
		sendNotificationToContacts(msg, recipients)
	}
	else {
		sendPush(msg)
		if (phone) {
			sendSms(phone, msg)
		}
	}
}
}