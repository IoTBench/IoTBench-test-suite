/**
 *
 *  Author: radley (cheese@vj.tv)
 *  Date: 2014-01-26
 *  based on Curling Iron by Dan Lieberman
 *
 */
preferences {
	section("Motion Sensors (Required)") {
		input name: "motionSensors", title: "Add sensors", type: "capability.motionSensor", multiple: true, required: true
	}
	section("Power Outlets (Required)") {
		input name: "outlets", title: "Add outlets", type: "capability.switch", multiple: true, required: true
	}
	section("Minimum Duration") {
		input name: "minutes", title: "Minutes", type: "number", multiple: false, description: "1", required: false
	}
}

def installed()
{
	defaultState()
	
}

def updated()
{
	unsubscribe()
    defaultState()
}

def defaultState() {

	subscribe(motionSensors, "motion.active", motionActive)
	subscribe(motionSensors, "motion.inactive", motionInactive)
}

def motionActive(evt) {
	log.debug "$evt.name: $evt.value"
	
	outletsOn()

}

def motionInactive(evt) {
	log.debug "$evt.name: $evt.value"

	outletsOff()

}


def outletsOn() {
	outlets.on()
	unschedule("scheduledTurnOff")
}

def outletsOff() {

	def duration = 1
    if(minutes != null && minutes >= 0)
    {
    	duration = minutes
    }
    
	def delay = duration * 60
	runIn(delay, "scheduledTurnOff")
}

def scheduledTurnOff() {
	outlets.off()
}

