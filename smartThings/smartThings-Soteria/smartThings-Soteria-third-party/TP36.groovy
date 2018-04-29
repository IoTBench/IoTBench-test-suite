/**
 *  Thermostat Mode: Heat/Cool/Auto/Off + Thermostat Fan On/Auto V-Switch 
 *
 *  Copyright 2015 Darc Ranger
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
 *	Version History
 *		V1.0 2015-07-15: Combined Thermostat Mode control with Thermostat Fan control in one app
 *		V1.1 2015-07-15: Add Operation State to AppTouch PushNotification
 *
 *
 */
definition(
    name: "Thermostat Mode/Fan:V-Switches v1.1",
    namespace: "DR",
    author: "DarcRanger",
    description: "App used primarily with dashboards,like SmartTiles.  This fills the void of not have control of the Thermostat Modes and Thermostat Fan from the dashboard. This App uses virtual switches to change the Thermostat Mode from HEAT/COOL/AUTO/OFF and also turns the Thermostat Fan from on to auto and back. Thermostat/V-Switch updates activity between them.",
    category: "Green Living",
    //iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    //iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",
    //iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png",)
   	iconUrl: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",
    iconX3Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/Science-Temperature-icon.png",)
    //oauth: true)


preferences {

	section("Change the Thermostat Mode by selected switches...") {
	 input "HEAT", "capability.switch", multiple: false,  title: "This Switch will turn on/off the HEAT...", required: false
	 input "COOL", "capability.switch", multiple: false,  title: "This Switch will turn on/off the COOL...", required: false
	 input "AUTO", "capability.switch", multiple: false,  title: "This Switch will turn on/off the AUTO...", required: false
	}

	section("Select Switch to change between Thermostat-Fan ON/AUTO...") {
	 input "master", "capability.switch", multiple: false, title: "Switch button is trigged...", required: false
	}
    
    section("Choose thermostat(s)") {
     input "thermostat", "capability.thermostat", multiple: false
    }
    
    section("Notify me...") {
     input "pushNotification_MODE", "bool", title: "Thermostat Mode change with Push Notification", required: false, defaultValue: "false"
     input "pushNotification_FAN", "bool", title: "Thermostat Fan change with Push Notification", required: false, defaultValue: "false"
    }
    
    section ("App Name (optional, defaults to Thermostat Name)...") {
     label title: "Rename App Option", required: false, defaultValue: "${thermostat} - "
     }
}


def installed() {
log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated(){
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize()  {
   

  subscribe(HEAT, "switch.on", HswitchHandler, [filterEvents: false])
  subscribe(COOL, "switch.on", switchHandler, [filterEvents: false])
  subscribe(AUTO, "switch.on", AswitchHandler, [filterEvents: false])
  
  subscribe(HEAT, "switch.off", offHandler, [filterEvents: false])
  subscribe(COOL, "switch.off", offHandler, [filterEvents: false])
  subscribe(AUTO, "switch.off", offHandler, [filterEvents: false])
  
    subscribe(thermostat, "thermostatMode.Heat", thermostatHHandler)
    subscribe(thermostat, "thermostatMode.Cool", thermostatCHandler)
    subscribe(thermostat, "thermostatMode.Auto", thermostatAHandler)
    subscribe(thermostat, "thermostatMode.Off", thermostatOHandler)


    subscribe(master, "switch.on", "onSwitchHandler", [filterEvents: false])
    subscribe(master, "switch.off", "offSwitchHandler", [filterEvents: false])

    subscribe(thermostat, "thermostatFanMode.fanOn", thermostatOnHandler)
    subscribe(thermostat, "thermostatFanMode.fanAuto", thermostatAutoHandler)
    
    subscribe(app, appTouch)
}

//import groovy.time.TimeCategory

//  Thermostat Mode

def HswitchHandler(evt) {
        log.debug ""
     log.debug "S-HEAT---------------------------------------------"

   log.info "switchHandler Event Value: ${evt.value}" //  which event fired is here
   log.info "switchHandler Event Name: ${evt.name}"   //  name of device firing it here
  
    def TMHeat = HEAT.currentValue("switch")
    def TMCool = COOL.currentValue("switch")
    def TMAuto = AUTO.currentValue("switch")
    /*log.debug "LHEAT-Mode: TM-Heat: ${TMHeat} TM-Cool: ${TMCool} TM-Auto: ${TMAuto}"*/
 if (TMHeat == "on" ) {    
    def ThermoMode = thermostat.currentValue("thermostatMode")
    
		log.debug "ThermoMode-Status: $ThermoMode" 

     COOL.off()
     AUTO.off()
     thermostat.setThermostatMode("heat")
    
    def TMHeat2 = HEAT.currentValue("switch")
    def TMCool2 = COOL.currentValue("switch")
    def TMAuto2 = AUTO.currentValue("switch")
       log.debug " HEAT-Mode: TM-Heat: ${TMHeat2} TM-Cool: ${TMCool2} TM-Auto: ${TMAuto2}"
    }
    }
   
def switchHandler(evt) {
        log.debug ""
     log.debug "S-COOL---------------------------------------------"

   log.info "switchHandler Event Value: ${evt.value}" //  which event fired is here
   log.info "switchHandler Event Name: ${evt.name}"   //  name of device firing it here

    def TMHeat = HEAT.currentValue("switch")
    def TMCool = COOL.currentValue("switch")
    def TMAuto = AUTO.currentValue("switch")
    /*log.debug "LCOOL-Mode: TM-Heat: ${TMHeat} TM-Cool: ${TMCool} TM-Auto: ${TMAuto}"*/
   if (TMCool == "on") {    
    def ThermoMode = thermostat.currentValue("thermostatMode")
    
		log.debug "ThermoMode-Status: $ThermoMode" 
   
     HEAT.off()
     AUTO.off()
     thermostat.setThermostatMode("cool")
     
    def TMHeat2 = HEAT.currentValue("switch")
    def TMCool2 = COOL.currentValue("switch")
    def TMAuto2 = AUTO.currentValue("switch")
        log.debug " COOL-Mode: TM-Heat: ${TMHeat2} TM-Cool: ${TMCool2} TM-Auto: ${TMAuto2}"
   }
   }
      
def AswitchHandler(evt) {
        log.debug ""
     log.debug "S-AUTO---------------------------------------------"

   log.info "switchHandler Event Value: ${evt.value}" //  which event fired is here
   log.info "switchHandler Event Name: ${evt.name}"   //  name of device firing it here

    
    def TMHeat = HEAT.currentValue("switch")
    def TMCool = COOL.currentValue("switch")
    def TMAuto = AUTO.currentValue("switch")
    /*log.debug "LAUTO-Mode: TM-Heat: ${TMHeat} TM-Cool: ${TMCool} TM-Auto: ${TMAuto}"*/
 if (TMAuto == "on") {    
    def ThermoMode = thermostat.currentValue("thermostatMode")
    
		log.debug "ThermoMode-Status: $ThermoMode" 

     COOL.off()
     HEAT.off()
     thermostat.setThermostatMode("auto")
     
    def TMHeat2 = HEAT.currentValue("switch")
    def TMCool2 = COOL.currentValue("switch")
    def TMAuto2 = AUTO.currentValue("switch")
   log.debug " AUTO-Mode: TM-Heat: ${TMHeat2} TM-Cool: ${TMCool2} TM-Auto: ${TMAuto2}"
   }
   }
   
def offHandler(evt) {
        log.debug ""
        log.debug "S-OFF----------------------------------------------"
   log.info "switchHandler Event Value: ${evt.value}" //  which event fired is here
   log.info "switchHandler Event Name: ${evt.name}"   //  name of device firing it here
   
	def ThermoMode = thermostat.currentValue("thermostatMode")
    //log.debug "ThermoMode-Status: $ThermoMode"
	def TMHeat2 = HEAT.currentValue("switch")
    def TMCool2 = COOL.currentValue("switch")
    def TMAuto2 = AUTO.currentValue("switch")
  if (TMAuto2 == "off" && TMHeat2 == "off" && TMCool2 == "off") {    


   //log.debug " OFF-Mode: TM-Off: ${TMHeat2} TM-Cool: ${TMCool2} TM-Auto: ${TMAuto2}"
   
  
   thermostat.setThermostatMode("off")
	log.debug "ThermoMode-OFF Status: $ThermoMode"
    log.debug "OFF-Mode: TM-Off: ${TMHeat2} TM-Cool: ${TMCool2} TM-Auto: ${TMAuto2}"
   }
}

def thermostatHHandler(evt) {
        log.debug ""
        log.debug "------T-HEAT----"
       //log.debug "$evt.value"
 
   log.info "ModetHandler Event Value-Heat: ${evt.value}" //  which event fired is here
   log.info "ModeHandler Event Name: ${evt.name}"   //  name of device firing it here
   
	HEAT.on()
	def Thermofan = thermostat.currentValue("thermostatMode")
      log.debug "ThermoMode-Heat: $Thermofan" //+ " Auto: $ThermofanA"+ " ON: $ThermofanO"

		Notification()
   }
   
         
def thermostatCHandler(evt) {

        log.debug ""
        log.debug "------T-COOL----"
       //log.debug "$evt.value"
 
	log.info "ModetHandler Event Value-Cool: ${evt.value}" //  which event fired is here
	log.info "ModeHandler Event Name: ${evt.name}"   //  name of device firing it here

	COOL.on()            
	def Thermofan = thermostat.currentValue("thermostatMode")
      log.debug "ThermoMode-Cool: $Thermofan" //+ " Auto: $ThermofanA"+ " ON: $ThermofanO"

		Notification()
   }
   
   def thermostatAHandler(evt) {
   
        log.debug ""
        log.debug "------T-AUTO----"
       //log.debug "$evt.value"
 
	log.info "ModetHandler Event Value-Auto: ${evt.value}" //  which event fired is here
	log.info "ModeHandler Event Name: ${evt.name}"   //  name of device firing it here

	AUTO.on()
  	def Thermofan = thermostat.currentValue("thermostatMode")
      log.debug "ThermoMode-Auto: $Thermofan" //+ " Auto: $ThermofanA"+ " ON: $ThermofanO"

		Notification()
   }
   
def thermostatOHandler(evt) {

        log.debug ""
        log.debug "------T-OFF-----"
       //log.debug "$evt.value"
 
	log.info "ModetHandler Event Value-OFF: ${evt.value}" //  which event fired is here
	log.info "ModeHandler Event Name: ${evt.name}"   //  name of device firing it here
         	//thermostat.setThermostatMode("off")
    AUTO.off()
	COOL.off()
	HEAT.off()
            
	def Thermofan = thermostat.currentValue("thermostatMode")
      log.debug "ThermoMode-OFF: $Thermofan" //+ " Auto: $ThermofanA"+ " ON: $ThermofanO"

		Notification()
   }
   
 def Notification(){
 log.debug "Test ThermoMode-Status: " + thermostat.currentValue("thermostatMode")
          if (pushNotification_MODE) {
            log.debug "Notify ThermoMode-Status: " + thermostat.currentValue("thermostatMode")
            sendPush("ThermoMode-Status: " + thermostat.currentValue("thermostatMode"))
            log.debug "NOTIFY---------------------------------------------"
        	}
}
//-----------------------------------------------------------------------------------------------------------------
//  Thermostat Fan
def onSwitchHandler(evt) {
       // log.debug ""
     log.debug "S-ON-------------------------------------------"
	
   log.info "onSwitchHandler Event Value: ${evt.value}" //  which event fired is here
   log.info "onSwitchHandler Event Name: ${evt.name}"   //  name of device firing it here
    def MasterV = master.currentValue("switch")
    def Thermofan = thermostat.currentValue("thermostatFanMode")
 
		/*log.debug "Thermofan-Status: $Thermofan"*/ 

        if (MasterV =="on" ){
            thermostat.setThermostatFanMode("on")  
            
            }else {
            log.debug "problem" + Thermofan
            }
          

	}
    def offSwitchHandler(evt) {
        //log.debug ""
     log.debug "S-OFF---------------------------------------------"

   log.info "offSwitchHandler Event Value: ${evt.value}" //  which event fired is here
   log.info "offSwitchHandler Event Name: ${evt.name}"   //  name of device firing it here
    def MasterV = master.currentValue("switch")
    def Thermofan = thermostat.currentValue("thermostatFanMode")
 
		/*log.debug "Thermofan-Status: $Thermofan" */

         if (MasterV =="off" ){
            thermostat.setThermostatFanMode("auto")
            
            }else {
            log.debug "problem" + Thermofan
            }  

	}
     
def thermostatOnHandler(evt) {

        log.debug ""
       log.debug "------T-ON-----"
       //log.debug "$evt.value"
   master.on()
   
   log.info "FanHandler Event Value-ON: ${evt.value}" //  which event fired is here
   log.info "FanHandler Event Name: ${evt.name}"   //  name of device firing it here

	 Notification1()
   }
   
         
def thermostatAutoHandler(evt) {

        log.debug ""
        log.debug "------T-OFF-----"
        //log.debug "$evt.value"
	master.off()
    
   log.info "FanHandler Event Value-Auto: ${evt.value}" //  which event fired is here
   log.info "FanHandler Event Name: ${evt.name}"   //  name of device firing it here

	Notification1()
        }
 def Notification1(){
 log.debug "test Thermofan-Status: " + thermostat.currentValue("thermostatFanMode")
          if (pushNotification_FAN) {
            log.debug "Notify Thermofan-Status: " + thermostat.currentValue("thermostatFanMode")
            sendPush("Thermofan-Status: " + thermostat.currentValue("thermostatFanMode"))
        	}
            }
def appTouch(evt) {
	log.debug "appTouch: $evt.value, $evt"
	log.debug "Notify Thermofan-Status: " + thermostat.currentValue("thermostatFanMode")
	log.debug "Notify ThermoMode-Status: " + thermostat.currentValue("thermostatMode")
        log.debug "Notify Thermostat-State: " + thermostat.currentValue("thermostatOperatingState")
        sendPush("ThermoMode-Status: " + thermostat.currentValue("thermostatMode") + "  |Thermostat-State: " + thermostat.currentValue("thermostatOperatingState") + " | Thermofan-Status: " + thermostat.currentValue("thermostatFanMode"))
    }