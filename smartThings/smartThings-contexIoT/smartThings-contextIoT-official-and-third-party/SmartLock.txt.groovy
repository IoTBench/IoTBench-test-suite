// The MIT License (MIT)
//
// Copyright (c) 2015 Eric Cirone
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

/**
 *  SmartLock
 *
 *  Author: 	Eric Cirone
 *  Company: 	Eric Cirone
 *  Email: 		ecirone@gmail.com
 *  Date: 		01/05/2015
 */

definition(
    name: "SmartLock",
    namespace: "ericcirone",
    author: "Eric Cirone",
    description: "SmartLock will rearm your specified lock after a set amount of time. To disable SmartLock: From a locked state, perform an unlock, lock, then unlock again within the specified amount of time.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true
)

preferences {
    section("Which lock to monitor?") {
        input "lock1","capability.lock", multiple: false
    }
    section("Automatically lock after") {
        input "after", "number", title: "Minutes", description: "10 minutes",  required: true
    }
    section("Timeout to disable SmartLock") {
        input "disableTimeout", "number", title: "Seconds", description: "5 Seconds",  required: true
    }
    section("Notification method") {
            input "push", "bool", title: "Smartthings Push Notification", metadata: [values: ["Yes","No"]]
            input "pushover", "bool", title: "Pushover.me Notification", metadata: [values: ["Yes","No"]]
    }
    section("Pushover Notifications") {
        input "apiKey", "text", title: "API Key", required: false
        input "userKey", "text", title: "User Key", required: false
        input "deviceName", "text", title: "Device Name (blank for all)", required: false
        input "sound", "text", title: "Sound", required: false
        input "priority", "enum", title: "Priority", required: false,
        metadata :[
           values: [ 'Normal', 'Low', 'High', 'Emergency' ]
        ]
    }
}

def installed()
{
    subscribe(lock1, "lock", eventHandler)
    state.count = 0;
    state.lockTheLock = false;
    state.lockingDoor = false;
}

def updated()
{
    unsubscribe()
    unschedule()
    subscribe(lock1, "lock", eventHandler)
    state.count = 0;
    state.lockTheLock = false;
    state.lockingDoor = false;
}

def eventHandler(evt)
{
    if (state.lockingDoor){
        state.lockingDoor = false;
        return;
    }
    if (state.count == 0){
        startTimer(disableTimeout, clearState);
        log.debug("startTimer(${disableTimeout},checkDisableSmartLock)");
    }
    state.count = state.count + 1;
    log.debug("Event Value: ${evt.value}");
    log.debug("Lock State: ${lock1.currentValue("lock")}");
    if (evt.value == "unlocked"){
        state.lockTheLock = true;
        //lockTheLock();
    }else{
        state.lockTheLock = false;
    }
    log.debug("state.lockTheLock: ${state.lockTheLock}");
    checkDisableSmartLock();
}

def checkDisableSmartLock(){
    log.debug("State count: ${state.count}");
    if (state.count == 3 && state.lockTheLock){
        clearState();
        unschedule();
        def msg = "SmartLock disabled. Door will remain unlocked indefinitly.";
        sendMessage(msg);
    }else if (state.lockTheLock){
        state.lockTheLock = false;
        def delay = (after != null && after != "") ? after * 60 : 600;
        runIn(delay, lockTheLock);
        log.debug("runIn(${delay},lockTheLock)");
    }else{
        state.lockTheLock = false;
        unschedule(lockTheLock);
        log.debug("Cleanup");
    }
}

def clearState(){
    state.count = 0;
    log.debug("State count cleared");
}

def lockTheLock()
{
    def anyUnlocked = (lock1.currentValue("lock") == "unlocked");
    if (anyUnlocked) {
        sendMessage("SmartLock: ${lock1.label} has been unlocked for ${after} minutes. Locking ${lock1.label} now.")
        state.lockingDoor = true;
        lock1.lock()
        log.debug("Attemped to send message and locked locks.")
    }else{
        log.debug("No doors unlocked")
    }
}

def sendMessage(msg)
{
    log.debug("sendMessage: ${msg}");
    if (push) {
        sendPush msg;
    }
    if (pushover){
    	sendPushover msg;
    }
}

def sendPushover(msg){
	// Make sure we still get the message into Hello Home
	sendNotificationEvent(msg);
 // Define the initial postBody keys and values for all messages
    def postBody = [
        token: "$apiKey",
        user: "$userKey",
        message: "${msg}",
        priority: 0
    ]
    
    if (sound){
    	postBody['sound'] = "$sound"
    }

    // Set priority and potential postBody variables based on the user preferences
    switch ( priority ) {
        case "Low":
            postBody['priority'] = -1
            break

        case "High":
            postBody['priority'] = 1
            break

        case "Emergency":
            postBody['priority'] = 2
            postBody['retry'] = "60"
            postBody['expire'] = "3600"
            break
    }

    // We only have to define the device if we are sending to a single device
    if (deviceName)
    {
        log.debug "Sending Pushover to Device: $deviceName"
        postBody['device'] = "$deviceName"
    }
    else
    {
        log.debug "Sending Pushover to All Devices"
    }

    // Prepare the package to be sent
    def params = [
        uri: "https://api.pushover.net/1/messages.json",
        body: postBody
    ]

    log.debug postBody

    if ((apiKey =~ /[A-Za-z0-9]{30}/) && (userKey =~ /[A-Za-z0-9]{30}/))
    {
        log.debug "Sending Pushover: API key '${apiKey}' | User key '${userKey}'"
        httpPost(params){
            response ->
                if(response.status != 200)
                {
                    sendPush("ERROR: 'Pushover Me When' received HTTP error ${response.status}. Check your keys!")
                    log.error "Received HTTP error ${response.status}. Check your keys!"
                }
                else
                {
                    log.debug "HTTP response received [$response.status]"
                }
        }
    }
    else {
        // Do not sendPush() here, the user may have intentionally set up bad keys for testing.
        log.error "API key '${apiKey}' or User key '${userKey}' is not properly formatted!"
    }
}

def startTimer(seconds, function) {
    def now = new Date();
    def runTime = new Date(now.getTime() + (seconds * 1000));
    runOnce(runTime, function); // runIn isn't reliable, use runOnce instead
}