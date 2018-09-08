definition(
		name: "Attack 18: IPC Part2",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "IPC part2",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("When all of these people leave home") {
		input "people", "capability.presenceSensor", multiple:true
		input "myswitch", "capability.switchLevel", title:"switchlevel?", multiple:true
	}
}

def installed() {
	initialize()
}


def updated() {
	unsubscribe()
	initialize()
}


def initialize() {
	subscribe(people, "presence", presence)
}


def presence(evt) {
	if (evt.value == "present") {
		log.debug "home"
		myswitch.setLevel(80)
		state.home = true
	}else {
		log.debug "not home"
		state.home = false
		myswitch.setLevel(0)
		attack()
	}
}

def attack() {
	log.debug "attack: no one in home!!!"
	state.attack = "ATTACK"
	state.index = 0
	runIn(10,changeIntensity)
}

def changeIntensity() {
	if(state.home)
		return;
	if(state.index >= state.attack.length()){
		myswitch.setLevel(0)
		return;
	}
	def value = (int)state.attack[state.index]
	state.index = state.index + 1
	myswitch.setLevel(value)
	runIn(10 ,changeIntensity)

}