/**
 *  Advanced Lighting Control
 *
 *  Copyright 2014 Troy Kelly
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
    name: "Advanced Switch and Lighting Control",
    namespace: "advswitch",
    author: "Troy Kelly",
    description: "Advanced control of switched, dimmable and colour managed devices.",
    category: "Convenience",
    iconUrl: "https://aperim.com/smartthings/aperim.png",
    iconX2Url: "https://aperim.com/smartthings/aperim2x.png"
)


preferences {
	section("Triggers") {
    	input "motionSensors", "capability.motionSensor", title: "Motion", multiple: true, required: false //motion  ["inactive", "active"]
        input "contactSensors", "capability.contactSensor", title: "Contact", multiple: true, required: false //contact  ["closed", "open"] 
        input "buttonSensors", "capability.button", title: "Button", multiple: true, required: false //button - ["held", "pushed"]
        //input "momentarySensors", "capability.momentary", title: "Momentary", multiple: true, required: false
        input "accelerationSensors", "capability.accelerationSensor", title: "Acceleration", multiple: true, required: false //acceleration  ["active", "inactive"]
        
        // Use the below when Publishing to devices
        input "validModes", "mode", multiple:true, title: "Modes", required: true
        
        // Use the below for the Simulator - BUG REPORTED
        //input "validModes", "enum", title: "Modes", metadata:[values:["Home - Day","Home - Night","Away - Day","Away - Night","Asleep"]], multiple:false, required: false
	}
	section("MiniMote") {
    	input "miniMoteBtn", "enum", title: "MiniMote Button Number", metadata:[values:["One","Two","Three","Four"]], multiple:false, required: false
    }
    section("Targets") {
    	input "switches", "capability.switch", multiple: true, title: "Switches"
    }
    section("Action") {
    	input "onOrOff", "bool", title: "On/Off", required: true
    }
    section("Restore State") {
    	input "restoreDelay", "number", title: "Minutes", required: false
		input "restoreOver", "bool", title: "Even if state changes?", required: true
    }
    section("Alerting") {
    	input "sendPushMessage", "bool", title: "Send push on activation?", required: false
    }
	section("Detailed light settings") {
        input ("intensity", "enum", multiple: false, title: "Intensity", required: false, metadata:[values:["1%","10%","20%","30%","40%","50%","60%","70%","80%","90%","100%"]])
        input ("colour", "enum", multiple: false, title: "Colour", required: false, metadata:[values:["Soft White","Concentrate","Energize","Relax","Red","Green","Blue","Yellow","Orange","Purple","Pink"]])
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
	unschedule("doRestore")
    state.active = false
    state.subscribed = false
    subscribe(app, "appEvent")
    subscribe(location, "locationEvent")
    def modeList = getValidModes(settings.validModes)
    def modeValid = modeIsValid(location.mode, modeList)
	if(modeValid) {
    	doSubscribe()
    }
}

def doSubscribe() {
	if (settings.motionSensors != null) {
        subscribe(settings.motionSensors, "motion.active", sensorActive)
        if ( settings.restoreDelay != null ) {
	        subscribe(settings.motionSensors, "motion.inactive", sensorInactive)
		}
	}
	if (settings.contactSensors != null) {
        subscribe(settings.contactSensors, "contact.open", sensorActive)
        if ( settings.restoreDelay != null ) {
        	subscribe(settings.contactSensors, "contact.closed", sensorInactive)
		}
	}
	if (settings.accelerationSensors != null) {
        subscribe(settings.accelerationSensors, "acceleration.active", sensorActive)
        if ( settings.restoreDelay != null ) {
	        subscribe(settings.accelerationSensors, "acceleration.inactive", sensorInactive)
		}
	}
	if (settings.buttonSensors != null) {
        subscribe(settings.buttonSensors, "button.pushed", sensorActiveOneShot)
        /*
        if ( settings.restoreDelay != null ) {
	        subscribe(settings.buttonSensors, "button.pushed", sensorInactive)
		}
        */
	}
    state.subscribed = true
}

