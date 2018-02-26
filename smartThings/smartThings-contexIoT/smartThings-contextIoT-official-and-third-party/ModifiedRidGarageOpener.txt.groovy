/**
 *  Ridiculously Automated Garage Door - Modified
 *
 *  Author: SmartThings
 *  Date: 2013-03-10
 *  Modified by chrisb
 *  Date: 2013-09-04
 *
 * Monitors arrival and departure of car(s) and
 *
 *    1) opens door when car arrives,
 *    2) closes door after car has departed (for N minutes),
 *  ----  3) opens door when car door motion is detected, ----  For people who do not have a mutli to use in the car, this part removed.
 *  ----  4) closes door when door was opened due to arrival and interior door is closed. ---- this part removed as well.
 */

preferences {

	section("Garage door") {
		input "doorSensor", "capability.contactSensor", title: "Which sensor?"
		input "doorSwitch", "capability.momentary", title: "Which switch?"
		input "openThreshold", "number", title: "Warn when open longer than (optional)",description: "Number of minutes", required: false
		input "phone", "phone", title: "Warn with text message (optional)", description: "Phone Number", required: false
	}
	section("Car(s) using this garage door") {
		input "cars", "capability.presenceSensor", title: "Presence sensor", description: "Which car(s)?", multiple: true, required: false
	}
	section("False alarm threshold (defaults to 10 min)") {
		input "falseAlarmThreshold", "number", title: "Number of minutes", required: false
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	log.debug "present: ${cars.collect{it.displayName + ': ' + it.currentPresence}}"
	subscribe(doorSensor, "contact", garageDoorContact)

	subscribe(cars, "presence", carPresence)

}

def doorOpenCheck()
{
	final thresholdMinutes = openThreshold
	if (thresholdMinutes) {
		def currentState = doorSensor.currentState("contact")
		log.debug "doorOpenCheck"
		if (currentState?.value == "open") {
			log.debug "open for ${now() - currentState.date.time}, openDoorNotificationSent: ${state.openDoorNotificationSent}"
			if (!state.openDoorNotificationSent && now() - currentState.date.time > thresholdMinutes * 60 *1000) {
				def msg = "${doorSwitch.displayName} was been open for ${thresholdMinutes} minutes"
				log.info msg
				sendPush msg
				if (phone) {
					sendSms phone, msg
				}
				state.openDoorNotificationSent = true
			}
		}
		else {
			state.openDoorNotificationSent = false
		}
	}
}

def carPresence(evt)
{
	log.info "$evt.name: $evt.value"
	// time in which there must be no "not present" events in order to open the door
	final openDoorAwayInterval = falseAlarmThreshold ? falseAlarmThreshold * 60 : 600

	if (evt.value == "present") {
		// A car comes home

		def car = getCar(evt)
		def t0 = new Date(now() - (openDoorAwayInterval * 1000))
		def states = car.statesSince("presence", t0)
		def recentNotPresentState = states.find{it.value == "not present"}

		if (recentNotPresentState) {
			log.debug "Not opening ${doorSwitch.displayName} since car was not present at ${recentNotPresentState.date}, less than ${openDoorAwayInterval} sec ago"
		}
		else {
			if (doorSensor.currentContact == "closed") {
				openDoor()
				sendPush "Opening garage door due to arrival of ${car.displayName}"
				state.appOpenedDoor = now()
			}
			else {
				log.debug "door already open"
			}
		}
	}
	else {
		// A car departs
		if (doorSensor.currentContact == "open") {
			closeDoor()
			log.debug "Closing ${doorSwitch.displayName} after departure"
			sendPush("Closing ${doorSwitch.displayName} after departure")
		}
		else {
			log.debug "Not closing ${doorSwitch.displayName} because its already closed"
		}
	}
}

def garageDoorContact(evt)
{
	log.info "garageDoorContact, $evt.name: $evt.value"
	if (evt.value == "open") {
		schedule("0 * * * * ?", "doorOpenCheck")
	}
	else {
		unschedule("doorOpenCheck")
	}
}

private openDoor()
{
	if (doorSensor.currentContact == "closed") {
		log.debug "opening door"
		doorSwitch.push()
	}
}

private closeDoor()
{
	if (doorSensor.currentContact == "open") {
		log.debug "closing door"
		doorSwitch.push()
	}
}

private getCar(evt)
{
	cars.find{it.id == evt.deviceId}
}
