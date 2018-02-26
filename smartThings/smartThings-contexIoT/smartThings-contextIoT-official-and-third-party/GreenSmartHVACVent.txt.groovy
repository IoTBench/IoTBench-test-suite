/**
 *  Green Smart HVAC  Vent
 *
 *  Copyright 2014 Barry A. Burke
 *
 *
 * For usage information & change log: https://github.com/SANdood/Green-Smart-HVAC-Vent
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
	name:		"Green Smart HVAC Vent",
	namespace: 	"Green Living",
	author: 	"Barry A. Burke",
	description: "Intelligent vent controller that manages both pressure purge and room temperature.",
	category: 	"Green Living",
	iconUrl: 	"https://s3.amazonaws.com/smartapp-icons/Solution/comfort.png",
	iconX2Url:	"https://s3.amazonaws.com/smartapp-icons/Solution/comfort@2x.png"
)

preferences {
	page( name: "setupApp" )
}

def setupApp() {
	dynamicPage(name: "setupApp", title: "Smart HVAC Vent Setup", install: true, uninstall: true) {

		section("HVAC Vent Controls") {
			input name: "ventSwitch", type: "capability.switchLevel", title: "Vent controller?", multiple: false, required: true
            input name: "pollVent", type: "bool", title: "Poll this vent (for setLevel state)?", defaultValue: false, required: true
            input name: "minVent", type: "number", title: "Minimum vent level?", defaultValue: "10", required: true
            
            paragraph ""
            input name: "thermometer", type: "capability.temperatureMeasurement", title: "Room thermometer?", multiple: false, required: true
			
            paragraph ""
            input name: "thermostatOne", type: "capability.thermostat", title: "1st Thermostat", multiple: false, required: true, refreshAfterSelection: true
			input name: "thermostatTwo", type: "capability.thermostat", title: "2nd Thermostat (optional)", multiple: false, required: false  // allow for tracking a single zone only
			input name: "pollTstats", type: "bool", title: "Poll these thermostats? (for state changes)?", defaultValue: true, required: true
            input name: "pollSwitch", type: "capability.relaySwitch", title: "Poll only while switch is on?", required: false
            
            input name: "trackTempChanges", type: "capability.temperatureMeasurement", title: "Monitor temp change events elsewhere also?", multiple:true, required: false
		}

		section("Temperature control parameters:") {
        
        	input name: "tempControl", type: "bool", title: "Actively manage room/zone temperature?", defaultValue: false, required: true, refreshAfterSelection: true
			input name: "followMe", type: "capability.thermostat", title: "Follow temps on this thermostat", multiple: false, required: true, refreshAfterSelection: true
            
            paragraph ""
           	input name: "humidControl", type: 'bool', title: 'Actively manage humidity (cooling)?', defaultValue: false, /*required: true,*/ refreshAfterSelection: true
            input name: 'humidSensor', type: 'capability.relativeHumidityMeasurement', title: "Use which humidity sensor?", multiple: false, refreshAfterSelection: true, required: false
           	input name: 'targetHumidity', type: 'decimal', title: 'Target humidity', defaultValue: 60, /*required: true,*/ refreshAfterSelection: true
            input name: 'maxOverCool', type: 'decimal', title: 'Max over-cool degrees', defaultValue: 2.0f, required: true

			paragraph ""
    		input name: "doorWatch", type: "capability.contactSensor", title: "Monitor which door?", multiple: false, required: false, refreshAfterSelection: true
           	input name: "doorControl", type: "enum", title: "Follow temps only when door is...", options: ['Open','Closed','Both','Never'], defaultValue: 'Both', required: true, refreshAfterSelection: true
		}
		
		section([mobileOnly:true], "Additional settings") {
        	input name: "modeOn",  type: "mode", title: "Enable only in specific mode(s)?", multiple: true, required: false
			label title: "Assign a name for this SmartApp", required: false
//			mode title: "Set for specific mode(s)", required: false
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
//    unschedule(tempHandler)
//    unschedule(timeHandler)
//    unschedule(checkOperatingStates)
	unschedule()
	initialize()
}

