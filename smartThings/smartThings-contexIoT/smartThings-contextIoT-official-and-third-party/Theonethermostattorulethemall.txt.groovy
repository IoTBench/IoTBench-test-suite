/**
 *  The one thermostat to rule them all
 *
 *  Copyright 2014 Eric Roberts
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
    name: "The one thermostat to rule them all",
    namespace: "baldeagle072",
    author: "Eric Roberts",
    description: "A thermostat for everything in your house",
    category: "Green Living",
    iconUrl: "http://www.williamsmusic.org/images/thermostat.png",
    iconX2Url: "http://www.williamsmusic.org/images/thermostat@2x.png",
    oauth: true
)


preferences {
	page name:"setupInit"
    page name:"setupWelcome"
    page name:"setupConfigure"
    page name:"setupRooms"
    page name:"setupProgram"
    page name:"setupControlPanel"
}

def setupInit() {
	TRACE("setupInit()")
    
    if (state.installed) {
    	return setupControlPanel()
    } else {
    	return setupWelcome()
    }
}


def setupWelcome() {
	TRACE("setupWelcome()")
    def textIntro =
    	"The one thermostat to rule them all is used to control all the heating " +
        "and cooling in your house from one place."
    
    def textNext =
    	"Hit the 'Next' button at the top of the page to continue"
    
    def pageProperties=[
    	name:		"setupWelcome",
        title:		"Welcome!",
        nextPage:	"setupConfigure",
        uninstall:	false
    ]
    
    return dynamicPage(pageProperties) {
    	section("Introduction") {
        	paragraph textIntro
            paragraph textNext
        }
    }
}

def setupConfigure() {
	TRACE("setupConfigure()")
    def helpPage =
    	"Hit the 'Next' button when done."
        
    def helpNumRooms = 
    	"You can have up to 8 rooms. Each room can control an air " +
        "conditioner, a heater, a thermostat, and have a temperature sensor."
    
    def helpModes =
    	"Each mode can have a different set temperature."
        
    def helpSmsNumber = 
    	"If you select text notifications, be sure to type in a phone number here."
        
    def helpSelectNotifications =
    	"Select the types of notifications you would like to recieve."
    
    def inputNumRooms = [
    	name:			"numRooms",
        type:			"number",
        title:			"How many rooms?",
        defaultValue:	"1",
        required: 		true
    ]
    
    def inputHomeModes = [
    	name:			"homeModes",
        type:			"mode",
        title:			"Home temperature for these modes",
        multiple: 		true,
        required:		true
    ]
    
    def inputAwayModes = [
    	name:			"awayModes",
        type:			"mode",
        title:			"Away temperature for these modes",
        multiple: 		true,
        required:		true
    ]
    
    def inputAsleepModes = [
    	name:			"asleepModes",
        type:			"mode",
        title:			"Asleep temperature for these modes",
        multiple: 		true,
        required:		false
    ]
   /* 
	def inputPushNotify = [
    	name:			"pushNotify",
        type:			"bool",
        title:			"Recieve push noifications",
  		defaultValue:	false
    ]
    
    def inputSmsNotify = [
    	name:			"smsNotify",
        type:			"bool",
        title:			"Recieve text noifications",
  		defaultValue:	false
    ]
    
    def inputSmsNumber = [
    	name:			"smsNumber",
        type:			"phone",
        title:			"Phone number to get texts"
    ]
    
    def inputSetTempNotify = [
    	name:			"setTempNotify",
        type:			"bool",
        title:			"Notify when set temperature changes",
  		defaultValue:	false
    ]
    
    def inputOnNotify = [
    	name:			"onNotify",
        type:			"bool",
        title:			"Notify when heater or A/C turn on",
  		defaultValue:	false
    ]
    
    def inputOffNotify = [
    	name:			"offNotify",
        type:			"bool",
        title:			"Notify when heater or A/C turn off",
  		defaultValue:	false
    ]
    */
    def inputApiKey = [
    	name:		"apiKey",
        type:		"string",
        title:		"ThingSpeak API Key"
    ]
    
    def pageProperties = [
    	name:		"setupConfigure",
        title:		"Configure",
        nextPage:	"setupRooms",
        uninstall:	state.installed
    ]
       
    return dynamicPage(pageProperties) {
    	section("Numbers") {
        	paragraph helpNumRooms
            input inputNumRooms
        }
        
        section("Modes") {
        	input inputHomeModes
            input inputAwayModes
            input inputAsleepModes
        }
        
        section("ThingSpeak") {
        	input inputApiKey
        }
        
    /*
        section("Notification") {
            input inputPushNotify
            input inputSmsNotify
            paragraph helpSmsNumber
            input inputSmsNumber
            section {
            	paragraph helpSelectNotifications
                input inputSetTempNotify
                input inputOnNotify
                input inputOffNotify
            }
        }
        */
    }
}

