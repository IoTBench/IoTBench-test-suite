/*
* Good night and make your morning ready
* Author: Z. Berkay Celik
* The app is created by Z. Berkay Celik
* Email: zbc102@cse.psu.edu
*/
definition(
    name: "goodnight",
    namespace: "soteria",
    author: "Soteria",
    description: "Set alarm and coffee machine when you turned off your lights",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
)


preferences {
	page(name: "mainPage", title: "Play a selected song when I turned off bedroom light switch", nextPage: "chooseTrack", uninstall: true)
	page(name: "chooseTrack", title: "Select a song", install: true)
}


def mainPage() {
	section("When I turned off bedroom light switch...") {
		input "bedroomSwitch", "capability.switch", multiple: true
	}
	
	//more advanced configuration is possible
	section ("Choose a Sonos and time to activate") {
       	input "SonosAlarm", "capability.musicPlayer", required: true
       	input "alarmTime", "time", required:true
    }

    // can support more advanced coffee machines
    // https://community.smartthings.com/t/smart-or-dumb-coffee-maker/58152
    section("Coffee Machine switch and time for turning on coffee machine") {
		input "coffeeMacSwitch", "capability.switch", type: "time", required: true
	}
}


def installed()
{
	subscribe(bedroomSwitch, "bedroomSwitch.off", offHandler)
}

def updated()
{
	unsubscribe()
	subscribe(bedroomSwitch, "bedroomSwitch.off", offHandler)
}


def offHandler(){
	schedule(alarmTime, "startAlarm")
	schedule(coffeeTime, "coffeeMachineTurnOn")
}

def startAlarm() {
	log.debug "startAlarm: $evt"
	SonosAlarm.playTrack(state.selectedSong)
}

def coffeeMachineTurnOn(){
	coffeeMacSwitch.on()
}

private songOptions() {
	// Make sure current selection is in the set
	def options = new LinkedHashSet()
	if (state.selectedSong?.station) {
		options << state.selectedSong.station
	}
	else if (state.selectedSong?.description) {
		// TODO - Remove eventually
		options << state.selectedSong.description
	}
	// Query for recent tracks
	def states = sonos.statesSince("trackData", new Date(0), [max:30])
	def dataMaps = states.collect{it.jsonValue}
	options.addAll(dataMaps.collect{it.station})

	log.trace "${options.size()} songs in list"
	options.take(20) as List
}

private saveSelectedSong() {
	try {
		def thisSong = song
		log.info "Looking for $thisSong"
		def songs = sonos.statesSince("trackData", new Date(0), [max:30]).collect{it.jsonValue}
		log.info "Searching ${songs.size()} records"

		def data = songs.find {s -> s.station == thisSong}
		log.info "Found ${data?.station}"
		if (data) {
			state.selectedSong = data
			log.debug "Selected song = $state.selectedSong"
		}
		else if (song == state.selectedSong?.station) {
			log.debug "Selected existing entry '$song', which is no longer in the last 20 list"
		}
		else {
			log.warn "Selected song '$song' not found"
		}
	}
	catch (Throwable t) {
		log.error t
	}
}

def chooseTrack() {
	dynamicPage(name: "chooseTrack") {
		section{
			input "song","enum",title:"Play this track", required:true, multiple: false, options: songOptions()
		}
		section([mobileOnly:true]) {
			label title: "Assign a name", required: false
			mode title: "Set for specific mode(s)", required: false
		}
	}
}

