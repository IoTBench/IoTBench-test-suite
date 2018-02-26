/**
 *  Turn on when Open, Only at Night
 *
 *  Author: chrisb
 *  Date: 2013-07-05
 */

 preferences {
     section("When the door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?"
     }
     section("Turn on a light..."){
		input "switches", "capability.switch", multiple: true
     }
	 section("Between this time at night:") {
		 input "timeOfDay1", "time", title: "Time?"
	 }
     section("And this time in the morning:") {
		 input "timeOfDay2", "time", title: "Time?"
	 }
}

def installed()
{
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}


def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
    def startTime = timeToday(timeOfDay1)
    def endTime = timeToday(timeOfDay2)
	if (now() < startTime.time && now() > endTime.time) 
    {
    }
    else {
         log.trace "Turning on switches: $switches"
	     switches.on()
    }
}