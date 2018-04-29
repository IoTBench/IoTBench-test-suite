/**
 *  Brighten My Path
 *  Author: Z. Berkay Celik
 *  The app Brighten My Path is modified by Z. Berkay Celik
 *  Email: zbc102@cse.psu.edu
 */
definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "Turn your lights on when motion is detected.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet@2x.png"
)

preferences {
	section("When there's movement...") {
		input "motion1", "capability.motionSensor", title: "Where?", multiple: true
	}
	section("Turn on a light...") {
		input "switch1", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt) {
	switch1.on()
	switch1.off() // updated
}