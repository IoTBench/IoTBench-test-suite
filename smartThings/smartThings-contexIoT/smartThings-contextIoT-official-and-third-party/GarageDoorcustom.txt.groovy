metadata {
    simulator {
		status "open": "zone report :: type: 19 value: 0031"
		status "closed": "zone report :: type: 19 value: 0030"
 
		status "acceleration": "acceleration: 1, rssi: 0, lqi: 0"
		status "no acceleration": "acceleration: 0, rssi: 0, lqi: 0"
 
		for (int i = 20; i <= 100; i += 10) {
			status "${i}F": "contactState: 0, accelerationState: 0, temp: $i F, battery: 100, rssi: 100, lqi: 255"
		}
 
		// kinda hacky because it depends on how it is installed
		status "x,y,z: 0,0,0": "x: 0, y: 0, z: 0, rssi: 100, lqi: 255"
		status "x,y,z: 1000,0,0": "x: 1000, y: 0, z: 0, rssi: 100, lqi: 255"
		status "x,y,z: 0,1000,0": "x: 0, y: 1000, z: 0, rssi: 100, lqi: 255"
		status "x,y,z: 0,0,1000": "x: 0, y: 0, z: 1000, rssi: 100, lqi: 255"
	}
 
	tiles {
		//COMPOSITE: add actions to the closed and open state
		standardTile("status", "device.status", width: 2, height: 2) {
			state("closed", label:'${name}', action: "push", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
			state("open", label:'${name}', action: "push", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
		}
		standardTile("contact", "device.contact") {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		standardTile("acceleration", "device.acceleration", decoration: "flat") {
			state("active", label:'${name}', icon:"st.motion.acceleration.active", backgroundColor:"#53a7c0")
			state("inactive", label:'${name}', icon:"st.motion.acceleration.inactive", backgroundColor:"#ffffff")
		}
		valueTile("temperature", "device.temperature", decoration: "flat") {
			state("temperature", label:'${currentValue}', unit:"F")
		}
		valueTile("3axis", "device.threeAxis", decoration: "flat") {
			state("threeAxis", label:'${currentValue}', unit:"")
		}
		valueTile("battery", "device.battery", decoration: "flat", inactiveLabel: false) {
			state "battery", label:'${currentValue}% battery', unit:""/*, backgroundColors:[
				[value: 5, color: "#BC2323"],
				[value: 10, color: "#D04E00"],
				[value: 15, color: "#F1D801"],
				[value: 16, color: "#FFFFFF"]
			]*/
		}
		/*
		valueTile("lqi", "device.lqi", decoration: "flat", inactiveLabel: false) {
			state "lqi", label:'${currentValue}% signal', unit:""
		}
		*/
		
		main(["status","contact", "acceleration"])
		details(["status","contact", "acceleration", "temperature", "3axis", "battery"/*, "lqi"*/])
	}
    
	//COMPOSITE: add preferences so we do not hard-code IDs in the device type
	preferences {
		input "smartAppId", "text", title: "SmartApp Id", description: "The ID for your SmartApp", required: true
		input "accessToken", "text", title: "Access Token", description: "The OAuth Access Token", required: true
	}
}
 
//COMPOSITE: add this push() method to actually make the remote call to the smart app
def push() {
	def accessToken = settings.accessToken
	def smartAppId = settings.smartAppId
    
	rest(
		method: 'PUT',
		endpoint: "http://graph.api.smartthings.com",
		path: "/api/smartapps/installations/${smartAppId}/remotePush",
		headers: ['Authorization': "Bearer ${accessToken}"],
		requestContentType: "application/json",
		synchronous: true
	)
}
 
def parse(String description) {
	log.debug "parse($description)"
	def results = null
 
	if (!isSupportedDescription(description) || zigbee.isZoneType19(description)) {
		// Ignore this in favor of orientation-based state
		// results = parseSingleMessage(description)
	}
	else {
		results = parseMultiSensorMessage(description)
	}
	log.debug "Parse returned ${results?.descriptionText}"
	return results
 
}
 
def actuate() {
	log.debug "Sending button press event"
	sendEvent(name: "buttonPress", value: "true", isStateChange: true)
}
 
private motionDirection() {
	def dir = device.currentValue("status")
	if (dir == "open") {
		"closing"
	}
	else if (dir == "closed") {
		"opening"
	}
	else {
		dir
	}
}
 
private List parseMultiSensorMessage(description) {
	def results = []
	if (isAccelerationMessage(description)) {
		results = parseAccelerationMessage(description)
	}
	else if (isContactMessage(description)) {
		results = parseContactMessage(description)
	}
	else if (isRssiLqiMessage(description)) {
		results = parseRssiLqiMessage(description)
	}
	else if (isOrientationMessage(description)) {
		results = parseOrientationMessage(description)
	}
 
	results
}
 
private List parseAccelerationMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('acceleration:')) {
			def event = getAccelerationResult(part, description)
			results << event
 
			if (event.value == "active") {
				log.info "Generating opening/closing status of ${motionDirection()}"
				results << createEvent(name: "status", value: motionDirection())
			}
 
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}
 
	results
}
 
private List parseContactMessage(String description) {
	def results = []
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('accelerationState:')) {
			results << getAccelerationResult(part, description)
		}
		else if (part.startsWith('temp:')) {
			results << getTempResult(part, description)
		}
		else if (part.startsWith('battery:')) {
			results << getBatteryResult(part, description)
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}
 
	results
}
 
