/**
 *  Light Follows Me If There Isn't Enought Light
 *
 *  Author: SmartThings
 */


// Automatically generated. Make future change here.
definition(
    name: "Light Follows Me If There Is No Light",
    namespace: "",
    author: "juano23@gmail.com",
    description: "The light turn on if there is movement and the illuminance is lower than input",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance@2x.png"
)

preferences {
	section("Turn on when..."){
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("If the light intensity on..."){
		input "lightSensor1", "capability.illuminanceMeasurement"
	}
	section("is lest than..."){
		input "lux1", "number", title: "Lux?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", eventHandler)
	subscribe(motion1, "motion", motionHandler)
	subscribe(lightSensor1, "illuminance", illuminanceHandler)
}

def illuminanceHandler(evt) {
	log.debug "$evt.name: $evt.value and lux: $lux1"
    int illuminanceState = Integer.parseInt(evt.value);
	if (illuminanceState >= lux1) {
        def switchesState = "off";
		def switchesValue = switches.currentState("switch")
		for (String item : switchesValue.value) {
    	  if (item == "on") {
      		switchesState = "on"
    	  }
		}	
		if (switchesState == "on"){
            log.debug "there is enought light: turning lights off"
        	switches.off()
        }    
    } else {
        def motionState = "inactive";
        def motionValue = motion1.currentState("motion")
        for (String item : motionValue.value) {
          if (item == "active") {
            motionState = "active"
          }
        }
        log.debug "motionState: $motionState"
        if (motionState == "active") {
            log.debug "Motion is active and the there isn't enought light: turning lights on"
            switches.on()
        }
    }
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
        def illuminanceState = lightSensor1.latestValue("illuminance")
        log.debug "$illuminanceState < $lux1"
		if (illuminanceState < lux1) {
            log.debug "turning on lights"
            switches.on()
        }
	} else if (evt.value == "inactive") {
		runIn(minutes1 * 60, scheduleCheck)
	}
}

def eventHandler(evt) {
	log.debug "$evt.name: $evt.value"
    def illuminanceState = lightSensor1.latestValue("illuminance")
    log.debug "$illuminanceState < $lux1"
    if (illuminanceState < lux1) {
        log.debug "turning on lights"
        switches.on()
    }    
}

def scheduleCheck() {
    def motionState = "inactive";
    def motionValue = motion1.currentState("motion")
	log.debug "Schedule check... Sensor $motionValue.value"
	for (String item : motionValue.value) {
      if (item == "active") {
      	motionState = "active"
      }
	}
    log.debug "motionState: $motionState"
	if (motionState == "inactive") {
        log.debug "Motion has stayed inactive long enough since last check: turning lights off"
        switches.off()
    } else {
    	log.debug "Motion is active, do nothing and wait for inactive"
    }
}
