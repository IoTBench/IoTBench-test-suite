/**
 *  App Name:   Scene Machine
 *
 *  Author: 	Todd Wackford
 *				twack@wackware.net
 *  Date: 		2013-06-14
 *  Version: 	1.0
 *  
 *  Updated:    2013-07-25
 *  
 *  BF #1		Fixed bug where string null was being returned for non-dimmers and
 *              was trying to assign to variable.
 *  
 *  
 *  This app lets the user select from a list of switches or dimmers and record 
 *  their currents states as a Scene. It is suggested that you name the app   
 *  during install something like "Scene - Romantic Dinner" or   
 *  "Scene - Movie Time". Switches can be added, removed or set at new levels   
 *  by editing and updating the app from the smartphone interface.
 *
 *  Usage Note: GE-Jasco dimmers with ST is real buggy right now. Sometimes the levels
 *              get correctly, sometimes not.
 *              On/Off is OK.
 *              Other dimmers shoud be OK.
 *  
 */

// Automatically generated. Make future change here.
definition(
    name: "Scene Machine",
    namespace: "",
    author: "todd@wackford.net",
    //description: "This app lets the user select from a list of switches or dimmers and record their currents states as a Scene. It is suggested that you name the app during install something like "Scene - Romantic Dinner" or "Scene - Movie Time". Switches can be added, removed or set at new levels by editing and updating the app from the smartphone interface.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Select switches ...") {
		input "switches", "capability.switch", multiple: true
	}
    
    //Uncomment this section below to test/change on the IDE. Smartphone does 
	//not need it.
	
    //section("Record New Scene?") {  	
	//	input "record", "enum", title: "New Scene...", multiple: false, 
	//         required: true, metadata:[values:['No','Yes']]
	//}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(app, appTouch) 
    getDeviceSettings()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    subscribe(app, appTouch)
    
   //if(record == "Yes") //uncomment this line to test/change stuff on the IDE
      getDeviceSettings()
}

def appTouch(evt) {
	log.debug "appTouch: $evt"
	setScene()
}

private setScene() {
log.debug "Setting Scene"

	switches.each {
        def thisSwitch = it
        def id = thisSwitch.id
        log.debug "ID = ${id}"
        state.deviceData.each {
        	if ( it.deviceId == id ) {
            	log.debug "we have a match"
                
            	if ( it.switchState == off ) {
                	thisSwitch."${it.switchState}"() //turn it off
                } else {
                	thisSwitch."${it.switchState}"() //turn it on
                    if (it.deviceType == "color")
                    	thisSwitch.setColor([hex:it.color, level:it.level])
                    if (it.deviceType == "dimmer")
                    	thisSwitch.setLevel(it.level)
                } 
                
            } else {
            	log.debug "no match"
            }
        }
        //def isSelected = state.deviceData.find { deviceId == id }
        //log.debug "isSelected: ${isSelected}"
    
    }	
}

def getColorDeviceSettings(device) {
	def deviceId = device.id
	def deviceType 	= "color"
	def switchState = device.latestValue("switch")
    def level 		= device.latestValue("level")
    def color   	= device.latestValue("color")
    def data = [deviceId   : deviceId,
                deviceType : deviceType,
    			switchState: switchState,
                level 	   : level,
                color	   : color]
    state.deviceData.push(data)     
}

def getDimmerDeviceSettings(device) {
	def deviceId = device.id
	def deviceType 	= "dimmer"
	def switchState = device.latestValue("switch")
    def level 		= device.latestValue("level")
    def data = [deviceId   : deviceId,
                deviceType : deviceType,
    			switchState: switchState,
                level	   : level]
    state.deviceData.push(data)
}

def getSwitchDeviceSettings(device) {
	def deviceId = device.id
	def deviceType 	= "switch"
	def switchState = device.latestValue("switch")
    def data = [deviceId   : deviceId,
                deviceType : deviceType,
    			switchState: switchState]
    state.deviceData.push(data) 
}

private getDeviceSettings() {
log.debug "running it"
	state.deviceData = []
	switches.each {
    	log.debug "IT: ${it}"
    	def thisDevice = it
        //log.debug "thisDevice: ${thisDevice}"
        //log.debug "Display Name: ${thisDevice.displayName}"
        //log.debug "Commands: ${thisDevice.supportedCommands}"
        log.debug "Attributes: ${thisDevice.supportedAttributes}"
        //log.debug "Switch State: ${thisDevice.latestValue('switch')}"
        
        //look for color/hue devices
        if (thisDevice.latestValue("color")) {
        	log.debug "This is a color device: ${thisDevice.latestValue('color')}"
            getColorDeviceSettings(thisDevice)
        //look for dimmers
        } else if ((thisDevice.latestValue("level")) && (!thisDevice.latestValue("color"))) {
        	log.debug "this is a dimmer device"
            getDimmerDeviceSettings(thisDevice)
        //must be switch   
        } else {
        	log.debug "this is a switch device"
            getSwitchDeviceSettings(thisDevice)
		}
    }
    
    log.debug "state device info: ${state.deviceData}"
    switches.off()
}