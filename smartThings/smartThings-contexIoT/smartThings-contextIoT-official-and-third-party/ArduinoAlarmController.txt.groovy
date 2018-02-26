/**
 *  Arduino Alarm Controller
 *
 *  Copyright 2014 Kevin Lewis
 *
 * 	See arduino sketches here: https://github.com/coolkev/smartthings-alarm
 *	Need to manually add device types for "smartthings : Open/Closed Sensor" and "smartthings : Motion Detector" (use smartthings sample code)
 */
definition(
    name: "Arduino Alarm Controller",
    namespace: "coolkev",
    author: "Kevin Lewis",
    description: "Turn your hardwired alarm into smart sensors",
    category: "Safety & Security",
    iconUrl: "https://graph.api.smartthings.com/api/devices/icons/st.Home.home3-icn",
    iconX2Url: "https://graph.api.smartthings.com/api/devices/icons/st.Home.home3-icn?displaySize=2x",
    iconX3Url: "https://graph.api.smartthings.com/api/devices/icons/st.Home.home3-icn?displaySize=3x")

 
preferences {

	page(name: "deviceList")
    page(name: "deviceDetail")
    
    
}


def deviceList() {

    
    dynamicPage(name: "deviceList", uninstall: true) {
        
        
        def description = settings["typezone$x"];
        
        def existingDevice = getChildDevice("zone$x")
        
        if (existingDevice) {
        	
            description += " (currently ${existingDevice.currentState})"
        
        }
        
        section("Your virtual devices") {        
            for (int x=1;x<settings.zoneCount;x++) {

                href(
                   name: "device$x", 
                   title: settings["zone$x"],
                   page: "deviceDetail", 
                   params: [
                       install: false,
                       zone: x
                   ], 
                   description: description,
                   state: "complete"
               )

            }
        }
        
        section {        
                href(
                    name: "deviceNew", 
                    title: "Add new virtual device",
                    page: "deviceDetail", 
                    params: [
                        install: false,
                        zone: 0
                    ], 
                    description: "",
                    state: ""
                )
            }
    }
}

def deviceDetail(params) {
     /* 
     * firstPage included `params` in the href element that navigated to here. 
     * You must specify some variable name in the method declaration. (I used 'params' here, but it can be any variable name you want).
     * If you do not specify a variable name, there is no way to get the params that you specified in your `href` element. 
     */

    log.debug "params: ${params}"

	int zone = params.zone

	def title = settings["zone$zone"]==null ? "New Virtual Device" : settings["zone$zone"]
    
    dynamicPage(name: "deviceDetail", title: title, uninstall: false, install: params?.install) {
        section {
                input "zoneId$zone", title: "ID", "number", description:"Zone ID $zone", required: true
                
                input "zone$zone", title: "Name", "string", description:"Zone $zone", required: false
                input "typezone$zone", "enum", title: "Type", options:["Open/Closed Sensor","Motion Detector","Light Sensor","Temperature Sensor", "Button"], required: false
          
            }
        
    }
}


def controllerSetup() {

    
	dynamicPage(name: "controllerSetup",title: "Controller Setup", nextPage:"wiredZoneSetup", uninstall:true) {
        
        section("Which Arduino shield?") {
            input "arduino", title: "Shield","capability.polling"
        }    
        
        section("Wired Zones") {
            input "zoneCount", title: "How many wired zones?","number"
        }    
        
        section("Wireless Zones") {
            input "wirelessZoneCount", title: "How many wireless zones?","number", required: false
        }    
    }

}
def wiredZoneSetup() {

	
    def nextPage = "wirelessZoneSetup"
    
    if (settings.wirelessZoneCount==null || settings.wirelessZoneCount==0) {
    //	nextPage = null;
    }
    
   	dynamicPage(name: "wiredZoneSetup", title: "Wired Zone Setup", nextPage: nextPage) {
    
    	for (int i=1;i<=settings.zoneCount;i++) {
        	section("Zone " + i) {
                input "zone" + i, title: "Name", "string", description:"Zone " + i, required: false
                input "typezone" + i, "enum", title: "Type", options:["Open/Closed Sensor","Motion Detector","Light Sensor","Temperature Sensor", "Button"], required: false
            }
        }
        
        
        
    }
    
    
}

