/**
 *  Auto Dimmer V1.8
 *
 *  Author: Mike Maxwell
 	1.1 2014-12-21
    	--updated logging for more clarity
    1.2 2014-12-27
    	--complete rewrite
    1.3 2015-01-08
    	--corrected logic errors (3 lux settings map to 4 lux ranges and dimmer settings)
    1.4	2015-02-10
    	--added refresh  for dynamic page dimmer overrides
        --added free non commercial usage licensing
        --pretty typing cleanup
	1.5 2015-02-11
		--variable re-name fix
		--mapped 100 to 99% dimmer level
    1.6 2015-03-01
    	--fixed dimmer overrides that wouldn't stick sometimes
    1.7 2015-05-27
    	--Had to change mode input and methods, broke for some reason
 	1.8 2015-06-29
    	--updated description and renamed app
 */
definition(
    name		: "autoDimmer",
    namespace	: "MikeMaxwell",
    author		: "Mike Maxwell",
    description	: 

"This add on smartApp automatically adjusts dimmer levels when dimmer(s) are turned on from physical switches or other smartApps, levels are set based on lux (illuminance) sensor readings and the dimmer levels that you specify." + 
"This smartApp does not turn on dimmers directly, this allows you to retain all your existing on/off smartApps. This smartApp provides intelligent level management to your existing setup.",
    category	: "Convenience",
    iconUrl		: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance.png",
    iconX2Url	: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet-luminance@2x.png"
)

preferences {
    page(name: "page1", title: "autoDimmer Configuration", nextPage: "page2", uninstall: true) {
		section("About: 'autoDimmer snap-in'"){
        	paragraph 	"This add on smartApp automatically adjusts dimmer levels when dimmer(s) are turned on from physical switches or other smartApps, levels are set based on lux (illuminance) sensor readings and the dimmer levels that you specify." + 
						"This smartApp does not turn on dimmers directly, this allows you to retain all your existing on/off smartApps. This smartApp provides intelligent level management to your existing setup."
        }
		section ("Setup:"){
            input(
            	name		: "luxOmatic"
                ,title		: "Use this lux Sensor..."
                ,multiple	: false
                ,required	: true
                ,type		: "capability.illuminanceMeasurement"
            )
            input(
                name		: "dimDark"
                ,title		: "Select default dim level to use when it's dark out..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum"
                ,options	: ["10","20","30","40","50","60"]
            )
            input(
            	name		: "luxDark"
                ,title		: "Select maximum lux level to be considered as Dark..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum"
                ,options	: ["10","25","50","75","100"]
            )
             input(
                name		: "dimDusk"
                ,title		: "Select default dim level to use during dusk/dawn..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum",
                ,options	: ["10","20","30","40","50","60"]
            )
            input(
            	name		: "luxDusk"
                ,title		: "Select maximum lux level to be considered as dusk/dawn..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum"
                ,options	: ["100","125","150","175","200","300","400","500","600"]
            )
            input(
                name		: "dimDay" 
                ,title		: "Select default dim level to use during an overcast day..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum"
                ,options	: ["40","50","60","70","80","90","100"]
            )
            input(
            	name		: "luxBright"
                ,title		: "Select maximum lux level to be considered as overcast..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum"
                ,options	: ["500","1000","2000","3000"]
            )
			input(
                name		: "dimBright" 
                ,title		: "Select default dim level to use when it's sunny outside..."
                ,multiple	: false
                ,required	: true
                ,type		: "enum"
                ,options	: ["40","50","60","70","80","90","100"]
            )
			input(
            	name		: "dimmers"
                ,title		: "Manage these Dimmers..."
                ,multiple	: true
                ,required	: true
                ,type		: "capability.switchLevel"
            )
            input(
            	name		: "modes"
                ,type		: "mode"
                ,title		: "Set for specific mode(s)"
                ,multiple	: true
                ,required	: false
            )
        }
    }

    page(name: "page2", title: "Set individual dimmer levels to override the default settings.", install: true, uninstall: false)

}

def page2() {
    return dynamicPage(name: "page2") {
    	//loop through selected dimmers
        dimmers.each() { dimmer ->
        	def safeName = dimmer.displayName.replaceAll(/\W/,"")
            section ([hideable: true, hidden: true], "${dimmer.displayName} overrides...") {
                input(
                    name					: safeName + "_dark"
                    ,title					: "Dark level"
                    ,multiple				: false
                    ,required				: false
                    ,type					: "enum"
                    ,options				: ["10","20","30","40","50","60"]
                    ,refreshAfterSelection	:true
                )
                input(
                    name					: safeName + "_dusk" 
                    ,title					: "Dusk/Dawn level"
                    ,multiple				: false
                    ,required				: false
                    ,type					: "enum"
                    ,options				: ["40","50","60","70","80"]
                    ,refreshAfterSelection	:true
                )
                input(
                    name					: safeName + "_day" 
                    ,title					: "Day level"
                    ,multiple				: false
                    ,required				: false
                    ,type					: "enum"
                    ,options				: ["40","50","60","70","80","90","100"]
                    ,refreshAfterSelection	:true
                )
                input(
                    name					: safeName + "_bright" 
                    ,title					: "Bright level"
                    ,multiple				: false
                    ,required				: false
                    ,type					: "enum"
                    ,options				: ["40","50","60","70","80","90","100"]
                    ,refreshAfterSelection	:true
                )

			}
    	}
    }
}

def installed() {
   init()
}

def updated() {
	unsubscribe()
    init()
}
def init(){
   subscribe(dimmers, "switch.on", dimHandler)
}

def dimHandler(evt) {
	if (modeIsOK()) {
    	def newLevel = 0
    
		//get the dimmer that's been turned on
		def dimmer = dimmers.find{it.id == evt.deviceId}
    
    	//get its current dim level
    	def crntDimmerLevel = dimmer.currentValue("level").toInteger()
    
    	//get currentLux reading
    	def crntLux = luxOmatic.currentValue("illuminance").toInteger()
    	def prefVar = dimmer.displayName.replaceAll(/\W/,"")
    	def dimVar
    	if (crntLux < luxDark.toInteger()) {
    		//log.debug "mode:dark"
        	prefVar = prefVar + "_dark"
        	dimVar = dimDark
    	} else if (crntLux < luxDusk.toInteger()) {
    		//log.debug "mode:dusk"
            prefVar = prefVar + "_dusk"
            dimVar = dimDusk
  		} else if (crntLux < luxBright.toInteger()) {
    		//log.debug "mode:day"
            prefVar = prefVar + "_day"
            dimVar = dimDay
    	} else {
    		//log.debug "mode:bright"
    		prefVar = prefVar + "_bright"
        	dimVar = dimBright
    	}
   
    	if (!this."${prefVar}") log.info "autoDimmer is using defaults..."
    	else log.info "autoDimmer is using overrides..."
     
    	def newDimmerLevel = (this."${prefVar}" ?: dimVar).toInteger()
		if (newDimmerLevel == 100) newDimmerLevel = 99
    
    	log.info "dimmer:${dimmer.displayName}, currentLevel:${crntDimmerLevel}%, requestedValue:${newDimmerLevel}%, currentLux:${crntLux}"
  
    	if ( newDimmerLevel != crntDimmerLevel ) dimmer.setLevel(newDimmerLevel)
    
	} else {
    	log.info 'skipping, current mode is not selected.'
    }
}
def modeIsOK() {
	def result = !modes || modes.contains(location.mode)
	return result
}