def setupRooms() {
	TRACE("setupRooms()")
    
    def helpName =
    	"Choose a name for the room to easily identify it."
        
    def helpThermostat =
    	"Pick a thermostat that controls either heat and/or air conditioning in this room. " +
        "The thermostat will be controlled by its own temperature sensor."
    
    def helpHeater = 
    	"Pick switches that control a heater in this room."
    
    def helpConditioner =
    	"Pick switches that control an air conditioner in this room."
    
    def helpTemp = 
    	"This device will monitor the temperature and control the separate heater and air conditioners." +
        "A temperature sensor is required. It can be the thermostat you used earlier."
    
    
    def pageProperties=[
    	name:		"setupRooms",
        title:		"Configure Rooms",
        install:	true,
        uninstall:	state.installed
    ]
    
    return dynamicPage(pageProperties) {
        
        for (int n = 1; n <= numRooms; n++) {
           section("Room ${n}", hideable:true, hidden:true) {
                paragraph helpName
                input "r${n}_name", "string", title:"Room name", defaultValue:"Room ${n}"
                paragraph helpThermostat
                input "r${n}_thermostat", "capability.thermostat", title:"Which thermostat?", multiple:false, required:false
                paragraph helpHeater
                input "r${n}_heater", "capability.switch", title:"Which heater?", multiple:true, required:false
               	paragraph helpConditioner
                input "r${n}_conditioner", "capability.switch", title:"Which air conditioner?", multiple:true, required:false
               	paragraph helpTemp
                input "r${n}_temp", "capability.temperatureMeasurement", title:"Which temperature sensor?", multiple:false, required:true
             
           }
        }
    }
}

