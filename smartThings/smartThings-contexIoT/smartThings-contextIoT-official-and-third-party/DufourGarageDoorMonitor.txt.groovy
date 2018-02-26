/**
 *  Dufour Garage Door Monitor
 *
 *  Copyright 2015 Greg Dufour
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
    name: "Dufour Garage Door Monitor",
    namespace: "Custom Garage Door Monitor",
    author: "Greg Dufour",
    description: "Monitors the garage door, and sends push notification and audible sound through Sonos warning when it is open.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png")

standardTile("myDevice", "device.myDeviceIcon", canChangeIcon: true)
{
    state "default", label: "", action: "refresh"
}

preferences {

	section("Monitor this door or window") {
		input "contact", "capability.contactSensor"
	}
	section("And notify me if it's open for more than this many minutes (default 10)") {
		input "openThreshold", "number", description: "Number of minutes", required: false
	}
    section("Delay between notifications (default 10 minutes") {
        input "frequency", "number", title: "Number of minutes", description: "", required: false
    }
	section("Via text message at this number (or via push notification if not specified") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone", "phone", title: "Phone number (optional)", required: false
    }
    }
	section( "chooseTrack", title: "Select a song or station")
	page(name: "timeIntervalInput", title: "Only during a certain time") {
		section {
			input "starting", "time", title: "Starting", required: false
			input "ending", "time", title: "Ending", required: false
		}
 	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(contact, "contact.open", doorOpen)
	subscribe(contact, "contact.closed", doorClosed)
}

def doorOpen(evt)
{
	log.trace "doorOpen($evt.name: $evt.value)"
	def t0 = now()
	def delay = (openThreshold != null && openThreshold != "") ? openThreshold * 60 : 600
	runIn(delay, doorOpenTooLong, [overwrite: false])
	log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def doorClosed(evt)
{
	log.trace "doorClosed($evt.name: $evt.value)"
}

def doorOpenTooLong() {
	def contactState = contact.currentState("contact")
    def freq = (frequency != null && frequency != "") ? frequency * 60 : 600

	if (contactState.value == "open") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = ((openThreshold != null && openThreshold != "") ? openThreshold * 60000 : 60000) - 1000
		if (elapsed >= threshold) {
			log.debug "Contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
            runIn(freq, doorOpenTooLong, [overwrite: false])
		} else {
			log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
	} else {
		log.warn "doorOpenTooLong() called but contact is closed:  doing nothing"
	}
}

private takeAction(evt) {

	log.trace "takeAction()"

	if (song) {
		sonos.playSoundAndTrack(state.sound.uri, state.sound.duration, state.selectedSong, volume)
	}
	else if (resumePlaying){
		sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
	}
	else {
		sonos.playTrackAndRestore(state.sound.uri, state.sound.duration, volume)
	}

	if (frequency || oncePerDay) {
		state[frequencyKey(evt)] = now()
	}
	log.trace "Exiting takeAction()"
}

void sendMessage()
{
	def minutes = (openThreshold != null && openThreshold != "") ? openThreshold : 10
	def msg = "${contact.displayName} has been left open for ${minutes} minutes."
	log.info msg
    if (message) {
				state.sound = textToSpeech(message instanceof List ? message[0] : message) // not sure why this is (sometimes) needed)
			}
			else {
				state.sound = textToSpeech("You selected the custom message option but did not enter a message in the $app.label Smart App")
			}
		
    if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (phone) {
            sendSms phone, msg
        } else {
            sendPush msg
        }
    }
}
