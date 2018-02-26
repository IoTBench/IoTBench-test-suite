/**
 *  Pushover Flood Alert! (modification of Flood Alert)
 *
 *  Author: SmartThings; updated by Chad Monroe (chad@monroe.io); updated by jim@concipio-solutions.com
 *
 *  concipio 07/20/2013: Added optional Pushover notifications
 *  cmonroe 07/16/2013:  Allow for multiple water sensors to be selected
 *  cmonroe 07/16/2013:  Set SMS delta to 2 m; push notification to 10 s
 *  cmonroe 07/16/2013:  Send messages for both wet and dry events
 */

// Automatically generated. Make future change here.
definition(
    name: "Pushover Flood Alert!",
    namespace: "",
    author: "jim@concipio-solutions.com",
    description: "Flood Alert with Pushover Notifications",
    category: "Safety & Security",
    iconUrl: "http://coad.net/blog/images/Noah_27s_20Ark_20Cartoon.jpg",
    iconX2Url: "http://coad.net/blog/images/Noah_27s_20Ark_20Cartoon.jpg",
    oauth: true
)

preferences 
{
	section("When there's water detected...")
	{
		input "waterSensors", "capability.waterSensor", required: true, multiple: true
	}
    section("Send SmartThings Notification (optional)"){
    	input "pushNotification", "enum", title: "Send SmartThings Notification?", required: true,
        metadata :[
        	values: ['Yes', 'No'
            ]
        ]
    }
	section("Send SMS Message to (optional)"){
		input "phone", "phone", title: "Phone Number", required: false
	}
    section("Send Pushover Notification (optional)"){
        input "apiKey", "text", title: "Pushover API Key", required: false
        input "userKey", "text", title: "Pushover User Key", required: false
        input "deviceName", "text", title: "Pushover Device Name", required: false
        input "priority", "enum", title: "Pushover Priority", required: false,
        metadata :[
           values: [ 'Low', 'Normal', 'High', 'Emergency'
           ]
        ]
        input "sound", "enum", title: "Pushover Alert Sound", required: false,
        metadata :[
           values: [ 'pushover', 'bike', 'bugle', 'cashregister', 'classical', 'cosmic',
           'falling', 'gamelan', 'incoming', 'intermission', 'magic', 'mechanical', 'pianobar',
           'siren', 'spacealarm', 'tugboat', 'alien', 'climb', 'persistent', 'echo', 'updown', 'none'
           ]
        ]
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
    def messageText = "Flood Alert! [$msg]"
   
	if ( phone )
    {
        if ( alreadyNotified( device, evtState, evt.isoDate, deltaSeconds ) > 0 ) 
        {
            log.debug "SMS message for sensor=${devName} state=${evtState} already sent within the last ${deltaSeconds} seconds"
        } 
        else 
        {
            log.debug "${devName} is ${evtState}, sending SMS to ${phone}"
            
			sendSms( phone, messageText )
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
      if(pushNotification == "Yes")
      {
        log.debug "Sending SmartThings Notification"
	    sendPush(messageText)
	  }
      else
      {
        log.debug "Skipping SmartThings Notification"
      }
    }
    
    if ( alreadyNotified( device, evtState, evt.isoDate, (deltaSeconds) ) > 0 )
    {
      log.debug "Pushover notification for sensor=${devName} state=${evtState} already sent within the last ${deltaSeconds} seconds"
    }
    else if(apiKey && userKey)
    {
      log.debug "${devName} is ${evtState}, sending Pushover notification"
      
      def postBody = []
      def pushPriority = 0
      
      // Set Priority for Pushover Notification
      pushPriority=state.priorityMap[priority];
      
      log.debug "priority = $pushPriority"
      log.debug "sound = $sound"
      
      if(deviceName)
      {
        log.debug "Sending Pushover to Device: $deviceName"
        
        if(pushPriority == 2)
        {
          postBody = [token: "$apiKey", user: "$userKey", device: "$deviceName", message: "$messageText", priority: "$pushPriority", retry: "60", expire: "3600", sound: "$sound"]
        }
        else
        {
          postBody = [token: "$apiKey", user: "$userKey", device: "$deviceName", message: "$messageText", priority: "$pushPriority", sound: "$sound"]
        }
        
        log.debug postBody
      }
      else
      {
        log.debug "Sending Pushover to All Devices"
        
        if(pushPriority == 2)
        {
          postBody = [token: "$apiKey", user: "$userKey", message: "$messageText", priority: "$pushPriority", retry: "60", expire: "3600"]
        }
        else
        {
          postBody = [token: "$apiKey", user: "$userKey", message: "$messageText", priority: "$pushPriority"]
        }
        
        log.debug postBody
      }
      
      def params = [
      		uri: 'https://api.pushover.net/1/messages.json',
            body: postBody
            ]
      
      httpPost(params){ response ->
          log.debug "Response Received: Status [$response.status]"
          
          if(response.status != 200)
          {
            sendPush("Received HTTP Error Response. Check Install Parameters.")
          }
      }
    }
}

def installed() 
{ 
	waterSensors.each
	{
		subscribe( it, "water.dry", waterEvent )
		subscribe( it, "water.wet", waterEvent )
	}
    
    // Custom states
    state.priorityMap = ["Low":-1,"Normal":0,"High":1,"Emergency":2];
}

def updated()
{
	unsubscribe()
    
	waterSensors.each
	{
		subscribe( it, "water.dry", waterEvent )
		subscribe( it, "water.wet", waterEvent )
	}

    // Custom states
    state.priorityMap = ["Low":-1,"Normal":0,"High":1,"Emergency":2];
}