/**
 *  Has Barkley Been Fed
 *
 *  Author: SmartThings
 */
preferences {
	section("Choose the pet cabinet...") {
		input "cabinet1", "capability.contactSensor", title: "Where?"
	}
	section("Feed my pet between...") {
		input "tweenStart", "time", title: "Start Time"
        input "tweenEnd", "time", title: "End Time"
	}
	section("Text me if I forget...(Optional, will use push message if no number entered)") {
		input "phone", "phone", title: "Phone number?", required: false
	}
}

def installed()
{
	schedule(tweenEnd, "scheduleCheck")
}

def updated()
{
	unsubscribe() //TODO no longer subscribe like we used to - clean this up after all apps updated
	unschedule()
	schedule(tweenEnd, "scheduleCheck")
}


def scheduleCheck()
{
	log.trace "scheduledCheck"

	def midnight = (new Date()).clearTime()
	def t0 = now()
    def startTime = timeToday(tweenStart)
    def endTime = timeToday(tweenEnd)
	def cabinetEvents = cabinet1.eventsBetween(startTime, endTime)
//    if (t0 <= endTime.time && t0 >= startTime.time) {
    if (t0 >= startTime.time) {
		log.trace "Found ${cabinetEvents?.size() ?: 0} cabinet events since $startTime"
		def cabinetOpened = cabinetEvents.count { it.value && it.value == "open" } > 0
		if (cabinetOpened) {
			log.debug "Cabinet was opened since $startTime"
            if (phone) {
   	   	    sendSms(phone, "The ${label} has been fed, yay!")
            } else {
            	send "The ${label} has been fed, yay!"
                }
		} else {
			log.debug "Cabinet was not opened since $startTime, texting $phone1"
			if (phone) {
            	sendSms(phone, "No one has fed the ${label}")
                } else {
            send "No one has fed the ${label}"
            	}	
        	}
	}
}

private send(msg) {
	sendPush msg
	log.debug msg
}

private getLabel() {
	app.label ?: "SmartThings"
}