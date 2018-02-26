/**
 *  Double Tap Switch for "Hello, Home" Action
 *
 *  Copyright 2014 George Sudarkoff
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
    name: "Double Tap Switch for 'Hello, Home' Action",
    namespace: "com.sudarkoff",
    author: "George Sudarkoff",
    description: "Execute a 'Hello, Home' action when an existing switch is tapped twice in a row.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartthings-device-icons/Appliances/appliances17-icn.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-device-icons/Appliances/appliances17-icn@2x.png"
)

preferences {
    page (name: "configApp")

    page (name: "timeIntervalInput", title: "Only during a certain time") {
        section {
            input "starting", "time", title: "Starting", required: false
            input "ending", "time", title: "Ending", required: false
        }
    }
}

def configApp() {
    dynamicPage(name: "configApp", install: true, uninstall: true) {
        section ("When this switch is double-tapped...") {
            input "master", "capability.switch", required: true
        }

        def phrases = location.helloHome?.getPhrases()*.label
        if (phrases) {
            phrases.sort()
            section("Perform this actions...") {
                input "onPhrase", "enum", title: "ON action", required: false, options: phrases
                input "offPhrase", "enum", title: "OFF action", required: false, options: phrases
            }
        }

        section (title: "Notification method") {
            input "sendPushMessage", "bool", title: "Send a push notification?"
        }

        section (title: "More Options", hidden: hideOptionsSection(), hideable: true) {
            input "phone", "phone", title: "Additionally, also send a text message to:", required: false
            input "flashLights", "capability.switch", title: "And flash these lights", multiple: true, required: false

            def timeLabel = timeIntervalLabel()
            href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null

            input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        }

        section([mobileOnly:true]) {
            label title: "Assign a name", required: false
            mode title: "Set for specific mode(s)", required: false
        }
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

def initialize()
{
    if (customName) {
        state.currentAppLabel = customName
    }
    subscribe(master, "switch", switchHandler, [filterEvents: false])
}

def switchHandler(evt) {
    log.info evt.value

    if (allOk) {
        // use Event rather than DeviceState because we may be changing DeviceState to only store changed values
        def recentStates = master.eventsSince(new Date(now() - 4000), [all:true, max: 10]).findAll{it.name == "switch"}
        log.debug "${recentStates?.size()} STATES FOUND, LAST AT ${recentStates ? recentStates[0].dateCreated : ''}"

        if (evt.isPhysical()) {
            if (evt.value == "on" && lastTwoStatesWere("on", recentStates, evt)) {
                log.debug "detected two taps, execute ON phrase"
                location.helloHome.execute(settings.onPhrase)
                def message = "${location.name} executed ${settings.onPhrase} because ${evt.title} was tapped twice."
                send(message)
                flashLights()
            } else if (evt.value == "off" && lastTwoStatesWere("off", recentStates, evt)) {
                log.debug "detected two taps, execute OFF phrase"
                location.helloHome.execute(settings.offPhrase)
                def message = "${location.name} executed ${settings.offPhrase} because ${evt.title} was tapped twice."
                send(message)
                flashLights()
            }
        }
        else {
            log.trace "Skipping digital on/off event"
        }
    }
}

private lastTwoStatesWere(value, states, evt) {
    def result = false
    if (states) {
        log.trace "unfiltered: [${states.collect{it.dateCreated + ':' + it.value}.join(', ')}]"
        def onOff = states.findAll { it.isPhysical() || !it.type }
        log.trace "filtered:   [${onOff.collect{it.dateCreated + ':' + it.value}.join(', ')}]"

        // This test was needed before the change to use Event rather than DeviceState. It should never pass now.
        if (onOff[0].date.before(evt.date)) {
            log.warn "Last state does not reflect current event, evt.date: ${evt.dateCreated}, state.date: ${onOff[0].dateCreated}"
            result = evt.value == value && onOff[0].value == value
        }
        else {
            result = onOff.size() > 1 && onOff[0].value == value && onOff[1].value == value
        }
    }
    result
}

private send(msg) {
    if (sendPushMessage != "No") {
        sendPush(msg)
    }

    if (phone) {
        sendSms(phone, msg)
    }

    log.debug msg
}

private flashLights() {
    def doFlash = true
    def onFor = onFor ?: 200
    def offFor = offFor ?: 200
    def numFlashes = numFlashes ?: 2

    if (state.lastActivated) {
        def elapsed = now() - state.lastActivated
        def sequenceTime = (numFlashes + 1) * (onFor + offFor)
        doFlash = elapsed > sequenceTime
    }

    if (doFlash) {
        state.lastActivated = now()
        def initialActionOn = flashLights.collect{it.currentSwitch != "on"}
        def delay = 1L
        numFlashes.times {
            flashLights.eachWithIndex {s, i ->
                if (initialActionOn[i]) {
                    s.on(delay: delay)
                }
                else {
                    s.off(delay:delay)
                }
            }
            delay += onFor
            flashLights.eachWithIndex {s, i ->
                if (initialActionOn[i]) {
                    s.off(delay: delay)
                }
                else {
                    s.on(delay:delay)
                }
            }
            delay += offFor
        }
    }
}

// execution filter methods
private getAllOk() {
    modeOk && daysOk && timeOk
}

private getModeOk() {
    def result = !modes || modes.contains(location.mode)
    result
}

private getDaysOk() {
    def result = true
    if (days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        if (location.timeZone) {
            df.setTimeZone(location.timeZone)
        }
        else {
            df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"))
        }
        def day = df.format(new Date())
        result = days.contains(day)
    }
    result
}

private getTimeOk() {
    def result = true
    if (starting && ending) {
        def currTime = now()
        def start = timeToday(starting).time
        def stop = timeToday(ending).time
        result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
    }
    log.trace "timeOk = $result"
    result
}

private hideOptionsSection() {
    (phone || starting || ending || flashLights || customName || days || modes) ? false : true
}

private hhmm(time, fmt = "h:mm a")
{
    def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    f.format(t)
}

private timeIntervalLabel() {
    (starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}
