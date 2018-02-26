/*************************************************************************************
*  Hue Party Mode
*
*  Author: Mitch Pond
*  Date: 2015-05-29

Copyright (c) 2015, Mitch Pond
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*************************************************************************************/

definition(
    name: "Hue Party Mode",
    namespace: "mitchpond",
    author: "Mitch Pond",
    description: "Change the color of your lights randomly at an interval of your choosing.",
    category: "Fun & Social",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-ItsPartyTime.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/FunAndSocial/App-ItsPartyTime@2x.png",
)

preferences {
    section("Choose lights..."){
        input "lights", "capability.colorControl", title: "Pick your lights", required: false, multiple: true
    }
    section("Adjust color change speed and timeout"){
        input "interval", "number", title: "Color change interval (seconds)",   required: false, defaultValue: 10
        input "timeout",  "number", title: "How long to run (minutes)", required: false, defaultValue: 60
    }
}

def installed() {
    settings.interval = 10    //default value: 10 seconds
    settings.timeout  = 60    //default value: 60 minutes
    state.running     = false
    log.debug("Installed with settings: ${settings}")
    updated()
}

def updated() {
    log.debug("Updated with settings: ${settings}")
    unsubscribe()
    subscribe(app, onAppTouch)
    for (light in lights) {
    	subscribeToCommand(light, "off", onLightOff)
    }
    
    
}

def onLightOff(evt) {
    //if one of the lights in our device list is turned off, and we are running, unschedule any pending color changes
    if (state.running) {
        log.info("${app.name}: One of our lights was turned off.")
        stop()
    }
}

def onAppTouch(evt) {
    //if currently running, unschedule any scheduled function calls
    //if not running, start our scheduling loop

    if (state.running) {
        log.debug("${app.name} is running.")
        stop()
    }
    else if (!state.running) {
        log.debug("${app.name} is not running.")
        start()
    }

}

def changeColor() {
    if (!state.running) return  //just return without doing anything in case unschedule() doesn't finish before next function call

    //calculate a random color, send the setColor command, then schedule our next execution
    log.info("${app.name}: Running scheduled color change")
    def nextHue = new Random().nextInt(101)
    def nextSat = new Random().nextInt(51)+50
    //def nextColor = Integer.toHexString(new Random().nextInt(0x1000000))
    log.debug nextColor
    lights*.setColor(hue: nextHue, saturation: nextSat)
    runIn(settings.interval, changeColor)
}

def start() {
    log.debug("${app.name}: Beginning execution...")
    state.running = true
    lights*.on()
    changeColor()
    runIn(settings.timeout*60, stop)
}

def stop() {
    log.debug("${app.name}: Stopping execution...")
    unschedule()
    state.running = false
}
