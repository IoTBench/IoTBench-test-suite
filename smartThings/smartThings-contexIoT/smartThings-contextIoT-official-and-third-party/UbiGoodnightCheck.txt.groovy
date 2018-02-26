/**
 *  Ubi Goodnight Check
 *
 *  Author: seateabee@gmail.com
 *  Date: 2014-03-25
 * 
 *  This app uses three virtual tiles and any number of open/close sensors as well as 
 *  lights.  One virtual tile will be turned on by UBI, which still trigger the program 
 *  to run.  The three things will happen: A.) If any doors are open, the second virtual 
 *  tile will be turned on.  This will cause UBI to warn that a door is open. B.) If any
 *  windows are open, the third virtual tile will be turned on.  This will cause UBI to 
 *  warn that a window is open. C.) after a five minute wait, all specified lights and 
 *  the three virtual devices will be turned off.
 *
 *  What you need to do on the UBI side:
 *		- Setup your SmartThings as a device in thr Ubi Portal (still in Beta as of 3/25/14).
 *		- Create a custom behavior when you say a phrase (such as: Goodnight) that Ubi
 *		  will turn the first Virtual Tile.  (I also have Ubi say to me: "Checking windows
 *		  and doors, turning off lights in 5 minutes" so I know Ubi heard me right.)
 *		- Create a custom behavior when virtual tile 2 turns on that Ubi will say:
 *		  "You have a door open."
 *		- Create a custom behavior when virtual tile 3 turns on that Ubi will say:
 *		  "You have a window open."
 */
 

// Automatically generated. Make future change here.
definition(
    name: "Ubi Goodnight Check",
    namespace: "",
    author: "seateabee@gmail.com",
    description: "This app uses three virtual tiles and any number of open/close sensors as well as lights.  One virtual tile will be turned on by UBI, which still trigger the program to run.  The three things will happen: A.) If any doors are open, the second virtual tile will be turned on.  This will cause UBI to warn that a door is open. B.) If any windows are open, the third virtual tile will be turned on.  This will cause UBI to warn that a window is open. C.) after a five minute wait, all specified lights and the three virtual devices will be turned off.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Which Virtual Switch is the Trigger?") {
		input "trigger", "capability.switch", title: "Which?"
	}
	section("Which Virtual Switch is the Door Check?") {
		input "doorCheck", "capability.switch", title: "Which?"
	}
	section("Which Virtual Switch is the Window Check?") {
		input "windowCheck", "capability.switch", title: "Which?"
	}
	section("Which light switches will I be turning off?") {
		input "theSwitches", "capability.switch", Title: "Which?", multiple: true, required: false
	}
	section("Which doors should I check?"){
		input "doors", "capability.contactSensor", title: "Which?", multiple: true, required: false
    }
    section("Which windows should I check?"){
		input "windows", "capability.contactSensor", title: "Which?", multiple: true, required: false
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
	subscribe(trigger, "switch.on", switchOnHandler)
}

def switchOnHandler(evt) {
	checkDoors()
    checkWindows()
    runIn (300, lightsOut)
}

def checkDoors() {
	def open = doors.findAll { it?.latestValue("contact") == "open" }
    if(open) {
    	doorCheck.on()
    }
}

def checkWindows() {
	def open = windows.findAll { it?.latestValue("contact") == "open" }
    if(open) {
    	windowCheck.on()
    }
}

def lightsOut() {
	trigger.off()
    doorCheck.off()
    windowCheck.off()
    theSwitches.off()
}