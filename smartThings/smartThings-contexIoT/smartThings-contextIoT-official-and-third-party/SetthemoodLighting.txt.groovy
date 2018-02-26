/**
 *  Set the Mood (Lighting)
 *
 *  Author: Craig K. Lyons
 */


// Automatically generated. Make future change here.
definition(
    name: "Set the mood (Lighting)",
    namespace: "",
    author: "craig.k.lyons@gmail.com",
    description: "This App allows you to select a dimmer and change the setting to a defined brightness when pressed.",
    category: "",
    iconUrl: "http://etc-mysitemyway.s3.amazonaws.com/icons/legacy-previews/icons-256/rounded-glossy-black-icons-business/086323-rounded-glossy-black-icon-business-light-bulb2-sc52.png",
    iconX2Url: "http://etc-mysitemyway.s3.amazonaws.com/icons/legacy-previews/icons-256/rounded-glossy-black-icons-business/086323-rounded-glossy-black-icon-business-light-bulb2-sc52.png",
    oauth: true
)

preferences {
	section("Which light to set the mood on:") {
		input "switches", "capability.switchLevel"
	}
    section("What Level to set at..."){
    	input "lvl", "number"
    }
}

def installed()
{
	subscribe(app)
}

def updated()
{
	unsubscribe()
	subscribe(app)
}


def appTouch(evt) {
	log.info evt.value
	log.info "lvl: $lvl.value"
    switches.setLevel(lvl.value)
    
    
}


