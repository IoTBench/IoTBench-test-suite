/**
* Notify Me When Smoke or Carbon Monoxide is Detected
*
* Author: Steve Sell
* steve.sell@gmail.com
*/
preferences {
section("Select smoke detector(s)..."){
input "smoke_detectors", "capability.smokeDetector", title: "Which one(s)...?", multiple: true
}
section( "Notifications" ) {
input "sendPushMessage", "enum", title: "Send a push notification?", metadata:[values:["Yes","No"]], required:false
input "phoneNumber", "phone", title: "Enter number for SMS (optional).", required: false
}
section( "Low battery warning" ){
input "lowBattThreshold", "number", title: "Low Batt Threshold % (default 10%)", required: false
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
 
def smokeHandler(evt) {
log.trace "$evt.value: $evt, $settings"
String theMessage
if (evt.value == "tested") {
theMessage = "${evt.displayName} was tested for smoke."
} else if (evt.value == "clear") {
theMessage = "${evt.displayName} is clear for smoke."
} else if (evt.value == "detected") {
theMessage = "${evt.displayName} detected smoke!"
} else {
theMessage = ("Unknown event received from ${evt.name}")
}
sendMsg(theMessage)
}
 
 
def carbonMonoxideHandler(evt) {
log.trace "$evt.value: $evt, $settings"
String theMessage
if (evt.value == "tested") {
theMessage = "${evt.displayName} was tested for carbon monoxide."
} else if (evt.value == "clear") {
theMessage = "${evt.displayName} is clear of carbon monoxide."
} else if (evt.value == "detected") {
theMessage = "${evt.displayName} detected carbon monoxide!"
} else {
theMessage = "Unknown event received from ${evt.name}"
}
sendMsg(theMessage)
}
 
def batteryHandler(evt) {
log.trace "$evt.value: $evt, $settings"
String theMessage
int battLevel = evt.integerValue
log.debug "${evt.displayName} has battery of ${battLevel}"
if (battLevel < lowBattThreshold ?: 10) {
theMessage = "${evt.displayName} has battery of ${battLevel}"
sendMsg(theMessage)
}
}
 
private sendMsg(theMessage) {
log.debug "Sending message: ${theMessage}"
if (phoneNumber) {
sendSms(phoneNumber, theMessage)
}
 
if (sendPushMessage == "Yes") {
sendPush(theMessage)
}
}
 
private initialize() {
subscribe(smoke_detectors, "smoke", smokeHandler)
subscribe(smoke_detectors, "carbonMonoxide", carbonMonoxideHandler)
subscribe(smoke_detectors, "battery", batteryHandler)
}