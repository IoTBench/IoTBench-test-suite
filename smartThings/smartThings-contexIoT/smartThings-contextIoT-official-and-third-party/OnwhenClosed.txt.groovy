/**
 *  Delayed Command Execution
 *
 *  Author: SmartThings
 */

// Automatically generated. Make future change here.
definition(
    name: "On when Closed",
    namespace: "",
    author: "Adrian Alonso",
    description: "Turns light on when door is closed",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When the bathroom door opens/closes...") {
		input "contact1", "capability.contactSensor", title: "Where?"
	}
	section("Toggle the extractor...") {
		input "switch1", "capability.switch"
	}
}

def installed()
{
	subscribe(contact1, "contact", contactHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contact1, "contact", contactHandler)
}

def contactHandler(evt) {
	log.debug "$evt.value: $evt"
	if (evt.value == "closed") {
		switch1.on()
	} else if (evt.value == "open") {
		switch1.off(delay: 0)
	}
}