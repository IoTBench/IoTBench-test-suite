/**
 *  Automated light
 *
 *  Copyright 2015 Timothe Fillion Brunet
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Automated lights",
    namespace: "timbit123",
    author: "Timothe Fillion Brunet",
    description: "Used to change the light using the multi-sensor",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name:"Settings", uninstall:true){
		section() {
			// TODO: put inputs here
            paragraph "Motion sensor to use"
            input "motionSensors", "capability.motionSensor", title: "Motions sensor?", multiple: false, required: true
		}
        section(){
        	paragraph "Light sensor to use"
            input "luxSensor", "capability.illuminanceMeasurement", title: "Light sensor?", multiple: false, required: true
            input "luxLevel", "number", title: "Darkness Lux level?", defaultValue: 300, require: true
            input "luxGoalLevel", "number", title: "Lux goal level?", defaultValue: 500, require: true
            input "luxDelta", "number", title: "Lux delta error?", defaultValue: 60, require: true
            input "timer", "number", title: "How long leave light on? (Min.)", defaultValue: 30, require : true
        }
        section("Turn on these lights") {
        	input "switches", "capability.switch", title: "Normal switch?", multiple: true, required: false
            input "dimmer", "capability.switchLevel", title: "Dimmer switch?", multiple: true, required: false
            input "dimmerMinLevel", "number", title: "Minimal light dimmer pourcentage?", defaultValue: 10, require : true
            input "dimmerMaxLevel", "number", title: "Maximum light dimmer pourcentage?", defaultValue: 100, require : true
            input "dimmerstartLevel", "number", title: "Starting level pourcentage?", defaultValue: 60, require : true
            input "dimmerStepLevel", "number", title: "Pourcentage step increment?", defaultValue: 5, require : true
    	}
        section("When someone at home"){
        	input "presence", "capability.presenceSensor", title: "Who?", multiple: true, required: false
        }
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
	log.debug "Initializing = subscribing to motionSensors"
    //validate dimmer level
    if(settings.dimmerMinLevel > 100 || settings.dimmerMinLevel < 0)
    	settings.dimmerMinLevel = 10
    if(settings.dimmerMaxLevel > 100 || settings.dimmerMaxLevel < 0)
    	settings.dimmerMaxLevel = 100
        
    if(settings.dimmerMinLevel > settings.dimmerMaxLevel){
    	settings.dimmerMinLevel = 10
    	settings.dimmerMaxLevel = 100
    }
    state.lightIsOn = false
    state.dimmerLevel = settings.dimmerstartLevel
    state.usePresence = presence == null ? false : true
    subscribe(motionSensors, "motion", MotionHandler)
    subscribe(luxSensor, "illuminance", lumixHandler)
    subscribe(presence, "presence", presenceHandler)
    subscribe(location, "mode", modeChangeHandler)
    log.debug "current mode ${location.mode.value}"
    log.debug "presences ${state.usePresence}"
	// TODO: subscribe to attributes, devices, locations, etc.
}

// ====================================
// Handlers
// ====================================

def MotionHandler(evt){
    if (evt.value == "active"){
    	def someoneAtHome = location.mode == "Home" ? true : false
        if (!someoneAtHome && state.usePresence){
        	presence.each{
            	def pres = it.currentState("presence")
            	if(pres.stringValue == "present"){
                	someoneAtHome = true
                }
            }
        }
        
        
        
    	if(someoneAtHome){
        	//check if current lumix < Darkness Lux level
            if(luxSensor.currentValue("illuminance") < settings.luxLevel){
            	if(!state.lightIsOn){
                    state.lightIsOn = true
                    triggerSwitches(true)
                }
                //if we had an inactive status before and we are waiting to close the lights... reset...
                unschedule("stopLight")
            }
        }else{
        	log.debug "Motion detected but nobody at home... !??!?!? alert?"
		}
        
    }
    else if (evt.value == "inactive"){
    	if(state.lightIsOn){
            log.debug "Inactive"
            log.debug "call stopLight in ${settings.timer} minutes"
            //we are changing to inactive
            runIn(settings.timer * 60, stopLight, [overwrite:true])
        }
    } else {
   		log.error "State Undefined ${evt.value}!!!!"
    }
}


def lumixHandler(evt){
	//adjust the light!
	log.debug "New lumix event ${evt.value}"
    if(state.lightIsOn)
    	updateDimmerLevel(evt.value.toInteger())

}

def presenceHandler(evt){
	log.debug "New presence event ${evt.value}"

}

def modeChangeHandler(evt){
	log.debug "current mode ${location.mode.value}"

}

def stopLight(){
	log.debug "Turning off lights"
    triggerSwitches(false)
}

def triggerSwitches(openLight){
	if(!openLight){
    	//reset state
		state.lightIsOn = false
        state.dimmerLevel = ((settings.dimmerMaxLevel - settings.dimmerMinLevel) / 2) + settings.dimmerMinLevel
    }
	if(switches){
        switches.each{
            if(openLight)
                it.on()
            else
                it.off()
        }
    }
    if(dimmer){
        if(openLight){
            setDimmerLevel(state.dimmerLevel)
        }else
            setDimmerLevel(0)
    }
}

def updateDimmerLevel(currentLumix){
	//check if we have dimmer
    if(dimmer == null)
    	return
	
	//check if the lumix is near our goal
    def DELTA = settings.luxDelta // lumix
    def DIMMER_STEP_LEVEL = settings.dimmerStepLevel//pourcentage
    
    def luxDiff = currentLumix - settings.luxGoalLevel
    
    log.debug "Current lumix ${currentLumix}, lumix goal ${settings.luxGoalLevel}, lumix diff ${luxDiff}"
    	
    if(luxDiff > DELTA){//
    	//if luxDiff is positif 300 - 200 = 100 (it mean we have too much light)
        state.dimmerLevel = state.dimmerLevel - DIMMER_STEP_LEVEL
        log.debug "too much light"
    }else if (luxDiff < -DELTA){
    	//if luxDiff is positif 200 - 300 = -100 (it mean we don't have enought light)
        log.debug "${DIMMER_STEP_LEVEL}"
        log.debug "${state.dimmerLevel}"
        state.dimmerLevel = state.dimmerLevel + DIMMER_STEP_LEVEL
        log.debug "${state.dimmerLevel}"
        log.debug "not enought light"
    }
    if(state.dimmerLevel < settings.dimmerMinLevel){
   		//we cannot go much less light, close the lights
    	triggerSwitches(false)
        unschedule("stopLight")
        return
        log.debug "dimmer level lower than dimmerMinLevel we are closing lights"
    }
    if(state.dimmerLevel > settings.dimmerMaxLevel){
    	//we cannot go over
    	state.dimmerLevel = settings.dimmerMaxLevel
        log.debug "dimmer cannot go over dimmerMaxLevel settings"
    }
    
    log.debug "Setting new dimmer level ${state.dimmerLevel}"
    setDimmerLevel(state.dimmerLevel)
    
    
}

def setDimmerLevel(level){
	dimmer.each{
    	it.setLevel(level)
    }
}
	
