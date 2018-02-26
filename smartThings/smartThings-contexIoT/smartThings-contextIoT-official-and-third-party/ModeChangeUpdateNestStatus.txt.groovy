/**
 *  Presence Change Device Presence
 *
 *  Author: coolkev
 * 		
 * 	Make sure your nest devices are using this deviceType: https://github.com/smartthings-users/device-type.nest
 */
definition(
    name: "Mode Change Update Nest Status",
    namespace: "coolkev",
    author: "coolkev",
    description: "Keep your Nest set to home/away automatically on mode changes",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Electronics/electronics1-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Electronics/electronics1-icn@2x.png"
)

preferences {
	section("Select Nest Devices...") {
		input "nestDevices", "device.nest", title: "Which Nest Devices?", multiple: true, required: true
        
	}
}

def installed() {
	subscribe(location, "mode", modeChangeHandler)
}

def updated() {
	unsubscribe()
	subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler(evt) {
	log.debug("modeChangeHandler: ${evt.value}")
    
	if (evt.value == "Away") {
		nestDevices.away()
	} else if (evt.value == "Home") {
    	nestDevices.present()
	}
}