private List parseOrientationMessage(String description) {
	def results = []
	def xyzResults = [x: 0, y: 0, z: 0]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('x:')) {
			def unsignedX = part.split(":")[1].trim().toInteger()
			def signedX = unsignedX > 32767 ? unsignedX - 65536 : unsignedX
			xyzResults.x = signedX
		}
		else if (part.startsWith('y:')) {
			def unsignedY = part.split(":")[1].trim().toInteger()
			def signedY = unsignedY > 32767 ? unsignedY - 65536 : unsignedY
			xyzResults.y = signedY
		}
		else if (part.startsWith('z:')) {
			def unsignedZ = part.split(":")[1].trim().toInteger()
			def signedZ = unsignedZ > 32767 ? unsignedZ - 65536 : unsignedZ
			xyzResults.z = signedZ
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}
 
	def xyz = getXyzResult(xyzResults, description)
	results << xyz
 
	// Looks for Z-axis orientation as virtual contact state
	log.debug "xyz = $xyz"
	log.debug "value = '$xyz.value'"
	log.debug "values = ${xyz.value.split(',')}"
	def a = xyz.value.split(',').collect{it.toInteger()}
	def absValue = Math.abs(a[2])
	log.debug "absValue: $absValue"
 
	def lastStatus = device.currentState("status")
	def age = lastStatus ? new Date().time - lastStatus.date.time : 8000
	log.debug "AGE: $age"
	if (age >= 8000) {
		if (absValue > 925) {
			results << createEvent(name: "contact", value: "open")
			results << createEvent(name: "status", value: "open")
			log.debug "STATUS: open"
		}
		else if (absValue < 75) {
			results << createEvent(name: "contact", value: "closed")
			results << createEvent(name: "status", value: "closed")
			log.debug "STATUS: closed"
		}
		//else if (lastStatus?.value in ["open","closed"]) {
		//    results << createEvent(name: "status", value: motionDirection())
		//    log.info "STATUS: ${motionDirection()}"
		//}
	}
	results
}
 
private List parseRssiLqiMessage(String description) {
	def results = []
	// "lastHopRssi: 91, lastHopLqi: 255, rssi: 91, lqi: 255"
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('lastHopRssi:')) {
			results << getRssiResult(part, description, true)
		}
		else if (part.startsWith('lastHopLqi:')) {
			results << getLqiResult(part, description, true)
		}
		else if (part.startsWith('rssi:')) {
			results << getRssiResult(part, description)
		}
		else if (part.startsWith('lqi:')) {
			results << getLqiResult(part, description)
		}
	}
 
	results
}
 
