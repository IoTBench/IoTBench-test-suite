/**
 *  Keep Me Cozy
 *
 *  Author: SmartThings
 */
preferences {
	section("Choose thermostat... ") {
		input "thermostat", "capability.thermostat"
	}
	section("Heat setting...") {
		input "heatingSetpoint", "number", title: "Degrees Fahrenheit?"
	}
	section("Air conditioning setting..."){
		input "coolingSetpoint", "number", title: "Degrees Fahrenheit?"
	}
}

def installed()
{
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostat, "temperature", temperatureHandler)
	subscribe(location)
	subscribe(app)
}

def updated()
{
	unsubscribe()
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostat, "temperature", temperatureHandler)
	subscribe(location)
	subscribe(app)
}

def heatingSetpointHandler(evt)
{
	log.debug "heatingSetpoint: $evt, $settings"
}

def coolingSetpointHandler(evt)
{
	log.debug "coolingSetpoint: $evt, $settings"
}

def temperatureHandler(evt)
{
	log.debug "currentTemperature: $evt, $settings"
}

def changedLocationMode(evt)
{
	log.debug "changedLocationMode: $evt, $settings"

	thermostat.setHeatingSetpoint(heatingSetpoint)
	thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

def appTouch(evt)
{
	log.debug "appTouch: $evt, $settings"

	thermostat.setHeatingSetpoint(heatingSetpoint)
	thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

// catchall
def event(evt)
{
	log.debug "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}
