/**
 *  Smart Welcome Light
 *
 *  Author: Gustav Nadeau
 */
definition(
    name: "Smart Welcome Light",
    namespace: "gustavnadeau",
    author: "Gustav Nadeau",
    description: "Turns lights on when you arrive but selects lights depending wether someone is already present or not.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_presence-outlet@2x.png"
)

preferences {
	section("When I arrive and leave..."){
		input "people", "capability.presenceSensor", title: "Who?", multiple: true
	}
	section("Turn on/off lights when no one is home..."){
		input "switchAlone", "capability.switch", multiple: true
	}
    section("Turn on/off lights when someone is home..."){
        input "switchNotAlone", "capability.switch", multiple: true
    }
    section("Only turn on the lights after sunset..."){
    	input "OnlyAtNight", "bool", title:"Yes ?", multiple: false
    }
}

def installed()
{
	subscribe(people, "presence", presenceHandler)
}

def updated()
{
	unsubscribe()
	subscribe(people, "presence", presenceHandler)
}

def presenceHandler(evt)
{
	log.debug "presenceHandler $evt.name: $evt.value"
	def current = people.currentValue("presence")
	log.debug current
	def presenceValue = people.find{it.currentPresence == "present"}
	log.debug presenceValue
	if(presenceValue && "$evt.value" == "present"){
		if(someoneHome()){
            if(AfterSunset()){
            	switchNotAlone.on()
            }
            log.debug "Someone else has arrived!"
        }
        else{
            if(AfterSunset()){
            	switchAlone.on()
            }
		    log.debug "Someone's home!"
        }
        
	}
	else if(!presenceValue) {
		switchAlone.off()
		log.debug "Everyone's away."
	}
}

private AfterSunset(){
	
    if(!OnlyAtNight){
    	return true
    }
	else if(getSunriseAndSunset().sunrise.time > now() || getSunriseAndSunset().sunset.time < now()){
    	return true
    }
    else {
    	return false
    }
}

private someoneHome(){

    def whoIsHome = people.findAll{it.currentPresence == "present"}
    if(whoIsHome.size() > 1){
    	return true
    }
    
    return false
}