/**
 *  Automatic Light
 *
 *  Copyright 2017 Vincent Gargiulo
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Automatic Light",
    namespace: "VincentGargiulo",
    author: "Vincent Gargiulo",
    description: "This app turns on a light when motion is detected during a user specified time period. The user can select which days this app is enabled. The user can also set a timeout period for the light. This will cause the light to turn off if the motion sensor has not detected movement in that amount of time.",
    category: "Convenience",
    iconUrl: "https://i.imgur.com/ygPB6wB.png",
    iconX2Url: "https://i.imgur.com/ygPB6wB.png",
    iconX3Url: "https://i.imgur.com/ygPB6wB.png")


preferences 
{
	section("Select Motion Sensor") 
    {
    	input "motionSensor", "capability.motionSensor", title: "Which Motion Sensor?", required: true, multiple: false
   	}
    section("Select Light Source") 
    {
      	input "light", "capability.switch", title: "Which Light Bulb?", required: true, multiple: false
   	}
    section("Time to wait before turning off the light if no motion is detected") 
    {
    	input "minutesToWait", "number", required: true, title: "How many Minutes?"
	}
	section("What time period should this app be active?") 
    {
      	input "fromTime", "time", title: "From", required: true
        input "toTime", "time", title: "To", required: true
    }
    section("On which days should this app be active?")
    {
        input "userDayEnable", "enum", title: "Select the Days of the Week", required: true, multiple: true, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday":"Saturday", "Sunday":"Sunday"]
    }
}

def installed() 
{
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize()
{
	log.debug "location.id: ${location.id}"
	subscribe(motionSensor, "motion", motionDetection)
}

def motionDetection(evt)
{
    //Date formatting and time zone normalizations (with respect to the HUB location)
    def dateFormat = new java.text.SimpleDateFormat("EEEE")
    dateFormat.setTimeZone(location.timeZone)
    //Check the current day
    def day = dateFormat.format(new Date())
    //Check if the current day, is a day enabled by the user
	def dayCheck = userDayEnable.contains(day)
	//Time Period Check
	def active = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
	
	//Motion is Detected
	if (evt.value == "active")
    {
    	log.debug "Motion Sensor reported active"
        if (active & dayCheck)
        {
            light.on()
        }
	}
    //Motion Stopped
    else if (evt.value == "inactive") 
    {
    	log.debug "Motion Sensor reported Inactive"
    	if (active & dayCheck)
        {
        	//Re-Check if inactive after waiting the user specified time
    		runIn(60 * minutesToWait, checkIfInactive) 
        }
    }
    
}

def checkIfInactive()
{
	log.debug "Checking if still inactive";
    //Get Current State of the motion sensor
    def motionState = motionSensor.currentState("motion")
     if (motionState.value == "inactive")
     {
     	//Gets the time that the inactive state was reported and subtracts that from the current time
     	def elapsedTime = now() - motionState.date.time
        //Convert to ms
        def timeToWaitMS = 1000 * 60 * minutesToWait
        
        if (elapsedTime > timeToWaitMS)
        {
        	log.debug "Turning off light"
			light.off()
        }
        else
        {	
        	log.debug "Motion was not inactive long enough, keep the light on"
        }

     }
     else if (motionState.value == "active")
     {
     	log.debug "Motion is still active, keeping light on"
     }
}