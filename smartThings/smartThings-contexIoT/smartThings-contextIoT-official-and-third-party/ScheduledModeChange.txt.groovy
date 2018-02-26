/**
 *  Scheduled Mode Change
 *
 *  Author: SmartThings

 *  Date: 2013-05-04
 */
preferences {
	section("At this time every day") {
		input "time", "time", title: "Time of Day"
	}
	section("Change to this mode") {
		input "newMode", "mode", title: "Mode?"
	}
}

def installed() {
	initialize()
}

def updated() {
	unschedule()
	initialize()
}

def initialize() {
	schedule(time, changeMode)
}

def changeMode() {
	if (location.mode != newMode) {
		if (location.modes?.find{it.name == newMode}) {
			setLocationMode(newMode)
			send "${label} has changed the mode to '${newMode}'"
		}
		else {
			send "${label} tried to change to undefined mode '${newMode}'"
		}
	}
}

private send(msg) {
	sendPush msg
	log.debug msg
}

private getLabel() {
	app.label ?: "SmartThings"
}