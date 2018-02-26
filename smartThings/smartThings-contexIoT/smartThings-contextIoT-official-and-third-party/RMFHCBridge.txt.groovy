/**
 *  smartthings.groovy
 *
 *
 * Disclaimers - this code has evolved as I learn and only makes sense in concert with the apps running on my desktop
 *               I'm also learning Groovy as I go and guess
 *               Also be warned -- any Github copy is an old version of the one I'm actively updating
 *
 * Based on sample from
 *  David Janes
 *  (But very heavily modified by Bob Frankston)
 *  IOTDB.org
 *  2014-02-01
 *
 *  Allow control of your SmartThings via an API;
 *  MQTT moved to a separate file
 *
 *  Follow us on Twitter:
 *  - @iotdb
 *  - @dpjanes
 *
 *  A work in progress!
 *
 *  This is for SmartThing's benefit. There's no need to
 *  change this unless you really want to
 */

 // Notes on web services
 // http://docs.smartthings.com/en/latest/smartapp-web-services-developers-guide/implementation.html#developing-an-api-access-application
 // GET https://graph.api.smartthings.com/oauth/authorize?response_type=code&client_id=myclient&scope=app&redirect_uri=https%3A%2F%2Fgraph.api.smartthings.com%2Foauth%2Fcallback

 // https://graph.api.smartthings.com/ide/doc/capabilities
 // https://graph.api.smartthings.com/ide/doc/device
  
 
 def myself() {
	 state.version = "144c";
	 return "RMFHCBridge/${state.version}";
 }

definition(
		name: "RMFHCBridge",
		namespace: "",
		author: "Bob Frankston",
		description: "Bridge for RMF Home Control.",
		category: "My Apps",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
		oauth: true)

/* --- IOTDB section --- */
/*
 *
 *  The values below can be copied from this page
 *  - https://iotdb.org/playground/mqtt/bridge
 *
 *  Make sure you are logged into IOTDB first
 */

/* Removed to protect the guilty */

/* --- setup section --- */
/*
 *  The user
 *  Make sure that if you change anything related to this in the code
 *  that you update the preferences in your installed apps.
 *
 *  Note that there's a SmartThings magic that's _required_ here,
 *  in that you cannot access a device unless you have it listed
 *  in the preferences. Access to those devices is given through
 *  the name used here (i.e. d_*)
 */

// Capabilities https://graph.api.smartthings.com/ide/doc/capabilities

preferences {
	section("Allow RMFHCBridge to Control & Access These Things...") {
		input "d_switch", "capability.switch", title: "Switch", multiple: true
		input "d_switchlevel", "capability.switchLevel", title: "SwitchLevel", multiple: true, required: false
		input "d_relayswitch", "capability.relaySwitch", title: "relaySwitch", multiple: true, required: false
		input "d_color", "capability.colorControl", title: "Color Control", multiple: true, required: false
		input "d_motion", "capability.motionSensor", title: "Motion", required: false, multiple: true
	 	input "d_temperature", "capability.temperatureMeasurement", title: "Temperature", multiple: true, required: false
		input "d_contact", "capability.contactSensor", title: "Contact", required: false, multiple: true
		input "d_acceleration", "capability.accelerationSensor", title: "Acceleration", required: false, multiple: true
		input "d_presence", "capability.presenceSensor", title: "Presence", required: false, multiple: true
		input "d_lock", "capability.lock", title: "Lock", multiple: true, required: false
		input "d_battery", "capability.battery", title: "Battery", multiple: true
		input "d_threeAxis", "capability.threeAxis", title: "3 Axis", multiple: true, required: false
		input "d_energy", "capability.energyMeter", title: "Energy Meter", multiple: true, required: false
	    input "d_button", "capability.button", title: "Button", multiple: true, required: false
		}
}

