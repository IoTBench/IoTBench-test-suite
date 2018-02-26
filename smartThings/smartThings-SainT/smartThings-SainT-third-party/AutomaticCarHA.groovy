/**
 *  AutomaticCarHA
 *
 *  Copyright 2015 Yves Racine
 *  LinkedIn profile: ca.linkedin.com/pub/yves-racine-m-sc-a/0/406/4b/
 *
 *  Developer retains all right, title, copyright, and interest, including all copyright, patent rights, trade secret 
 *  in the Background technology. May be subject to consulting fees under the Agreement between the Developer and the Customer. 
 *  Developer grants a non exclusive perpetual license to use the Background technology in the Software developed for and delivered 
 *  to Customer under this Agreement. However, the Customer shall make no commercial use of the Background technology without
 *  Developer's written consent.
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *
 *  Software Distribution is restricted and shall be done only with Developer's written approval.
 *
 *  N.B. Requires MyAutomatic device available at 
 *          http://www.ecomatiqhomes.com/#!store/tc3yr 
 *
 */
definition(
	name: "AutomaticCarHA",
	namespace: "yracine",
	author: "Yves Racine",
	description: "Near Real-Time Automatic Car automation with SmartThings", 
	category: "My Apps",
	iconUrl: "https://www.automatic.com/_assets/images/favicons/favicon-32x32-3df4de42.png",
	iconX2Url: "https://www.automatic.com/_assets/images/favicons/favicon-96x96-06fd8c85.png",
	iconX3Url: "https://www.automatic.com/_assets/images/favicons/favicon-96x96-06fd8c85.png"
)

preferences {

	page(name: "HASettingsPage", title: "Home Automation Settings")
	page(name: "otherSettings", title: "OtherSettings")

}

def HASettingsPage() {
	def phrases = location.helloHome?.getPhrases()*.label

	dynamicPage(name: "HASettingsPage", install: false, uninstall: true, nextPage: "otherSettings") {
		section("About") {
			paragraph "Near Real-Time Automatic Car automation with SmartThings" 
			paragraph "Version 1.0.4" 
			paragraph "If you like this smartapp, please support the developer via PayPal and click on the Paypal link below " 
				href url: "https://www.paypal.me/ecomatiqhomes",
					title:"Paypal donation..."
			paragraph "Copyright©2016 Yves Racine"
				href url:"http://github.com/yracine/device-type.myautomatic", style:"embedded", required:false, title:"More information..."  
					description: "http://github.com/yracine"
		}
		section("For the following Automatic Connected Vehicle") {
			input "vehicle", "capability.presenceSensor", title: "Which vehicle?"
		}
		section("And, when these trip events are triggered") {
			input "givenEvents", "enum",
				title: "Which Events(s)?",
				multiple: true,
				required: true,
				metadata: [
					values: [
						'ignition:on',
						'ignition:off',
						'trip:finished',
						'notification:speeding',
						'notification:hard_brake',
						'notification:hard_accel',
						'mil:on',
						'mil:off',
						'hmi:interaction',
						'location:updated',
					]    
				]    

		}
		section("Turn on/off or Flash the following switch(es) [optional]") {
			input "switches", "capability.switch", required:false, multiple: true, title: "Which switch(es)?"
			input "switchMode", "enum", metadata: [values: ["Flash", "Turn On","Turn Off"]], required: false, defaultValue: "Turn On", title: "Action?"
		}
		section("Select Routine for Execution [optional]") {
			input "phrase", "enum", title: "Routine?", required: false, options: phrases
		}
	} /* end of dynamic page */
}


def otherSettings() {
	dynamicPage(name: "otherSettings", title: "Other Settings", install: true, uninstall: false) {
		section("Detailed Notifications") {
			input "detailedNotif", "bool", title: "Detailed Notifications?", required:
				false
		}
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", metadata: [values: ["Yes", "No"]], required:
				false
			input "phoneNumber", "phone", title: "Send a text message?", required: false
		}
		section([mobileOnly: true]) {
			label title: "Assign a name for this SmartApp", required: false
		}
	}
}




def installed() {
	initialize()
}

def updated() {
	try {
		unschedule()
	} catch (e) {
		log.debug ("updated>exception $e while trying to call unschedule") 
	}    
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(vehicle, "eventTripCreatedAt", eventHandler)
}

def eventHandler(evt) {
	def msg

	def createdAt =vehicle.currentEventTripCreatedAt   
	String eventType = vehicle.currentEventType
	log.debug "eventHandler>evt.value=${evt.value}, eventType=${eventType}"
    
	def lat = vehicle.currentEventTripLocationLat    
	def lon = vehicle.currentEventTripLocationLon
	msg = "AutomaticCarHA>${vehicle} vehicle has triggered ${eventType} event at ${createdAt}, (lon: ${lon}, lat: ${lat})..."
	log.debug msg
	if (detailedNotif) {
		send msg    
	}
	check_event(eventType)
}


private boolean check_event(eventType) {
	def msg
  	boolean foundEvent=false  
    
	log.debug "check_event>eventType=${eventType}, givenEvents list=${givenEvents}"
	if ((givenEvents.contains(eventType))) {
		foundEvent=true    
		msg = "AutomaticCarHA>${vehicle} vehicle has triggered ${eventType}, about to ${switchMode} ${switches}"
		log.debug msg
		if (detailedNotif) {
			send msg    
		}
		        
		if (switches) {
			if (switchMode?.equals("Turn On")) {
				switches.on()
			} else if (switchMode?.equals("Turn Off")) {
				switches.off()
			} else {
				flashLights()
			}	
		}
		if (phrase) {
			msg = "AutomaticCarHA>${vehicle} vehicle has triggered ${eventType}, about to execute ${phrase} routine"
			log.debug msg
			if (detailedNotif) {
				send msg    
			}
			location.helloHome?.execute(phrase)        
		}        
	}
	return foundEvent
}


private flashLights() {
	def doFlash = true
	def onFor = onFor ?: 1000
	def offFor = offFor ?: 1000
	def numFlashes = numFlashes ?: 3

	log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
	if (state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (onFor + offFor)
		doFlash = elapsed > sequenceTime
		log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
	}

	if (doFlash) {
		log.debug "FLASHING $numFlashes times"
		state.lastActivated = now()
		log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switches.collect {
			it.currentSwitch != "on"
		}
		int delay = 1 
		numFlashes.times {
			log.trace "Switch on after  $delay msec"
			switches.eachWithIndex {
				s, i ->
					if (initialActionOn[i]) {
						s.on(delay: delay)
					} else {
						s.off(delay: delay)
					}
			}
			delay += onFor
			log.trace "Switch off after $delay msec"
			switches.eachWithIndex {
				s, i ->
					if (initialActionOn[i]) {
						s.off(delay: delay)
					} else {
						s.on(delay: delay)
					}
			}
			delay += offFor
		}
	}
}





private def send(msg) {
	if (sendPushMessage != "No") {
		log.debug("sending push message")
		sendPush(msg)

	}

	if (phoneNumber) {
		log.debug("sending text message")
		sendSms(phoneNumber, msg)
	}

	log.debug msg
}
