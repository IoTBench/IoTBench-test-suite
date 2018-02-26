/**
 *  Turn It On When It Opens
 *
 *  Author: SmartThings
 */

// Automatically generated. Make future change here.
definition(
    name: "Turn It On When It Opens",
    namespace: "",
    author: "ryan",
    description: "When the door opens, turn on a light",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When the door opens..."){
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Turn on a light..."){
		input "switches", "capability.switch", multiple: true
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
	log.trace "Turning on switches: $switches"
	switches.on()
}

