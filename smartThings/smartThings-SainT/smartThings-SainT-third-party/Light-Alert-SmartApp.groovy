/**
 *  Smart Bulb Notifier/Flasher
 *
 *  Copyright 2015 Scott Gibson
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
 *  Many thanks to Eric Roberts for his virtual switch creator, which served as the template for creating child switch devices!
 *
 */
definition(
    name: "Smart Bulb Alert (Blink/Flash)",
    namespace: "sticks18",
    author: "Scott Gibson",
    description: "Creates a virtual switch button that when turned on will trigger selected smart bulbs to blink or flash",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page(name: "basicInfo")
	page(name: "configureBulbs")
}

def basicInfo(){
	dynamicPage(name: "basicInfo", title: "First, name your trigger and select smart bulb types", nextPage: "configureBulbs", uninstall: true){
		section("Create Virtual Momentary Button as Trigger") {
			input "switchLabel", "text", title: "Switch Button Label", multiple: false, required: true
		}
		section("Choose your Smart Bulbs") {
			paragraph "This SmartApp will work with most zigbee bulbs by directly calling the Identify function built into the hardware. This function is generally run when you first pair a bulb to the hub to let you know it was successful. Different bulbs have different capabilities and/or endpoints to address, so they must be selected separately in this SmartApp. If expanded functionality exists for a particular bulb type, you will be given additional options to select. Caveats: Non-Hue bulbs connected to the Hue Bridge, such as GE Link or Cree Connected, will not work because the Hue API is not designed for them.",
            title: "How to select your bulbs...", required: true
			input "geLinks", "capability.switch level", title: "Select GE Link Bulbs", required: false, multiple: true
			//input "creeCons", "capability.switch level", title: "Select Cree Connected Bulbs", required: false, multiple: true
			//input "hueHubs", "capability.switch level", title: "Select Hue Bulbs connected to Hue Hub", required: false, multiple: true
			//input "hueDirs", "capability.switch level", title: "Select Hue Bulbs directly connected to ST Hub", required: false, multiple: true
			//input "osramLs", "capability.switch level", title: "Select Osram Lightify Bulbs", required: false, multiple: true
  		}
	}
}

