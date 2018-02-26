/**
 *  Habit Helper
 *  Every day at a specific time, get a text reminding you about your habit
 *
 *  Author: SmartThings
 */
preferences {
	section("Remind me about..."){
		input "message1", "text", title: "What?"
	}
	section("At what time?"){
		input "time1", "time", title: "When?"
	}
	section("Text me at..."){
		input "phone1", "phone", title: "Phone number?"
	}
}

def installed()
{
	schedule(time1, "scheduleCheck")
}

def updated()
{
	unschedule()
	schedule(time1, "scheduleCheck")
}

def scheduleCheck()
{
	log.trace "scheduledCheck"

	def message = message1 ?: "SmartThings - Habit Helper Reminder!"

	log.debug "Texting reminder: ($message) to $phone1"
	sendSms(phone1, message)
}