/*
 input "d_alarm", "capability.alarm", title: "alarm", multiple: true
 input "d_configuration", "capability.configuration", title: "configuration", multiple: true
 input "d_illuminanceMeasurement", "capability.illuminanceMeasurement", title: "illuminanceMeasurement", multiple: true
 input "d_polling", "capability.polling", title: "polling", multiple: true
 input "d_relativeHumidityMeasurement", "capability.relativeHumidityMeasurement", title: "relativeHumidityMeasurement", multiple: true
 input "d_thermostatCoolingSetpoint", "capability.thermostatCoolingSetpoint", title: "thermostatCoolingSetpoint", multiple: true
 input "d_thermostatFanMode", "capability.thermostatFanMode", title: "thermostatFanMode", multiple: true
 input "d_thermostatHeatingSetpoint", "capability.thermostatHeatingSetpoint", title: "thermostatHeatingSetpoint", multiple: true
 input "d_thermostatMode", "capability.thermostatMode", title: "thermostatMode", multiple: true
 input "d_thermostatSetpoint", "capability.thermostatSetpoint", title: "thermostatSetpoint", multiple: true
 input "d_threeAxisMeasurement", "capability.threeAxisMeasurement", title: "threeAxisMeasurement", multiple: true
 input "d_waterSensor", "capability.waterSensor", title: "waterSensor", multiple: true
 lqi: 100 %
 acceleration: inactive
 threeAxis: -38,55,1021
 battery: 88 %
 temperature: 65 F
 */

/*
 *  The API
 *  - ideally the command/update bit would actually
 *    be a PUT call on the ID to make this restful
 * Also see https://support.smartthings.com/hc/en-us/articles/200901780-Sample-Web-Services
 */

mappings {
//	path("/setHost/:host") {
//		action: [
//			PUT: "_api_putHost"
//		]
//	}
	path("/:type") {
		action: [
			GET: "_api_list"
		]
	}
	path("/:type/:id") {
		action: [
			GET: "_api_get",
			PUT: "_api_put"
		]
	}
}


def msg(txt) {
	log.debug "${myself()}: ${txt}"
}

/*
 *  This function is called once when the app is installed
 */
def installed() {
	msg "Installed with settings: ${settings}"
	_event_subscribe()
}

/*
 *  This function is called every time the user changes
 *  their preferences
 */

def updated() {
	msg "Updated with settings: ${settings}"
	unsubscribe()
	_event_subscribe()
}

/* --- event section --- */

/*
 *  What events are we interested in. This needs
 *  to be in it's own function because both
 *  updated() and installed() are interested in it.
 */
def _event_subscribe() {
	subscribe(app, getURL)
	getURL(null)
	
	subscribe(d_switchlevel, "switchLevel", "_on_event")
	subscribe(d_switch, "switch", "_on_event")
	subscribe(d_relayswitch, "relaySwitch", "_on_event")
	subscribe(d_color, "colorControl", "_on_event")
	subscribe(d_motion, "motion", "_on_event")
	subscribe(d_temperature, "temperature", "_on_event")
	subscribe(d_contact, "contact", "_on_event")
	subscribe(d_acceleration, "acceleration", "_on_event")
	subscribe(d_presence, "presence", "_on_event")
	subscribe(d_battery, "battery", "_on_event")
	subscribe(d_threeAxis, "threeAxis", "_on_event")
	subscribe(d_lock, "lock", "_on_event")
	subscribe(d_energy, "energyMeter", "_on_event")
	subscribe(d_button, "button", "_on_event")
}

def getURL(e) {	// From ActionDashApp
	msg "getURL(${e})"
	if (resetOauth) {
		msg "Resetting Access Token"
		state.accessToken = null
	}
	
	// http://docs.smartthings.com/en/latest/cloud-and-lan-connected-device-types-developers-guide/building-cloud-connected-device-types/building-the-service-manager.html
	if (!state.accessToken) {
		try {
			createAccessToken()
			msg "Creating new Access Token: $state.accessToken"
		} catch (ex) {
			msg "Did you forget to enable OAuth in SmartApp settings for this app Dashboard?"
			msg ex
		}
	}
	if (state.accessToken) {
		def url1 = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}/ui"
		def url2 = "?access_token=${state.accessToken}"
		msg "${title ?: location.name} URL: $url1$url2"
		sendURL(url1, url2, "bridge")
		/*if (phone) {
			sendSmsMessage(phone, url1)
			sendSmsMessage(phone, url2)
		}*/
	}
}

