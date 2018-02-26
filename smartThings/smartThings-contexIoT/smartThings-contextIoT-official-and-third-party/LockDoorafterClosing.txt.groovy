/**
 *  Enhanced Auto Lock Door
 *
 *  Added: Door contact sensor awareness
 *  
 *  Modified by: alegreg@verizon.net
 *  Date: 03/23/2014
 *
 *  Original Author: Chris Sader (@csader)
 *  Collaborators: @chrisb
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

preferences
{
    section("Select the door lock:") {
        input "lock1", "capability.lock", required: true
    }
    section("Select the door contact sensor:") {
    	input "contact", "capability.contactSensor", required: true
    }
    
    section("Automatically lock the door...") {
        input "minutesLater", "number", title: "Delay (in minutes):", required: true
    }
    
    section("Unlock it if the lock is manually engaged while the door is open...") {
        input "minutesLater2", "number", title: "Delay (in minutes):", required: true
    }
}

def installed()
{
    log.debug "Auto Lock Door installed."
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
    subscribe(lock1, "lock", doorHandler, [filterEvents: false])
    subscribe(lock1, "unlock", doorHandler, [filterEvents: false])  
    subscribe(contact, "contact.open", doorHandler)
	subscribe(contact, "contact.closed", doorHandler)
}

def lockDoor()
{
    log.debug "Locking the door."
    lock1.lock()
}

def unlockDoor()
{
    log.debug "Unlocking the door."
    lock1.unlock()
}

def doorHandler(evt)
{
    log.debug "Event: The ${evt.name} is ${evt.value}."
    log.debug "Contact value: The contact is ${contact.latestValue("contact")}."
    log.debug "Lock value: The lock is ${lock1.latestValue("lock")}."

    if ((contact.latestValue("contact") == "open") && (evt.value == "locked")) { // If the door is open and a person locks the door then...  
        def delay = (minutesLater2 * 60) // runIn uses seconds
        log.debug "Door is open and somebody just locked it.  Unlocking the door!"
        runIn( delay, unlockDoor )   // ...schedule (in minutes) to unlock...  We don't want the door to be closed while the lock is engaged. 
    }
    else if ((contact.latestValue("contact") == "open") && (evt.value == "unlocked")) { // If the door is open and a person unlocks it then...
        log.debug "Cancel the current task. Door is open and somebody just unlocked it!"
        unschedule( unlockDoor ) // ...we don't need to unlock it later.
	}
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "locked")) { // If the door is closed and a person manually locks it then...
        log.debug "Cancel the current task. Door is closed and somebody just locked it!"
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }   
    else if ((contact.latestValue("contact") == "closed") && (evt.value == "unlocked")) { // If the door is closed and a person unlocks it then...
        log.debug "Door is closed and somebody just unlocked it.  Locking the door!"
        def delay = (minutesLater * 60) // runIn uses seconds
        log.debug "Re-arming lock in ${minutesLater} minutes (${delay}s)."
        runIn( delay, lockDoor ) // ...schedule (in minutes) to lock.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "open")) { // If a person opens an unlocked door...
        log.debug "Cancel the current task. Door is unlocked and somebody just opened it!"
        unschedule( lockDoor ) // ...we don't need to lock it later.
    }
    else if ((lock1.latestValue("lock") == "unlocked") && (evt.value == "closed")) { // If a person closes an unlocked door...
        log.debug "Door is unlocked and somebody just closed it.  Locking the door!"
        def delay = (minutesLater * 60) // runIn uses seconds
        log.debug "Re-arming lock in ${minutesLater} minutes (${delay}s)."
        runIn( delay, lockDoor ) // ...schedule (in minutes) to lock.
    }
    else {
        log.debug "Ohh.. no!!.. The lock is jammed!!  or worst.. The door was forced open or close!!!"
        unschedule( lockDoor )
        unschedule( unlockDoor )
    }
}