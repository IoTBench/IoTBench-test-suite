/**
 *  Modifying to turn a switch off after a predetermined period.
 *	
 *	Turn It On For 5 Minutes
 *  Turn on a switch when a contact sensor opens and then turn it back off 5 minutes later.
 *
 *  Author: Michael Taylor
 */
definition(
    name: "Switch Timer",
    author: "Michael Taylor",
    category: "My Apps",
    description: "Set a switch to turn off after a predetermined time.",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_contact-outlet@2x.png"
)

preferences {
	section("Turn on a switch..."){
		input "switch1", "capability.switch"
	}
    section("For some minutes..."){
		input "minutes1", "decimal"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(app, appTouch)
}

def updated(settings) {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(app, appTouch)
}

def appTouch(evt) {
	switch1.on()
	def timerDelay = 60 * minutes1
    unschedule(turnOffSwitch)
	runIn(timerDelay, turnOffSwitch)
}

def turnOffSwitch() {
	switch1.off()
}