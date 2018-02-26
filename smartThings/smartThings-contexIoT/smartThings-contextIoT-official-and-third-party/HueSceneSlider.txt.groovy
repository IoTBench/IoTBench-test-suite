/**
 *  Hue Scene Slider
 *
 *	Allows you to create scenes for your Hue and other smart bulbs and navigate between them with a single Dimmer slider. I recommend the "Better Virtual Dimmer" device type by twack@wackware.net.
 *
 *  Author: Josiah Spence
 *  
 *  Version: 1.0.0
 *
 * 	Based in part on Hue/Bulb Scenes by Rob Landry
 *
 *	Special thanks to Terry Gauchat (github @cosmicpuppy) for help with the variable switch names. That was a doozy to figure out.
 *  
 *  Date: 2015-02-21
 */
definition(
    name: "Hue Scene Slider",
    namespace: "hueslider",
    author: "Josiah Spence",
    description: "Cycle through Hue Scenes based on a dimmer slider.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png"
)

preferences {
	
	page(name: "basicSetup", title: "Basic Setup", nextPage: "sceneSetup", uninstall: true) {
        section("Info") {
            paragraph "Author: \tJosiah Spence"
            paragraph "Version: \t1.0.0"
            paragraph "Date: \t2/21/2015"
            paragraph "The distance a dimmer needs to move to switch scenes will depend on the number of scenes you configure. For example, for 5 scenes, each scene will occupy 20% of your dimmer slider, sequentially."
        }


        section("Dimmer") {
            input "dimmer", "capability.switchLevel", title: "Which dimmer switch?", description: "Tap to select a switch", multiple: false
            input name: "sceneNumber", type: "number", title: "How many Scenes Do You Need?", defaultValue: 2
            input name: "defaultOption", type: "bool", title: "Default to Scene 1 When Switched On?", defaultValue: false
        }


        // Bulb Selection //
        section("Bulb Selection") {
            input "hues", "capability.colorControl", title: "Which Hue Bulbs?", required:false, multiple:true
        }
    }
    
    page(name: "sceneSetup", title: "Basic Setup", install: true, uninstall: true)

}


def sceneSetup() {
   dynamicPage(name: "sceneSetup"){

        for(int i = 1; i <= sceneNumber; i++) {
            // Scene 1 //
            section("Scene $i settings") {
                input "color$i", "enum", title: "Hue Color?", required: false, multiple:false, options: [
                    "Soft White":"Soft White - Default",
                    "White":"White - Concentrate",
                    "Daylight":"Daylight - Energize",
                    "Warm White":"Warm White - Relax",
                    "Red":"Red",
                    "Movie Red":"Movie Red",
                    "Green":"Green",
                    "Blue":"Blue",
                    "Yellow":"Yellow",
                    "Orange":"Orange",
                    "Purple":"Purple",
                    "Pink":"Pink"]
                input "lightLevel$i", "enum", title: "Light Level?", required: false, options: [10:"10%",20:"20%",30:"30%",40:"40%",50:"50%",60:"60%",70:"70%",80:"80%",90:"90%",100:"100%"]



                // Smart Bulbs //
                input "bulbs$i", "capability.switchLevel", title: "Which Smart Bulbs to Dim?", required:false, multiple:true
                input "bulbLevel$i", "enum", title: "Light Level?", required: false, options: [10:"10%",20:"20%",30:"30%",40:"40%",50:"50%",60:"60%",70:"70%",80:"80%",90:"90%",100:"100%"]

                // Turn Off //
                input "bulbsOff$i", "capability.switch", title: "Turn Off Which Bulbs?", required:false, multiple:true

                // Turn On //
                input "bulbsOn$i", "capability.switch", title: "Turn On Which Bulbs?", required:false, multiple:true
            }
        }
    }
}


def installed() {
	log.debug "Installing 'Hue Scene Slider'"

	commonInit()
}

def updated() {
	log.debug "Updating 'Hue Scene Slider'"
	unsubscribe()

	commonInit()
}

private commonInit() {
	subscribe(dimmer,"level",updateLevel)
    subscribe(dimmer,"switch.on",onHandler)
    subscribe(dimmer,"switch.off",offHandler)
}

def onHandler(evt) {
	 log.debug "Multi Dimmer: ON"
     
     if (defaultOption){
          
        dimmer.setLevel(1)
     
     } else {
     
     	hues*.on()
     	bulbsAllOff*.on()
     
     }
}


def offHandler(evt) {
    log.debug "Multi Dimmer: OFF"
	hues*.off()
    bulbsAllOff*.off()
}

def updateLevel(evt) {

	log.debug "UpdateLevel: $evt"
	int level = dimmer.currentValue("level")
	log.debug "level: $level"

	def i = 1
	def hueColor = 0
	def saturation = 100
    
    def mathPercent = 100/sceneNumber
    def mathCurrentTop = 0
    def mathCurrentBottom = 0
    
    log.debug "Math Percent = $mathPercent"

	for(int n = 1; n <= sceneNumber; n++) {
    
		mathCurrentTop = mathPercent * n
        
        if(n == 1){
	        mathCurrentBottom = mathCurrentTop - mathPercent
    	} else {
        	mathCurrentBottom = mathCurrentTop - mathPercent + 1
        }
    
    	log.debug "Math Top = $mathCurrentTop"
        log.debug "Math Bottom = $mathCurrentBottom"
            
    	if (level >= mathCurrentBottom && level < mathCurrentTop) {
    
            switch(settings."color${n}") {
                case "White":
                    hueColor = 52
                    saturation = 19
                    break;
                case "Daylight":
                    hueColor = 53
                    saturation = 91
                    break;
                case "Soft White":
                    hueColor = 23
                    saturation = 56
                    break;
                case "Warm White":
                    hueColor = 20
                    saturation = 80 //83
                    break;
                case "Blue":
                    hueColor = 70
                    break;
                case "Green":
                    hueColor = 39
                    break;
                case "Yellow":
                    hueColor = 25
                    break;
                case "Orange":
                    hueColor = 10
                    break;
                case "Purple":
                    hueColor = 75
                    break;
                case "Pink":
                    hueColor = 83
                    break;
                case "Red":
                    hueColor = 100
                    break;
                case "Movie Red":
                    hueColor = 8
                    break;
            }

            def newValue = [hue: hueColor, saturation: saturation, level: lightLevel as Integer ?: 100]
            def bulbNewValue = [level: bulbLevel as Integer ?: 100]

            log.debug "new hue value = $newValue"
            log.debug "new bulb value = $bulbNewValue.level"

            hues*.setColor(newValue)
            settings."bulbs${n}"*.setLevel(bulbNewValue.level)
            settings."bulbsOn${n}"*.on()
            settings."bulbsOff${n}"*.off()
        }
    }
    
}