def initialize() {
	log.debug "Initializing"
    
    atomicState.lastStatus = ""
    atomicState.checking = false
    atomicState.ventChanged = true
    atomicState.timeHandlerLast = false
    atomicState.lastPoll = 0
    atomicState.managingTemp = tempControl

// Get the latest values from all the devices we care about BEFORE we subscribe to events...this avoids a race condition at installation 
// & reconfigure time

	atomicState.highHeat = 0 as Integer
	atomicState.lowCool = 100 as Integer
    atomicState.recoveryMode = false
    state.fastPollSecs = 45
    state.slowPollSecs = 90
    state.minPollSecs = 15			// Should be what the ecobee device is using
    
    if (pollTstats) {
    	pollThermostats()
        if (!pollSwitch || (pollSwitch && (pollSwitch.currentSwitch == 'on'))) {
    		runIn( state.fastPollSecs.toInteger(), timeHandler, [overwrite: true] )  // schedule another poll in 4 minutes
        }
    }
    
    def startLevel = minVent
    if (humidControl) {
       	def humidity = humidSensor.currentHumidity
        if (humidity) {
           	if (humidity.toFloat() > targetHumidity) { 
               	startLevel = 99 
            }
    	}    
    }
    
    if (tempControl) {
        if (thermostatTwo) { startLevel = 99 }
        if (ventSwitch.currentLevel != startLevel) { ventSwitch.setLevel(startLevel as Integer) }

        if(followMe.currentProgramType != 'hold') {
        	if (followMe.currentThermostatMode.contains('eat')) {
        		atomicState.highHeat = followMe.currentHeatingSetpoint.toFloat()
            }
            else if (followMe.currentThermostatMode.contains('cool')) {
        		atomicState.lowCool = followMe.currentCoolingSetpoint.toFloat()
            }
        }
        
        def checkState = followMe.currentThermostatOperatingState
        if (checkstate == 'heating') {
        	if ((atomicState.highHeat > 0) && (followMe.currentTemperature > atomicState.highHeat)) {
            	atomicState.recoveryMode = true
            }
        }
        else if (checkState == 'cooling') {
        	if ((atomicState.lowCool < 100) && (followMe.currentTemperature < atomicState.lowCool)) {
            	atomicState.recoveryMode = true
            }
        }
    }
    else { 
    	ventSwitch.setLevel(minVent as Integer)
		atomicState.highHeat = 0 as Float
   		atomicState.lowCool = 100 as Float
    }
    if (pollVent) { ventSwitch.poll() }	// get the current / latest status of everything
    atomicState.ventChanged = false		// just polled for the latest, don't need to poll again until we change the setting (battery saving)

// Since we have to poll the thermostats to get them to tell us what they are doing, we need to track events that might indicate
// one of the zones has changed from "idle", so we subscribe to events that could indicate this change. Ideally, we don't have to also
// use scheduled polling - temp/humidity changes around the house should get us checking frequently enough - and at more useful times

	subscribe(thermostatOne, "thermostatOperatingState", tHandler)
	if (thermostatTwo) { subscribe(thermostatTwo, "thermostatOperatingState", tHandler) }
    
    subscribe(thermometer, "temperature", tempHandler)
    
    if (pollSwitch && pollTstats) {
    	subscribe( pollSwitch, "switch", switchHandler)
//        subscribe( pollSwitch, "switch.off", offHandler)
    }
    
    if (humidControl) {
    	subscribe( humidSensor, "humidity", tempHandler)
//        humidSensor.refresh()
    }
    else if (thermometer.capabilities.toString().contains('Relative Humidity Measurement')) {
       	subscribe(thermometer, "humidity", tempHandler)				// if it has humidity, follow that too because it may change before temperature
    }
    
    if (tempControl) {
    	subscribe(followMe, "heatingSetpoint", tHandler)			// Need to know when these change (night, home, away, etc.)
        subscribe(followMe, "coolingSetpoint", tHandler)
    }
    if (trackTempChanges) {
    	subscribe(trackTempChanges, "temperature", tempHandler)		// other thermometers may send temp change events sooner than the room
    }
    
    if (doorWatch) {
    	if (doorControl == 'Both') {
        	atomicState.managingTemp = true
        }
        else if (doorControl == 'Never') {
        	atomicState.managingTemp = false
        }
        else {
        	checkDoor(doorWatch.currentContact)
        	subscribe(doorWatch, "contact", doorHandler)
        }
    }
    
    String init = "INIT: temp is ${thermometer.currentTemperature}"
    if (tempControl) {
    	init = init + ", target is ${followMe.currentHeatingSetpoint}(H), ${followMe.currentCoolingSetpoint}(C)"
    }
    log.info init
    
    if (humidControl) {
    	log.info "INIT: humidity is ${humidSensor.currentHumidity}%, target is ${targetHumidity}%"
    }
    
    log.info "INIT: vent is ${ventSwitch.currentLevel}% open"
    
    if (doorWatch) {
    	log.info "INIT: door is ${doorWatch.currentContact}"
    }

// Let the event handlers schedule the first check
	runIn( 2, checkOperatingStates, [overwrite: false] )		// but just in case they don't
    
    log.debug "Initialization finished."
}