def setupProgram() {
	TRACE("setupProgram()")
    
    def pageProperties=[
    	name:		"setupProgram",
        title:		"Program",
        install:	true,
        uninstall:	state.installed
    ]
    
    return dynamicPage(pageProperties) {
    
    	def helpProgramPage = "Press 'Done' at the top to set the new program"
        
        section {
        	paragraph helpProgramPage
        }
        for (int n = 0; n < state.numRooms; n++) {
        	
            def r = n + 1
            
            def room = state.rooms[n]
            
            def homeSetTemp = room.homeSetTemp
            def awaySetTemp = room.awaySetTemp
            def asleepSetTemp = room.asleepSetTemp
            def homeRun = room.homeRun
            def awayRun = room.awayRun
            def asleepRun = room.asleepRun
            def time1 = room.time1
            def time2 = room.time2
            def time3 = room.time3
            def timeSetTemp1 = room.timeSetTemp1
            def timeSetTemp2 = room.timeSetTemp2
            def timeSetTemp3 = room.timeSetTemp3
            def timeRun1 = room.timeRun1
            def timeRun2 = room.timeRun2
            def timeRun3 = room.timeRun3
            
            def helpModeChange = "Set these temperature to change the next time it enters that mode."
            
            def inputHomeSetNewTemp = [
                name:           "r${r}_homeSetTemp",
                type:           "number",
                title:          "Set Home Temperature",
                defaultValue:   homeSetTemp,
                required:       true
            ]
            
            def inputAwaySetNewTemp = [
                name:           "r${r}_awaySetTemp",
                type:           "number",
                title:          "Set Away Temperature",
                defaultValue:   awaySetTemp,
                required:       true
            ]
            
            def inputAsleepSetNewTemp = [
                name:           "r${r}_asleepSetTemp",
                type:           "number",
                title:          "Set Asleep Temperature",
                defaultValue:   asleepSetTemp,
                required:       true
            ]
            
            def inputHomeRun = [
                name:           "r${r}_homeRun",
                type:           "bool",
                title:          "Run home",
                defaultValue:   homeRun,
                required:       true
            ]	
            
            def inputAwayRun = [
                name:           "r${r}_awayRun",
                type:           "bool",
                title:          "Run away",
                defaultValue:   awayRun,
                required:       true
            ]
            
            def inputAsleepRun = [
                name:           "r${r}_asleepRun",
                type:           "bool",
                title:          "Run asleep",
                defaultValue:   asleepRun,
                required:       true
            ]
            
            section(room.name) {
            	paragraph helpModeChange
            	input inputHomeSetNewTemp
                input inputHomeRun
                input inputAwaySetNewTemp
                input inputAwayRun
                input inputAsleepSetNewTemp
                input inputAsleepRun
            }
            
            def helpTimeChange = "Set these to change the set temperature at specific times every day."
             
            def inputTime1 = [
                name:           "r${r}_time1",
                type:           "time",
                title:          "Set time for time 1",
                defaultValue:   time1,
                required:       false
            ]
            
            def inputTime2 = [
                name:           "r${r}_time2",
                type:           "time",
                title:          "Set time for time 2",
                defaultValue:   time2,
                required:       false
            ]
            
            def inputTime3 = [
                name:           "r${r}_time3",
                type:           "time",
                title:          "Set time for time 3",
                defaultValue:   time3,
                required:       false
            ]
            
            def inputTimeSetTemp1 = [
                name:           "r${r}_timeSetTemp1",
                type:           "number",
                title:          "Set temperature for time 1",
                defaultValue:   timeSetTemp1,
                required:       false
            ]
            
            def inputTimeSetTemp2 = [
                name:           "r${r}_timeSetTemp2",
                type:           "number",
                title:          "Set temperature for time 2",
                defaultValue:   timeSetTemp2,
                required:       false
            ]
            
            def inputTimeSetTemp3 = [
                name:           "r${r}_timeSetTemp3",
                type:           "number",
                title:          "Set temperature for time 3",
                defaultValue:   timeSetTemp3,
                required:       false
            ]
            
            def inputTimeRun1 = [
                name:           "r${r}_timeRun1",
                type:           "bool",
                title:          "Run time 1",
                defaultValue:   timeRun1,
                required:       false
            ]
            
            def inputTimeRun2 = [
                name:           "r${r}_timeRun2",
                type:           "bool",
                title:          "Run time 2",
                defaultValue:   timeRun2,
                required:       false
            ]
            
            def inputTimeRun3 = [
                name:           "r${r}_timeRun3",
                type:           "bool",
                title:          "Run time 3",
                defaultValue:   timeRun3,
                required:       false
            ]
            
           	section("Set at specific times", hideable:true, hidden:true) {
           		paragraph helpTimeChange
            	input inputTime1
                input inputTimeSetTemp1
                input inputTimeRun1
                
                input inputTime2
                input inputTimeSetTemp2
                input inputTimeRun2
                
                input inputTime3
                input inputTimeSetTemp3
                input inputTimeRun3
            } 
        }
    }
}

def setupControlPanel() {
	TRACE("setupControlPanel()")
    
    def pageProperties=[
    	name:		"setupControlPanel",
        title:		"Control Panel",
        install:	true,
        uninstall:	true
    ]
    
    return dynamicPage(pageProperties) {
    	def helpControlPanel = "Press 'Done' at the top to set the new temperature"
    
    	section {
            paragraph helpControlPanel
        }
        for (int n = 0; n < state.numRooms; n++) {
        	
            def r = n + 1
            
            def room = state.rooms[n]
            def devices = getRoomDevices(n)
            
            def setTemp = room.setTemp
            def setMode = room.setMode
            
            def currentTemp = devices.tempMonitor.currentTemperature
            
            //log.debug(currentTemp)
            
            
            def textCurrentTemp = "Current Temperature: ${currentTemp}"
            
            def inputSetNewTemp = [
                name:           "r${r}_setTemp",
                type:           "number",
                title:          "Set Temperature",
                defaultValue:   setTemp,
                required:       true
            ]
            
            def inputSetNewMode = [
                name:           "r${r}_setMode",
                type:           "enum",
                title:          "Mode",
                metadata:       [values:["heat", "cool", "off"]],
                defaultValue:   setMode,
                required:       true
            ]
            
            
            
            section(room.name) {
            	paragraph textCurrentTemp
                input inputSetNewTemp
                input inputSetNewMode
            }
        }
        
        section {
            href "setupConfigure", title:"Setup system", description:"Tap to open"
            href "setupRooms", title:"Configure rooms", description:"Tap to open"
            href "setupProgram", title:"Set program", description:"Tap to open"
        }
    }
}

