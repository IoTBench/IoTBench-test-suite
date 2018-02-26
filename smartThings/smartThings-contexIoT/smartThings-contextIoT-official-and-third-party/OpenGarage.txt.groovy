/**
 *  Open Garage
 *
 *  Author: Tariq Ali (tasi@mail.com)
 *  Date: 2013-09-07
 */

// 
definition(
    name: "Open Garage",
    namespace: "",
    author: "tasi@mail.com",
    description: "Open garage when presence detected only if Garage door is closed",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
	section("When I arrive..."){
		input "presence1", "capability.presenceSensor", multiple: true, title: "Who Arrives?"
	}
    section("If Garage door closed..."){
		input "GarageDoorContact", "capability.contactSensor", title: "Is door Closed or Open?"
	}
	section("Open Garage Door..."){
		input "GarageDoorOpener", "capability.switch", title: "Open which door?"
	}

}

def installed()
{
	subscribe(presence1, "presence", presenceHandler)
	subscribe(GarageDoorContact, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
	subscribe(GarageDoorContact, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt)
{
	def GarageDoorPosition = evt.value
}

def presenceHandler(evt)
{
	def currentPresence = evt.value
	if (currentPresence == "present") {
    	if (GarageDoorPosition == "closed") {
			GarageDoorOpener.on()
        	GarageDoorOpener.off(delay: 3000)
        }
	}
}