def sender(where, pollParams) {
	try {
//		pollParams['uri'] = "http://aaz.lt"
//		pollParams['headers'] = ["content-Type": "text/json"]
		msg "Sender(${where}) uri(${pollParams.uri}) path(${pollParams.path})"
		msg "JS: ${pollParams.body}"
		httpPostJson(pollParams) {resp ->
			if (resp.data) msg "PUT Response ${resp.data}";
			if (resp.status != 200) msg "PUT Push Error $resp.status";
		}
		msg "Sender(${where})/5"
		msg "${where} sent"
		
	}
	catch (e) {
		msg "sender(${where}) error: ${e}\n"
//		try {
//			//var host = pollParams.uri.toURL().host
//			var ipa = InetAddress.getByName("aaz.lt") // .address.collect{it & 0xFF}.join('.')
//			msg "Sender(${where}) Host address ${ipa}"
//		}
//		catch (ee) {
//			msg "Error getting IP address ${ee}"
//		}
	}
}

def sendURL(urlx, parmsx, app) {
	try {
		msg "sendURL(${urlx}, ${parmsx})"
		def js = ["url": urlx.toString(), parms: parmsx.toString(), app: app.toString(), version: state.version]
		msg "sendURL: js ${js}"
		def pollParams = [
			uri: "http://aaz.lt",
			path: "/st/url",
			headers: ["content-Type": "text/json"],
			body: js
			];
		sender("sendURL", pollParams)
//		msg "Posting ${pollParams}"
//		httpPostJson(pollParams) {resp ->
//			if (resp.data) msg "PUT Response ${resp.data}";
//			if (resp.status != 200) msg "PUT Push Error $resp.status";
//		}
//		msg "sendURL: Did PostJSON URL ${urlx}";
	}
	catch (e) {
		msg "sendURL: Error sending URL: ${e}\n"
	}
}

/*
 *  This function is called whenever something changes.
 *  Right now it just
 */
def _on_event(evt) {
	// https://graph.api.smartthings.com/ide/doc/event

    msg "_on_event XXX event.id=${evt?.id} event.deviceId=${evt?.deviceId} event.isStateChange=${evt?.isStateChange} event.name=${evt?.name}"

	try {
		def dt = _device_and_type_for_event(evt)

		if (!dt) {
			msg "_on_event deviceId=${evt.deviceId} not found?"
			return;
		}

		def jd = _device_to_json(dt.device, dt.type)
		// msg "_on_event deviceId=$jd"
		jd["current"] = null;		// Work-around for now
//		try {
//			jd["event"] = evt.jsonValue
////			isChange: evt.isStateChange(),
////			raw: evt.description,
////			desc: evt.descriptionText,
////			value: evt.value,
////			name: evt.name,
////			unit: evt.unit,
////			js:  evt.jsonValue
////			]
//		}
//		catch (e) {
//			msg("Error getting evt value ${e}")
//		}

		_send_pushingbox jd
		//_send_mqtt(dt.device, dt.type, jd)
	}
	catch (e) {
		msg "_on_event error ${e}"
	}
}

/* --- API section --- */
def _api_list() {
	try {
		if (params.type == "ui") {
			return [msg: "ui is not a type"]
		}
		//msg "_api_list for ${params.type}"
	    def jx = _devices_for_type(params.type).collect{
			_device_to_json(it, params.type)
		}
		return jx;
	}
	catch (e) {
		msg("_api_list error ${e}")
		return [msg: "Error ${e}"]
	}
}

def _api_put() {
	def devices = _devices_for_type(params.type)
	msg "_api_put Type ${params.type} id=${params.id}"
    def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	} else {
		_device_command(device, params.type, request.JSON)
		_device_to_json(device, params.type);	// can this return a value?
	}
}

def _api_get() {
	def devices = _devices_for_type(params.type)
	msg "_api_get Type ${params.type} id=${params.id}"
	def device = devices.find { it.id == params.id }
	if (!device) {
		httpError(404, "Device not found")
	} else {
		_device_to_json(device, type);
	}
}

