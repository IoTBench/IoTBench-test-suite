/**
 *  Smarter I'm At Home Simulator.
 *
 *  Author: Austin Fonacier
 *  Twitter: @austinrfnd
 *  Github: http://github.com/austinrfnd
 *
 */


// Automatically generated. Make future change here.
definition(
    name: "Smarter I'm At Home Simulator.",
    namespace: "",
    author: "austinrf@gmail.com",
    description: "This program will turn on and off random light(s) to give a better illusion that you are home.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png")

preferences {
  section("Light switches to turn on/off"){
    input "switches", "capability.switch", title: "Switches", multiple: true, required: true
  }
  section("How often to cycle the lights"){
    input "frequency_minutes", "number", title: "Minutes?"
  }
  section("Number of actives lights at any given time"){
    input "number_of_active_lights", "number", title: "Number of active lights"
  }
}

def installed() {
  scheduleCheck()
}

def updated() {
  unsubscribe()
  unschedule()
  scheduleCheck()
}

def initialize()
{
  unsubscribe()
  unschedule()
  scheduleCheck()
}


// We want to turn off all the lights
// Then we want to take a random set of lights and turn those on
// Then run it again when the frequency demands it
def scheduleCheck() {
  // turn off all the switches
  switches.off()
  
  // grab a random switch
  def random = new Random()
  def inactive_switches = switches
  for (int i = 0 ; i < number_of_active_lights ; i++) {
    // if there are no inactive switches to turn on then let's break
    if (inactive_switches.size() == 0){
      break
    }

    // grab a random switch and turn it on
    def random_int = random.nextInt(inactive_switches.size())
    inactive_switches[random_int].on()

    // then remove that switch from the pool off switches that can be turned on
    inactive_switches.remove(random_int)
  }

  // re-run again when the frequency demands it
  runIn(frequency_minutes * 60, scheduleCheck)
}

