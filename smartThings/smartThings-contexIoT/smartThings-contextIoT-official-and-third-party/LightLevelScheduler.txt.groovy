/**
 *  Light Level Scheduler
 *
 *
 *  Sets light levels at a scheduled time.
 *  similar to the once a day app.
 */

preferences {
	section("Select dimmers to control...") {
		input name: "switches", type: "capability.switchLevel", multiple: true
	}
	section("Time and Levels...") {
		input name: "days", "enum", title: "What day(s)?", description: "Every day (default)", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
        input name: "startTime", title: "Time?", type: "time"
        input name: "lightLevel", title: "Level 0-100", type: "number"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	schedule(startTime, "scheduleCheck")
}

def updated(settings) {
	unschedule()
	schedule(startTime, "scheduleCheck")
}

def scheduleCheck() {
	defaultState()

	debug "Light Level Scheduler checking scheduling"

	def today = new Date().format("EEEE")

	debug "today: ${today}, days: ${days}"

	if (!days || days.contains(today))
	{ 
    	debug "Setting light levels"
		switches.setLevel(lightLevel)
	}
	else
	{
		debug "Light Level Scheduler skipped running today"
	}
}