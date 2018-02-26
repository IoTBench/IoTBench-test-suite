/**
 *  Flood Watch (modification of Flood Alert)
 *
 *  Author: SmartThings; updated by Chad Monroe (chad@monroe.io)
 *
 *  cmonroe 07/16/2013: Allow for multiple water sensors to be selected
 *  cmonroe 07/16/2013: Set SMS delta to 2 m; push notification to 10 s
 *  cmonroe 07/16/2013: Send messages for both wet and dry events
 */
preferences 
{
	section("When there's water detected...")
	{
		input "waterSensors", "capability.waterSensor", required: true, multiple: true
	}

	section("Also send a text to... (optional)")
	{
		input "phone", "phone", title: "Phone Number?", required: false
	}
}

def alreadyNotified(device, state, evtDate, deltaSeconds)
{
	def notified = 0
	def expireTime = new Date( now() - (1000 * deltaSeconds) )
	def recentEvents = device.eventsSince( expireTime )
    
	log.debug "found ${recentEvents?.size() ?: 0} event(s) in the last ${deltaSeconds} seconds"
    
	recentEvents.count()
    {
		if ( it.value && it.value == state )
		{
			if ( it.isoDate == evtDate )
			{
				log.debug "it.isoDate ${it.isoDate} and evtDate match, ignoring"
            }
            else
            {
                log.debug "it.value ${it.value} state ${state}"
                notified = 1
			}
		}
	}
    
    return notified
}

def waterEvent(evt)
{
	def deltaSeconds = 120
    def evtState = evt.value
    def devName = "UNKNOWN"
	def device = null
	
    log.debug "${evt.name}: ${evt.value}"
	
	waterSensors.each
	{
		if ( it.id == evt.deviceId )
		{
			device = it
		}
	}
    
	if ( device == null )
    {
		log.debug "waterEventHandler: BUG - no device associaed with event!!"
        return
	}
	else
	{
		devName = device.displayName
	}
    
    def msg = "${devName} is ${evtState}!"
   
	if ( phone )
    {
        if ( alreadyNotified( device, evtState, evt.isoDate, deltaSeconds ) > 0 ) 
        {
            log.debug "SMS message for sensor=${devName} state=${evtState} already sent within the last ${deltaSeconds} seconds"
        } 
        else 
        {
            log.debug "${devName} is ${evtState}, sending SMS to ${phone}"
            
			sendSms( phone, msg )
        }
	}
    
    // shorten for push notification
    deltaSeconds = 10
        
    if ( alreadyNotified( device, evtState, evt.isoDate, (deltaSeconds) ) > 0 ) 
    {
    	log.debug "push notification for sensor=${devName} state=${evtState} already sent within the last ${deltaSeconds} seconds"
    } 
    else
    {
        log.debug "${devName} is ${evtState}, sending push notification"
        
        sendPush( msg )
    }
}

def installed() 
{ 
	waterSensors.each
	{
		subscribe( it, "water.dry", waterEvent )
		subscribe( it, "water.wet", waterEvent )
	}
}

def updated()
{
	unsubscribe()
    
	waterSensors.each
	{
		subscribe( it, "water.dry", waterEvent )
		subscribe( it, "water.wet", waterEvent )
	}
}