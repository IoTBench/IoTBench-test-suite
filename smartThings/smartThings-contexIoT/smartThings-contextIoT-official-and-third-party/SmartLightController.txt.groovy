/**
 *  Smart Light Controller
 *
 *  Author: Chad Monroe (chad@monroe.io)
 */
definition(
    name: "Smart Light Controller",
    namespace: "cmonroe",
    author: "Chad Monroe",
    description: "Allows you to enable lights only when lux reading is below a certain value and motion is detected, then turn them off after a specified period of time.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences 
{
	section( "Turn on when there's movement..." )
	{
		input( "motionSensor", "capability.motionSensor", title: "Where?" );
	}
    
	section( "And it's dark..." ) 
	{
		input( "illuminanceSensor", "capability.illuminanceMeasurement", title: "Where?" );
	}
    
	section( "How dark (recommended value: 30)..." ) 
	{
		input( "numLuxNight", "number", title: "Night Time Illuminance?" );
	}
    
	section( "Then turn off when there's been no movement for..." )
	{
		input( "numMinutes", "number", title: "Minutes?" );
	}

    section( "Or it has become light (recommended value: 100+)..." ) 
    {
    	input( "numLuxDay", "number", title: "Day Time Illuminance?" );
    }
    
	section( "Turn on/off light(s)..." )
	{
		input( "switches", "capability.switch", multiple: true );
	}
}

/**
 * Typedefs and Enums
 */
 


/**
 * Methods defined by SmartApp
 */
def installed()
{
	subscribe( motionSensor, "motion", motionHandler )
	subscribe( illuminanceSensor, "illuminance", illuminanceHandler )
	schedule( "0 * * * * ?", "scheduleCheck" )
}

def updated()
{
	unsubscribe()
	subscribe( motionSensor, "motion", motionHandler )
	subscribe( illuminanceSensor, "illuminance", illuminanceHandler )
	unschedule()
	schedule( "0 * * * * ?", "scheduleCheck" )
}

/**
 * SmartApp Implementation
 */
def isDark()
{
	def result = false;
    
    if ( illuminanceSensor.latestValue("illuminance") < numLuxNight )
	{
    	result = true;
    }
    
    return( result );
}

def isLight()
{
	def result = false;
    
    if ( illuminanceSensor.latestValue("illuminance") > numLuxDay )
    {
    	result = true;
	}

	return( result );
}

def switchesOff(reason)
{
	log.debug( reason );
    
	switches.off();
	state.inactiveAt = null;
}

def switchesOn(reason)
{
	log.debug( reason );
    
	switches.on();
	state.inactiveAt = null;
}

def switchesAlreadyOn()
{
	def result = false;
    
	for ( s in switches ) 
	{
		if ( s.currentSwitch == "on" ) 
		{
			result = true;
			break;
		}
	}
    
	return( result );
}

/**
 * action is one of: on, inactivity, lux
 */
def checkSwitches(action)
{
	def dark = isDark();
	def light = isLight();
	def alreadyOn = switchesAlreadyOn();
    
	log.debug( "checkSwitches: dark=${dark} light=${light} alreadyOn=${alreadyOn} action=${action}" );
    
	if ( action == "on" )
	{
		if ( dark && !light && !alreadyOn )
		{
			switchesOn( "dark, motion detected and lights off; turning lights on" );
		}
		else
		{
			state.inactiveAt = null;
		}
	}    
	else if ( action == "inactivity" )
	{
		if ( alreadyOn )
		{
			switchesOff( "inactivity timer expired; turning off lights" );
		}
	}
	else if ( action == "lux" )
	{
		if ( light && alreadyOn )
		{
			switchesOff( "light threshold passed and lights are on; turning off" );
			alreadyOn = false;
		}
	}

    return( alreadyOn );
}

def motionHandler(evt) 
{    
	log.debug( "${evt.name}: ${evt.value}" );
    
    if ( evt.value == "active" )
    {
		checkSwitches( "on" );
    }
    else if ( evt.value == "inactive" ) 
    {
		if ( !state.inactiveAt )
		{
			state.inactiveAt = now();
		}
    }
}

def illuminanceHandler(evt) 
{
    def numLux = evt.integerValue;
    
	log.debug( "current illuminance value=${numLux}" );
    
    checkSwitches( "lux" );
}

def scheduleCheck() 
{
	if ( state.inactiveAt == null )
	{
		log.debug( "schedule check, timer not active" );
	}
	else
	{
		log.debug( "schedule check, timer inactiveAt=${state.inactiveAt}" );
	}
    
	if ( state.inactiveAt ) 
	{
		def elapsed = now() - state.inactiveAt;
		def threshold = 1000 * 60 * numMinutes;
    	
		if ( elapsed >= threshold ) 
		{
			checkSwitches( "inactivity" );
		}
		else 
		{
			log.debug( "${elapsed / 1000} sec since motion stopped" );
		}
	}
}
