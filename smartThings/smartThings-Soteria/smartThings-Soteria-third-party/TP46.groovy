/**
 *  Laundry Monitor with Sonos Custom Message
 *
 *  Eric Bushmeier
 *  bewshy[at]live.com
 *
 *  This is a combination of the SmartThings Laundry Monitor and Sonos Custom Message SmartApp.
 *  With this app you can generate a custom message over your Sonos device when your 
 *  laundry is finished.  Feel free to send me a quick comment if you find this useful.
 * 
 *  Original Source:
 *  Sonos Custom Message
 *  Author: SmartThings
 *  Date: 2014-1-29
 *
 *  Laundry Monitor
 *  Author: SmartThings
 *  Date: 2013-02-21
 */
definition(
    name: "Laundry Monitor with Sonos Custom Message",
    description: "Laundry Monitor with Sonos Custom Message",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    )


preferences {
	section("Tell me when this washer/dryer has stopped..."){
		input "sensor1", "capability.accelerationSensor"
	}
	section("Via this number (optional, sends push notification if not specified)"){
		input "phone", "phone", title: "Phone Number", required: false
        input "message","text",title:"Play this message", required:false, multiple: false
	}
	section("Time thresholds (in minutes, optional)"){
		input "cycleTime", "decimal", title: "Minimum cycle time", required: false, defaultValue: 10
		input "fillTime", "decimal", title: "Time to fill tub", required: false, defaultValue: 5
	}
    section("Select your Sonos Device"){
		input "sonos", "capability.musicPlayer", title: "On this Sonos player", required: false
    }
    section("Select a message to be played"){
		input "message","text",title:"Play this message", required: false, multiple: false
		
	}
    section("More options", hideable: true, hidden: true) {
		input "resumePlaying", "bool", title: "Resume currently playing music after notification", required: false, defaultValue: true
		href "chooseTrack", title: "Or play this music or radio station", description: song ? state.selectedSong?.station : "Tap to set", state: song ? "complete" : "incomplete"
		input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: false
	}
}

def installed()
{
	initialize()
}

def updated()
{
	unsubscribe()
	initialize()
}

def initialize() {
	loadText()
	subscribe(sensor1, "acceleration.active", accelerationActiveHandler)
	subscribe(sensor1, "acceleration.inactive", accelerationInactiveHandler)
}

def accelerationActiveHandler(evt) {
	log.trace "vibration"
	if (!state.isRunning) {
		log.info "Arming detector"
		state.isRunning = true
		state.startedAt = now()
	}
	state.stoppedAt = null
}

def accelerationInactiveHandler(evt) {
	log.trace "no vibration, isRunning: $state.isRunning"
	if (state.isRunning) {
		log.debug "startedAt: ${state.startedAt}, stoppedAt: ${state.stoppedAt}"
		if (!state.stoppedAt) {
			state.stoppedAt = now()
			runIn(fillTime * 60, checkRunning, [overwrite: false])
		}
	}
}


private takeAction(evt) {

	log.trace "takeAction()"

	if (resumePlaying){
		sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
	}
	else {
		sonos.playTrackAndRestore(state.sound.uri, state.sound.duration, volume)
	}

	log.trace "Exiting takeAction()"
}

def checkRunning() {
	log.trace "checkRunning()"
	if (state.isRunning) {
		def fillTimeMsec = fillTime ? fillTime * 60000 : 300000
		def sensorStates = sensor1.statesSince("acceleration", new Date((now() - fillTimeMsec) as Long))

		if (!sensorStates.find{it.value == "active"}) {

			def cycleTimeMsec = cycleTime ? cycleTime * 60000 : 600000
			def duration = now() - state.startedAt
			if (duration - fillTimeMsec > cycleTimeMsec) {
				log.debug "Sending notification"
				takeAction(evt)
				def msg = "${sensor1.displayName} is finished"
				log.info msg

				if (phone) {
					sendSms phone, msg
				} else {
					sendPush msg
				}

			} else {
				log.debug "Not sending notification because machine wasn't running long enough $duration versus $cycleTimeMsec msec"
			}
			state.isRunning = false
			log.info "Disarming detector"
		} else {
			log.debug "skipping notification because vibration detected again"
		}
	}
	else {
		log.debug "machine no longer running"
	}
}

private loadText() {
			if (message) {
				state.sound = textToSpeech(message instanceof List ? message[0] : message) // not sure why this is (sometimes) needed)
			}
			else {
				state.sound = textToSpeech("You selected the custom message option but did not enter a message in the $app.label Smart App")
			}
	}
