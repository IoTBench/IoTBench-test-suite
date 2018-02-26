/**
 *  Squeezebox Controller
 *
 *  Author: office.automation@bizstream.com Mark Schmidt @ www.BizStream.com
 *  Date: 2014-02-09
 */
preferences {
    section("When there's movement...") {
        input "motion1", "capability.motionSensor", title: "Where?"
    }
    section("Squeezebox/Logitech Server...") {
        input "ip",  "text", title: "IP Address?", description: "ip or servername (exclude http)"
        input "port",  "number", title: "Port?", defaultValue: 9000
    }
    section("Anytime after...") {
        input "timeAfter", "time", title: "When?"
        input "afterSetVolumeTo",  "number", title: "Set Volume to (0-100)?",     defaultValue: 35, description: "default is 35"
        input "afterSleepMinutes", "number", title: "Sleep Timer (in minutes)?", defaultValue: 60, description: "default is 60 minutes"
    }
    section("Anytime before...") {
        input "timeBefore", "time", title: "When?"
        input "beforeSetVolumeTo",  "number", title: "Set Volume to (0-100)?",     defaultValue: 20, description: "default is 20"
        input "beforeSleepMinutes", "number", title: "Sleep Timer (in minutes)?", defaultValue: 15, description: "default is 15 minutes"
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
	subscribeDevices()
}

def subscribeDevices() {
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	def sleepFor = 0
    def setVolume = 10;
	def doIt = false
    
   	// if after time 19:30 (7:30 Pm, kid's bed time is around 8 pm)
    if (isTimeAfter(timeAfter)) {
    	doIt = true
        sleepFor = (afterSleepMinutes * 60) // seconds
        setVolume = afterSetVolumeTo
    
    // or if it is the middle of the night and the kids called me up, they usually want me to turn their music on so they can go to bed.
	} else if(isTimeBefore(timeBefore)) {
    	doIt = true
        sleepFor = (beforeSleepMinutes * 60) // seconds
        setVolume = beforeSetVolumeTo
    
    }
    
    
    if (doIt) {
		// http://www.fact4ward.com/blog/not-done-yet/squeezebox/

        // set volume
        sendCommandToSqueezeBox("/status.html?p0=mixer&p1=volume&p2=$setVolume") 
    
        // play playlist
        sendCommandToSqueezeBox("/plugins/spotifylogi/index.html?action=playall&index=cdd4e1af.13&player=00%3A04%3A20%3A2c%3A13%3A4a&sess=&start=&_dc=1391969890774")
        // I used fiddler to figure this one out
    
        // set sleep timer
        sendCommandToSqueezeBox("/status.html?p0=sleep&p1=$sleepFor") 
	}

}

def sendCommandToSqueezeBox(command) {
    def squeezeBoxServer = "http://$ip:$port"

      log.debug "attempting: $squeezeBoxServer$command"

    def successClosure = { response ->
      log.debug "Request was successful, $response"
    }
    httpGet(squeezeBoxServer + command, successClosure)
}

private isTimeAfter(afterTime) {
  	//return true // testing only

	def t0 = now()
    def timeZone = location.timeZone ?: timeZone(timeOfDay)
    def start = timeToday(afterTime, timeZone)
    log.debug "startTime: $start, t0: ${new Date(t0)}, currentMode: $location.mode"
    
    if (t0 >= start.time) {
    	return true
    } else {
    	return false
    }
}

private isTimeBefore(beforeTime) {
  	//return true // testing only

	def t0 = now()
    def timeZone = location.timeZone ?: timeZone(timeOfDay)
    def end = timeToday(beforeTime, timeZone)
    log.debug "endTime: $end, t0: ${new Date(t0)}, currentMode: $location.mode"
    
    if (t0 <= end.time) {
    	return true
    } else {
    	return false
    }
}
