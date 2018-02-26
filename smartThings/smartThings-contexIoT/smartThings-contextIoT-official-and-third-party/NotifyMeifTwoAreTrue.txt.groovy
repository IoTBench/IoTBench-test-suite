/**
 *  Notify Me If Two Are True
 *	(Pick two sensors and tell me if the states that I picked are both true!)
 *
 *  Author: zach@beamlee.com
 *  Date: 2013-08-02
 */
preferences {
	section("Presence") {
		input "presence", "capability.presenceSensor", title: "Pick a presence sensor", required: false
		input "presenceState", "enum", title: "Presence value", required: false, metadata: [values:["Present", "Not Present"]]
	}
	section("Contact"){
		input "contact", "capability.contactSensor", title: "Pick a contact sensor", required: false
		input "contactState", "enum", title: "Contact Value", required: false, metadata: [values:["Open", "Closed"]]
	}
	section("Switches"){
		input "switches", "capability.switch", title: "Pick switch(es)", required: false, multiple: true
		input "switchState", "enum", title: "Switch(es) Value", required: false, metadata: [values:["On", "Off"]]
	}
	section("Motion"){
		input "motion", "capability.motionSensor", title: "Pick a motion sensor", required: false
		input "motionState", "enum", title: "Motion Value", required: false, metadata: [values:["Active", "Inactive"]]
	}

}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	def total = totalSelected()
    log.debug "total selected is $total"
	if(totalSelected() == 2){
		init()
	}
	else{
		sendPush("You have selected $total services.  You may only select two services.")
	}
}

def init(){
	comboCreator()
	if(state.deviceCombo == "presenceContact" || state.deviceCombo == "presenceSwitches" || state.deviceCombo == "presenceMotion"){
		subscribe(presence, "presence", presenceHandler)
        log.debug "combo creator returned presence subscription"
	}
	else if(state.deviceCombo == "contactSwitches" || state.deviceCombo == "contactMotion"){
		subscribe(contact, "contact", contactHandler)
        log.debug "combo creator returned contact subscription"
	}
	else if(state.deviceCombo == "motionSwitches"){
		subscribe(motion, "motion", motionHandler)
        log.debug "combo creator returned motion subscription"
	}
}

def presenceHandler(evt){
	def currentPresence = evt.value
    if(currentPresence == "present" && presenceState == "Present" || currentPresence == "not present" && presenceState == "Not Present"){
	if(state.deviceCombo == "presenceContact"){
		def currentContact = contact.currentValue("contact")
		if(contactState == "Open" && currentContact == "open" || contactState == "Closed" && currentContact == "closed"){
			sendPush("Your presence sensor is $currentPresence and your contact sensor reads $currentContact") 
		}
	}
	else if(state.deviceCombo == "presenceMotion"){
		def currentMotion = motion.currentValue("motion")
		if(motionState == "Active" && currentMotion == "active" || motionState == "Inactive" && currentMotion == "inactive"){
			sendPush("Your presence sensor is $currentPresence and your motion sensor reads $currentMotion")
		}
	}
	else if(state.deviceCombo == "presenceSwitches"){
		def switchOn = switches.find{it.currentSwitch == "on"}
        def switchOff = switches.find{it.currentSwitch == "off"}
        def currentSwitches = switches.currentValue("switch")
        log.debug switchOn
        log.debug switchOff
        log.debug currentSwitches
		if(switchState == "On" && switchOn || switchState == "Off" && switchOff){
			sendPush("Your presence sensor is $currentPresence and your switches read $currentSwitches")
		} 
	}
    }
}

def contactHandler(evt){
	def currentContact = evt.value
    log.debug "current Contact is $currentContact"
	if(currentContact == "open" && contactState == "Open" || currentContact == "closed" && contactState == "Closed"){
    if(state.deviceCombo == "contactSwitches"){
    	log.debug "Made it to contact switch inside if"
		def switchOn = switches.find{it.currentSwitch == "on"}
        def switchOff = switches.find{it.currentSwitch == "off"}
        def currentSwitches = switches.currentValue("switch")
        log.debug switchOn
        log.debug switchOff
        log.debug currentSwitches
		if(switchState == "On" && switchOn || switchState == "Off" && switchOff){
        	log.debug "made it inside this if statement here."
			sendPush("Your contact sensor reads $currentContact and your switches read $currentSwitches")
		}
	}
	else if(state.deviceCombo == "contactMotion"){
		def currentMotion = motion.currentValue("motion")
		if(motionState == "Active" && currentMotion == "active" || motionState == "Inactive" && currentMotion == "inactive"){
			sendPush("Your contact sensor reads $currentContact and your motion sensor reads $currentMotion")
		}
	}
    }
}

def motionHandler(evt){
	def currentMotion = evt.value
    if(currentMotion == "active" && motionState == "Active" || currentMotion == "inactive" && motionState == "Inactive"){
	if(state.deviceCombo == "motionSwitches"){
		def switchOn = switches.find{it.currentSwitch == "on"}
        def switchOff = switches.find{it.currentSwitch == "off"}
        def currentSwitches = switches.currentValue("switch")
        log.debug switchOn
        log.debug switchOff
        log.debug currentSwitches
		if(switchState == "On" && switchOn || switchState == "Off" && switchOff){
			sendPush("Your motion sensor reads $currentMotion and your switches read $currentSwitches")
		}	
	}
    }
}

private totalSelected(){
	def result = 0
	if(presence && presenceState){
		result += 1
	}
	if(contact && contactState){
		result += 1
	}		
	if(switches && switchState){
		result += 1
	}
	if(motion && motionState){
		result += 1
	}
	result
}

def comboCreator(){
	if(presence && contact){
		state.deviceCombo = "presenceContact"    
	}
	else if(contact && switches){
		state.deviceCombo = "contactSwitches"
	}
	else if(switches && motion){
		state.deviceCombo = "motionSwitches"
	}
	else if(presence && motion){
		state.deviceCombo = "presenceMotion"
	}
	else if(presence && switches){
		state.deviceCombo = "presenceSwitches"
	}
	else if(contact && motion){
		state.deviceCombo = "contactMotion"
	}
	log.debug "Device combo is $state.deviceCombo"
}