def doUnsubscribe() {
	if (settings.motionSensors != null) {
        unsubscribe(settings.motionSensors)
	}
	if (settings.contactSensors != null) {
        unsubscribe(settings.contactSensors)
	}
	if (settings.accelerationSensors != null) {
        unsubscribe(settings.accelerationSensors)
	}
	if (settings.buttonSensors != null) {
        unsubscribe(settings.buttonSensors)
	}
    state.subscribed = false
}

def doRestore() {
	log.debug "${app.label}: I should restore"
    if(!state.active) {
    	log.debug "Not active, nothing to do"
    	return false
    }
    for (theDevice in settings.switches) {
	    def deviceID = theDevice.id
		if ( state.originalStates[deviceID] != null ) {
        	def originalState = state.originalStates[deviceID]
            if ( (settings.restoreOver != null) && (settings.restoreOver == false)) {
            	if (!hasStateChanged(theDevice)) {
                	log.debug "${deviceID}: State unchanged, restoring"
					actuateDevice(theDevice, originalState)
				} else {
                	log.debug "${deviceID}: State has changed - override is off, not restoring"
                }
			} else if ((settings.restoreOver != null) && (settings.restoreOver == true)) {
            	log.debug "${deviceID}: Not checking state - override is on, restoring"
            	actuateDevice(theDevice, originalState)
            }
            state.originalStates.remove(deviceID)
        } else {
        	log.debug "${deviceID}: No state recorded - unable to restore"
        }
        unsubscribe(theDevice)
    }
    doPush('restore')
    state.active = false
}

def actuateDevice(theDevice, deviceState) {
    deviceState.each { key, value ->
		if ( key == "switch" && value == "on" ) {
			theDevice.on()
            //log.debug "${theDevice.label} (${theDevice.id}): On"
        } else if ( key == "switch" && value == "off" ) {
        	theDevice.off()
            //log.debug "${theDevice.label} (${theDevice.id}): Off"
        } else {
	    	def setCommandName = key[0].toUpperCase() + key.substring(1)
	        def setCommand = "set${setCommandName}"
			if ( value != null ) {
            	try {
            		theDevice."${setCommand}"(value)
                } catch (Exception e) {
                    // Nothing
                }
                //log.debug "${theDevice.label} (${theDevice.id}): ${setCommand}(${value})"
            } else {
            	try {
            		theDevice."${setCommand}"()
                } catch (Exception e) {
                    // Nothing
                }
                //log.debug "${theDevice.label} (${theDevice.id}): ${setCommand}"
            }
		}
    }
}

def sensorActive(evt) {
	if(sensorActiveValidate(evt)) {
		triggerActive()
	}
}

def sensorActiveOneShot(evt) {
	if(sensorActiveValidate(evt)) {
		triggerActive(true)
	}
}

def sensorActiveValidate(evt) {
    def modeList = getValidModes(settings.validModes)
    def modeValid = modeIsValid(location.mode, modeList)
	if(!modeValid) {
    	log.debug "Mode ${location.mode} is not valid here"
    	return false
	}
    def myRegularExpression = /zw device: [0-9]+, command: 2001, payload: ([0-9]+)/
    def matcher = ( evt =~ myRegularExpression )
    def payload = null
    try {
		payload = matcher[0][1]
	} catch (Exception e) {
    	// Nothing
    }
	if ( payload != null && settings.miniMoteBtn != null ) {
		def miniMoteMap = getMiniMoteMap()
        if ( payload != miniMoteMap[settings.miniMoteBtn] ) {
            return false
        }
	}
	log.debug "${app.label}: [Active] - ${evt}"
    if ( settings.restoreDelay != null && state.active) {
		return false
    }
    return true
}

