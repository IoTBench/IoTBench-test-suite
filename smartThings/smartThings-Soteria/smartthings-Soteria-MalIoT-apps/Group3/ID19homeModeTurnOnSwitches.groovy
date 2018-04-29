/*
* Home mode automation
* Take control when home mode is set
* Author: Z. Berkay Celik
* The app is created by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/
definition(
    name: "home mode automation",
    namespace: "soteria",
    author: "Soteria",
    description: "When home mode happens turn on your switches, heater, AC, crockpot etc.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)

preferences {
	section("When mode changes to this: ") {
		input "newMode", title: "Mode?", "enum", metadata: [values: ["Home"]], required: true
	}
	section("When mode is true, turn on...") {
		input "switches", "capability.switch", multiple: true
	}
}

def installed()
{
	subscribe(location, "mode", modeChangeHandler)
}

def updated()
{
	unsubscribe()
	subscribe(location, "mode", modeChangeHandler)
}

def modeChangeHandler(evt) {
	if (location.mode == newMode) {
		//AC, heaters etc.
		switches?.on()
}


