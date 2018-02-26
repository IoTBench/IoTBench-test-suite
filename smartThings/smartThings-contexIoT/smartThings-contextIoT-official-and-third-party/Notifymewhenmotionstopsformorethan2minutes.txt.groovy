/**
 *  Text Me When There's Motion
 *
 *  Author: SmartThings
 */
definition(
    name: "Notify me when motion stops for more than 2 minutes",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Get a text message sent to your phone when motion stops for more than 2 minute.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/intruder_motion@2x.png"
)

preferences {
	section("When motion stops..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Text me at..."){
		input "phone1", "phone", title: "Phone number?"
	}
    section("If lights are on..."){
		input "light1", "capability.illuminanceMeasurement", title: "Where?"
	}
}

def installed()
{
	state.count = 0
	subscribe(motion1, "motion.inactive", motionInactiveHandler)
    subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.inactive", motionInactiveHandler)
    subscribe(motion1, "motion.active", motionActiveHandler)
    state.count = 0
}

def motionInactiveHandler(evt) {
	if(state.count!=0)
    {
    	state.count=state.count-1
    }
	log.trace"$state.count, in Handler"
	

	
	runIn(10,"takeAction", [overwrite: false])

}

def motionActiveHandler(evt) {
	
	state.count=state.count+1
	log.trace"$state.count, motion occured"
}

def takeAction() {
	log.trace"$state.count, in takeAction"
    if(state.count==0)
    {
    	log.trace"$state.count"
    	log.debug "There has been no motion in 10 seconds"
        if(light1.latestValue("illuminance") > 5){
        	log.debug "Lights were left on"
            log.debug "Lights were left on in $location, texting $phone1"
            	sendSms(phone1, "Lights were left on in $light1")
    			sendPush("Lights were left on in $location")
            }
    }
    
    else{
    	log.debug "MOTION"
	}
}