definition(
    name: "Lutron Group Control",
    namespace: "sticks18",
    author: "sgibson18@gmail.com",
    description: "Add bulbs to a Lutron Remote without using the remote's join process",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page(name: "inputPage")
}

def inputPage() {
	dynamicPage(name: "inputPage", title: "", install: true, uninstall: true) {
    
    section("Remote Setup") {
		paragraph "To get the Group Id of your remote, you must open Live Logging then fill in the inputs from this section. The code for the connected bulb selected must have a debug statement in its parse section, so you can see the response containing the group id.",
            title: "First let's get the Group Id from your Remote by checking an already connected bulb", required: true        
        input (name: "remote", type: "capability.button", title: "Lutron Remote", multiple: false, required: true, submitOnChange: true)
		input (name: "cBulb", type: "capability.switch", title: "Select a bulb connected to the remote", multiple: false, required: true, submitOnChange: true)
		input (name: "endpoint", type: "text", title: "Zigbee Endpoint of selected bulb", required: true, submitOnChange: true)
    }
    
    if (remote && cBulb && endpoint) {
    	
        remote.getGroup(cBulb.deviceNetworkId, endpoint)
        
        section("Enter the Group Id") {
        	input (name: "grpId", type: "text", title: "Group Id for Remote", required: true)
        }
    	section("Now let's pick some new bulbs"){
			input (name: "nBulbs", type: "capability.switch", title: "Select bulbs to add to the remote (all must use same endpoint)", multiple: true, required: true)
            input (name: "nBulbEp", type: "text", title: "Zigbee Endpoint of selected bulbs", required: true)
		}
    }
    }
}

def installed() {
    initialize()
}

def updated() {
	unsubscribe()
    removeBulbs()
    addBulbs()
    updateGrp()
}


def addBulbs() {
    nBulbs.each {
    	def zigId = it.deviceNetworkId
        def endpoint = nBulbEp
        remote.assignGroup(zigId, endpoint, grpId)
    }
    
}

def removeBulbs() {
    nBulbs.each {
    	def zigId = it.deviceNetworkId
        def endpoint = nBulbEp
        remote.removeGroup(zigId, endpoint, grpId)
    }
    
}

def updateGrp() {
	remote.updateGroup(grpId)
}

def initialize() {
	addBulbs()
    updateGrp()
}

def uninstalled() {
	removeBulbs()
}
