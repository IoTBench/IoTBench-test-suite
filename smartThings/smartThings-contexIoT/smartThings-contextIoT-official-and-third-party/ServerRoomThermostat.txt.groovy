/**
 *  Keep Me Cozy II MOD: Server Room Thermostat
 *
 *  Author: SmartThings
 */


// Automatically generated. Make future change here.
definition(
    name: "Server Room Thermostat",
    namespace: "",
    author: "danielbarak@live.com",
    description: "Save energy on power up and down of compressor by creating an upper threshold and a lower threshold. The unit would should turn on the compressor at the upper threshold then cool to the low ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

preferences() {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
	section("Air conditioning starts at...") {
		input "coolingSetpoint", "decimal", title: "Deg F"
	}
	section("And cools to...") {
		input "coolDownTo", "decimal", title: "Deg F"
	}
	section("Optionally choose temperature sensor to use instead of the thermostat's... ") {
		input "sensor", "capability.temperatureMeasurement", title: "Temp Sensors", required: false
	}
    section("Prevent Tampering During This Mode...") 
    {
		input "aironmode", "mode", title: "Mode?"
	}
	section("Via a push notification and/or an SMS message"){
		input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
		input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, metadata: [values: ["Yes","No"]]
	}
}

def installed()
{
	log.debug "enter installed, state: $state"
	subscribeToEvents()
}

def updated()
{
	log.debug "enter updated, state: $state"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents()
{
	subscribe(location, changedLocationMode)
	if (sensor) {
		subscribe(sensor, "temperature", temperatureHandler)
		subscribe(thermostat, "temperature", temperatureHandler)
		subscribe(thermostat, "thermostatMode", temperatureHandler)
	}
	evaluate()
}

def changedLocationMode(evt)
{
	log.debug "changedLocationMode mode: $evt.value, heat: $heat, cool: $cool"
	evaluate()
}

def temperatureHandler(evt)
{
	evaluate()
}

private evaluate()
{
	if (sensor) {
    	
		def threshold = 1.0
		def tm = thermostat.currentThermostatMode
		def ct = thermostat.currentTemperature
		def currentTemp = sensor.currentTemperature
        def currentState = thermostat.currentThermostatOperatingState
        //sendPush("DanieTest: ${currentState}")
		log.trace("evaluate:, mode: $tm -- temp: $ct, heat: $thermostat.currentHeatingSetpoint, cool: $thermostat.currentCoolingSetpoint -- "  +
			"sensor: $currentTemp, heat: $heatingSetpoint, cool: $coolingSetpoint")
       
       //Check if someone is messing with the system:
       if (location.mode == aironmode) 
		{
       if (tm in ["cool"]) { 
		}//END if (tm in ["cool"]) 
        else{
        sendPush("HEY! SOMEONE TURNED OFF THE AC!! No worries, I'll turn it back on...")
        sendSms(phone,"HEY! SOMEONE TURNED OFF THE AC!! No worries, I'll turn it back on...")
        thermostat?."cool"()
        }//END ELSE    
	
		tm = thermostat.currentThermostatMode
        }//END if (location.mode != aironmode)
		if (tm in ["cool","auto"]) {
			// air conditioner
            
            try{
			if (currentTemp - coolingSetpoint >= threshold) {
				thermostat.setCoolingSetpoint(ct - 2)
				log.debug "thermostat.setCoolingSetpoint(${ct - 2}), ON"
                if(currentState == "idle")
                {
                sendPush("AIR: Turning ON, Server Room: ${currentTemp}F")
                }//END if(currentState == "idle")
			}
			else if (coolDownTo - currentTemp >= threshold && ct - thermostat.currentCoolingSetpoint >= threshold) {
				thermostat.setCoolingSetpoint(ct + 7)
				log.debug "thermostat.setCoolingSetpoint(${ct + 7}), OFF"
                if(currentState == "cooling" || currentState =="pending cool")
                {
                sendPush("AIR: Turning OFF, Server Room: ${currentTemp}F")
                } //END if(currentState == "cooling")
			}
            }//END DRY
            catch(e){log.debug "NO TEMP READING FROM SENSOR"}
		}
        
        //END air conditioner
        
	}
	else {
		
		thermostat.poll()
	}
}

// for backward compatibility with existing subscriptions
def coolingSetpointHandler(evt) {
	log.debug "coolingSetpointHandler()"
}
def heatingSetpointHandler (evt) {
	log.debug "heatingSetpointHandler ()"
}
