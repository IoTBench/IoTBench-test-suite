/**
 *  Leak Stopper
 *
 *  Copyright 2016 Louis Jackson
 *
 *  Version 1.0.1   31 Jan 2016
 *
 *	Version History
 *
 *	1.0.2   22 Dec 2016		Added Sonos speaker support by Lou Jackson
 *	1.0.1   31 Jan 2016		Added version number to the bottom of the input screen
 *	1.0.0	28 Jan 2016		Added to GitHub
 *	1.0.0	27 Jan 2016		Creation :-)
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
import groovy.time.* 
 
definition(
    name: "Leak Stopper",
    namespace: "lojack66",
    author: "Louis Jackson",
    description: "Turn off switch when wet and on when dry.",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png"
    )

preferences {
	section("Select Things to Control:") 
    {
		input "lsensor", "capability.waterSensor", title: "When water is detected...", required: true
		input "offswitches", "capability.switch", title: "Turn off...", multiple: true
        input "onswitches", "capability.switch", title: "Turn on...", multiple: true
	}

	section ("Additionally", hidden: hideOptionsSection(), hideable: true) 
    {
        input "sonos", "capability.musicPlayer", title: "On this Speaker player", required: false
        input "SonosMsg","text", title: "Leak Notification message", description: "Leak detection message", required: false
	}
    
    section ("Version 1.0.2") {}
}

def installed() {
	log.trace "(0A) ${app.label} - installed() - settings: ${settings}"
	initialize()
}

def updated() {
	log.info "(0B) ${app.label} - updated()"
	unsubscribe()
	initialize()
}

def initialize() {
	log.info "(0C) ${app.label} - initialize()"
   	subscribe(app, appTouchHandler)   //In-App button to play Start Message
    subscribe(lsensor, "water.dry", waterHandler)
	subscribe(lsensor, "water.wet", waterHandler)
}

def appTouchHandler(evt) 
{
	if (sonos) sonos.playTextAndResume("${SonosMsg} at the ${lsensor.label}", 100)
}

/* =====================================================================================================
 * FUNTION: waterHandler()
 *
 * Turn off power if wet
 * Turn on power if dry
 * =====================================================================================================
 */
def waterHandler(evt) 
{   
	def strMessage = ""
    
    offswitches.each 
    {
    	log.debug "(0D) ${app.label} - ${it.label} is ${it.latestState("switch").value}"
        
        if (evt.value == "wet") 
        {
        	it.off()
            strMessage = "${SonosMsg} - ${lsensor.label}"
        }
        else
        {
            it.on()
        }
    }
    
    onswitches.each 
    {
    	log.debug "(0E) ${app.label} - ${it.label} is ${it.latestState("switch").value}"
        if (evt.value == "wet") 
        {
        	it.on()
            strMessage = "${SonosMsg} - ${lsensor.label}"
        }
    }
    
	if (sonos && strMessage) 
	{
        log.info "(0F) ${evt.value} - ${evt.name} - ${lsensor.label}"
		state.sound = textToSpeech(strMessage, true)
		sonos.playTrackAndResume(state.sound.uri, state.sound.duration, 100)
	}
}

private hideOptionsSection() 
{
  (sonos) ? false : true
}