def configureBulbs(){
	dynamicPage(name: "configureBulbs", title: "Additional Configuration Options by Bulb Type", install: true, uninstall: true) {
		section(title: "Auto-off for bulbs directly connected to SmartThings Hub") {
			input "autoOff", "bool", title: "Automatically stop the alert", required: false, submitOnChange: true
			input "offDelay", "number", title: "Turn off alert after X seconds (default = 5, max = 10)", required: false, submitOnChange: true
		}
		/*section(title: "Options for Hue Bulbs connected to Hue Bridge", hidden: hideHueHubBulbs(), hideable: true) {
			input "hBlink", "enum", title: "Select Blink for single flash or Breathe for 15 seconds continuous (default = Breathe)", multiple: false, required: false,
				options: ["Blink", "Breathe"]
		}
		section(title: "Options for Hue Bulbs connected directly to SmartThings Hub", hidden: hideHueDirBulbs(), hideable: true) {
			input "hdStyle", "enum", title: "Select alert style: Normal = Bulb will blink until stopped via auto-off or trigger, Blink = Single flash, Breathe = Mult Flash for 15 seconds, Okay = Bulb turn green for 2 seconds", 
				multiple: false, required: false, options: ["Normal", "Blink", "Breathe", "Okay"] 
		}
		section(title: "Options for Osram Lightify Bulbs", hidden: hideOsramBulbs(), hideable: true) {
			input "osStyle", "enum", title: "Select alert style: Normal = Bulb will blink until stopped via auto-off or trigger", 
				multiple: false, required: false, options: ["Normal"] 
		}*/
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
    
    state.autoOff = offDelay
   // state.hueHubOption = getHueHubOption()
   // state.hueDirOption = getHueDirOption()
   // state.osramOption = getOsramOption()
   // state.onlyHueHub = getHueHubOnly()
    
    def deviceId = app.id + "SimulatedSwitch"
    log.debug(deviceId)
    def existing = getChildDevice(deviceId)
    log.debug existing
    if (!existing) {
        log.debug "Add child"
        def childDevice = addChildDevice("sticks18", "Smart Bulb Alert Switch", deviceId, null, [label: switchLabel])
    }
    
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}

/* private getOffDelay() {
	def result = 5
    log.debug "autoOff is ${autoOff}"
	if (autoOff) {
        result = 5
        log.debug "offDelay is ${offDelay}"
		if(offDelay) {
			def offSec = Math.Min(offDelay, 10)
			offSec = Math.Max(offSec, 1)
			result = offSec
		}
	}
	log.debug "off delay: ${result}"
    return result
}

private getHueDirOption() {
	def result = null
    log.debug hueDirs
	if (hueDirs) {
		switch(hdStyle) {
			case "Normal":
				result = ""
				break
			case "Blink":
				result = "00"
				break
			case "Breathe":
				result = "01"
				break
			case "Okay":
				result = "02"
				break
			default:
				result = ""
				break
		}
	}
    log.debug "hue dir option: ${result}"
    return result
}

private getHueHubOnly() {
	(creeCons || geLinks || osramLs || hueDirs) ? false : true
}

private getHueHubOption() {
	def result = null
	if (hueHubs) {
		switch(hBlink) {
			case "Blink":
				result = "select"
				break
			case "Breathe":
				result = "lselect"
				break
			default:
				result = "lselect"
				break
		}
	}
    log.debug "hue hub option: ${result}"
    return result
}

private getOsramOption() {
	def result = null
	if (osramLs) {
		switch(osStyle) {
			case "Normal":
				result = ""
				break
			case "Blink":
				result = "00"
				break
			case "Breathe":
				result = "01"
				break
			case "Okay":
				result = "02"
				break
			default:
				result = ""
				break
		}
	}
    log.debug "osram option: ${result}"
    return result
}
*/
private hideHueHubBulbs() {
	(hueHubs) ? false : true
}

private hideHueDirBulbs() {
	(hueDirs) ? false : true
}

private hideOsramBulbs() {
	(osramLs) ? false : true
}

private hex(value, width=4) {
	def s = new BigInteger(Math.round(value).toString()).toString(16)
	while (s.size() < width) {
		s = "0" + s
	}
	s
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String swapEndianHex(String hex) {
    reverseArray(hex.decodeHex()).encodeHex()
}

private byte[] reverseArray(byte[] array) {
    int i = 0;
    int j = array.length - 1;
    byte tmp;
    while (j > i) {
        tmp = array[j];
        array[j] = array[i];
        array[i] = tmp;
        j--;
        i++;
    }
    return array
}

// Child device methods after this point. Instead of subscribing to child, have child directly call parent

def on(childDevice) {
	log.debug "Start alert"
	if(state.autoOff != null) {
		log.debug "Only alert for ${state.autoOff} seconds"
		if(geLinks != null) {
			geLinks.each { bulb ->
				// bulb.setLevel(99, 1800)
                bulb.alert(state.autoOff)
                // childDevice.attWrite(bulb.deviceNetworkId, 1, 3, 0, "0x21", state.autoOff)
                // childDevice.zigbeeCmd(bulb.deviceNetworkId, 1, 3, 0, "")
			}
		}
		/*if(creeCons != null) {
			creeCons.each { bulb ->
				childDevice.attWrite(bulb.deviceNetworkId, "10", "3", "0", "0x21", state.autoOff)
			}
		}
		if(state.hueDirOption != null) {
			if(state.hueDirOption == "") {
				hueDirs.each { bulb ->
					childDevice.attWrite(bulb.deviceNetworkId, "0B", "3", "0", "0x21", state.autoOff)
				}
			}
			else {
				hueDirs.each { bulb ->
					def payload = state.hueDirOption + " 00"
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "0B", "3", "0x40", payload)
				}
			}
		}
		if(state.osramOption != null) {
			if(state.osramOption == "") {
				osramLs.each { bulb ->
					childDevice.attWrite(bulb.deviceNetworkId, "03", "3", "0", "0x21", state.autoOff)
				}
			}
			else {
				osramLs.each { bulb ->
					def payload = state.osramOption + " 00"
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "03", "3", "0x40", payload)
				}
			}
		} */
		childDevice.justOff()
		
	} /*
	else {
		log.debug "Starting continous alert"
		if(geLinks != null) {
			geLinks.each { bulb ->
				childDevice.zigbeeCmd(bulb.deviceNetworkId, "1", "3", "0", "")
			}
		}
		if(creeCons != null) {
			creeCons.each { bulb ->
				childDevice.zigbeeCmd(bulb.deviceNetworkId, "10", "3", "0", "")
			}
		}
		if(state.hueDirOption != null) {
			if(state.hueDirOption == "") {
				hueDirs.each { bulb ->
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "0B", "3", "0", "")
				}
			}
			else {
				hueDirs.each { bulb ->
					def payload = state.hueDirOption + " 00"
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "0B", "3", "0x40", payload)
				}
			}
		}
		if(state.osramOption != null) {
			if(state.osramOption == "") {
				osramLs.each { bulb ->
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "03", "3", "0", "")
				}
			}
			else {
				osramLs.each { bulb ->
					def payload = state.osramOption + " 00"
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "03", "3", "0x40", payload)
				}
			}
		}	
	}
	if(state.hueHubOption != null) {
		log.debug "Send planned alert to Hue Bulbs via Hue Bridge"
		hueHubs.each { bulb ->
			def hue = bulb.currentValue("hue")
			def sat = bulb.currentValue("saturation")
			def alert = state.hueHubOption
			def value = [hue: hue, saturation: sat, alert: alert]
			bulb.setColor(value)
		}
		if(state.hueHubOnly) { childDevice.justOff() }
	}*/
	
}

def off(childDevice) {
	log.debug "Stop Alert" /*
	if(state.autoOff != null) {
		childDevice.justOff()
	}
	else {
		log.debug "Stopping continous alert"
		if(geLinks != null) {
			geLinks.each { bulb ->
				childDevice.zigbeeCmd(bulb.deviceNetworkId, "1", "3", "0", "")
			}
		}
		if(creeCons != null) {
			creeCons.each { bulb ->
				childDevice.zigbeeCmd(bulb.deviceNetworkId, "10", "3", "0", "")
			}
		}
		if(state.hueDirOption != null) {
			if(state.hueDirOption == "") {
				hueDirs.each { bulb ->
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "0B", "3", "0", "")
				}
			}
		}
		if(state.osramOption != null) {
			if(state.osramOption == "") {
				osramLs.each { bulb ->
					childDevice.zigbeeCmd(bulb.deviceNetworkId, "03", "3", "0", "")
				}
			}
		}	
	}
	*/
}

def allOff(childDevice) {
	log.debug "Stopping alerts" /*
	if(geLinks != null) {
		geLinks.each { bulb ->
			childDevice.attWrite(bulb.deviceNetworkId, "1", "3", "0", "0x21", "0100")
		}
	}
	if(creeCons != null) {
		creeCons.each { bulb ->
			childDevice.attWrite(bulb.deviceNetworkId, "10", "3", "0", "0x21", "0100")
		}
	}
	if(hueDirs != null) {
		hueDirs.each { bulb ->
			childDevice.attWrite(bulb.deviceNetworkId, "0B", "3", "0", "0x21", "0100")
		}
	}
	if(osramLs != null) {
		osramLs.each { bulb ->
			childDevice.attWrite(bulb.deviceNetworkId, "03", "3", "0", "0x21", "0100")
		}
	} */
} 
