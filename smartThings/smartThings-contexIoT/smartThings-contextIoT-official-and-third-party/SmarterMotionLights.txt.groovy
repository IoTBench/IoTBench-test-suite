definition(
    name: "Smarter Motion Lights",
    namespace: "qwertypo",
    author: "JB",
    description: "Turn your lights on when motion is detected unless you turn them off!",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)



preferences {
	section("When there is movement here:") {
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
    }
    section("Turn ON These Dimmers"){
        input "Dimswitches", "capability.switchLevel", multiple: true, required: false
        //input "BrightLevel", "Integer", title: "Dimmer Level %1-99 (OPTIONAL) Zero for no dimming", required: true, defaultValue: "0"
        input "BrightLevel", "number", title: "Dimmer Level %1-99 (OPTIONAL) Zero for no dimming", required: false, defaultValue: ""
	}
    section("Turn ON Switches"){
		input "switches", "capability.switch", multiple: true, required: false
        }
    section("Unless there has been movement within the past"){
		input "minutes1", "number", title: "Minutes?"
    }
    section("And it is dark (OPTIONAL)") {
		input "LightMeter", "capability.illuminanceMeasurement", title: "Where?", required: false
        input "luminanceLevel", "number", title: "1-1000", defaultValue: "250", required: false
	}
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    initialize()
    
}

def initialize() {
	ResetClock()
    ActivityClock()
    
    // IF MOTION DETECTOR SET SUBSCRIBE TO IT
    if (motion1){
    log.debug "subscribe: motion1"
	subscribe(motion1, "motion.active", motionActiveHandler)
    subscribe(motion1, "motion.inactive", motionInactiveHandler)
    }
    
    // IF LIGHTMETER SET SUBSCRIBE TO IT
    if (LightMeter){
    log.debug "subscribe: LightMeter"
    subscribe(LightMeter, "illuminance", handleLuxChange)
    } 
    
    // IF BrightLevel IS SET, CONTAIN IT TO 0-99
    if (BrightLevel != null) {
    	state.BrightLevel = BrightLevel // as Integer
    	if (state.BrightLevel >= 100) {
    		log.debug "state.BrightLevel >= 100"
    		state.BrightLevel = 99
    	}
    	if (state.BrightLevel <= 0) {
    		log.debug "state.BrightLevel <= 0"
     	   state.BrightLevel = 0
    	}
    }
    
    // IF BrightLevel IS NOT SET, SET IT TO 0
    else {
    	state.BrightLevel = 0
        log.debug "BrightLevel was left empty"
    }
    
    // IF luminanceLevel IS SET, CONTAIN IT TO 0-1000
    if (luminanceLevel != null) {
    	state.luminanceLevel = luminanceLevel // as Integer
   		if (state.luminanceLevel <= 0) {
    		state.luminanceLevel = 0
   		}
    	if (state.luminanceLevel >= 1000) {
    		state.luminanceLevel = 1000
    	}
    }
    
    // IF luminanceLevel IS NOT SET, SET IT TO 250
    else {
        state.luminanceLevel = 250
        log.debug "luminanceLevel was left empty"
    }
    
    log.info "Brightness: $state.BrightLevel Luminance: $state.luminanceLevel"
}
   
def ActivityClock() {
	log.debug "ActivityClock"
    state.threshold = 1000 * 60 * minutes1
    state.elapsed = now() - state.motionEvent
    //state.threshold =  minutes1
    //state.elapsed = (now() - state.motionEvent) / 60000
    state.elapsedMinutes = (now() - state.motionEvent) / 60000
    state.roundElapsed = Math.round(state.elapsedMinutes) 
    //log.debug "ActivityClock: $state.threshold ms Threshold is $minutes1 Minute(s)"
    //log.debug "ActivityClock: $state.elapsed ms Elapsed"
    log.info "ActivityClock: $state.roundElapsed Minutes Elapsed: Threshold is $minutes1 Minute(s)"
}     
def ResetClock() {
	state.motionEvent = now()
    log.trace "ResetClock"
}

def motionActiveHandler(evt) {
	log.info "$evt.name: $evt.value"
    ActivityClock()
    if (state.elapsed >= state.threshold) {
    	log.debug "motionActiveHandler: ${state.elapsedMinutes} Minutes >= ${minutes1} Minute(s) Check Light Sensor"
    	checkLuminance()
        //ResetClock()
    } else {
    	//ResetClock()
    	log.debug "motionActiveHandler: Not enough time has elapsed, do nothing"
    }
} 

def motionInactiveHandler(evt) {
	log.info "$evt.name: $evt.value" 
    ResetClock()
    //ActivityClock()
}

def handleLuxChange(evt){
	if (LightMeter){
    	def lightSensorState = LightMeter.currentIlluminance
		log.info "handleLuxChange: SENSOR = ${lightSensorState}"
    } else {
    	log.debug "handleLuxChange: SENSOR = No Light Meter"
    }
}
	
def checkLuminance() {
	log.debug "checkLuminance"
	if (LightMeter){
        def lightSensorState = LightMeter.currentIlluminance
		log.debug "checkLuminance: SENSOR = ${lightSensorState}"
		if (lightSensorState != null && lightSensorState <= state.luminanceLevel) {
			log.debug "checkLuminance: SENSOR = ${lightSensorState} is <= ${state.luminanceLevel}: Turn On Lights"
			TurnOnLights()
        } else {
        	log.debug "checkLuminance: SENSOR = ${lightSensorState} is >= ${state.luminanceLevel}: Do Not Turn On Lights"
        }
    } else {
    	log.debug "checkLuminance: SENSOR = No Light Meter: TURN ON LIGHTS"
        TurnOnLights()
        }
}

def TurnOnLights() {
	log.debug"TurnOnLights"
    if (switches) {
    	log.trace "TurnOnLights: ${switches} ON"
    	switches.on()
    }
    if (Dimswitches) {
    	if (state.BrightLevel > 0) {
         	log.trace "TurnOnLights: User set Dim Level ${BrightLevel}%: ${Dimswitches} Light ON at ${state.BrightLevel}%"
            Dimswitches?.setLevel(state.BrightLevel)
        } else {
           	log.trace "TurnOnLights: Not set to Dim Level: ${Dimswitches} Light ON"
            Dimswitches.on()
        }
	}
}