// Fan just went on or off - schedule a time check ASAP
def switchHandler( evt ) {
	log.trace "switchHandler $evt.displayName $evt.name: $evt.value"
    
    atomicState.timeHandlerLast = true
 
//	def timestamp = state.lastPoll
//	if (!(timestamp instanceof Number)) {
//		if (timestamp instanceof Date) {
//			timestamp = timestamp.time
//		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
//			timestamp = timestamp.toLong()
//		} else {
//			timestamp = 0
//		}
//	}
//	def milliSeconds = new Date().time - timestamp
//   
//    Integer seconds = 0			// Allow time for the tstat to update Ecobee's servers (need more time for off() than on(), it seems)
//    												
//    if (milliSeconds < 180000) {									// 3 minutes, in milliSeconds
//    	seconds = (185-(milliSeconds / 1000)) as Integer	// how many more seconds do we have to wait before we can poll()?
//    }
// 	log.info "msecs: ${milliSeconds}, seconds: ${seconds}"
//    

	Integer seconds = state.minPollSecs
	if (seconds != 0) {
    	log.trace "Scheduling timeHander"
    	runIn( seconds, timeHandler, [overwrite: true] )				// catch the last state after thermostat stops
    }
   	else {
    	log.trace "calling timeHander"
    	timeHandler()
    }
}

def tHandler( evt ) {
	log.trace "tHandler $evt.displayName $evt.name: $evt.value"
	
	if (atomicState.managingTemp) {
    	if (evt.displayName == followMe.displayName) {
            def inRecovery = atomicState.recoveryMode
    		log.trace "tHandler followMe: inRecovery: ${inRecovery}"
        
        	if (evt.name == 'thermostatOperatingState' ) {
            	if (evt.value == 'idle') {
                	if (inRecovery) { 
                        log.info 'Recovery finished - idle'
                    	atomicState.recoveryMode == false
                    }
                }
                else if (evt.value == 'fan only') {
                	if (inRecovery) {
                    	log.info 'Recovery finished - fan only'
                        atomicState.recoveryMode = false
                    }
                }
                else if (evt.value == 'heating') {
                	if (followMe.currentTemperature > followMe.currentHeatingSetpoint.toFloat()) {
                    	if (!inRecovery) {
                        	log.info 'Recovery started - heating'
                        	atomicState.recoveryMode = true
                        }
                    }
                }
                else if (evt.value == 'cooling') {
                	if (followMe.currentTemperature < followMe.currentCoolingSetpoint.toFloat()) {
                    	if (!inRecovery) {
                    		log.info 'Recovery started - cooling'
                        	atomicState.recoveryMode = true
                        }
                    }
                }
            }
            else if (evt.name == 'heatingSetpoint') {
            	atomicState.lastStatus = 'heatingSetpoint changed'				// force a re-check of vent when target changes
            	if (followMe.currentProgramType != 'hold') {					// Some thermostats report manual hold temps as both heat&cool (e.g., EcoBee)
                	if (followMe.currentThermostatMode.contains('eat')) {		// Some return 'emergencyHeat', so we just look for 'eat' instead of 'heat'		
            			if (evt.value.toFloat() > atomicState.highHeat) { atomicState.highHeat = evt.value.toFloat() }
                    }
                }
                if (followMe.currentThermostatOperatingState == 'heating') {
            		if (inRecovery) {
                		if (evt.value.toFloat() >= followMe.currentTemperature) {
                    		log.info 'Recovery finished - heating'
                        	atomicState.recoveryMode = false
                        }
                    }
                }
            }
            else if (evt.name == 'coolingSetpoint') {
            	atomicState.lastStatus = 'coolingSetpoint changed'				// force recheck
            	if (followMe.currentProgramType != 'hold') {
                	if (followMe.currentThermostatMode.contains('cool')) {
            			if (evt.value.toFloat() < atomicState.lowCool) { atomicState.lowCool = evt.value.toFloat() }
                    }
                }
            	if (followMe.currentThermostatOperatingState == 'cooling') {
            		if (inRecovery) {
                		if (evt.value.toFloat() <= followMe.currentTemperature) {
                    		log.info 'Recovery finished - cooling'
                        	atomicState.recoveryMode = false
                        }
                    }
                }
            }
        }
    }
    log.trace 'Scheduling check'
 	runIn( 2, checkOperatingStates, [overwrite: true] )	// Wait for all the states from thermostat(s) to arrive (e.g. heating/coolingSetpoint arrive separately)
}

