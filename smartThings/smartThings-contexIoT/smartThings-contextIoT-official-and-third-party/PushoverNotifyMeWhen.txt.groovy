/**
 *  Pushover Notify Me When
 *
 *  Author: jim@concipio-solutions.com (original SmartApp written by SmartThings)
 *  Date: 2013-07-20 (original SmartApp written on 2013-03-20)
 *  Date: 2014-05-06 (added unlock notification w/ user code value)
 */

// Automatically generated. Make future change here.
definition(
    name: "Pushover Notify Me When",
    namespace: "concipio-solutions",
    author: "jim@concipio-solutions.com",
    description: "Adds Pushover service to the 'Notify Me When' SmartApp",
    category: "Convenience",
    iconUrl: "https://drupal.org/files/project-images/pushover-app-logo.png",
    iconX2Url: "https://drupal.org/files/project-images/pushover-app-logo.png",
    oauth: true
)

import groovy.json.JsonSlurper

preferences {
	section("Choose one or more, when..."){
		input "motion", "capability.motionSensor", title: "Motion Here", required: false, multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
		input "acceleration", "capability.accelerationSensor", title: "Acceleration Detected", required: false, multiple: true
		input "mySwitch", "capability.switch", title: "Switch Turned On", required: false, multiple: true
		input "arrivalPresence", "capability.presenceSensor", title: "Arrival Of", required: false, multiple: true
		input "departurePresence", "capability.presenceSensor", title: "Departure Of", required: false, multiple: true
        input "lock","capability.lock", title: "Unlock/Lock", required: false, multiple: true
	}
	section("Message Text"){
		input "messageText", "text", title: "Message Text", required: true
	}
    section("Lock Notifications"){
    	input "lockNotify", "enum", title: "Notify on Lock?", required: false,
        metadata :[
        	values: ['Yes', 'No'
            ]
        ]
        input "unlockNotify", "enum", title: "Notify on Unlock?", required: false,
        metadata :[
        	values: ['Yes', 'No'
            ]
        ]
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
           values: [ 'Badge Only', 'Low', 'Normal', 'High', 'Emergency'
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

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
    
    // Custom states
    state.priorityMap = ["Low":-1,"Normal":0,"High":1,"Emergency":2];
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
    
    // Custom states
    state.priorityMap = ["Badge Only":-2,"Low":-1,"Normal":0,"High":1,"Emergency":2];
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", sendMessage)
	subscribe(acceleration, "acceleration.active", sendMessage)
	subscribe(motion, "motion.active", sendMessage)
	subscribe(mySwitch, "switch.on", sendMessage)
	subscribe(arrivalPresence, "presence.present", sendMessage)
	subscribe(departurePresence, "presence.not present", sendMessage)
    subscribe(lock, "lock", sendMessage)
}

def sendMessage(evt) {
	log.debug "$evt.name: $evt.value, $messageText"
    
    def thisMessageText = messageText;
    
    if(evt.name == "lock")
    {
      if ((evt.value == "unlocked") && (unlockNotify == "Yes"))
      {
        log.debug "$evt.data"
        
        if(evt.data)
        {
          def data = new JsonSlurper().parseText(evt.data)
          log.debug data.usedCode
          thisMessageText = messageText + ", Used Code[$data.usedCode]";
        }
      }
      else if ((evt.value == "locked") && (lockNotify == "Yes"))
      {
        log.debug "$evt.data"
      }
      else
      {
        return;
      }
    }
    
    if(pushNotification == "Yes")
    {
      log.debug "Sending SmartThings Notification"
	  sendPush(thisMessageText)
	}
    else
    {
      log.debug "Skipping SmartThings Notification"
    }
    
    if (phone)
    {
      log.debug "Sending SMS message to [$phone]"
	  sendSms(phone, thisMessageText)
	}
    else
    {
      log.debug "Skipping SMS message"
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
          postBody = [token: "$apiKey", user: "$userKey", device: "$deviceName", message: "$thisMessageText", priority: "$pushPriority", retry: "60", expire: "3600", sound: "$sound"]
        }
        else
        {
          postBody = [token: "$apiKey", user: "$userKey", device: "$deviceName", message: "$thisMessageText", priority: "$pushPriority", sound: "$sound"]
        }
        
        log.debug postBody
      }
      else
      {
        log.debug "Sending Pushover to All Devices"
        
        if(pushPriority == 2)
        {
          postBody = [token: "$apiKey", user: "$userKey", message: "$thisMessageText", priority: "$pushPriority", retry: "60", expire: "3600"]
        }
        else
        {
          postBody = [token: "$apiKey", user: "$userKey", message: "$thisMessageText", priority: "$pushPriority"]
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
    else
    {
      log.debug "Skipping Pushover Notification"
    }
}