def installed() {
	TRACE("installed()")
    state.installed = true
	initialize()
}

def updated() {
	TRACE("updated()")
    unschedule()
	//unsubscribe()
	initialize()
}

def initialize() {
	TRACE("initialize()")
    log.debug "settings: ${settings}"
    
    // set global settings
    
    state.numRooms 		= settings.numRooms.toInteger()
   
   	state.homeModes 	= settings.homeModes
    state.awayModes 	= settings.awayModes
    state.asleepModes 	= settings.asleepModes
   /* 
    state.pushNotify 	= settings.pushNotify
    state.smsNotify 	= settings.smsNotify
    state.smsNumber 	= settings.smsNumber
    state.setTempNotify = settings.setTempNotify
    state.onNotify 		= settings.onNotify
    state.offNotify 	= settings.offNotify
    */
    state.rooms = []
    
    // set rooms
    
    for (int n = 0; n < state.numRooms; n++) {
    	state.rooms[n] = roomInit(n)
        makeSubscriptions(n)
        onOrOffCheck(n)
        setThermostat(n)
    }
    
    // set for current mode and subscribe to mode changes
    
    schedule("0 * * * * ?", "checkSchedule")
    
    subscribe(location, onLocation)  

    if (!state.accessToken) {
        createAccessToken()
    }
    getURL(null)
}

def getURL(e) {
    log.debug("getURL")
    for (int n = 0; n < state.numRooms; n++) {
        def url = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/ui/${n}?access_token=${state.accessToken}"
        log.debug " url: $url"
    }
}

private def roomInit(n) {
	def r = n + 1
    
    def room = state.rooms[n]
    
    def handlers = [onRoom1, onRoom2, onRoom3, onRoom4, onRoom5, onRoom6, onRoom7, onRoom8]
    def thermHandlers = [thermRoom1, thermRoom2, thermRoom3, thermRoom4, thermRoom5, thermRoom6, thermRoom7, thermRoom8]
    
    if (room == null) {
    	room = [:]}
    room.name			= settings."r${r}_name"
    
    if (settings."r${r}_homeSetTemp" == null) {
    	room.homeSetTemp = 72
    } else {
    	room.homeSetTemp = settings."r${r}_homeSetTemp"
    }
    
    if (settings."r${r}_awaySetTemp" == null) {
    	room.awaySetTemp = 72
    } else {
    	room.awaySetTemp = settings."r${r}_awaySetTemp"
    }
    
    if (settings."r${r}_asleepSetTemp" == null) {
    	room.asleepSetTemp = 72
    } else {
    	room.asleepSetTemp = settings."r${r}_asleepSetTemp"
    }
    
    if (settings."r${r}_homeRun" == null) {
    	room.homeRun = false
    } else {
    	room.homeRun = settings."r${r}_homeRun"
    }
    
    if (settings."r${r}_awayRun" == null) {
    	room.awayRun = false
    } else {
    	room.awayRun = settings."r${r}_awayRun"
    }
    
    if (settings."r${r}_asleepRun" == null) {
    	room.asleepRun = false
    } else {
    	room.asleepRun = settings."r${r}_asleepRun"
    }
    
    if (settings."r${r}_setTemp" == null) {
    	room.setTemp = 72
    } else {
    	room.setTemp = settings."r${r}_setTemp"
    }
    
    if (settings."r${r}_setMode" == null) {
    	room.setMode = "off"
    } else {
    	room.setMode = settings."r${r}_setMode"
    }
    
    room.time1 = settings."r${r}_time1"
    room.time2 = settings."r${r}_time2"
    room.time3 = settings."r${r}_time3"
    
    if (settings."r${r}_timeSetTemp1" == null) {
    	room.timeSetTemp1 = 72
    } else {
    	room.timeSetTemp1 = settings."r${r}_timeSetTemp1"
    }
    
    if (settings."r${r}_timeSetTemp2" == null) {
    	room.timeSetTemp2 = 72
    } else {
    	room.timeSetTemp2 = settings."r${r}_timeSetTemp2"
    }
    
    if (settings."r${r}_timeSetTemp3" == null) {
    	room.timeSetTemp3 = 72
    } else {
    	room.timeSetTemp3 = settings."r${r}_timeSetTemp3"
    }
    
    if (settings."r${r}_timeRun1" == null) {
    	room.timeRun1 = false
    } else {
    	room.timeRun1 = settings."r${r}_timeRun1"
    }
    
    if (settings."r${r}_timeRun2" == null) {
    	room.timeRun2 = false
    } else {
    	room.timeRun2 = settings."r${r}_timeRun2"
    }
    
    if (settings."r${r}_timeRun3" == null) {
    	room.timeRun3 = false
    } else {
    	room.timeRun3 = settings."r${r}_timeRun3"
    }
    
    room.handler = handlers[n]
    room.thermHandler = thermHandlers[n]
    
    return room
}

