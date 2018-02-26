/**
 *  Notify Me as I Leave That I Left It Open
 *
 *
 *  TODO: Doesn't quite handle if you've returned within the delay as far as I can tell.
 *
 *  Author: 	Chris LeBlanc (LeBlaaanc)
 *  Company: 	Clever Lever
 *  Email: 		chris@cleverlever.co
 *  Date: 		05/22/2014
 */
definition(
    name: "Notify Me as I Leave That I Left It Open",
    namespace: "cl",
    author: "Chris LeBlanc",
    description: "Notifies you as you leave that you left a door or window open.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/ModeMagic/bon-voyage%402x.png"
)

preferences 
{
	section("As this device leaves") {
		input "departer", "capability.presenceSensor", title: "Presence sensor"
        input "openThreshold", "number", title: "Delay before considered gone (default: immediately)", required: false
	}
	section("Check that this is closed") {
		input "contact", "capability.contactSensor", title: "Contact sensor"
	}
    section("Message to send") {
		input "message", "text", title: "Message (optional)", required: false
	}
}

def installed() 
{
	log.trace "installed()"
	initialize()
}

def updated() 
{
	log.trace "updated()"
	unsubscribe()
	initialize()
}

def initialize() 
{
    subscribe(departer, "presence", departerLeft) 
}

def departerLeft(evt)
{
	if (evt.value == "not present") {
		log.trace "departerLeft($evt.name: $evt.value) $departer.displayName"
		def t0 = now()
		def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 0
        def departer = evt
		runIn(delay, doorOpenTooLong, [overwrite: false])
    }
}


def doorOpenTooLong() 
{
	def contactState = contact.currentState("contact")
	if (contactState.value == "open") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 0) - 1000
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
	def msg = (message != null && message != "") ? message : "${departer.displayName} has left ${contact.displayName} open."
	log.info msg
	sendPush msg
}