//def _api_putHost() {
//	try {
//		state.host = $params.host
//		msg "_api_putHost[${state.host}]"
//	}
//	catch (e) {
//		msg "Error: _api_putHost ${e}"
//	}
//}

// void _api_update() {
//    _do_update(_devices_for_type(params.type), params.type)
// }

/*
 *  I don't know what this does but it seems to be needed
 */
def deviceHandler(evt) {
	msg "Device handler called: ${evt}"
}

/* --- communication section: not done --- */

/*
 *  An example of how to get PushingBox.com to send a message
 */

def _send_pushingbox(js) {
	msg "_send_pushingbox(${js})"

	// http://docs.smartthings.com/en/latest/smartapp-developers-guide/calling-web-services-in-smartapps.html#httpget-example]

	//    httpGet("http://aaz.lt/st/?msg=${msg}")
	//    devinfo because this is a message with information about a device
	//    later we may introduce action messages

	js['version'] = state.version	// Identify the version for debugging and whatever
	try {
		def pollParams = [
			uri: "http://aaz.lt",
			path: "/st/devinfo",
			headers: ["Content-Type": "text/json"],
			body: js
		];
		sender("_send_pushingbox", pollParams)
	
//		try {
//			msg("Posting ${pollParams}");
////			pollParams.remove("query"); // Because we put it in the body
////			pollParams['body'] = js;
//			httpPostJson(pollParams) { resp ->
//				if (resp.data) msg "PUT Response ${resp.data}";
//				if (resp.status != 200) msg "PUT Push Error $resp.status";
//			}
//			msg "Did PostJSON";
//		}
//		catch (e) {
//			msg "httpPostJson Error: ${e}\nSending ${js}"
//		}
	}
	catch(e) {
		msg "Error in _send_pushingbox: ${e}"
	}
}

/* --- internals --- */
/*
 *  Devices and Types Dictionary
 */
def _dtd() {
	[
		switch: d_switch,
		switchLevel: d_switchlevel,
		relaySwitch: d_relayswitch,
		colorControl: d_color,
		motion: d_motion,
		temperature: d_temperature,
		contact: d_contact,
		acceleration: d_acceleration,
		presence: d_presence,
		battery: d_battery,
		threeAxis: d_threeAxis,
		lock: d_lock,
		button: d_button,
		energyMeter: d_energy
	]
}

def _devices_for_type(type) {
    _dtd()[type]
}

def _device_and_type_for_event(evt) {
	def dtd = _dtd()

	for (dt in _dtd()) {
		if (dt.key != evt.name) {
			continue
		}

		def devices = dt.value
		for (device in devices) {
			if (device.id == evt.deviceId) {
				return [ device: device, type: dt.key ]
			}
		}
	}
}

def stateNames () { ["alarm", "battery", "button", "carbonMonoxide",
	"color", "contact", "door", "energy", "hue", "illuminance",
	"indicatorStatus", "level", "lock", "mode", "motion", "power",
	"presence", "rssi", "saturation", "smoke", "switch", "temperature",
	"threeAxis", "touch", "water"]}
	
def stateNames2() { ["activities", "channel", "codeChanged",
	"codeReport", "coolingSetpoint", "currentActivity", "goal",
	"heatingSetpoint", "humidity", "image", "lqi", "movieMode", "mute",
	"phraseSpoken", "picture", "sleeping", "sound", "status", "steps",
	"thermostatFanMode", "thermostatMode", "thermostatOperatingState",
	"thermostatSetpoint", "trackData", "trackDescription", "volume"]}
/**
 *  Do a device command
 */