private getAccelerationResult(part, description) {
	def name = "acceleration"
	def value = part.endsWith("1") ? "active" : "inactive"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value"
	def isStateChange = isStateChange(device, name, value)
 
	[
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: value,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}
 
private getTempResult(part, description) {
	def name = "temperature"
	def value = zigbee.parseSmartThingsTemperatureValue(part, "temp: ")
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $valueF"
	def isStateChange = isTemperatureStateChange(device, name, value)
 
	[
		name: name,
		value: value,
		unit: "F",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: displayed(description, isStateChange)
	]
}
 
private getXyzResult(results, description) {
	def name = "threeAxis"
	def value = "${results.x},${results.y},${results.z}"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value"
	def isStateChange = isStateChange(device, name, value)
 
	[
		name: name,
		value: value,
		unit: null,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}
 
private getBatteryResult(part, description) {
	def batteryDivisor = description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"} ? description.split(",").find {it.split(":")[0].trim() == "batteryDivisor"}.split(":")[1].trim() : null
	def name = "battery"
	def value = zigbee.parseSmartThingsBatteryValue(part, batteryDivisor)
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was ${value}${unit}"
	def isStateChange = isStateChange(device, name, value)
 
	[
		name: name,
		value: value,
		unit: unit,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: name,
		isStateChange: isStateChange,
		displayed: false
	]
}
 
private getRssiResult(part, description, lastHop=false) {
	def name = lastHop ? "lastHopRssi" : "rssi"
	def valueString = part.split(":")[1].trim()
	def value = (Integer.parseInt(valueString) - 128).toString()
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was $value dBm"
 
	def isStateChange = isStateChange(device, name, value)
 
	[
		name: name,
		value: value,
		unit: "dBm",
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: null,
		isStateChange: isStateChange,
		displayed: false
	]
}
 
/**
 * Use LQI (Link Quality Indicator) as a measure of signal strength. The values
 * are 0 to 255 (0x00 to 0xFF) and higher values represent higher signal
 * strength. Return as a percentage of 255.
 *
 * Note: To make the signal strength indicator more accurate, we could combine
 * LQI with RSSI.
 */
private getLqiResult(part, description, lastHop=false) {
	def name = lastHop ? "lastHopLqi" : "lqi"
	def valueString = part.split(":")[1].trim()
	def percentageOf = 255
	def value = Math.round((Integer.parseInt(valueString) / percentageOf * 100)).toString()
	def unit = "%"
	def linkText = getLinkText(device)
	def descriptionText = "$linkText ${name} was: ${value}${unit}"
 
	def isStateChange = isStateChange(device, name, value)
 
	[
		name: name,
		value: value,
		unit: unit,
		linkText: linkText,
		descriptionText: descriptionText,
		handlerName: null,
		isStateChange: isStateChange,
		displayed: false
	]
}
 
private Boolean isAccelerationMessage(String description) {
	// "acceleration: 1, rssi: 91, lqi: 255"
	description ==~ /acceleration:.*rssi:.*lqi:.*/
}
 
private Boolean isContactMessage(String description) {
	// "contactState: 1, accelerationState: 0, temp: 14.4 C, battery: 28, rssi: 59, lqi: 255"
	description ==~ /contactState:.*accelerationState:.*temp:.*battery:.*rssi:.*lqi:.*/
}
 
private Boolean isRssiLqiMessage(String description) {
	// "lastHopRssi: 91, lastHopLqi: 255, rssi: 91, lqi: 255"
	description ==~ /lastHopRssi:.*lastHopLqi:.*rssi:.*lqi:.*/
}
 
private Boolean isOrientationMessage(String description) {
	// "x: 0, y: 33, z: 1017, rssi: 102, lqi: 255"
	description ==~ /x:.*y:.*z:.*rssi:.*lqi:.*/
}