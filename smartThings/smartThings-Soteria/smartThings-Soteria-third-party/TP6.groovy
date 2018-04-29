/**
 * Home Alone Lighting
 *
 * Author: Nic Jansma
 *
 * Licensed under the MIT license
 *
 * Based on:
 *   https://github.com/tslagle13/SmartThingsPersonal/blob/master/smartapps/tslagle13/vacation-lighting-director.groovy
 *
 * Notes:
 * Turns lights on then off around the times specified with a bit of random difference each time.  Can be used to simulate
 * people moving from room to room in the evening, e.g. from Kitchen to Living Room to Bedroom as the night progresses.
 */

definition(
    name: "Home Alone Lighting",
    namespace: "nicjansma",
    author: "Nic Jansma",
    category: "Safety & Security",
    description: "Simulates someone being home by turning lights on then off in different room as the night progresses",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light11-icn@3x.png"
)

preferences {
    section("Group 1") {
        input "group1Enabled", "bool", title: "Enabled", required: true, defaultValue: true
        input "group1Lights", "capability.switch", title: "Which lights?", multiple: true
        input "group1Start", "time", title: "Start Time (hh:mm 24h)", required: false
        input "group1End", "time", title: "Stop Time (hh:mm 24h)", required: false
    }

    section("Group 2") {
        input "group2Enabled", "bool", title: "Enabled", required: true, defaultValue: false
        input "group2Lights", "capability.switch", title: "Which lights?", multiple: true
        input "group2Start", "time", title: "Start Time (hh:mm 24h)", required: false
        input "group2End", "time", title: "Stop Time (hh:mm 24h)", required: false
    }

    section("Group 3") {
        input "group3Enabled", "bool", title: "Enabled", required: true, defaultValue: false
        input "group3Lights", "capability.switch", title: "Which lights?", multiple: true
        input "group3Start", "time", title: "Start Time (hh:mm 24h)", required: false
        input "group3End", "time", title: "Stop Time (hh:mm 24h)", required: false
    }

    section("Group 4") {
        input "group4Enabled", "bool", title: "Enabled", required: true, defaultValue: false
        input "group4Lights", "capability.switch", title: "Which lights?", multiple: true
        input "group4Start", "time", title: "Start Time (hh:mm 24h)", required: false
        input "group4End", "time", title: "Stop Time (hh:mm 24h)", required: false
    }

    section ("Random") {
        paragraph "How many minutes +/- can the lights turn on or off"
        input "randomMinutes", "number", title: "Minutes", defaultValue: 20, required: true
    }

    section ("Modes") {
        mode(name: "mode", title: "Which mode should this run in?", required: true, multiple: false)
    }
}

/**
 * Run when the app is installed
 */
def installed() {
    log.debug "Installed"

    initialize()
}

/**
 * Run when the app is updated
 */
def updated() {
    log.debug "Updated"

    reset()

    initialize()
}

/**
 * Run to reset the app
 */
def reset() {
    log.debug "Reset"

    // unsubscribe from everything
    unsubscribe()

    // unschedule all tasks
    unschedule()
}

/**
 * Initializes the app - configures schedules and ensures the initial state
 */
def initialize() {
    log.debug "Initializing"

    // ensure we generate a new schedule today
    state.lastScheduledDay = 0

    checkForNewSchedule()

    log.debug "Configuring loop()"

    // setup schedules
    runEvery5Minutes(loop)
    runEvery1Hour(checkForNewSchedule)
}

/**
 * Main loop - Determines if any lights need to be changed
 */
def loop() {
    log.debug "loop()"

    if (location.mode != mode) {
        log.debug "loop(): Not running in this mode"
        return
    }

    def now = (new Date()).time

    // look to see if any of the groups need their lights changed
    for (def i = 0; i <= 3; i++) {
        def groupNum = i + 1

        // string index
        def groupIdx = i + ""

        log.debug "Group # $groupNum" + state.groups[groupIdx]

        if (!state.groups[groupIdx] || !state.groups[groupIdx].enabled) {
            log.debug "Skipping Group #${groupNum} because it is not enabled"
            continue
        }

        def group = state.groups[groupIdx]

        // determine which lights to use
        def lights
        if (i == 0) {
            lights = group1Lights
        } else if (i == 1) {
            lights = group2Lights
        } else if (i == 2) {
            lights = group3Lights
        } else if (i == 3) {
            lights = group4Lights
        }

        if (!group.on && now > group.start && now < group.end) {
            // the lights were off, but should be turned on
            log.debug "Group #${groupNum}: Turning on"
            lights.on()
            group.on = true
        } else if (group.on && now > group.end) {
            // the lights we on, but should be turned off
            log.debug "Group #${groupNum}: Turning off"
            lights.off()
            group.on = false
        } else {
            log.debug "Group #${groupNum}: No change"
        }
    }

    log.debug "loop() complete"
}

/**
 * Run daily to determine if a new schedule should be picked
 */
def checkForNewSchedule() {
    // determine what day of the year this is
    Calendar cal = Calendar.getInstance()
    def day = cal.get(Calendar.DAY_OF_YEAR)

    // if we haven't scheduled lights for today, do so now
    if (state.lastScheduledDay != day) {
        log.debug "Picking a new schedule for day # ${day}"

        state.groups = [:]

        schedule(1, group1Enabled, group1Start, group1End)
        schedule(2, group2Enabled, group2Start, group2End)
        schedule(3, group3Enabled, group3Start, group3End)
        schedule(4, group4Enabled, group4Start, group4End)

        state.lastScheduledDay = day
    }

    // make sure our lights are in the correct state
    loop()
}

/**
 * Schedules the specific group
 */
def schedule(num, enabled, start, end) {
    def group = [:]
    group.enabled = false

    if (!enabled || start == null || end == null) {
        // light is disabled or not configured
        state.groups[num - 1] = group
        log.debug "schedule(): Skipping #${num} because not everything is defined"
        return
    }

    group.enabled = true

    // pick a new time to start
    def random = new Random()

    def plusMinus = random.nextBoolean() ? 1 : -1
    def rnd = 0

    if (randomMinutes > 0) {
        rnd = plusMinus * random.nextInt(randomMinutes)
    }

    def startRand = new Date(timeToday(start).time + (plusMinus * rnd * 1000 * 60))

    // pick a new time to end
    plusMinus = random.nextBoolean() ? 1 : -1
    if (randomMinutes > 0) {
        rnd = plusMinus * random.nextInt(randomMinutes)
    } else {
        rnd = 0
    }

    def endRand = new Date(timeToday(end).time + (plusMinus * rnd * 1000 * 60))

    log.debug "schedule(): #${num} start: ${startRand} end: {$endRand}"

    group.start = startRand.time
    group.end = endRand.time
    group.on = false

    // save this group to state
    // (note we should use a string index as that's how it will be serialized)
    state.groups[(num - 1) + ""] = group
}