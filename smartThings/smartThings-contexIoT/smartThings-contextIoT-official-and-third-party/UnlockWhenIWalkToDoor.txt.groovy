/**
 *  Unlock When I Walk To Door
 *
 *  Copyright 2015 Matt Cowger
 *
 */
definition(
    name: "Unlock When I Walk To Door",
    namespace: "mcowger",
    author: "Matt Cowger",
    description: "Unlocks 1 or more locks when presence is detected & motion sensor is activated.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("When I arrive..."){
    input "presence1", "capability.presenceSensor", title: "Who...", multiple: true
  	input "motion1", "capability.motionSensor", title: "and this sensor sees movement...", required: true, multiple: false
  }
  
  section("Unlock the lock..."){
    input "lock1", "capability.lock", multiple: true
  }
   section("And changes the house mode to..."){
	input "targetmode", "mode", title: "to mode..."
  }
}

def installed()
{
  subscribe(motion1, "motion", motionevent)
}

def updated()
{
  unsubscribe()
  subscribe(motion1, "motion", motionevent)
}

def motionevent(evt)
{
	log.debug "Unlock-on-move event: $evt.value | presence: $presence1.currentPresence[0]"

    if (evt.value == "active" && presence1.currentPresence[0] == "present") {
    	log.debug "Movement was active "
        sendPush "Unlocking door because motion sensor and presence detected"
        lock1.unlock()
        log.debug "Settings target mdoe to $targetmode"
        setLocationMode(targetmode)
    }

}
