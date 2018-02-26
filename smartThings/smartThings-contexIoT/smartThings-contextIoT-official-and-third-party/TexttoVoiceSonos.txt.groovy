/**
 *  Text to Voice(Sonos)
 *
 *  Author: tbaird81@gmail.com
 *  Date: 2014-03-12
 */

preferences {
	section("This door is closed...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
    section {
		input "sonos", "capability.musicPlayer", title: "Sonos Device", required: true
	}
        section {
		input "textHere", "text", title: "Type in the message"
	}
}

def installed()
{
    subscribe(contact1, "contact.closed", contactClosedHandler)
}

def updated()
{
	unsubscribe()
    subscribe(contact1, "contact.closed", contactClosedHandler)
}

def contactClosedHandler(evt) {
	sonos.playText(textHere)
}
