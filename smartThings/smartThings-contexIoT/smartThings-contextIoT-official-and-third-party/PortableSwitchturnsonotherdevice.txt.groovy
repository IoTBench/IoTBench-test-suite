/**
 *  Portable Switch turns on other device
 *
 *  Author: seateabee@gmail.com
 *  Date: 2013-10-19
 */


// Automatically generated. Make future change here.
definition(
    name: "Portable Switch turns on other device",
    namespace: "",
    author: "seateabee@gmail.com",
    description: "This is app is designed to allow a portable switch  (In my case, a non-load zwave switch that is hardwired to a plug meaning I can move it around and plug into any outlet) to act as a controller of another device, such as an outlet or switch.  The portable switch is then turned off right away.Possible uses include using a portable switch along side a bed to give you a way to turn on an overhead light from your bed.  Or turn on an outlet from your couch.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("When this portable switch is turned on...") {
		input "master", "capability.switch", title: "Which?"
	}
	section("This outlet or switch is turned on...") {
		input "slave", "capability.switch", title: "Which?"
	}
}

def installed() {
	subscribe (master, "switch.on", masterOn)
}

def updated() {
	unsubscribe()
	subscribe (master, "switch.on", masterOn)
}

def masterOn(evt) {							
	slave.on()
    master.off()
}