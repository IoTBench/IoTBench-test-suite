/**
 *  Left It Open Still
 *
 *  Author: SmartThings, modified by Trevor Prentice (http://www.whiverwill.com)
 *  Creation date: 2013-05-09
 *  Modified on: 2014-09-23
 */

definition(
    name: "Left It Open Still",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Repetedly notifies you when you have left a door or window open longer that a specified amount of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences {

	section("Monitor this door or window") {
		input "contact", "capability.contactSensor"
	}
	section("And notify me repetedly if it stays open for more than this many minutes (default 10)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
	section("Via text message at this number (or via push notification if not specified") {
		input "phone", "phone", title: "Phone number (optional)", required: false
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
	subscribe(contact, "contact.open", doorOpen)
	subscribe(contact, "contact.closed", doorClosed)
}

def doorOpen(evt)
{
	log.trace "doorOpen($evt.name: $evt.value)"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 600
	runIn(delay, doorOpenTooLong, [overwrite: false])  //http://community.smartthings.com/t/does-overwrite-false-actually-do-anything-to-runin/1964/3
	log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def doorOpen2()
{
	log.trace "doorOpen2"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 600
	runIn(delay, doorOpenTooLong, [overwrite: false])
	log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def doorClosed(evt)
{
	log.trace "doorClosed($evt.name: $evt.value)"
}

def doorOpenTooLong() {
	def contactState = contact.currentState("contact")
	if (contactState.value == "open") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
		if (elapsed >= threshold) {
			log.debug "Contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
		} else {
			log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
	} else {
		log.warn "doorOpenTooLong() called but contact is closed:  doing nothing"
	}
}

void sendMessage()
{
	def contactState = contact.currentState("contact")
	def elapsedMinutes = ( now() - contactState.rawDateCreated.time ) / 60000
	def msg = "${contact.displayName} has been left open for ${elapsedMinutes} minutes."
	log.info msg
	if (phone) {
		sendSms phone, msg
	}
	else {
		sendPush msg
	}
    doorOpen2()
    
}
