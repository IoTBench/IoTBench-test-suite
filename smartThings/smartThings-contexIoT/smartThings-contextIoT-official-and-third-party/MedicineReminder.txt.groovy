/**
 *  Medicine Reminder
 *
 *  Author: SmartThings
 */

preferences {
	section("Choose your medicine cabinet..."){
		input "cabinet1", "capability.contactSensor", title: "Where?"
	}
	section("Take my medicine at..."){
		input "time1", "time", title: "When?"
	}
	section("Text me if I forget..."){
		input "phone1", "phone", title: "Phone number?"
	}
}



def installed()
{
	schedule(time1, "scheduleCheck")
}

def updated()
{
	unsubscribe() //TODO no longer subscribe like we used to - clean this up after all apps updated
	unschedule()
	schedule(time1, "scheduleCheck")
}

def scheduleCheck()
{
	log.trace "medicineCheck: $settings"

	def midnight = (new Date()).clearTime()
	def now = new Date()
	def cabinetEvents = cabinet1.eventsBetween(midnight, now)
	log.trace "Found ${cabinetEvents?.size() ?: 0} cabinet events since $midnight"
	def cabinetOpened = cabinetEvents.count { it.value && it.value == "open" } > 0

	if (cabinetOpened) {
		log.debug "Medicine cabinet was opened since $midnight, no SMS required"
	} else {
		log.debug "Medicine cabinet was not opened since $midnight, texting $phone1"
		sendSms(phone1, "Please remember to take your medicine")
	}
}
