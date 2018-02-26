/**
 *  Hello Door Lock Code
 *
 *  Copyright 2015 skp19
 *
 */
definition(
    name: "Hello Door Lock Code",
    namespace: "skp19",
    author: "skp19",
    description: "Changes to a specified mode or runs a Hello Home action depending on door unlock code",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

import groovy.json.JsonSlurper

preferences {
	page(name: "settings")
}

def settings() {
	dynamicPage(name: "settings", title: "Settings", uninstall:true, install:true) {        
        section("Choose Lock") {
            input "lock1", "capability.lock", title: "Which lock?", multiple: false
        }
        section("Enter User Code Number (This is not the code used to unlock the door)") {
            input "visitorCode", "number", title: "Which User Code Number?", defaultValue: "0"
        }
        def phrases = location.helloHome?.getPhrases()*.label
        section("Change Mode or Run Hello Home Action?") {
            input "actionType", "enum", title: "Action Type", metadata: [values: ["Change Mode", "Run Hello Home Action"]], defaultValue: "Change Mode", refreshAfterSelection: true

//			if (actionType == "Change Mode") {
            	input "visitormode", "mode", title: "Change to this mode when the unlock code is entered", required: false
//            }
            
//			if (actionType == "Run Hello Home Action") {
                if (phrases) {
                    phrases.sort()
                    input name: "homeAction", type: "enum", title: "Run this Hello Home Action when the unlock code is entered", required: false, options: phrases, refreshAfterSelection: true
                }
//            }
        }
        section("Visitor Notification Details") {
            input "notificationType", "enum", title: "Notification Type", metadata: [values: ["None", "Push Message", "Text Message", "Both"]], defaultValue: "None", refreshAfterSelection: true
//            if((notificationType == "Text Message") || (notificationType == "Both")) {
                input "phone1", "phone", title: "Phone number to send message to", required: false
//            }
//            if(notificationType != "None" && notificationType) {
	            input "visitorMsg", "text", title: "Message to send", required: false
//            }
        }
        section("User Code Discovery Mode (Enable and unlock the door using desired code. A message will be sent containing the user code used to unlock the door.)") {
            input "discoveryMode", "bool", title: "Enable"
        }
	}
}

def installed() {
    subscribe(lock1, "lock", checkCode)
}

def updated() {
	unsubscribe()
    subscribe(lock1, "lock", checkCode)
}

def checkCode(evt) {
    log.debug "$evt.value: $evt, $settings"

    if(evt.value == "unlocked" && evt.data) {
    	def lockData = new JsonSlurper().parseText(evt.data)
        
        if(discoveryMode) {
        	sendPush "Door unlocked with user code $lockData.usedCode"
        }
        
        if(lockData.usedCode == visitorCode && discoveryMode == false) {
        	log.info "Door Unlocked Notification Sent"
            
            if (actionType == "Change Mode") {
	            changeMode(visitormode)
            }
            if (actionType == "Run Hello Home Action") {
            	location.helloHome.execute(homeAction)
            }
                
        	sendMessage(visitorMsg)
        }
    }
}

def changeMode(newMode) {
	if (newMode && location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
			log.trace "Mode changed to '${newMode}'"
		}
		else {
			log.trace "Undefined mode '${newMode}'"
		}
	}
}

def sendMessage(msg) {
	if(customMessage) {
    	msg = customMessage
    }
	if (notificationType == "Push Message" || notificationType == "Both") {
		sendPush msg
	}
	if ((notificationType == "Text Message" || notificationType == "Both") && phone1 != null) {
		sendSms(phone1, msg)
	}
}