def onRoom1(evt) { tempHandler(0) }
def onRoom2(evt) { tempHandler(1) }
def onRoom3(evt) { tempHandler(2) }
def onRoom4(evt) { tempHandler(3) }
def onRoom5(evt) { tempHandler(4) }
def onRoom6(evt) { tempHandler(5) }
def onRoom7(evt) { tempHandler(6) }
def onRoom8(evt) { tempHandler(7) }

def thermRoom1(evt) { thermostatChange(0, evt.value) }
def thermRoom2(evt) { thermostatChange(1, evt.value) }
def thermRoom3(evt) { thermostatChange(2, evt.value) }
def thermRoom4(evt) { thermostatChange(3, evt.value) }
def thermRoom5(evt) { thermostatChange(4, evt.value) }
def thermRoom6(evt) { thermostatChange(5, evt.value) }
def thermRoom7(evt) { thermostatChange(6, evt.value) }
def thermRoom8(evt) { thermostatChange(7, evt.value) }

private def thermostatChange(n, newSetTemp) {
	def room = state.rooms[n]
    def devices = getRoomDevices(n)
    def newThermMode = devices.thermostat.latestValue("thermostatMode")
    
    log.debug "thermostat change"
    
    room.setTemp = newSetTemp
    
    if (newThermMode == "cool") {
    	room.setMode = "cool"
    } else if (newThermMode == "heat") {
    	room.setMode = "heat"
    } else {
    	room.setMode = "off"
    }
    
    onOrOffCheck(n)
}

private def onOrOffCheck(n) {
	TRACE("Checking temperature")
    
    def room = state.rooms[n]
    def devices = getRoomDevices(n)
    def setTemp = room.setTemp.toBigDecimal()
    def currentTemp = devices.tempMonitor.currentTemperature
    
    log.debug("room.name: ${room.name}, setTemp: ${setTemp}, currentTemp: ${currentTemp}")
    
    if (room.setMode == "cool") {
    	if (devices.tempMonitor) {
    		if (currentTemp >= (setTemp + 1)) {
            	devices.conditioner?.on()   
            } else if (currentTemp <= (setTemp - 1)) {
            	devices.conditioner?.off()
            }
    	} else {
        	log.debug("No way to tell temp")
        }
    } else if (room.setMode == "heat") {
    	if (devices.tempMonitor) {
    		if (currentTemp <= (setTemp - 1)) {
            	devices.heater?.on()
            } else if (currentTemp >= (setTemp + 1)) {
            	devices.heater?.off()
            }
    	} else {
        	log.debug("No way to tell temp")
        }
    } else {
    	log.debug("Mode set to off")
        log.debug("turning off ${devices.heater} and ${devices.conditioner}")
        devices.heater?.off()
        devices.conditioner?.off()
    }
    getURL(null)
}

private def setThermostat(n) {
	def room = state.rooms[n]
    def devices = getRoomDevices(n)
    
    def setTemp = room.setTemp
    def setMode = room.setMode
    
    if (devices.thermostat) {
        if (setMode == "heat") {
            devices.thermostat.setHeatingSetpoint(setTemp)
            devices.thermostat.heat()
        } else if (setMode == "cool") {
            devices.thermostat.setCoolingSetpoint(setTemp)
            devices.thermostat.cool()
        } else {
            devices.thermostat.off()
        }
    }
}

private def makeSubscriptions(n) {
	def room = state.rooms[n]
    def devices = getRoomDevices(n)
    
    subscribe(devices.tempMonitor, "temperature", room.handler)
    subscribe(devices.thermostat, "thermostatSetpoint", room.thermHandler)
    subscribe(devices.thermostat, "heatingSetpoint", room.thermHandler)
    subscribe(devices.thermostat, "coolingSetpoint", room.thermHandler)
}

