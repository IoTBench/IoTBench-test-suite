/**
 *  Toggle Nest Home/Away state based on my presence
 *
 *  Author: Gus Perez
 */
definition(
    name: "NestAutoPresence",
    namespace: "gusper",
    author: "Gus Perez",
    description: "Set Nest's presence state to Home when you arrive and to Away when you leave.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/presence.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/presence@2x.png"
)

preferences {
	section("When I arrive and leave...") {
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Set Nest to home/away...") {
		input "nest1", "capability.presenceSensor", multiple: false
	}
}

def installed()
{
	subscribe(presence1, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
}

def presenceHandler(evt)
{
	log.debug "presenceHandler $evt.name: $evt.value"
	def current = presence1.currentValue("presence")
	log.debug current
	def presenceValue = presence1.find { it.currentPresence == "present" }
	log.debug presenceValue
	
    if (presenceValue) {
		//nest1.present()
        runIn(300, setNestToPresent)
		log.debug "Someone's home!"
	}
	else {
		nest1.away()
		log.debug "Everyone's away."
	}
}

def setNestToPresent()
{
	nest1.present()
}