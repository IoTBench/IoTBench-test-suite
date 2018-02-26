/**
 *  Improved Power Allowance
 *
 *  Author: chrisb
 *
 *  Based on Power Allowance by SmartThings
 *
 *  This program using runIn instead of a delay option.  This prevents 'stacking' of off commands.
 */

// Automatically generated. Make future change here.
definition(
    name: "Improved Power Allowance",
    namespace: "",
    author: "seateabee@gmail.com",
    description: "An improvement on power allowance.  If the switch/outlet is powered off prior to the 'time out,' then the scheduled time off is cancelled.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When a switch turns on...") {
		input "theSwitch", "capability.switch"
	}
	section("Turn it off how many minutes later?") {
		input "minutesLater", "number", title: "When?"
	}
}

def installed() {
	sendPush "Installed with settings: ${settings}"
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: true])
}

def updated() {
	sendPush "Updated with settings: ${settings}"
	unsubscribe()
	subscribe(theSwitch, "switch.on", switchOnHandler, [filterEvents: true])
}

def switchOnHandler(evt) {
//	sendPush "Switch ${theSwitch} turned: ${evt.value}"
	def delay = minutesLater * 60
//	sendPush "Turning off in ${minutesLater} minutes (${delay}s)"
	runIn(delay, turnOff)
}

def turnOff() {
//	sendPush "Turning off ${theSwitch} now."
	theSwitch.off()
}