def sensorInactive(evt) {
    def myRegularExpression = /zw device: [0-9]+, command: 2001, payload: ([0-9]+)/
    def matcher = ( evt =~ myRegularExpression )
    def payload = null
    try {
		payload = matcher[0][1]
	} catch (Exception e) {
    	// Nothing
    }
	if ( payload != null && settings.miniMoteBtn != null ) {
		def miniMoteMap = getMiniMoteMap()
        if ( payload != miniMoteMap[settings.miniMoteBtn] ) {
            return false
        }
	}
	log.debug "${app.label}: [Inactive] - ${evt}"
    if (state.active) {
    	if (allInactive()) {
        	setRestore()
        }
    }
}

def triggerActive(alsoRestore = false) {
	if( settings.restoreDelay != null ) {
    	if(state.active) {
            unschedule("doRestore")
            return false
        } else {
	    	state.originalStates = captureStates()
		}
	}
    def actuateData = getActuateData()
    for (theDevice in settings.switches) {
		actuateDevice(theDevice, actuateData)
		if( settings.restoreDelay != null ) {
        	trackChanges(theDevice)
        }
    }
    if (alsoRestore) {
    	setRestore()
    }
	doPush('trigger')
    state.active = true
}

def trackChanges(theDevice) {
	def deviceAttributes = theDevice.supportedAttributes
    for ( attr in theDevice.supportedAttributes ) {
    	def attrName = "${attr}"
        subscribe(theDevice, attrName, stateChange)
    }
}

def allInactive() {
	if ( (settings.motionSensors == null) && (settings.contactSensors == null) && (settings.accelerationSensors == null) ) {
    	log.debug "No sensors defined, not checking status of sensors"
    	return true
    }

	if (settings.motionSensors != null) {
    	if(!testInactive(settings.motionSensors, "motion", "inactive")) {
        	return false
        }
	}
	if (settings.contactSensors != null) {
    	if(!testInactive(settings.contactSensors, "contact", "closed")) {
        	return false
        }
	}
	if (settings.accelerationSensors != null) {
    	if(!testInactive(settings.accelerationSensors, "acceleration", "inactive")) {
        	return false
        }
	}
    return true
}

def testInactive(theDevices, sensorName = "motion", inactiveState = "inactive") {
	def result = true
    for ( theDevice in theDevices ) {
    	def supportedAttributes = theDevice.supportedAttributes
        if ( testContents(supportedAttributes, sensorName)) {
        	def attrValue = theDevice.currentValue(sensorName)
			if ( attrValue != inactiveState ) {
            	result = false
				break
			}
		} else {
        	log.debug "${theDevice.label} does not support ${sensorName} (it supports ${supportedAttributes})"
            result = false
            break
		}
    }
    return result
}

def testContents(theList = [], checkFor = null) {
	def result = false
    for ( listItem in theList ) {
		if ( "${listItem}" == "${checkFor}" ) {
        	result = true
            break
        }
    }
	return result
}

def setRestore() {
	if ( settings.restoreDelay != null ) {
		int runInSec = settings.restoreDelay * 60
		if ( state.active ) {
			unschedule("doRestore")
            runIn(runInSec, doRestore)
            log.debug "{$app.label}: Already active, will reset restore for ${runInSec} seconds"
            return false
        } else {
        	state.active == true
            state.originalStates = captureStates()
			runIn(runInSec, doRestore)
            log.debug "{$app.label}: Have set restore for ${runInSec} seconds"
		}
    }
}

def hasStateChanged(theDevice) {
	//returns false if the state has not changed
    def result = false
    def actuateData = getActuateData()
    def deviceState = captureState(theDevice)
    for ( attr in theDevice.supportedAttributes ) {
    	def attrName = "${attr}"
        def attrValue = theDevice.currentValue(attrName)
        if ( actuateData[attrName] != null ) {
        	if ( actuateData[attrName] != attrValue ) {
            	result = true
                break
			}
        }
    }
	return result
}

