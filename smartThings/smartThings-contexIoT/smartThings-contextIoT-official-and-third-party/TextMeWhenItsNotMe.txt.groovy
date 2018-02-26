/**
 *  Text Me When It's Not Me
 *
 *  Author: apostlePD
 */
preferences {
	section("When there's movement:"){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Text me at:"){
		input "phone1", "phone", title: "Phone number?"
	}
    section("And text this number (optional):"){
		input "phone2", "phone", title: "Phone number?", required: false
	}
    section("But not if this door was opened recently:"){
		input "contact1", "capability.contactSensor", title: "Which Door?"
	}
    section("Threshold for alert is:") {
		input "minutes", "number", title: "Minutes?", required: true
    }
}

def installed() {
	createSubscriptions()
}

def updated() {
	unsubscribe()
	createSubscriptions()
}

def createSubscriptions()
{
	subscribe(motion1, "motion.active", motionActiveHandler)
	subscribe(contact1, "contact.open", contactOpenHandler)
}

def motionActiveHandler(evt) 
{   
	log.debug "marking motion active time"
	state.motionActiveAt = now()
    
    unschedule("DoorCheck")
    //If you do not include this unschedule(), the runIn() will not work.
    
    runIn(60, "DoorCheck")
    //the runIn() counts seconds, so you must multiply by 60 to get minutes.
    log.debug "Door Check will begin in 1 minute."
}

def contactOpenHandler(evt)
{
	log.debug "marking open door time"
	state.doorOpenedAt = now()
}

def DoorCheck(evt)
{ 	
	log.debug "motionActiveAt = ${state.motionActiveAt}"
	log.debug "doorOpenedAt = ${state.doorOpenedAt}"

	def threshold = 1000 * 60 * (minutes)
    def elapsedTime = state.motionActiveAt - state.doorOpenedAt
    log.debug "Elapsed Time = $elapsedTime"
    
    if (elapsedTime <= 0) {
    elapsedTime = state.doorOpenedAt - state.motionActiveAt
    }
   	
	log.debug "Elapsed Time = $elapsedTime"
    log.debug "Threshold = $threshold"

    if (elapsedTime > threshold){
    	def message = "There's motion in the $motion1."
        //If you would rather have a push message, just remove the forward slashes by sendPush and add them to the sendSms command to disable it
		sendSms(phone1, "$message")
        sendSms(phone2, "$message")
		//sendPush(message)
		log.debug "$motion1 detected motion, texting $phone1"
        }
        else {
            //if you want to know when the motion is normal or to know the app is working, remove the forward slashes before the sendPush command
			log.debug "someone entered the house"
            //sendPush("$motion1 motion is normal.")
         			}           
}