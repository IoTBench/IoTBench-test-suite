/**
 *  Notify Me When It Opens
 *
 *  If any contact sensors open and there is a particular current mode. Turn a switch on and contact someone (optional).
 *
 *  Author: Matt Blackmore
 */
preferences {
	section("When these open..."){
		input "contacts", "capability.contactSensor", title: "Sensors?", multiple:true
	}
    section("And the current mode is...") {
		input "currentMode", "mode", title: "Mode?"
    }
    section("Turn this on...") {
    	input "switch1", "capability.switch", title: "Switch?", required: false
    }
    section( "And Notify..." ) {
		input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:true
		input "phone", "phone", title: "Send a Text Message?", required: false
	}
}

def installed()
{
	subscribe(contacts, "contact.open", contactOpenHandler)
}

def updated()
{
	unsubscribe()
	subscribe(contacts, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {
	log.trace "$evt.value: $evt, $settings"

	if (location.mode == currentMode) {
    	if(switch1) {
    		switch1.on();
        }
		log.debug "A contact was opened, notifying user and turning on switch"
        def message = "A contact was unexpectedly opened at ${evt.date}!"
		send(message)
    } else {
    	log.debug "A contact was opened but the mode is not currect to trigger an action"
    }
}

private send(msg) {
	if ( sendPushMessage == "Yes" ) {
		log.debug( "sending push message" )
		sendPush( msg )
	}

	if ( phone ) {
		log.debug( "sending text message" )
		sendSms( phone, msg )
	}

	log.debug msg
}