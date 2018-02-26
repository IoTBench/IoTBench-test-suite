/**
 *  Something Left Open
 *
 *  Author: jim@concipio-solutions.com
 *  Date: 2013-07-20
 *
 *  Date: 2013-09-04, Added pushover notification sound customization
 */

// Automatically generated. Make future change here.
definition(
    name: "Something Left Open",
    namespace: "",
    author: "jim@concipio-solutions.com",
    description: "Alerts you when a contact sensor has been left open for a specified number of minutes. Alerts are in the form of a user-defined message that is provided as a push notification to the user's mobile device. Additional optional notifications can be sent by: (1) SMS message, (2) BEEPing a device, and/or (3) Pushover notification (http://pushover.net/). This is great for monitoring refrigerators, freezers, gates, doors, and windows. Save energy or keep your family and pets secure with this SmartApp.",
    category: "Convenience",
    iconUrl: "http://www.yourpurposeguide.com/sites/default/files/door_ajar.jpg",
    iconX2Url: "http://www.yourpurposeguide.com/sites/default/files/door_ajar.jpg",
    oauth: true
)

preferences {
	section("When . . .") {
		input "contactSensor", "capability.contactSensor", title: "Something is left open"
        input "numMinutes", "number", title: "For this many minutes", required: true
    }
	section("Message Text"){
		input "messageText", "text", title: "Message Text", required: true
	}
    section("Send SmartThings Notification (optional)"){
    	input "pushNotification", "enum", title: "Send SmartThings Notification?", required: true,
        metadata :[
        	values: ['Yes', 'No'
            ]
        ]
    }
	section("Send SMS Message (optional)"){
		input "phoneNumber", "phone", title: "Phone Number", required: false
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
    section("Send BEEP to (optional)"){
		input "alertDevice", "capability.tone", title: "Alert Device", required: false
    }
}


def installed() {
	subscribe(contactSensor, "contact", onContactChange);
    
    // Custom states
    state.priorityMap = ["Low":-1,"Normal":0,"High":1,"Emergency":2];
}

def updated() {
	unsubscribe()
   	subscribe(contactSensor, "contact", onContactChange);
    
    // Custom states
    state.priorityMap = ["Badge Only":-2,"Low":-1,"Normal":0,"High":1,"Emergency":2];
}

def onContactChange(evt) {
	log.debug "onContactChange";
	if (evt.value == "open") {
    	runIn(numMinutes * 60, onContactLeftOpenHandler);
    }
}

def onContactLeftOpenHandler() {
	log.debug "onContactLeftOpenHandler";
    
    if(contactSensor.latestValue("contact") == "open")
    {
    log.debug "Something Left Open...Sending Notifications!"
   
    if(pushNotification == "Yes")
    {
      log.debug "Sending SmartThings Notification"
	  sendPush(messageText)
	}
    else
    {
      log.debug "Skipping SmartThings Notification"
    }
      
    if(phoneNumber)
    {
      log.debug "Sending to Phone"
      sendSms(phoneNumber, messageText);
    }
      
    if(alertDevice)
    {
      log.debug "Sending Beep to Device"
      alertDevice.beep();
    }
    
    if(apiKey && userKey)
    {
      log.debug "Sending Pushover with API Key [$apiKey] and User Key [$userKey]"
      
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
  else
  {
    log.debug "Something Opened...but it's now closed!"
  }
}