private def checkSchedule() {
	//TRACE("Checking Schedule")
    for (int n = 0; n < state.numRooms; n++) {
        def room = state.rooms[n]
		
        def time1Hour = 0
        def time1Minute = 0
        def time2Hour = 0
        def time2Minute = 0
        def time3Hour = 0
        def time3Minute = 0
        def time1 = null
        def time2 = null
        def time3 = null

        if (room.time1) {
            time1           = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", room.time1)
        }
        if (time1 != null) {
            time1Hour 	= time1.getHours()
            time1Minute	= time1.getMinutes()
            
        }
        if (room.time2) {
            time2           = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", room.time2)
        }
        if (time2 != null) {
            time2Hour 	= time2.getHours()
            time2Minute	= time2.getMinutes()
        }
        if (room.time3) {
            time3           = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", room.time3)
        }
        if (time3 != null) {
            time3Hour 	= time3.getHours()
            time3Minute	= time3.getMinutes()
        }
        def timeSetTemp1	= room.timeSetTemp1
        def timeSetTemp2 	= room.timeSetTemp2
        def timeSetTemp3 	= room.timeSetTemp3
        def timeRun1 		= room.timeRun1
        def timeRun2 		= room.timeRun2
        def timeRun3 		= room.timeRun3
        
        def currentTime 	= new Date(now())
        def currentHour 	= currentTime.getHours()
        def currentMinute	= currentTime.getMinutes()
       
        //TRACE("Time1 Hour: $time1Hour Time1 Minute: $time1Minute timeRun1: $timeRun1")
        //TRACE("currentHour: $currentHour currentMinute: $currentMinute")
        
        if ((time1Hour == currentHour) && (time1Minute == currentMinute) && timeRun1) {
			room.setTemp = timeSetTemp1
            log.debug ("setting time 1 temp")
            onOrOffCheck(n)
            setThermostat(n)
        } else if ((time2Hour == currentHour) && (time2Minute == currentMinute) && timeRun2) {
			room.setTemp = timeSetTemp2
            log.debug ("setting time 2 temp")
            onOrOffCheck(n)
            setThermostat(n)
        } else if ((time3Hour == currentHour) && (time3Minute == currentMinute) && timeRun3) {
			room.setTemp = timeSetTemp3
            log.debug ("setting time 3 temp")
            onOrOffCheck(n)
            setThermostat(n)
        }
    }
}
    
def tempHandler(n) {
	sendValue(n)
	onOrOffCheck(n)
}

def sendValue(n) {
	def devices = getRoomDevices(n)
    def currentTemp = devices.tempMonitor.currentTemperature
    def i = n + 1
    
    
    def url = "https://api.thingspeak.com/update?api_key=${settings.apiKey}&field${i}=${currentTemp}"
    log.debug("Logging to ThingSpeak: $url")
    /*
    def putParams = [
        uri: url,
        body: []
    ]
*/
    httpGet(url) { 
        response -> 
        if (response.status != 200 ) {
            log.debug "ThingSpeak logging failed, status = ${response.status}"
        }
    }
}

def onLocation(evt) {
	TRACE("onLocation(${evt})")
    
    def mode = evt.value
    if (settings.homeModes?.contains(mode)) {
    	for (int n = 0; n < state.numRooms; n++) {
            def room = state.rooms[n]
            if (room.homeRun) {
                room.setTemp = room.homeSetTemp
            }
            onOrOffCheck(n)
            setThermostat(n)
        }
    } else if (settings.awayModes?.contains(mode)) {
    	for (int n = 0; n < state.numRooms; n++) {
            def room = state.rooms[n]
            if (room.awayRun) {
                room.setTemp = room.awaySetTemp
            }
            onOrOffCheck(n)
            setThermostat(n)
        }
    } else if (settings.asleepModes?.contains(mode)) {
    	for (int n = 0; n < state.numRooms; n++) {
            def room = state.rooms[n]
            if (room.asleepRun) {
                room.setTemp = room.asleepSetTemp
            }
            onOrOffCheck(n)
            setThermostat(n)
        }
    }
}

def getRoomDevices(n) {
	if (n >= state.numRooms) {
    	return null
    }
    
    n++
    
    def devices = [:]
    
    devices.thermostat	= settings."r${n}_thermostat"
    devices.heater		= settings."r${n}_heater"
    devices.conditioner	= settings."r${n}_conditioner"
    devices.tempMonitor	= settings."r${n}_temp"
    
    return devices
}

