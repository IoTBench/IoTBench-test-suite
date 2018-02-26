/**
 *  Turn on when Open, Only at Night, Then off after x minutes
 *
 *  Author: Ryan Nathanson with the main "turn on when open & night" code created by chrisb
 *  Date: 2013-10-20
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
     section ("Turn off after this many minutes") {
     input "minutesLater", "number", title: "Delay"
     }
     }

def installed() {
	initialize()
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def initialize(){
subscribe(contact1, "contact.open", contactOpenHandler)
}

def lightsoff(){
switches.off()
}

def contactOpenHandler(evt) {
	log.debug "$evt.value: $evt, $settings"
    def startTime = timeToday(timeOfDay1)
    def endTime = timeToday(timeOfDay2)
  	if (now() < startTime.time && now() > endTime.time) 
    {
    }
    else {
        switches.on()
        def delay = minutesLater * 60
	runIn(delay, lightsoff)
    log.debug "turning lights off aftet delay"
          }
  

}