private _device_command(device, type, jsond) {
	try {
		msg "_device_command: device=${device.id} name=${device.displayName} type=${type} json=${jsond}"
		if (!device) {
			return;
		}
		if (!jsond) {
			return;
		}
		
		switch (type) {
			//    if (type == "switch") {
			case "relaySwitch":
			case "switch":
				def n = jsond['switch'] ?: jsond['value'];
				switch (n) {
					case 'on':
					case '1':
						device.on()
						device.setLevel(99)
						break;

					case 'off':
					case '0':
						device.off()
						break;

					default:
						msg "Invalid ${type} value ${n}"
				}
				break;
		    case "lock":
				def n = jsond['lock'] ?: jsond['value'];
				switch (n) {
					case "locked":
					case "lock":
					case 'on':
					case '1':
					    msg "Locking ${device.displayName}"
						device.lock()
						break;

					case "unlocked":
				    case "unlock":
				    case 'off':
					case '0':
						msg "Unlocking ${device.displayName}"
						device.unlock()
						break;

					default:
						msg "Invalid ${type} value ${n}"
				}
				break;
		    case "switchLevel":
				def level = jsond['level'].toInteger()	// 0 to 100 or actually 99
				msg "setting level for ${device.displayName} to ${level}"
				if (level == 0)
					device.off()
			    else
				    device.on()
				device.setLevel(Math.min((level as Integer), 99))
				break;
			case "colorControl":
				msg "setting color ... not yet implemented" 
			default:
				msg "_device_command: device type=${type} doesn't take commands"
		}
	}
	catch (e) {
		msg "_device_command: device type=${type} with command ${jsond}"
	}
}

/*
 *  Convert a single device into a JSONable object
 */

private _device_to_json(device, type) {
//	msg("_device_to_json ${device} ${type}")
	if (!device) {
		return ["error": "No Device", "type": type]
	}

	def vd = [:]
    def cs = [:]
	def ls = [:]
	def jd = [id: device.id,
		label: device.label,
		type: type,
		displayName: device.displayName,
		value: vd,
        current: cs,
		latest: ls,
		nid: device.deviceNetworkId];
//	jd["sa"] = device.supportedAttributes;
//	jd["sc"] = device.supportedCommands;

	// https://graph.api.smartthings.com/ide/doc/state

    try {
//		msg "Mapping over ${stateNames()}"
		stateNames().each {
			if (device.currentState(it)) cs[it] = device.currentState(it);
//			if (device.latestState(it)) ls[it] = device.latestState(it);
		}
    }
    catch (e) {
        cs['error'] = e;
    }

	try {
		def s = device.currentState(type)
		vd['timestamp'] = s?.isoDate
		vd['value'] = s?.value
		vd['name'] = s?.name
		//vd['s'] = s; -- seems to sometimes screw-up posting?
		//vd['d'] = device
		//vd['supported'] = device.supportedAttributes
		
		if (s?.hasProperty('level')) vd['level'] = s?.level
		
		switch (type) {
			case "switch":
				vd['switch'] = s?.value == "on"
				break
			case  "motion":
				vd['motion'] = s?.value == "active"
				break
			case  "temperature":
				vd['temperature'] = s?.value.toFloat()
				break
			case  "contact":
				vd['contact'] = s?.value == "closed"
				break
			case  "acceleration":
				vd['acceleration'] = s?.value == "active"
				break
			case  "presence":
				vd['presence'] = s?.value == "present"
				break
			case  "battery":
			    def bat = s?.value?.toFloat();
				if (bat) bat /= 100.0;
				vd['battery'] = bat;
				break
			case  "threeAxis":
				vd['x'] = s?.xyzValue?.x
				vd['y'] = s?.xyzValue?.y
				vd['z'] = s?.xyzValue?.z
				break
			case "lock":
			// vd['rest'] = s?
				break
			case "switchLevel":
				//vd['level'] = s?.level
				vd['level'] = device.currentValue('level')
				break;
			case "colorControl":
				vd['saturation'] = s?.saturation
				vd['hue'] = s?.hue
				vd['color'] = s?.color
				vd['switch'] = s?.switch
				vd['level'] = s?.level
				break;
			case "energyMeter":
				vd['energy'] = s?.energy
				break;
		    case "button":
				break;
			case "relaySwitch":
				break;
			default:
				msg("Unrecognized type ${type}")
		}
	} catch (e) {
		vd["Error"] = e;
		msg("Failure in device to JSON ${e}")
	}
	return jd;
}