/*
private def notify(message) {
	if (state.pushNotify) {
    	sendPush(message)
    }
    
    if (state.smsNotify) {
    	if (state.smsNumber) {
        	sendSms(state.smsNumber, message)
        } else {
        	log.debug("No SMS number")
        }
    }
}
*/

private def TRACE(message) {
    log.debug message
    //log.debug "state: ${state}"
}

// Endpoint API

mappings {
    
    path("/currentTemp/:room") {
    	action: [GET: "getRoomTemp"]
    }
    
    path("/setTemp/:room/:newSetTemp") {
    	action: [GET: "setRoomTemp"]
    }
    
    path("/setMode/:room/:newSetMode") {
    	action: [GET: "setRoomMode"]
    }

    path("/ui/:room") {
        action: [GET: "html"]
    }
    
}

def getRoomTemp() {
    roomTemp(params.room.toInteger())
}

private roomTemp(n) {
	def room = state.rooms[n]
    def devices = getRoomDevices(n)
    def setTemp = room.setTemp.toInteger()
    def currentTemp = devices.tempMonitor.currentTemperature
    def setMode = room.setMode
    
    [roomNum: n, room: room.name, currentTemp: currentTemp, setTemp: setTemp, setMode: setMode]
}

void setRoomTemp() {
	def n = params.room.toInteger()
    def newSetTemp = params.newSetTemp.toInteger()
	def room = state.rooms[n]
    
    room.setTemp = newSetTemp
    
    onOrOffCheck(n)
}

void setRoomMode() {
	def n = params.room.toInteger()
    def newSetMode = params.newSetMode
    def room = state.rooms[n]
    
    if (newSetMode == "cool") {
    	room.setMode = "cool"
    } else if (newSetMode == "heat") {
    	room.setMode = "heat"
    } else {
    	room.setMode = "off"
    }
    onOrOffCheck(n)
}

def html() {
    render contentType: "text/html", data: "<!DOCTYPE html><html><head>${head(params.room.toInteger())}</head><body>${body(params.room.toInteger())}</body></html>"
}

def head(roomNum) {
    def output = """
    <script>
    
        function changeTemp() {
            
            var url = 'https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/setTemp/${roomNum}/';
            
            var temp = document.getElementById('temp').value;
            
            url += temp;

            url += '?access_token=${state.accessToken}';
            
            document.getElementById('cmd').src=url;
        }
        
        function turnOnHeat() {
            
            var url = 'https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/setMode/0/heat?access_token=${state.accessToken}';
            
            document.getElementById('cmd').src=url;
            document.getElementById('mode').innerHTML = '<h4>Mode: heat</h4>';
        }
        
        function turnOnCool() {
            
            var url = 'https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/setMode/0/cool?access_token=${state.accessToken}';
            
            document.getElementById('cmd').src=url;
            document.getElementById('mode').innerHTML = '<h4>Mode: cool</h4>';
        }
        
        function turnOffThermostat() {
            
            var url = 'https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/setMode/0/off?access_token=${state.accessToken}';
            
            document.getElementById('cmd').src=url;
            document.getElementById('mode').innerHTML = '<h4>Mode: off</h4>';
        }

    </script>
    """
    
    return output
}

private body(roomNum) {
    log.debug("roomNum: $roomNum")
    def room = state.rooms[roomNum]
    def devices = getRoomDevices(roomNum)
    log.debug("numRooms ${state.numRooms}")
    log.debug("devices $devices")
    def output = 
        "<h3>Thermostat for ${room.name}</h3>" +
        "<form>" +
            "<label name='temp'>Change Temperature: </label>" +
            "<input type='text' id='temp' name='temp' size='3' value='${room.setTemp}'><br>" +
            "<input type='button' value='Heat' onclick='turnOnHeat();'>" +
            "<input type='button' value='Cool' onclick='turnOnCool();'>" +
            "<input type='button' value='Change Temperature' onclick='changeTemp();'>" +
            "<input type='button' value='Off' onclick='turnOffThermostat();'><br>" +
            "<div id='currentTemp'><h3>Current Temperature: ${devices.tempMonitor.currentTemperature} F</h3></div>" +
            "<div id='mode'><h4>Mode: ${room.setMode}</h4></div>" +
        "</form>" +
		" " +
        "<iframe name='cmd' id='cmd' style='display:none'></iframe> "
    return output
}