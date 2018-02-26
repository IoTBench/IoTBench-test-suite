/**
 *  Knock Knock
 *
 *  Author: thad@edgerunner.org
 *  Date: 2013-06-07
 */

// Automatically generated. Make future change here.
definition(
    name: "Knock Knock",
    namespace: "Org.Edgerunner",
    author: "Thaddeus Ryker",
    description: "Do something when small vibrations are detected.  This app triggers an action when an acceleration sensor activates, but also suppresses the activation if there is a combined contact sensor in an open state (such as with the SmartSense Multi).  This way the app only triggers on a door knock, and not a door open.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

preferences
{
	section("When vibration is detected...")
    {
		input "accelSensor", "capability.accelerationSensor", title: "Where?", descrption: "Which acceleration sensor?"
    }
	section ("But not when this door is opened...")
    {
		input "contactSensor", "capability.contactSensor", title: "Ignore if this contact opens", description: "Which contact?"
	}    
    section("Send this message in a push notification")
    {
    	input "message", "text", title: "Message Text"
    }
    section("And text me at (optional)...")
    {
    	input "phone", "phone", title: "Phone Number", required: false
    }
}

def installed()
{
	subscribe(accelSensor, "acceleration", vibrationHandler)
    subscribe(contactSensor, "contact.open", contactOpenedHandler)
	log.debug "Installed with settings: ${settings}"
    state.ShouldCheckForKnock = false
    state.DoorWasOpened = false
}

def updated()
{
	unsubscribe()
    subscribe(accelSensor, "acceleration", vibrationHandler)
    subscribe(contactSensor, "contact.open", contactOpenedHandler)
	log.debug "Updated with settings: ${settings}"
    state.ShouldCheckForKnock = false
    state.DoorWasOpened = false
}

def vibrationHandler(evt)
{
    log.debug "Should Check For Knock: $state.ShouldCheckForKnock"
    log.debug "Door Was Opened: $state.DoorWasOpened"
    log.debug "Value: $evt.value"
    log.debug "Accelerometer: $settings.accelSensor"
    def contactState = contactSensor.currentState("contact")
    if ((evt.value == "active") && (contactState.value == "closed"))
    {
    	log.debug "Potential knock detected"
    	state.DoorWasOpened = false
        state.ShouldCheckForKnock = true
    }
    else if ((evt.value == "active") && (contactState.value == "open"))
    	log.debug "Contact was already open, knock ignored"
    else if ((evt.value == "inactive") && state.ShouldCheckForKnock)
    {
    	state.ShouldCheckForKnock = false
        log.debug "Attempting to notify user"
        def notifyUser = true
        if (state.DoorWasOpened)
        {
            log.debug "Contact was opened, knock ignored"
            notifyUser = false // it was a false alarm since the door was opened, so we don't notify
        }
        if (notifyUser)
        {
            log.debug "Notifying user with the following: ${message}"
            sendPush(message)
            if (phone)
                sendSms(phone, message)
        }
    }
 	else
    	log.debug "skipping knock check"
}

def contactOpenedHandler(evt)
{
	log.debug "$settings.contactSensor: $evt.value"
    if (state.ShouldCheckForKnock)
    	state.DoorWasOpened = true
}