def wirelessZoneSetup() {

	
    dynamicPage(name: "wirelessZoneSetup", title: "Wireless Zone Setup", install:true) {
    
    	for (int i=1;i<=settings.wirelessZoneCount;i++) {
        	section("Zone " + i) {
                input "wirelesszone" + i, title: "Name", "string", description:"Zone " + i, required: false
                input "wirelesszonetype" + i, "enum", title: "Type", options:["Open/Closed Sensor","Motion Detector","Light Sensor","Temperature Sensor", "Button"], required: false
            }
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

    
    // Listen to anything which happens on the device
    subscribe(arduino, "response", zonestatusChanged)
    
    for (int i=1;i<=settings.zoneCount;i++) {
    	
    	def name = "zone$i"
		def value = settings[name]

        log.debug "checking device: ${name}, value: $value"

        def zoneType = settings["type" + name];

        if (zoneType == null || zoneType == "")
        {
            zoneType = "Open/Closed Sensor"
        }

        def existingDevice = getChildDevice(name)
        if(!existingDevice) {
            log.debug "creating device: ${name}"
            def childDevice = addChildDevice("smartthings", zoneType, name, null, [name: "Device.${name}", label: value, completedSetup: true])
        }
        else {
            //log.debug existingDevice.deviceType
            //existingDevice.type = zoneType
            existingDevice.label = value
            existingDevice.take()
            log.debug "device already exists: ${name}"

        }


    }
    
    for (int i=1;i<=settings.wirelessZoneCount;i++) {
    	
    	def name = "wirelesszone$i"
		def value = settings[name]

        log.debug "checking device: ${name}, value: $value"

        def zoneType = settings["wirelesszonetype$i"];

        if (zoneType == null || zoneType == "")
        {
            zoneType = "Open/Closed Sensor"
        }

        def existingDevice = getChildDevice(name)
        if(!existingDevice) {
            log.debug "creating device: ${name}"
            def childDevice = addChildDevice("smartthings", zoneType, name, null, [name: "$name", label: value, completedSetup: true])
        }
        else {
            //log.debug existingDevice.deviceType
            //existingDevice.type = zoneType
            existingDevice.label = value
            existingDevice.take()
            log.debug "device already exists: ${name}"

        }


    }
    
    
    
    def delete = getChildDevices().findAll { it.deviceNetworkId.startsWith("zone") && !settings[it.deviceNetworkId] }

    delete.each {
        log.debug "deleting child device: ${it.deviceNetworkId}"
        deleteChildDevice(it.deviceNetworkId)
    }
    

	runIn(300, "checkHeartbeat")
    
}

def uninstalled() {
    //removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
    	log.debug "deleting child device: ${it.deviceNetworkId}"
        deleteChildDevice(it.deviceNetworkId)
    }
}
def zonestatusChanged(evt)
{

 	log.debug "zonestatusChanged ${evt.value}"
   
    def states = evt.value.split(";")*.toInteger()

	int eventId = states[0] as int
    
    def lastStates = state.lastStatus
    
    if (lastStates==null) {
    	lastStates = new int[states.size()]
    }
    else {
    	lastStates = lastStates*.toInteger()
	}
    
    int lastEventId = lastStates[0] as int

	if (lastEventId > eventId && (lastEventId < eventId+2)) {
    	log.debug "EventId out of order lastEventId: $lastEventId, eventId: $eventId"
        return
    }
    
    for (int x=1;x<states.size();x++) {
    
    	int currentState = states[x]
        int lastState = lastStates[x]
        
    	if (currentState != lastState) {
        
        	log.debug "zone $x state changed was: $lastState, now $currentState"        

            deviceStateChanged(x, currentState)

    	}
    }
        
    state.lastStatus = states

/*    
    log.debug "zonestatusChanged ${evt.value}"
    
    def parts = evt.value.split();
        
    def part0 = parts[0]


    if (part0=="hb") {
    	
       	state.lastHeartbeat = now()
        log.debug "received heartbeat: ${state.lastHeartbeat}"
        
    }
    else if (part0=="r" || part0=="w") {
    
        if (parts.length==2) {
        	return
        }    
        
        def zone = parts[1]
        def status = parts[2]

        def deviceName = "zone$zone"
        def typeSettingName = "typezone$zone"

        if (part0=="r") 
        {
            deviceName = "wirelesszone$zone"
            typeSettingName = "wirelesszonetype$zone"
        }

        log.debug "$part0 zone $zone status=$status"

        def device = getChildDevice(deviceName)

        if (device)
        {
            

            def zoneType = settings[typeSettingName];

            def eventName;

//            if (zonetype=="r") 
//            {
//                status = status=="0" ? "open" : "closed"

//            }

			if (zoneType == null || zoneType == "" || zoneType=="Open/Closed Sensor") {
 				eventName = "contact"
                status = status=="0" ? "open" : "closed"
            }
            else if (zoneType=="Motion Detector")
            {
                eventName = "motion";
                status = (status=="0" || status=="active") ? "active" : "inactive"
            }   
            else if (zoneType=="Light Sensor")
            {
                eventName = "illuminance" 
            }   
            else if (zoneType=="Temperature Sensor") {
            	eventName = "temperature"
            }
 
            else if (zoneType=="Button") {
            	eventName = "button"
            	status = "pushed"
            }
            
			log.debug "$device statusChanged $status"

            device.sendEvent(name: eventName, value: status, isStateChange:true)
        }
        else {

            log.debug "couldn't find device for zone ${zone}"

        }
        
    }
    
    */
}


def deviceStateChanged(int zone, int stateValue) {


	def deviceName = "zone$zone"
    def typeSettingName = "typezone$zone"

    
    def device = getChildDevice(deviceName)

    if (device)
    {

		def status;
        
        def zoneType = settings[typeSettingName];

        def eventName;

        if (zoneType == null || zoneType == "" || zoneType=="Open/Closed Sensor") {
            eventName = "contact"
            status = stateValue==1 ? "open" : "closed"
        }
        else if (zoneType=="Motion Detector")
        {
            eventName = "motion";
            status = (stateValue==1 || status=="active") ? "active" : "inactive"
        }   
        else if (zoneType=="Light Sensor")
        {
            eventName = "illuminance" 
            status = stateValue
        }   
        else if (zoneType=="Temperature Sensor") {
            eventName = "temperature"
            status = stateValue
        }

        else if (zoneType=="Button") {
            eventName = "button"
            status = "pushed"
        }

        log.debug "$device statusChanged $status"

        device.sendEvent(name: eventName, value: status, isStateChange:true)
    }
    else {

        log.debug "couldn't find device for zone ${zone}"

    }

}


def checkHeartbeat() {


	def elapsed = now() - state.lastHeartbeat;
    log.debug "checkHeartbeat elapsed: $elapsed"
    
	if (elapsed > 30000) {
    
    	log.debug "Haven't received heartbeat in a while - alarm is offline"
        sendPush("Arduino Alarm appears to be offline - haven't received a heartbeat in over 5 minutes");
    }

	
}