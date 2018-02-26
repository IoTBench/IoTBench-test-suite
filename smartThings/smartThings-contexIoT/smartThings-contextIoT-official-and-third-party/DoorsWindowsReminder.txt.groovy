/**
 *  Fence
 *
 *  Copyright 2014 Gabriele Nizzoli
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
    name: "Doors & Windows Reminder",
    namespace: "gabrielenizzoli",
    author: "Gabriele Nizzoli",
    description: "Monitor continuously if a set of doors or windows are open. And if they are, send a periodic reminder.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartthings-device-icons/contact/contact/closed.png",
    iconX2Url: "https://s3.amazonaws.com/smartthings-device-icons/contact/contact/closed@2x.png"
)


preferences {
    page name:"setupInit"
    page name:"setupConfigure"
    page name:"setupMonitor"
}

def setupInit() {
    if (state.installed) 
        return setupMonitor()
    return setupConfigure()
}

def setupConfigure() {

    def pageProperties = [
        name:       "setupConfigure",
        title:      "Doors or Windows to monitor",
        uninstall:  true,
        install: 	true
    ]

    return dynamicPage(pageProperties) {
        section {
            input "contacts", "capability.contactSensor", title: "Which?", multiple: true, required: true
            input "frequencyCheckMinutes", "number", title: "Frequency for checking open status (defaults to 5 minutes)", required: false
        }
        section("Notification") {
            input "notification", "bool", title: "Send notification", default: true
            input "phone", "phone", title: "Send also a message to this phone", required: false
            input "modes", "mode", title: "Notify only in those modes", required: false, multiple: true
        }
    }
}

def setupMonitor() {

    def pageProperties = [
        name:       "setupMonitor",
        title:      "Open Doors & Windows",
        install:    true,
        uninstall:  state.installed
    ]
    
    def openContacts = state.openContactsByDeviceId
    	.sort{ device -> device.value.openTime }
        .collect{ device -> [
        	name: contacts.find{ it.id == device.key }?.displayName, 
            time: ((now()/1000 - device.value.openTime)/60).toInteger()
        ] }

    return dynamicPage(pageProperties) {
        section {
        	if (openContacts.size() > 0) 
            	for (openContact in openContacts)
                	paragraph openContact.name + ": " + (openContact.time <= 0 ? 'now' : openContact.time + ' minutes')
            else
            		paragraph "Nothing open"
        }
        section {
            href "setupConfigure", title:"Configuration", description:"Tap to open"
        }
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    state.installed = true
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    state.installed = true
	unsubscribe()
	initialize()
}

def initialize() {
	log.trace "init..."
    
    unschedule()
	
    state.openContactsByDeviceId = [:]
        
    def currentTime = now()/1000
    state.openContactsByDeviceId = contacts
    	.findAll{ it.currentContact == 'open' }
        .collectEntries{ [it.id,
            [  	nextReminder:	timeNextReminderSeconds(currentTime), 
                remindersCount:	0,
                openTime: 		now()/1000]] 
            }
	subscribe(contacts, "contact", contact)
    
   	schedule("0 0/1 * * * ?", heartbeat)
}

def contact(evt) {
	log.trace "contact ${evt.value} for ${evt.deviceId}"
    if (evt.value == 'open' && !state.openContactsByDeviceId.containsKey(evt.deviceId))
    	state.openContactsByDeviceId[evt.deviceId] = [
        	openTime:		now()/1000,
        	nextReminder:	timeNextReminderSeconds(now()/1000), 
            remindersCount:	0
        ]
    else
    	state.openContactsByDeviceId.remove(evt.deviceId)
}

def heartbeat() {
    if (state.openContactsByDeviceId.size() == 0) 
    	return
    
    def currentTime = now()/1000
    
    def openContacts = state.openContactsByDeviceId.sort{ it.value.nextReminder }
    def openContactsWithReminder = openContacts.findAll{ it.value.nextReminder < currentTime }
    if (openContactsWithReminder.size() == 0) 
    	return
    
    // get data for contacts that needs a reminder
    def numberOfOpenContacts = openContactsWithReminder.size()
    def olderContactId = openContactsWithReminder.find{ true }.key
    def olderDeviceLabel = contacts.find{ it.id == olderContactId }.displayName 
        
    // update state data
    openContactsWithReminder.keySet().each{ 
       	def reminder = state.openContactsByDeviceId[it] 
        reminder.nextReminder = timeNextReminderSeconds(currentTime)
        reminder.remindersCount = reminder.remindersCount+1
    } 
    
    def minutes = openContactsWithReminder.collect{ it.value.remindersCount }.min() * frequencyReminderMinutes()
        
    // send reminder
    if (notification && modes?.contains(location.mode)) {
    
        // generate text for reminder
        def msg = 
            olderDeviceLabel + 
            (numberOfOpenContacts > 1 ? " (and ${numberOfOpenContacts-1} other) were open" : " was open") + 
        	" for more than " + (minutes > 1 ? "${minutes} minutes" : "1 minute")
            
    	sendPush(msg)
    	if (phone) sendSms(phone, msg)
    }
    
}

private frequencyReminderMinutes() {
	frequencyCheckMinutes == null ? 5 : frequencyCheckMinutes
}

private timeNextReminderSeconds(currentTimeSeconds) {
	frequencyReminderMinutes() * 60  + currentTimeSeconds
}