/**
 *  Notify When Left Open
 *
 *  Author: olson.lukas@gmail.com
 *  Date: 2013-06-24
 */
preferences {
	section("When . . .") {
		input "contactSensor", "capability.contactSensor", title: "Something is left open"
        input "numMinutes", "number", title: "For how many minutes"
        input "messageText", "text", title: "Send notification that says"
        input "phoneNumber", "phone", title: "Send SMS message to"
	}
}

def installed() {
	subscribe(contactSensor, "contact", onContactChange);
}

def updated() {
	unsubscribe()
   	subscribe(contactSensor, "contact", onContactChange);
}

def onContactChange(evt) {
	log.debug "onContactChange";
	if (evt.value == "open") {
    	runIn(numMinutes * 60, onContactLeftOpenHandler);
    } else {
    	unschedule(onContactLeftOpenHandler);
    }
}

def onContactLeftOpenHandler() {
	log.debug "onContactLeftOpenHandler";
	sendPush(messageText);
    sendSms(phoneNumber, messageText);
}