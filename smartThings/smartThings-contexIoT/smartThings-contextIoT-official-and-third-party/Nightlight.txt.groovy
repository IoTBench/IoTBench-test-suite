/**
 *  Nightlight
 *
 *  Author: chrisb
 *  Date: 2013-07-30
 * 
 *  Thanks to C.Chen for some pointers on running the recursive procedure
 *
 *	This app is designed to use another switch as a trigger to run a dimmer - Night Light procedure.
 *  The app will set a dimmer light to a specified level, and then slowly dim it until off.
 *	Two hours after off, the app turns the light back on to full, then quickly off.  This is done to
 *  make sure the switch "works" in the morning.
*/


// This part is to ask the user for information on what we need to run.
preferences {
	section("Which switch activates the App?") {
    	input "trigger", "capability.switch"					//We're looking for a switch here to start the app.
    }
    section("Which light is the night light?") {
		input "switches", "capability.switchLevel"				//Need a dimmer switch of course.
	}
    section("What percentage should we start at?"){
    	input "number", "number", title: "Percentage, 0-50"     //By using title I'm suggesting to the user what range to set, but the
        														//program itself doesn't limit it.
	}
    section("How often should we decrease levels?") {
    	input "time", "number", title: "Minutes?"				//We're asking for minutes, but later when we schedule we'll convert 
        														//to seconds for the runIn procedure.
    }
}

// What do we do when the program is installed?
def installed()
{
	subscribe(trigger, "switch.on", triggerOn)  // Subscribing to the trigger switch, watching for when it turns on.
}


// What do we do when the program is updated?
def updated()
{
	unsubscribe()								// Unsubscribe from everything first to clear out anything from previous install.
	subscribe(trigger, "switch.on", triggerOn)	// Re-subscribe to the trigger switch, watching for it to turn on.
}


// This is the procedure that is run when the Trigger switch is turned on.  Notice that this is mostly just un-subscribing from 
// various schedule things and then calling other procedures.
def triggerOn(evt) {							// Trigger was turned on so...
	log.debug "Running dimEvent"
	unschedule( dimEvent )						// If someone turns on the trigger again in the middle of a dimEvent, we want to make sure we don't
    											// have the event running twice, so we unschedule any dimEvent first.
    unschedule( reset )                         // Same thing - we need to make sure that reset is scheduled.                   
    initialize()								// Run initilalize to setup numbers.  We run this here so number are reset everything the trigger turns on.
	dimEvent()									// Run the dimmer event.
}


// This procedure is run to setup numbers for the program.
def initialize() {								// This procedure set up numbers for the program
    state.currentLevel = number					// We don't want to change 'number' later, so we'll use state.currentLevel instead
    state.delay = time * 60						// runIn is done by seconds, so we multiple by 60 to get minutes.
    log.debug "Setting numbers"
}

// Thie procedure is the major work part of the program.  This does the dimming, adjusting of numbers and then calling itself to run
// again for later.
def dimEvent(evt) {								// This is the procedure that dims the light.
	log.debug "Unscheduling dimEvent"
	unschedule( dimEvent )				 		// I think a handler can only show up in the pending job list once. 
                          						// So it has to be unscheduled for the runIn below to work. 
	log.debug "Turning off trigger"
	trigger.off()								// Make sure the trigger is off.
	log.debug "Setting dimmer light to $state.currentLevel"
	switches.setLevel( state.currentLevel )		// Turns the light to the dim level.
    state.currentLevel = state.currentLevel - 5 // lower the level for next time
    if ( state.currentLevel > 4 )				// If the dim level is over 4%, then we're going to schedule the dim event to run again
    {
    	runIn( state.delay, dimEvent )			// Schedule the dim event to run again in the number of minutes the user entered.
    }
    else										// If the dim level isn't over 4%, then we're going to...
	{
		switches.off()							// Turn off the night light.
        log.debug "Turning off switch"
        runIn( 7200, reset )					// In two hours (2 * 60 minutes * 60 seconds) we'll run the reset procedure.
    }
}


// This procedure for resetting the dimmer switch.
def reset() {									// This is procedure to reset the night light for "normal" operation.  This will run two hours after
												// the light is turned off in the dimEvent. Because the program lowers the dim level when you try to
                                                // turn on the light in the morning, the light will be as extreme dim.  So, we'll reset it:
	log.debug "Running reset"
    switches.setLevel( 99 )						// We're turning on the light full blast.
    switches.off()								// Don't want to wake anyone up, so as soon as we turn on the lights full, we'll turn them off.
}

