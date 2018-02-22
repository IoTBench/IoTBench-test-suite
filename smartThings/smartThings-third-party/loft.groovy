/**
 *  Copyright 2015 Jesse Newland
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
    name: "Loft",
    namespace: "jnewland",
    author: "Jesse Newland",
    description: "All the business logic for my crappy loft lives here",
    category: "SmartThings Labs",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
    page(name: selectThings)
}

def selectThings() {
    dynamicPage(name: "selectThings", title: "Select Things", install: true) {
        section("Webhook URL"){
            input "url", "text", title: "Webhook URL", description: "Your webhook URL", required: true
        }
        section("Things to monitor for events") {
            input "monitor_switches", "capability.switch", title: "Switches", multiple: true, required: false
            input "monitor_motion", "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false
            input "monitor_presence", "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false
        }
        section("Things to control") {
            input "hues", "capability.colorControl", title: "Hue Bulbs", multiple: true, required: false
            input "switches", "capability.switch", title: "Switches", multiple: true, required: false
            input "dimmers", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
        }
        section("Voice") {
            input "voice", "capability.speechSynthesis", title: "Voice", required: false
        }
    }
}


def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    subscribe(monitor_switches, "switch", eventHandler)
    subscribe(monitor_motion, "motion", eventHandler)
    subscribe(monitor_presence, "presence", eventHandler)
    subscribe(location, "mode", eventHandler)
    subscribe(location, "sunset", eventHandler)
    subscribe(location, "sunrise", eventHandler)
    tick()
}

def eventHandler(evt) {
    def everyone_here = presense_is_after(monitor_presence, "present", 10)
    def everyone_gone = presense_is_after(monitor_presence, "not present", 10)
    def current_count = monitor_presence.findAll { it.currentPresence == "present" }.size()
    webhook([
      displayName:   evt.displayName,
      value:         evt.value,
      daytime:       is_daytime(),
      mode:          location.mode,
      current_count: current_count,
      everyone_here: everyone_here,
      everyone_gone: everyone_gone
    ])

    if (mode == "Pause") {
        webhook([ at: 'paused' ])
        log.info("No actions taken in Pause mode")
    } else {
        def lights = [switches, hues].flatten()
        // turn on lights near stairs when motion is detected
        if (evt.displayName == "motion@stairs" && evt.value == "active") {
            webhook([ at: 'stair_motion_lights' ])
            lights.findAll { s ->
                s.displayName == "stairs" ||
                    s.displayName == "loft" ||
                    s.displayName == "entry"
            }.findAll { s ->
                s.currentSwitch == "off"
            }.each { s ->
                if ("setLevel" in s.supportedCommands.collect { it.name }) {
                    if (location.mode == "Sleep") {
                        s.setLevel(50)
                    } else if (location.mode == "Home / Night") {
                        s.setLevel(75)
                    } else {
                        s.setLevel(100)
                    }
                }
                s.on()
            }
        }

        // Turn on some lights if one of us is up
        if (evt.value == "Yawn") {
            webhook([ at: 'yawn' ])
            lights.findAll { s ->
                s.displayName == "loft" ||
                    s.displayName == "entry" ||
                    s.displayName == "chandelier"
            }.findAll { s ->
                s.currentSwitch == "off"
            }.each { s ->
                if ("setLevel" in s.supportedCommands.collect { it.name }) {
                    s.setLevel(75)
                }
                s.on()
            }
        }

        // turn on all lights when entering day mode
        if (evt.value == "Home / Day") {
            webhook([ at: 'home_day' ])
            lights.each { s ->
                if (s.currentSwitch == "off") {
                    s.on()
                }
                 if ("setLevel" in s.supportedCommands.collect { it.name }) {
                    s.setLevel(100)
                }
            }
        }

        // turn on night mode at sunset
        if (evt.displayName == "sunset" && current_count > 0 && location.mode == "Home / Day") {
            webhook([ at: 'sunset' ])
            changeMode("Home / Night")
        }

        // dim lights at night
        if (evt.value == "Home / Night") {
            webhook([ at: 'home_night' ])
            lights.findAll { s ->
                "setLevel" in s.supportedCommands.collect { it.name }
            }.each { s ->
                log.info("Night mode enabled, dimming ${s.displayName}")
                s.setLevel(75)
            }

            // turn off that light by the door downstairs entirely
            lights.findAll { s ->
                s.displayName == "downstairs door"
            }.each { s ->
                log.info("Night mode enabled, turning off ${s.displayName}")
                s.off()
            }
        }

        // turn off all lights when entering sleep mode
        if (evt.value == "Sleep") {
            webhook([ at: 'sleep' ])
            lights.findAll { s ->
                s.currentSwitch == "on"
            }.each { s ->
                log.info("Sleep mode enabled, turning off ${s.displayName}")
                s.off()
            }
        }

        // turn off all lights when everyone goes away
        if (everyone_gone && location.mode != "Away") {
            webhook([ at: 'away' ])
            changeMode("Away")
            lights.findAll { s ->
                s.currentSwitch == "on"
            }.each { s ->
                log.info("Away mode enabled, turning off ${s.displayName}")
                s.off()
            }
        }

        // switch mode to Home when we return
        if (current_count > 0 && location.mode == "Away") {
            webhook([ at: 'home' ])
            changeMode("Home")
        }

        // Make home mode specific based on day / night
        if (evt.value == "Home") {
            webhook([ at: 'home_day_night' ])
            if (is_daytime()) {
                changeMode("Home / Day")
            } else {
                changeMode("Home / Night")
            }
        }

        if (canSchedule()) {
            runIn(61, tick)
        } else {
            webhook([ can_schedule: 'false' ])
            log.error("can_schedule=false")
        }
    }
}

def changeMode(mode) {
    //voice?.speak("changing mode to ${mode}")
    setLocationMode(mode)
    eventHandler([
        displayName: "changeMode",
        value:       mode
    ])
}

def tick() {
    eventHandler([
      displayName: "tick",
      value: "tock"
    ])
}

def webhook(map) {
    def successClosure = { response ->
      log.debug "Request was successful, $response"
    }

    def json_params = [
        uri: settings.url,
        success: successClosure,
        body: map
    ]
    httpPostJson(json_params)
}


private is_daytime() {
    def data = getWeatherFeature("astronomy")
    def sunset = "${data.moon_phase.sunset.hour}${data.moon_phase.sunset.minute}"
    def sunrise = "${data.moon_phase.sunrise.hour}${data.moon_phase.sunrise.minute}"
    def current = "${data.moon_phase.current_time.hour}${data.moon_phase.current_time.minute}"
    if (current.toInteger() > sunrise.toInteger() && current.toInteger() < sunset.toInteger()) {
        return true
    }
    else {
        return false
    }
}

private presense_is_after(people, presence, minutes) {
    def result = true
    for (person in people) {
        if (person.currentPresence != presence) {
            result = false
            break
        } else {
            def threshold = 1000 * 60 * minutes
            def elapsed = now() - person.currentState("presence").rawDateCreated.time
            if (elapsed < threshold) {
              result = false
              break
            }
        }
    }
    return result
}