def checkOperatingStates() {

	if (atomicState.checking) { 
    	log.trace 'Already checking'
//        if (tempControl) { runIn( 2, checkOperatingStates, [overwrite: true] ) } // don't want to ignore an atomicState or temperature change
        return
    }
    atomicState.checking = true
    log.trace 'Checking started'
    
    def inRecovery = atomicState.recoveryMode
    def priorStatus = atomicState.lastStatus
    def manageTemp = atomicState.managingTemp
	def activeNow = 0
    def stateNow = 'idle'

    def opStateOne = thermostatOne.currentThermostatOperatingState
	if (opStateOne != 'idle') {
    	activeNow = activeNow + 1
        stateNow = opStateOne
    }
    
    def opStateTwo = 'idle'    
    if (thermostatTwo) { opStateTwo = thermostatTwo.currentThermostatOperatingState }
	if (opStateTwo != 'idle') {
    	activeNow = activeNow + 1
       	stateNow = opStateTwo
    }
    
    if (activeNow == 2) {
    	if (opStateOne != opStateTwo) {
        	if (opStateTwo == 'fan only') {
            	stateNow = opStateOne			// if the second tstat is fan only, we want what the first is doing
                activeNow = 1
            }
            else if (opStateOne != 'fan only') { // if neither are 'fan only', then we have a heating/cooling (or cooling/heading) conflict
                stateNow = priorStatus[0..6] 	// let's just assume that the zone controller is still running whatever we were doing last                    
                activeNow = 1				
                if ((stateNow != 'heating') && (stateNow != 'cooling')) { 
                 	stateNow = 'cooling'		// default to the "safe" answer
                    activeNow = 1
                }
                log.info "Heating/Cooling Conflict - assumed ${stateNow}!"
            }
            else {
            	// stateNow = opStateTwo  // opStateOne is fan only, so opStateTwo is the right answer
            }
        }
    }

	log.info "stateNow: $opStateOne $opStateTwo $stateNow, activeNow: $activeNow inRecovery: $inRecovery manageTemp: $manageTemp"
    def currentStatus = "$stateNow $activeNow $inRecovery $manageTemp"
    
	if (currentStatus == priorStatus) {
    	log.trace 'Nothing changed!'
    }
    else {
    	atomicState.lastStatus = currentStatus

		if (atomicState.ventChanged) {					// if we changed the vent last time, poll to make sure it's still set
        	if (pollVent) { ventSwitch.poll() }			// shouldn't need to poll if the vent's device driver reports setLevel updates correctly
            atomicState.ventChanged = false
        }
         
    	log.info "${ventSwitch.displayName} is ${ventSwitch.currentLevel}%, ${thermometer.displayName} temperature is ${thermometer.currentTemperature}"
        
        if (activeNow == 0) {
			atomicState.recoveryMode = false //belt & suspenders
            
            if (thermostatTwo) {			// default to full open if we are acting as purge vent
            	if (!pollTstats || (pollStats && !pollSwitch)) { // but only if we're not using a pollSwitch (which allows us near-instant recognition of fan-on)
                	if (ventSwitch.currentLevel != 99) {
                		ventSwitch.setLevel( 99 )
                    	atomicState.ventChanged = true
                    }
                }
            }
            Integer seconds = state.slowPollSecs
            if (pollTstats) {				// (re)schedule the next timed poll for 6 minutes if we just switched to both being idle
            	if (pollSwitch) {
                	if (pollSwitch.currentSwitch != 'off') { 
                    	pollSwitch.off()
                    }
                    seconds = state.slowPollSecs 			// defaul 10 minute polling if we have a pollSwitch to wake us up
                }
        		runIn( seconds, timeHandler, [overwrite: true] )  	// it is very unlikely that heat/cool will come on in next 8 minutes
			}
		}
    	else if (activeNow == 1) {
      		if (pollTstats) {
            	if (pollSwitch) {
                	if (pollSwitch.currentSwitch != 'on') { 
                    	pollSwitch.on()
                    }
                }
            }
            if (stateNow == 'cooling') {
            	def coolLevel = 0
            	if (thermostatTwo) {						// if we're monitoring two zones (on the same system)
                	coolLevel = 99							// we always have to dump extra cold air when only 1 zone open
                }
                else {
                    if (manageTemp || humidControl) {						// if only 1 Tstat, we manage to the target temperature & humidity
                    	coolLevel = minVent
                		Float coolSP = followMe.currentCoolingSetpoint.toFloat()
                        log.info "target: ${coolSP}"
                        if (coolSP < atomicState.lowCool) { atomicState.lowCool = coolSP }	// another belt & suspenders (in case we are in Auto mode)
                        
                    	if (inRecovery) {
                        	if (thermometer.currentTemperature.toFloat() > atomicState.lowCool) { coolLevel = 99 }
                        }
                        else {
                        	if (thermometer.currentTemperature.toFloat() > coolSP) {
                            	coolLevel = 99 
                            }
                            else {
                            	if (humidControl) {
                                	def humidity = humidSensor.currentHumidity
                                    if (humidity) {									// currentHumidity inexplicably returns null sometimes
                                		if (humidity > targetHumidity) {
                                    		if (thermometer.currentTemperature.toFloat() > (coolSP - maxOverCool)) {	// don't over-cool!
                                    			log.info "Overriding temperature to reduce humidity ($humidity)"
                                    			coolLevel = 99		// Use cooling to reduce humidity
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
    			if ( ventSwitch.currentLevel != coolLevel ) {
        			log.info "Cooling, ${coolLevel}% vent"
        			ventSwitch.setLevel(coolLevel as Integer)
                    atomicState.ventChanged = true
                }
        	}
            else if (stateNow == 'heating') {
            	def heatLevel = 0
                if (manageTemp) {
                	heatLevel = minVent
                	Float heatSP = followMe.currentHeatingSetpoint.toFloat()
                    Float hiHeat = atomicState.highHeat
                	log.info "target: ${heatSP}, ${hiHeat}"
                    if (heatSP > hiHeat) { hiHeat = heatSP }
                    
                    if (inRecovery) {
                    	if (thermometer.currentTemperature.toFloat() < hiHeat) { heatLevel = 99 }
                    }
                    else {
                		if (thermometer.currentTemperature.toFloat() < heatSP) { heatLevel = 99 }
                    }
                    if (atomicState.highHeat != hiHeat) { atomicState.highHeat = hiHeat }
                }
    			if (ventSwitch.currentLevel != heatLevel) {
        			log.info "Heating, ${heatLevel}% vent"
        			ventSwitch.setLevel(heatLevel as Integer)
                    atomicState.ventChanged = true
        		}  		
            }
            else if (stateNow == 'fan only') {
            	def fanLevel = minVent
                if (thermostatTwo) { fanLevel = 99 }
               	if (manageTemp) {
                	def priorState = priorStatus[0..6]
                	if (priorState == 'cooling') {
                    	if (thermometer.currentTemperature.toFloat() > followMe.currentCoolingSetpoint.toFloat()) { 
                        	fanLevel = 99 			 // refresh the air if only managing 1 zone/room and we aren't already cold enough!
                    	}
                    }
                    else if (priorState == 'heating') {
                       	if (thermometer.currentTemperature.toFloat() < followMe.currentHeatingSetpoint.toFloat()) { 
                        	fanLevel = 99 			 // refresh the air if only managing 1 zone/room and we aren't already warm enough! 
                        }
                    }
                }
     			if ( ventSwitch.currentLevel != fanLevel ) {
        			log.info "Fan Only, ${fanLevel}% vent"
        			ventSwitch.setLevel(fanLevel as Integer)
                    atomicState.ventChanged = true
                }
            }
        }
    	else if (activeNow == 2) {
            if (pollTstats) {				
            	if (pollSwitch) {
                	if (pollSwitch.currentSwitch != 'on') { 
                    	pollSwitch.on()
                    }
                }
            }
			if (stateNow == 'cooling') {
            	def coolLevel = minVent				// no cooling unless we're managing the temperature
                if (manageTemp) {
                    log.info "target: ${followMe.currentCoolingSetpoint.toFloat()}"
                	if (inRecovery) {
                       	if (thermometer.currentTemperature.toFloat() > atomicState.lowCool) { coolLevel = 99 }
                    }
                    else {
                   		if (thermometer.currentTemperature.toFloat() > followMe.currentCoolingSetpoint.toFloat()) { coolLevel = 99 }
                    }
                }
                if (humidControl && (coolLevel != 99)) {
                	def humidity = humidSensor.currentHumidity
                    if (humidity) {						// currentHumidity inexplicably returns null sometimes
                    	if (humidity > targetHumidity) {
                            if (thermometer.currentTemperature.toFloat() > (coolSP - maxOverCool)) {	// don't over-cool!
                    			log.info "Overriding temperature to reduce humidity ($humidity)"
                        		coolLevel = 33		// Use A LITTLE cooling to reduce humidity when we are in Purge mode
                            }
                        }
                    }
                }
    			if ( ventSwitch.currentLevel != coolLevel ) {
        			log.info "Dual cooling, ${coolLevel}% vent"
        			ventSwitch.setLevel(coolLevel as Integer)
                    atomicState.ventChanged = true
        		}
            }
            else if (stateNow == 'heating') {
            	def heatLevel = minVent				// no heating unless we're managing the temperature
                if (manageTemp) {
                    log.info "target: ${followMe.currentHeatingSetpoint.toFloat()}"
                    if (inRecovery) {
                    	if (thermometer.currentTemperature.toFloat() < atomicState.highHeat) { heatLevel = 99 }
                    }
                    else {
                		if (thermometer.currentTemperature.toFloat() < followMe.currentHeatingSetpoint.toFloat()) { heatLevel = 99 }
                    }
                }
    			if ( ventSwitch.currentLevel != heatLevel ) {
        			log.info "Dual heating, ${heatLevel}% vent"
        			ventSwitch.setLevel(heatLevel as Integer)
                    atomicState.ventChanged = true
        		}  		
            }
            else if (stateNow == 'fan only') {
            	def fanLevel = minVent				// no fan unless we're managing the temperature (even then only a little)
                if (manageTemp) { fanLevel = 33 }
     			if ( ventSwitch.currentLevel != fanLevel ) {
        			log.info "Dual fan only, ${fanLevel}% vent"
        			ventSwitch.setLevel(fanLevel as Integer)
                    atomicState.ventChanged = true
                }
            }
    	}
    }
    atomicState.checking = false
    log.trace 'Checking finished'
}


def tempHandler(evt) {
	log.trace "tempHandler $evt.displayName $evt.name: $evt.value"
    
    Integer pollFreq = state.fastPollSecs		// Minimum poll is 3 minutes - we add a little to accomodate network delays
    
    atomicState.timeHandlerLast = false

// Limit polls to no more than 1 per $pollFreq (3) minutes (if required: Nest & Ecobee require, native Zwave typically don't)
	if (pollTstats && secondsPast( state.lastPoll, pollFreq)) {
//    	if ((!pollSwitch) || (pollSwitch && (pollSwitch.currentSwitch == 'on'))) {
    		log.trace 'tempHandler polling'
        	pollThermostats() 
        	runIn( pollFreq, timeHandler, [overwrite: true] )	// schedule a timed poll in no less than 5 minutes after this one
//        }
    }

// if we are managing the temperature or humidity, check if we've reached the target when either changes
    if (atomicState.managingTemp) { 									
    	if ((evt.displayName == thermometer.displayName) && (evt.name == 'temperature')) {
        	atomicState.lastStatus = 'temperature changed'		// force a vent adjustment, if necessary
            log.trace 'Scheduling check (temp)'
        	runIn( 2, checkOperatingStates, [overwrite: true] )
        }
    }
    else if (humidControl) {
    	if ((evt.displayName == humidSensor.displayName) && (evt.name == 'humidity')) {
            atomicState.lastStatus = 'humidity changed'
            log.trace 'Scheduling check (humid)'
            runIn( 2, checkOperatingStates, [overwrite: true] )
        }
    }
}

def timeHandler() {
    log.trace 'timeHandler polling'
        
    if (atomicState.timeHandlerLast) {
	   	if (atomicState.checking) {
        	log.debug 'Checking lockout - resetting!'
   			atomicState.checking = false			// hack to ensure we do get locked out by a missed state change (happens)
        }
        atomicState.timeHandlerLast = false			// essentially, if timehandler initiates the poll 2 times in a row while checking=true, reset chacking
    }
    else {
     	if (atomicState.checking) {
            atomicState.timeHandlerLast = true
        }
    }
        
    if (pollTstats) { 
        pollThermostats()
        
        Integer delayPoll = state.slowPollSecs					// Minimum poll time is 3 minutes
        	
        if (thermostatOne.currentThermostatOperatingState == 'idle') {
        	if (thermostatTwo) {
            	if (thermostatTwo.currentThermostatOperatingState == 'idle') {   
                    delayPoll = delayPoll * 2					// both are idle - long wait (unless something changes)
                }
            }
            else {
            	delayPoll = delayPoll * 2					// no thermostatTwo and thermostatOne is idle - long wait
            }
        }
        if (pollSwitch) {
        	if (pollSwitch.currentSwitch != 'on') { 
            	return 
          	}	// pollSwitch is off - stop scheduling polls
            else {
            	delayPoll = state.slowPollSecs
            }
        }
        runIn( delayPoll, timeHandler, [overwrite: true] )  // schedule the next poll
 	
    }
}

def doorHandler(evt) {
	log.debug "doorHandler $evt.displayName $evt.name: $evt.value"
    
    def before = atomicState.managingTemp
    checkDoor( evt.value )
    
    if (before != atomicState.managingTemp) { 				// did it change?
    	log.trace "doorHandler managingTemp changed: ${atomicState.managingTemp}, scheduling Check"
    	runIn( 10, checkOperatingStates, [overwrite: true] )	// delay checking because it just may be someone entering/leaving
    }
}

def checkDoor(contact) {

	switch( doorControl ) {
    	case 'Open':
        	if (contact == 'open') {
            	atomicState.managingTemp = true
            }
            else {
            	atomicState.managingTemp = false
            }
        	break
        case 'Closed':
        	if (contact == 'closed') {
            	atomicState.managingTemp = true
            }
            else {
            	atomicState.managingTemp = false
            }
        	break
        case 'Both':				// Shouldn't happen (we didn't subscribe in this case)
        	atomicState.managingTemp = true
        	break
        case 'Never':				// Ditto
        	atomicState.managingTemp = false
        	break
        default:
        	break
    }
}

def pollThermostats() {
	if (pollTstats) {
    	thermostatOne.poll()
    	if (thermostatTwo) { thermostatTwo.poll() }	// Can be used for single-zone vent control also
    	atomicState.lastPoll = new Date().time
    }
    else {
    	atomicState.lastPoll = 0
    }
}

//check last message so thermostat poll doesn't happen all the time
private Boolean secondsPast(timestamp, seconds) {
	if (!(timestamp instanceof Number)) {
		if (timestamp instanceof Date) {
			timestamp = timestamp.time
		} else if ((timestamp instanceof String) && timestamp.isNumber()) {
			timestamp = timestamp.toLong()
		} else {
			return true
		}
	}
	return (new Date().time - timestamp) > (seconds * 1000)
}