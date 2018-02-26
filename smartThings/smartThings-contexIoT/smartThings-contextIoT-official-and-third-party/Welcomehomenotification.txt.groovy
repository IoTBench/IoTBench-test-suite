/**
 *  Welcome home notification
 *
 *  Copyright 2014 Garrette Grouwstra
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
    name: "Welcome home notification",
    namespace: "",
    author: "Garrette Grouwstra",
    description: "Notify using Sonos when somebody comes home, and a door is opened",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)


preferences {

    section("When one of these people arrive at home") {
        input "people", "capability.presenceSensor", title: "Who?",
        multiple: true
    }
    section("When the door opens...") {
        input "contact1", "capability.contactSensor", title: "Where?"
    }

    section {
        input "sonos", "capability.musicPlayer", title: "Sonos Device",
        required: true
    }
    section("More options", hideable: false, hidden: false) {
		
			input "volume", "number", title: "Temporarily change volume", description: "0-100%", required: true
}
}

def installed() {
    subscribe(contact1, "contact.open", contactOpenHandler)

}

def updated() {

    unsubscribe()
    subscribe(contact1, "contact.open", contactOpenHandler)
}

def contactOpenHandler(evt) {

    sonos.on()
    def t0 = new Date()
    def t1 = new Date(t0.getTime() - (1000 * 60 * 2))
    def recentNotPresentState
	def message = ""
    def length = 5000

    for (person in people) {
        if (person.latestValue == "present")
            log.info("Looking for last status of ${person.displayName}")
        def states = person.statesSince("presence", t1)
        log.debug "${states?.size()} STATES FOUND, LAST AT ${states ? states[0].dateCreated : ''}"

        recentNotPresentState = states.find {
            it.value == "present"
        }
        try {
            log.info("user last here at ${recentNotPresentState.date}")
        } catch (ex) {}
        if (recentNotPresentState) {
            message = "Welcome home, ${person.displayName}"            
			break;
        } else {
            log.debug "skipping notification of arrival of ${person.displayName} because they have been here for a while"
        }
    }
	
    if (message.size() > 2){
	state.sound = textToSpeech(message instanceof List ? message : message)
            log.debug(message)
            
	} 
	else {
        log.debug("Nobody i know came home")
        state.sound = [uri: "http://s3.amazonaws.com/smartapp-media/sonos/a+door+opened.mp3", duration: "1"]
	
}
musicplayer()
}


private musicplayer() {
    log.info(state.sound.uri)
    sonos.playTrackAndResume(state.sound.uri, state.sound.duration, volume)
}