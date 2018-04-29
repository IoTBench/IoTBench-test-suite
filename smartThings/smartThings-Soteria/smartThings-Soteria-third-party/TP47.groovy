/**
 *  Control Switch Based Upon Play Status
 *
 *  Copyright 2014 Josh Bohde
 *
 */
definition(
    name: "Control Switch Based Upon Play Status",
    namespace: "joshbohde",
    author: "Josh Bohde",
    description: "Turn things on and off based upon the play status of a media player",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("When the player plays..."){
		input "player", "capability.musicPlayer", title: "Which Player?"
	}

	section("Turn on a switch..."){
		input "switches", "capability.switch", multiple: true

    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(player, "status", handler)
}


def handler(evt) {
	log.debug "$evt.value: $evt, $settings"
	log.trace "Turning on switches: $switches"
    if(evt.value == "playing" || evt.value == "paused"){
      unschedule('offHandler')
      switches.on()
    }else{
      runIn(2 * 60, 'offHandler')
    }
}


def offHandler(){
  log.debug "running off handler"
  switches.off()
}
