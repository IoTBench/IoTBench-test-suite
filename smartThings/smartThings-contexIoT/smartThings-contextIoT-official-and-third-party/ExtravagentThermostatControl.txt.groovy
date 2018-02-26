/**
 *  Extravagent Thermostat setting
 *     Allows you to set your thermostat to different temp during
 *     different days of the week
 *
 *  Author: Samer Theodossy
 */
preferences {
	section("Set these thermostats") {
		input "thermostat", "capability.thermostat", title: "Which?", multiple:true

	}
	
    section("To these temperatures") {
		input "heatingSetpoint", "number", title: "When Heating"
		input "coolingSetpoint", "number", title: "When Cooling"
	}
    
    section("Configuration") {
    	input "dayOfWeek", "enum",
                        title: "Which day of the week?",
                        multiple: false,
                        metadata: [
                    values: [
                    'All Week',
                    'Monday to Friday',
                    'Saturday & Sunday',
                    'Monday',
                    'Tuesday',
                    'Wednesday',
                    'Thursday',
                    'Friday',
                    'Saturday',
                    'Sunday'
                ]
                        ]
    	input "time", "time", title: "At this time"
        //input "newMode", "mode", title: "Change to this mode"
    }
	
    section( "Notifications" ) {
    	input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes", "No"]], required: false
        input "phoneNumber", "phone", title: "Send a text message?", required: false
    }
}

def installed() {
        // subscribe to these events
        subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
		subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
		subscribe(thermostat, "temperature", temperatureHandler)
        initialize()
}

def updated() {
        // we have had an update
        // remove everything and reinstall
        unschedule()
        subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
		subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
		subscribe(thermostat, "temperature", temperatureHandler)
        initialize()
}

def initialize() {
    
        log.debug "Scheduling Temp change for day " + dayOfWeek + " at time " + time
    
        schedule(time, setTheTemp)
}

def heatingSetpointHandler(evt)
{
	log.debug "heatingSetpoint: $evt, $settings"
}

def coolingSetpointHandler(evt)
{
	log.debug "coolingSetpoint: $evt, $settings"
}

def temperatureHandler(evt)
{
	log.debug "currentTemperature: $evt, $settings"
}

def setTheTemp() {
    
    def doChange = false
    Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
    int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
    
    // Check the condition under which we want this to run now
    // This set allows the most flexibility.
    if(dayOfWeek == 'All Week'){
            doChange = true
    }
    else if((dayOfWeek == 'Monday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.MONDAY){
            doChange = true
    }
    
    else if((dayOfWeek == 'Tuesday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.TUESDAY){
            doChange = true
    }
    
    else if((dayOfWeek == 'Wednesday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.WEDNESDAY){
            doChange = true
    }
    
    else if((dayOfWeek == 'Thursday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.THURSDAY){
            doChange = true
    }
    
    else if((dayOfWeek == 'Friday' || dayOfWeek == 'Monday to Friday') && currentDayOfWeek == Calendar.instance.FRIDAY){
            doChange = true
    }
    
    else if((dayOfWeek == 'Saturday' || dayOfWeek == 'Saturday & Sunday') && currentDayOfWeek == Calendar.instance.SATURDAY){
            doChange = true
    }
    
    else if((dayOfWeek == 'Sunday' || dayOfWeek == 'Saturday & Sunday') && currentDayOfWeek == Calendar.instance.SUNDAY){
            doChange = true
    }
    
    
    
    // some debugging in order to make sure things are working correclty
    log.debug "Calendar DOW: " + currentDayOfWeek
    log.debug "SET DOW: " + dayOfWeek
    
    // If we have hit the condition to schedule this then lets do it
    if(doChange == true){
        log.debug "setTheTemp, location.mode = $location.mode, newMode = $newMode, location.modes = $location.modes"
        
        // We only want to allow this set of commands to run when we are in any mode other than "Away" 
        // If we are Away then we do not want to change it, this will take effect from the good night setting
        // or from another instance of this which will setup at 12:00 am to take effect everyday.
        if (location.mode != 'Away') {
            log.debug " Entering the set part"
        	thermostat.setHeatingSetpoint(heatingSetpoint)
			thermostat.setCoolingSetpoint(coolingSetpoint)
            thermostat.poll()
            send "${label} has changed the heat to '${heatingSetpoint}' and cooling to '${coolingSetpoint}'"
        }
    }
    else {
            log.debug "Temp change not scheduled for today."
    }
    log.debug "End of Fcn"
}

private send(msg) {
        if ( sendPushMessage != "No" ) {
                log.debug( "sending push message" )
                sendPush( msg )
        }

        if ( phoneNumber ) {
                log.debug( "sending text message" )
                sendSms( phoneNumber, msg )
        }

        log.debug msg
}

private getLabel() {
        app.label ?: "SamerTheodossy"
}


// catchall
def event(evt)
{
	log.debug "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}

/*
def installed()
{
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler) 	// done
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler) 	// done
	subscribe(thermostat, "temperature", temperatureHandler)	  	 	// done
	subscribe(location)	
	subscribe(app)
}

def updated()
{
	unsubscribe() 														// done
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler) 	// done
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler) 	// done
	subscribe(thermostat, "temperature", temperatureHandler) 		 	// done
	subscribe(location)
	subscribe(app)
}

// SAMER
// see if we can leverage this to make this much smarter
// this seems to be a location change event happening and can take some value already set ???
def changedLocationMode(evt)
{
	log.debug "changedLocationMode: $evt, $settings"

	thermostat.setHeatingSetpoint(heatingSetpoint)
	thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

// SAMER
// Not sure what this is about appTouch. Is this when I change the mode myself ???
def appTouch(evt)
{
	log.debug "appTouch: $evt, $settings"

	thermostat.setHeatingSetpoint(heatingSetpoint)
	thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

*/