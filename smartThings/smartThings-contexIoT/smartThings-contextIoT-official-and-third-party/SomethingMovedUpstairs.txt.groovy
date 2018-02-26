/**
 *  Something Moved
 *
 *  Author: MarkoPolo
 */
definition(
    name: "Something Moved Upstairs",
    namespace: "smartthings",
    author: "MarkoPolo",
    description: "Send a text when movement is detected",
    category: "Fun & Social",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text_accelerometer.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text_accelerometer@2x.png"
)

preferences {
    section("When movement is detected upstairs...") {
        input "motionSensor", "capability.motionSensor", title: "Where?"
    }
    section("Text me at...") {
        input "phone1", "phone", title: "Phone number?"
    }
}

def installed() {
    subscribe(motionSensor, "motion.active", motionActiveHandler)
}

def updated() {
    unsubscribe()
    subscribe(motionSensor, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
    // Don't send a continuous stream of text messages
    def deltaSeconds = 60
    def timeAgo = new Date(now() - (1000 * deltaSeconds))
    def recentEvents = motionSensor.eventsSince(timeAgo)
    log.trace "Found ${recentEvents?.size() ?: 0} events in the last $deltaSeconds seconds"
    def alreadySentSms = recentEvents.count { it.value && it.value == "active" } > 1

    if (alreadySentSms) {
        log.debug "SMS already sent to $phone1 within the last $deltaSeconds seconds"
    } else {
        log.debug "$motionSensor has moved, texting $phone1"
        sendSms(phone1, "Dude! ${motionSensor.label ?: motionSensor.name} detected that someting moved at Kokopelli!")
    }
}