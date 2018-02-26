/**
 *  App Name:   Switch With Me
 *
 *  Author: 	Todd Wackford
 *				twack@wackware.net
 *  Date: 		2013-06-29
 *  Version: 	0.1
 *  
 *  This app lets the user select from a list of switches to act as triggering
 *  masters. Meaning that if anyone of them is tapped it will make a single 
 *  or group of slave devices switch on or off. The user can specify if this
 *  behavior is only to happen between certain times of the day. He/She also
 *  can specify the number of minutes they want the slave to stay on for, or if
 *  already on, how long until it is turned off.
 *
 *  Use Cases:
 *		You have a switch that does nothing much but makes a good master for 
 * 		other things to happen.
 *
 *		You get home and you want to turn on the house lights for a few minutes 
 *		and then settle in with only a few lights.
 *
 *		Turn on the bedroom lights as you get ready for bed and the have all the
 *		lights turn off after 15 minutes.
 *
 *		Turn on certain things as you're leaving and then turn off certain 
 *		things after a certin amount of time.
 *
 *		If between 9:00PM and 11:30PM you turn off the media room lights you
 *		want to have the bedroom lights turn on for 15 minutes.
 *  
 */

preferences {
	section("When These...") {
		input "masters", "capability.switch", 
			multiple: true, 
			title: "Master Switches...", 
			required: true
        input "mastersOnOff", "enum", 
			title: "Are Switched On or Off?...", 
			required: true, 
			metadata:[values:['on','off']]
	}
    section("And it is Between...") {
    	input "tweenStart", "time", title: "This time...", required: false
        input "tweenEnd", "time", title: "and This Time...", required: false
    }
	section("Then these...") {
		input "slaves", "capability.switch", 
			multiple: true, 
			title: "Slave Switches...", 
			required: true
        input "slavesOnOff", "enum", 
			title: "Are Switched On or Off?...", 
			required: true, 
			metadata:[values:['on','off']]
        input "slaveTime", "decimal", 
			title: "For or After How Many Minutes?...", 
			required: false 
	}
}

def installed()
{
	subscribe(masters, "switch.on", switchHandler)
    subscribe(masters, "switch.off", switchHandler)
}

def updated()
{
	unsubscribe()
	subscribe(masters, "switch.on", switchHandler)
    subscribe(masters, "switch.off", switchHandler)
}


def switchHandler(evt) {
	log.info "switchHandler Event: ${evt.value}"
    
    if((tweenStart != null) && (tweenEnd != null)){
    	def now = now()
    	def startTime = timeToday(tweenStart)   
    	def endTime = timeToday(tweenEnd)
    
    	if((now >= startTime.time) && (now <= endTime.time)){
            log.info "we're in the time slot, continue with the handler"
        } else{
        	log.info "we're not in the time slot so bail"
        	return
        }
    }
       
    //set up delay time for our slave following action
	
	// must have non zero value, or else delaying the On Off command does 
	// nothing 
    def delayTime = 1 //(milliseconds)
    if( slaveTime != null){
    	delayTime = slaveTime.toDouble() * 60000
    }

    //a master has been switched on or off
    if((evt.value == mastersOnOff) && (slavesOnOff == 'on')) {
    	slaves?.on()
        if( slaveTime != null)
        	slaves?.off(delay: delayTime)
    }
    if((evt.value == mastersOnOff) && (slavesOnOff == 'off')) {
        if( slaveTime != null)
        	slaves?.off(delay: delayTime)
        else
        	slaves?.off()
    }
}