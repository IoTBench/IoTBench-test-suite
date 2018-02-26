/**
 *  Illuminated Response to Unexpected Visitors
 *
 *	Responds to a sustained period of detected motion by successively turning on a series of lights. 
 *  Can be used, for example, to detect someone snooping around your front door at night. 
 *  The longer motion is detected, the more lights will be activated.
 *
 *
 *	1. If movement is detected, turn on switch1
 *	2. Check the motion detector's status after a delay to determine if there was sustained movement beyond the first detection period
 *	2a. If there was sustained movement, turn on switch2
 *	2b. If there wasn't, do nothing. Wait for a defined inactivity period and then turn switch1 off
 *  3. Check the motion detector's status after a second detection period to determine if movement has continued past the first period
 *	3a. If there was sustained movement, turn on switch3 and (optionally) switch4
 *	3b. If there wasn't, do nothing. Wait for a defined inactivity period and then turn switch1 and switch2 off. 
 *	4. The system is now at its highest escalation. Once an inactivity event is finally fired, wait a defined inactivity period from that 
 *	   point and then turn off all the set lights
 *	5. If further motion is detected, reset the inactivity timer to fire at the defined inactivity period from the time movement was last detected.
 *	6. At the end of the defined active period, run a check to turn off any activated lights
 *
 *
 *  Author: danrwill@gmail.com
 *  Date: 2013-12-04
 */
preferences {
	
	section("Detect initial motion") {
    	
        input "motion", 
			"capability.motionSensor", 
			title: "When motion has been detected by...", 
			description: "Select sensor"
            
        input "time0",
        	"time",
            title: "Starting from this time...",
            description: "Select time"
            
		input "time1",
        	"time",
            title: "Until this time...",
            description: "Select time"
            
        input "switch1",
        	"capability.switch",
            title: "Turn on this light",
            description: "Select light"
	}
    
    section("First Escalation") {
    	input "period1", 
        	"decimal", 
            title: "If motion is detected beyond this number of minutes...",
            description: "Enter minutes",
            defaultValue: 0.5
            
    	input "switch2", 
        	"capability.switch",
            title: "Turn on a light",
            description: "Select light"
    }
    
    section("Second Escalation") {
    	input "period2", 
        	"decimal", 
            title: "If motion continues beyond this number of minutes...",
            description: "Select minutes",
            defaultValue: 1
            
    	input "switch3", 
        	"capability.switch",
            title: "Turn on this light",
            description: "Select light"
            
        input "switch4", 
        	"capability.switch",
            title: "And (optionally) another light",
            required: false,
            description: "Select light"
    }
    
    section("Deactivation") {
    	input "inactiveTimeout", 
        	"decimal", 
            title: "Turn off the lights if no motion is detected for this number of minutes",
            description: "Enter minutes",
            defaultValue: 10
    }
     
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
    unschedule()
	initialize()
}


/**
 * Creates our defaults and sets up our listeners 
 *
 * Subscribe to motion events and schedule a call to run at the end 
 * of the defined active period (time0 - time1)
 */
def initialize() {
    
    subscribe(motion, "motion.active", motionActiveHandler)
    subscribe(motion, "motion.inactive", motionInactiveHandler)
    
    if(state.escalationLevel == null) {
    	state.escalationLevel = 0
    }
    
    // reset everything at the end of the active period
    schedule(time1, "inactiveTimeoutHandler")
    
}


/**
 * Called when motion is detected 
 * 
 * If this is the first motion event since the last reset: 
 * - increase the escalation level
 * - turn on the first light
 * - create a delayed call to check on the motion detector's status
 */
def motionActiveHandler(evt) {

    if (isActivePeriod()) {
        
        if(state.escalationLevel < 1) {
        	
            escalate()
            
            // We need to take the inactive delay of the motion detector into account. By default, the motion
            // detector will wait for 60 seconds of inactivity before sending an "inactive" event.
            runIn((period1 * 60) + 60, checkMotionStatus)
        }
    }
}


/**
 * Called after a defined period to evaluate the motion value and escalate, if necessary 
 * 
 * If switch1 and 2 have already been activated, sets up one last delayed call to checkMotionStatus
 */
def checkMotionStatus() {

    if(isActivePeriod() && state.escalationLevel > 0) {
		
        if(motion.currentValue("motion") == "active") {

        	escalate()
            
	       	if(state.escalationLevel == 2) {
        		runIn((period2 * 60) - (period1 * 60), checkMotionStatus)
        	}
        }
    }
}

/**
 * Creates a timer that will execute at the end of the inactiveTimeout
 *
 * Is called when an inactive motion event is sent. The timer is reset for each new inactive event that is triggered.
 */
def motionInactiveHandler(evt) {
	
	if(state.escalationLevel > 0) {
    
    	runIn(inactiveTimeout * 60, inactiveTimeoutHandler, [overwrite: true])
        
    }
}



/**
 * Called whenever motion is detected at one of the motion "checkpoints"
 *
 * Increments the escalation level and turns on the appropriate lights
 */
def escalate() {

	state.escalationLevel = state.escalationLevel + 1
    setLights(state.escalationLevel)
}


def setLights(level) {
	if(level == 1) {
    	switch1.on()
    } else if (level == 2) {
    	switch2.on()
    } else if (level > 2) {
    	switch3.on()
        switch4?.on()
    } else {
    	switch1.off()
        switch2.off()
        switch3.off()
        switch4?.off()
    }
}

/**
 * Called when our inactivity timer has run its course, without detecting any further motion
 *
 * Also called once per day, at the end of our defined activePeriod
 */
def inactiveTimeoutHandler() {
	if(state.escalationLevel > 0) {
    	reset()	
    }
}

/**
 * Makes sure we are in within the defined activePeriod
 */
def isActivePeriod() {
	def t0 = now()
	def startTime = timeToday(time0, location.timeZone)
	def endTime = timeTodayAfter(time0, time1, location.timeZone)
    // Make sure the startTime is always before the endTime
    if (startTime.time > endTime.time) {
    	startTime = new Date(startTime.time - 86400000)
    }
	if (t0 >= startTime.time && t0 < endTime.time) {
		true
	} else {
		log.debug "The current time of day (${new Date(t0)}), is not in the correct time window ($startTime) - ($endTime):  doing nothing"
		false
	}
}

def reset() {
	state.escalationLevel = 0
    setLights(state.escalationLevel)
}

