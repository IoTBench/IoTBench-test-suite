/**
 *  Copyright 2015 SmartThings
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
 *	DropCam Service Manager
 *
 *	Author: scott
 *	Date: 2013-08-15
 */

definition(
	name: "Dropcam (Connect)",
	namespace: "smartthings",
	author: "SmartThings",
	description: "Integrate your Dropcam cameras with SmartThings.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/dropcam.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/dropcam@2x.png"
)

preferences {
	page(name: "loginToDropcam", title: "Dropcam")
	page(name: "listAvailableCameras", title: "Dropcams")
}

def loginToDropcam() {
	def showUninstall = username != null && password != null
	return dynamicPage(name: "loginToDropcam", title: "Connect your Dropcam", nextPage:"listAvailableCameras", uninstall:showUninstall) {
		section("Log in to your Dropcam account:") {
			input "username", "text", title: "Username", required: true, autoCorrect:false
			input "password", "password", title: "Password", required: true, autoCorrect:false
		}
		section("To use Dropcam, SmartThings encrypts and securely stores your Dropcam credentials.") {}
	}
}

def listAvailableCameras() {
	def loginResult = forceLogin()

	if(loginResult.success)
	{
		state.cameraNames = [:]

		def cameras = getCameraList().inject([:]) { c, it ->
			def dni = [app.id, it.uuid].join('.')
			def cameraName = it.title ?: "Dropcam"

			state.cameraNames[dni] = cameraName
			c[dni] = cameraName

			return c
		}

		return dynamicPage(name: "listAvailableCameras", title: "Dropcams", install:true, uninstall:true) {
			section("Select which Dropcams to connect"){
				input(name: "cameras", type: "enum", required:false, multiple:true, metadata:[values:cameras])
			}
			section("Turn on which Lights when taking pictures")
				{
					input "switches", "capability.switch", multiple: true, required:false
				}
		}
	}
	else
	{
		log.error "login result false"
		return [errorMessage:"There was an error logging in to Dropcam"]
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

def uninstalled() {
	removeChildDevices(getChildDevices())
}

def initialize() {

	if(!state.suppressDelete)
	{
		state.suppressDelete = [:]
	}

	log.debug "settings: $settings"

	def devices = cameras.collect { dni ->

		def name = state.cameraNames[dni] ?: "Dropcam"

		def d = getChildDevice(dni)

		if(!d)
		{
			d = addChildDevice("smartthings", "Dropcam", dni, null, [name:"Dropcam", label:name])
			d.take()
			log.debug "created ${d.displayName} with id $dni"
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists"
		}

		return d
	}

	log.debug "created ${devices.size()} dropcams"

/* //Original Code seems to delete the dropcam that is being added */

	// Delete any that are no longer in settings
	def delete = getChildDevices().findAll { !cameras?.contains(it.deviceNetworkId) }
	removeChildDevices(delete)

/*
	log.debug devices

	devices.each{
	  log.debug "D:" + it.deviceNetworkId
	}

	def allChildren = getChildDevices()

	if(!devices) //if nothing selected, delete all child devices
	{
	  log.debug "delete all children"
	  allChildren.each{child->
		deleteChildDevice(child.deviceNetworkId)
	  }
	}
	else
	{
		allChildren.each{child -> //for each child
		  log.debug "Check Child: " + child.deviceNetworkId
		  def del = true
		  devices.each{dev -> //check if we want to keep the device id
			if(dev.deviceNetworkId == child.deviceNetworkId)
			  del = false
		  }
		  if(del)
		  {
			 log.debug "Delete:" + child.deviceNetworkId
			 deleteChildDevice(child.deviceNetworkId)
		  }
		}
	}
*/

}

private removeChildDevices(delete)
{
	log.debug "deleting ${delete.size()} dropcams"
	delete.each {
		state.suppressDelete[it.deviceNetworkId] = true
		deleteChildDevice(it.deviceNetworkId)
		state.suppressDelete.remove(it.deviceNetworkId)
	}
}

private List getCameraList()
{
	def cameraListParams = [
		uri: "https://www.dropcam.com",
		path: "/cameras/list",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()]
	]

	log.debug "cam list via: $cameraListParams"

	def multipleHtml
	def singleUrl
	def something
	def more
	httpGet(cameraListParams) { resp ->

		log.debug "getting camera list with cookie ${getCookieValue()}"

		something = resp.status
		more = "headers: " + resp.headers.collect { "${it.name}:${it.value}" }

		if(resp.status == 200)
		{
			multipleHtml = resp.data.toString()
		}
		else if(resp.status == 302)
		{
			singleUrl = resp.headers.Location.value
		}
		else
		{
			// ERROR
			log.error "camera list: unknown response"
		}

	}

	log.debug "list: after getting cameras: " + [url:singleUrl, html:multipleHtml?.size(), something:something, more:more]
	// log.debug "cameras - ITS FULL ON: $multipleHtml"

	if(singleUrl)
	{
		// TODO: Can we get the name if there's only one?
		log.debug "list: returning one Dropcam: " + singleUrl.split("/").last()
		return [ [name:"Dropcam", uuid:singleUrl.split("/").last()] ]
	}
	else if(multipleHtml)
	{
		def singleJsonStringMarker = "DC.util.setConfig('viewerParams',"
		def singleJsonStringMarker2 = "DC.viewer.show({"
		def listJsonStringMarker = "DC.util.setConfig('ownedCamerasJson', "

		def singleJsonStringStart = multipleHtml.indexOf(singleJsonStringMarker)
		def singleJsonStringStart2 = multipleHtml.indexOf(singleJsonStringMarker2)
		def listJsonStringStart = multipleHtml.indexOf(listJsonStringMarker)

		if(singleJsonStringStart > -1)
		{
			singleJsonStringStart += singleJsonStringMarker.size()

			def singleJsonStringEnd = multipleHtml.indexOf("});", singleJsonStringStart + 1)
			def singleJsonString = multipleHtml.substring(singleJsonStringStart, singleJsonStringEnd + 1)

			log.debug "parsing json for single cam as >${singleJsonString.trim()}<"

			def cam = new org.codehaus.groovy.grails.web.json.JSONObject(singleJsonString.trim())

			log.debug "list: returning 1 Dropcam "

			return [ [title:cam.title, uuid:cam.cameraUuid] ]
		}
		else if(singleJsonStringStart2 > -1)
		{
			singleJsonStringStart2 += singleJsonStringMarker2.size() - 1

			def singleJsonStringEnd = multipleHtml.indexOf("});", singleJsonStringStart2 + 1)
			def singleJsonString = multipleHtml.substring(singleJsonStringStart2, singleJsonStringEnd + 1)

			log.debug "parsing json for single cam as >${singleJsonString.trim()}<"

			def cam = new org.codehaus.groovy.grails.web.json.JSONObject(singleJsonString.trim())

			log.debug "list: returning 1 Dropcam "

			return [ [title:cam.title, uuid:cam.uuid] ]
		}
		else if(listJsonStringStart > -1)
		{
			listJsonStringStart += listJsonStringMarker.size()

			def listJsonStringEnd = multipleHtml.indexOf("\n", listJsonStringStart + 1)
			def listJsonString = multipleHtml.substring(listJsonStringStart, listJsonStringEnd)

			log.debug "parsing json for single cam as >${listJsonString.trim()}<"

			def a = new org.codehaus.groovy.grails.web.json.JSONArray(listJsonString.trim())

			log.debug "list: returning ${a.size()} Dropcams "

			return a.collect { [title:it.title, uuid:it.uuid] }
		}
		else
		{
			log.warn "camera list got html with no 'ownedCamerasJson'"
		}
	}

	// ERROR?
	return []
}

def removeChildFromSettings(child)
{
	def device = child.device

	def dni = device.deviceNetworkId
	log.debug "removing child device $device with dni ${dni}"

	if(!state?.suppressDelete?.get(dni))
	{
		def newSettings = settings.cameras?.findAll { it != dni } ?: []
		app.updateSetting("cameras", newSettings)
	}
}

private forceLogin() {
	updateCookie(null)
	login()
}


private login() {

	if(getCookieValueIsValid())
	{
		return [success:true]
	}
	return doLogin()
}

private doLogin() {
	def loginParams = [
		uri: "https://www.dropcam.com",
		path: "/api/v1/login.login",
		headers: ['User-Agent': validUserAgent()],
		requestContentType: "application/x-www-form-urlencoded",
		body: [username: username, password: password]
	]

	def result = [success:false]

	httpPost(loginParams) { resp ->
		if (resp.status == 200 && resp.headers.'Content-Type'.contains("application/json"))
		{
			log.debug "login 200 json headers: " + resp.headers.collect { "${it.name}:${it.value}" }
			def cookie = resp?.headers?.'Set-Cookie'?.split(";")?.getAt(0)
			if (cookie) {
				log.debug "login setting cookie to $cookie"
				updateCookie(cookie)
				result.success = true
			}
			else
			{
				// ERROR: any more information we can give?
				result.reason = "Bad login"
			}
		}
		else
		{
			// ERROR: any more information we can give?
			result.reason = "Bad login"
		}


	}

	return result
}

def takePicture(String dni, Integer imgWidth=null)
{

	//turn on any of the selected lights that are off
	def offLights = switches.findAll{(it.currentValue("switch") == "off")}
	log.debug offLights
	offLights.collect{it.on()}

	log.debug "parent.takePicture(${dni}, ${imgWidth})"

	def uuid = dni?.split(/\./)?.last()

	log.debug "taking picture for $uuid (${dni})"

	def imageBytes
	def loginRequired = false

	try
	{
		imageBytes = doTakePicture(uuid, imgWidth)
	}
	catch(Exception e)
	{
		log.error "Exception $e trying to take a picture, attempting to login again"
		loginRequired = true
	}

	if(loginRequired)
	{
		def loginResult = doLogin()
		if(loginResult.success)
		{
			// try once more
			imageBytes = doTakePicture(uuid, imgWidth)
		}
		else
		{
			log.error "tried to login to dropcam after failing to take a picture and failed"
		}
	}

	//turn previously off lights to their original state
	offLights.collect{it.off()}
	return imageBytes
}

private doTakePicture(String uuid, Integer imgWidth)
{
	imgWidth = imgWidth ?: 1280

	def takeParams = [
		uri: "https://nexusapi.dropcam.com",
		path: "/get_image",
		headers: [Cookie: getCookieValue(), 'User-Agent': validUserAgent()],
		requestContentType: "application/x-www-form-urlencoded",
		query: [width: imgWidth, uuid: uuid]
	]

	def imageBytes

	try {
		httpGet(takeParams) { resp ->
			if (resp.status == 200 && resp.headers.'Content-Type'.contains("image/jpeg")) {
				imageBytes = resp.data
			} else {
				log.error "unknown takePicture() response: ${resp.status} - ${resp.headers.'Content-Type'}"
			}
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "Dropcam get_image failure: ${e} with status: ${e.statusCode}"
		if (e.statusCode == 403) {
			throw new RuntimeException("Login Required")
		} else if (e.statusCode == 404) {
			log.error "Dropcam 404, camera may be offline"
		}
	} catch (Exception e) {
		log.error "Unexpected Dropcam exception", e
		//sendNotification("Your dropcam is offline.")
	}

	return imageBytes
}

private Boolean getCookieValueIsValid()
{
	// TODO: make a call with the cookie to verify that it works
	return getCookieValue()
}

private updateCookie(String cookie) {
	atomicState.cookie = cookie
	state.cookie = cookie
}

private getCookieValue() {
	state.cookie
}

private validUserAgent() {
	"curl/7.24.0 (x86_64-apple-darwin12.0) libcurl/7.24.0 OpenSSL/0.9.8x zlib/1.2.5"
}
