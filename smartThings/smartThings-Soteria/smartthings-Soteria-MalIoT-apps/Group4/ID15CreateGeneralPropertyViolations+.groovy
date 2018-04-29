/**
 *  The app violates general properties when installed with other apps
 *  Included in repo by Z. Berkay Celik
 *  Email: zbc102@cse.psu.
 *  Notes: Though the functionality of the app is benign, the apps might be chained to validate a property.
 */
definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "Soteria",
    description: "when I arrive, turn on ...",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
	section("When I arrive and leave..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Turn on/off a light..."){
		input "switch1", "capability.switch", multiple: true
	}
}

def installed(){
	subscribe(presence1, "presence", presenceHandler)
}

def updated(){
	unsubscribe()
	subscribe(presence1, "presence", presenceHandler)
}

def presenceHandler(evt){
	log.debug "presenceHandler $evt.name: $evt.value"
	def current = presence1.currentValue("presence")
	log.debug current
	
	def presenceValue = presence1.find{it.currentPresence == "present"}
	def notpresenceValue = presence1.find{it.currentPresence == "not present"}

	log.debug presenceValue
	if(presenceValue){
		switch1?.on()
		log.debug "Someone's home!"
	}
	if(notpresenceValue){
		switch1?.off()
		log.debug "Everyone's away."
	}
}