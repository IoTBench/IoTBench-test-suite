/**
 *  Auto Lock Door
 *
 *  Author: Chris Sader (@csader)
 *  Collaborators: @chrisb
 *  Date: 2013-08-21
 *  URL: http://www.github.com/smartthings-users/smartapp.auto-lock-door
 *
 * Copyright (C) 2013 Chris Sader.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


// Automatically generated. Make future change here.
definition(
    name: "Door Lock Auto",
    namespace: "",
    author: "http://github.com/smartthings-users/smartapp.auto-lock-door",
    description: "auto door lock",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png"
)

preferences
{
    section("When a door unlocks...") {
        input "lock1", "capability.lock"
    }
    section("Lock it how many minutes later?") {
        input "minutesLater", "number", title: "When?"
    }
}

def installed()
{
    log.debug "Auto Lock Door installed. (URL: http://www.github.com/smartthings-users/smartapp.auto-lock-door)"
    initialize()
}

def updated()
{
    unsubscribe()
    unschedule()
    log.debug "Auto Lock Door updated."
    initialize()
}

def initialize()
{
    log.debug "Settings: ${settings}"
    subscribe(lock1, "lock", doorHandler)
}

def lockDoor()
{
    log.debug "Locking ${name} after inactivity."
    lock1.lock()
}

def doorHandler(evt)
{
    log.debug "Lock ${evt.displayName} is ${evt.value}."

    if (evt.value == "locked") {                // If the human locks the door then...
        log.debug "Cancelling previous lock task for ${evt.displayName}..."
        unschedule()                            // ...we don't need to lock it later.
    }
    else if (evt.value == "unlocked") {         // If the door is unlocked then...
        def delay = minutesLater * 60           // runIn uses seconds
        log.debug "Re-arming ${evt.displayName} in ${minutesLater} minutes (${delay}s)."
        runIn( delay, lockDoor("${evt.displayName}") )                // ...schedule to lock in x minutes.
    }
    else {
        log.warn "Value '${evt.value}' for '${evt.displayName}' has not been processed."
    }
}