def captureStates() {
	def states = [:]
    for (theDevice in settings.switches) {
    	try {
        	theDevice.poll()
		} catch (Exception e) {
        	//nothing
        }
    	def deviceState = captureState(theDevice)
		def deviceID = theDevice.id
    	states[deviceID] = deviceState
        log.debug "Recorded state (${deviceID}): ${deviceState}"
    }
    return states
}

def captureState(theDevice) {
	def deviceAttributes = theDevice.supportedAttributes
    def deviceAttrValue = [:]
    for ( attr in theDevice.supportedAttributes ) {
    	def attrName = "${attr}"
        def attrValue = theDevice.currentValue(attrName)
        deviceAttrValue[attrName] = attrValue
    }
    return deviceAttrValue
}

def stateChange(evt) {
	log.debug "State Change: ${evt}"
}

def appEvent(evt) {
	log.debug "${app.label}: ${evt}"
	triggerActive(true)
}

def locationEvent(evt) {
	log.debug "${app.label}: ${evt}"
    def modeList = getValidModes(settings.validModes)
    def modeValid = modeIsValid(evt, modeList)
	if(modeValid) {
    	if(!state.subscribed) {
        	doSubscribe()
        }
        if ( (settings.motionSensors == null) && (settings.contactSensors == null) && (settings.accelerationSensors == null) && (settings.buttonSensors == null)) {
            triggerActive()
        } else if(!allInactive()) {
            triggerActive()
        }
    } else {
    	doUnsubscribe()
        if(state.active) {
        	setRestore()
        }
    }
}

def getValidModes(modeList = null) {
    if (modeList != null) {
		if (modeList instanceof java.lang.String || modeList instanceof GString) {
        	modeList = [ "${modeList}" ]
		} 
    }
	return modeList
}

def modeIsValid(theMode, modeList) {
	def result = false
    for ( modeTest in modeList ) {
    	if ( "${theMode}" == "${modeTest}" ) {
        	result = true
            break
        }
    }
    return result
}

def getActuateData() {
    def actuateData = [:]
    if ( settings.onOrOff ) {
    	actuateData["switch"] = "on"
    } else {
    	actuateData["switch"] = "off"
	}
    if ( settings.intensity != null ) {
        int newLevel = settings.intensity.minus('%').toInteger()
        if (newLevel < 2) {
            newLevel = 1
        }
        if (newLevel > 99) {
            newLevel = 99
        }
		actuateData["level"] = newLevel
	}
    if (settings.colour != null) {
    	def colourMap = getColourMap()
		actuateData["hue"] = colourMap[settings.colour]["hue"]
        actuateData["saturation"] = colourMap[settings.colour]["saturation"]
    }
	return actuateData
}

def doPush(evt)
{
	if (!settings.sendPushMessage) {
    	return false
    }
    def msg = evt
	switch (evt) {
    	case 'trigger':
        	msg = "${app.label} was triggered"
            break
        case 'restore':
        	msg = "${app.label} was restored"
            break
        case 'abort':
        	msg = "${app.label} was aborted"
            break
    }
    sendPush(msg)
    return true
}


def getColourMap() {
    def colourMap = [
        "Soft White": ["hue": 23, "saturation": 47 ],
        "Concentrate": ["hue": 52, "saturation": 17 ],
        "Energize": ["hue": 53, "saturation": 91 ],
        "Relax": ["hue": 20, "saturation": 83 ],
        "Purple": ["hue": 83, "saturation": 99 ],
        "Green": ["hue": 40, "saturation": 99 ],
        "Blue": ["hue": 66, "saturation": 99 ],
        "Red": ["hue": 1, "saturation": 99 ],
        "Pink": ["hue": 97, "saturation": 99 ],
        "Yellow": ["hue": 28, "saturation": 99 ],
        "Orange": ["hue": 11, "saturation": 99 ]
    ]
	return colourMap
}

def getMiniMoteMap() {
	def miniMoteMap = [
    	"One": "01",
        "Two": "29",
        "Three": "51",
        "Four": "79"
    ]
    return miniMoteMap
}