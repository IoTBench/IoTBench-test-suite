/**
 *  Green Thermostat
 *
 *  Author: lailoken@gmail.com
 *  Date: 2014-05-28
 */

definition(
  name: "Green Smart Thermostat",
  namespace: "",
  author: "lailoken@gmail.com",
  description: "Try and save power by using alternate thermostat profiles for modes: Away, Home and Night automatically. Used for semi-smart thermostats that can handle these ranges.\nUse with Away detection as well as day/night detection apps.",
  category: "Green Living",
  iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo.png",
  iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo@2x.png",
  oauth: true
)

preferences {
  section("Thermostat") {
    input "thermostat", "capability.thermostat"
  }

  section("When home (day)") {
    input "homeHeat",  "decimal", title:"Heat (default 72)", required:false
    input "homeCool",  "decimal", title:"Cool (default 76)", required:false
    input "homeFan",   "enum", title:"Fan Mode (optional)", required:false, multiple:false, metadata:[values:["Auto","On","Circulate"]]
  }
  section("When home (night)") {
    input "nightHeat", "decimal", title:"Heat (default 70)", required:false
    input "nightCool", "decimal", title:"Cool (default 78)", required:false
    input "nightFan",  "enum", title:"Fan Mode (optional)", required:false, multiple:false, metadata:[values:["Auto","On","Circulate"]]
  }
  section("When away") {
    input "awayHeat",  "decimal", title:"Heat (default 50)", required:false
    input "awayCool",  "decimal", title:"Cool (default 85)", required:false
    input "awayFan",   "enum", title:"Fan Mode (optional)", required:false, multiple:false, metadata:[values:["Auto","On","Circulate"]]
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
	subscribe(thermostat, "heatingSetpoint", heatingSetpointHandler)
	subscribe(thermostat, "coolingSetpoint", coolingSetpointHandler)
	subscribe(thermostat, "temperature",     temperatureHandler)
	subscribe(thermostat, "thermostatFanMode", fanModeHandler)
	subscribe(location)
	subscribe(app)
}

def heatingSetpointHandler(evt)
{
	log.info "heatingSetpoint: $evt, $settings"
}

def coolingSetpointHandler(evt)
{
	log.info "coolingSetpoint: $evt, $settings"
}

def temperatureHandler(evt)
{
	log.info "currentTemperature: $evt, $settings"
}

def fanModeHandler(evt)
{
	log.info "currentFanMode: $evt, $settings"
}

def setFanMode(mode) {
  switch(mode) {
    case "Auto":
      log.debug("Fan set to Auto")
      thermostat.setThermostatFanMode('auto')
      //thermostat.fanAuto();
      break
      
    case "On":
      log.debug("Fan set to On")
      thermostat.setThermostatFanMode('on')
      //thermostat.fanOn();
      break
      
    case "Circulate":
      log.debug("Fan set to Circulate")
      thermostat.setThermostatFanMode('circulate')
      //thermostat.fanCirculate();
      break
  }
}

def changedLocationMode(evt)
{
  log.info "changedLocationMode: $evt, $settings"

  def homeHeat  = homeHeat  ?: 72
  def homeCool  = homeCool  ?: 76
  def nightHeat = nightHeat ?: 70
  def nightCool = nightCool ?: 78
  def awayHeat  = awayHeat  ?: 50
  def awayCool  = awayCool  ?: 85

  if ( evt.value == "Home" ) {
    thermostat.setHeatingSetpoint(homeHeat)
    thermostat.setCoolingSetpoint(homeCool)
    setFanMode(homeFan)
  }
  if ( evt.value == "Away" ) {
    thermostat.setHeatingSetpoint(awayHeat)
    thermostat.setCoolingSetpoint(awayCool)
    setFanMode(awayFan)
  }
  if ( evt.value == "Night" ) {
    thermostat.setHeatingSetpoint(nightHeat)
    thermostat.setCoolingSetpoint(nightCool)
    setFanMode(nightFan)
  }

  thermostat.poll()
}

def appTouch(evt)
{
	log.info "appTouch: $evt, $settings"

	//thermostat.setHeatingSetpoint(heatingSetpoint)
	//thermostat.setCoolingSetpoint(coolingSetpoint)
	thermostat.poll()
}

// catchall
def event(evt)
{
	log.info "value: $evt.value, event: $evt, settings: $settings, handlerName: ${evt.handlerName}"
}
