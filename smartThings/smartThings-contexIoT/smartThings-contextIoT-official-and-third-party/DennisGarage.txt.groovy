/**
 *  Turn It On When I'm Here
 *
 *  Author: SmartThings
 */
preferences {
	section("When I arrive ..."){
		input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Monem..."){
		input "switch1", "capability.switch", multiple: true
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
	def presenceValue = presence1.find{it.currentPresence == "present"}
	log.debug presenceValue
	if(presenceValue){
		switch1.on()
		log.debug
	}
	else{
		switch1.off()
		log.debug 
	}
}
