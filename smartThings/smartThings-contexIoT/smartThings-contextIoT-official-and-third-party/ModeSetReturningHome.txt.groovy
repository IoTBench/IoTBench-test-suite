/**
 *  Returning Home, Mode Set
 *
 *  Copyright 2015 Christopher Fraley
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
 *  Based on code from RBoy's "5-2 Day Thermostat" app, mode control code posted in the community by Ronny Carr (ronnycarr),
 *	and Tim Slagle's (tslagle13) "Hello, Home Phrase Director" app.
 *
 *	Originally published: 2015-05-22
 *  Current version 1.11
 *  Last updated: 2015-05-25
 *  Summary of Changes:
 *		(1.1) Changed code to control Night mode switch based on sunset.  Optional code supplied at lines 120/121 and 139/140 
 *			  to have the Night mode based upon specific time set in app preferences. 
 *		(1.11) Bug fixes.
 *
 */
 
definition(
    name: "Mode Set, Returning Home",
    namespace: "fraleyct",
    author: "Christopher Fraley",
    description: "Sets smart house mode when someone returns home using current day of the week, sun state, and time.",
    category: "Mode Magic",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
  section("When who arrives home..."){
	input "presence1", "capability.presenceSensor", title: "Who?", multiple: true
  }
  section("Monday thru Friday Schedule:") {
	input "time1", "time", title: "Morning", required: true
	input "time2", "time", title: "Day",     required: true
	input "time3", "time", title: "Evening", required: true
	input "time4", "time", title: "Night"  , required: true
  }
  section("Saturday and Sunday Schedule:") {
	input "time11", "time", title: "Morning", required: true
	input "time21", "time", title: "Day",     required: true
	input "time31", "time", title: "Evening", required: true
	input "time41", "time", title: "Night"  , required: true
  }
    section("Zip code:") {
	input "zip", "text", required: true
  }
}

  def installed() {
	subscribe(presence1, "presence.present", presence)
}

  def updated() {
	unsubscribe()
	subscribe(presence1, "presence.present", presence)
}

  def presence(evt){

// This section determines which day and what time it is.
unschedule()
  def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
  def today = calendar.get(Calendar.DAY_OF_WEEK)
  def timeNow = now()
  def homeMode
  def midnightToday = timeToday("2000-01-01T23:59:59.999-0000", location.timeZone)
  def midnightYesturday = timeToday("2000-01-01T23:59:59.999-0000", location.timeZone)-1

// This section determines the current state of the sun.  
  def sunMode
  def zip = settings.zip as String
  def sunInfo = getSunriseAndSunset(zipCode: zip)
  		if (sunInfo.sunrise.time < timeNow && sunInfo.sunset.time > timeNow) {sunMode = "up"}
			else {sunMode = "down"}
  def sunsetToday = sunInfo.sunset          
  
// This section is only used for debug purposes.
   	log.debug("Current time is ${(new Date(timeNow)).format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
	log.debug("Midnight today is ${midnightToday.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
    log.debug("Midnight yesturday was ${midnightYesturday.format("EEE MMM dd yyyy HH:mm z", location.timeZone)}")
    log.debug("Smart House's current system mode is ${location.mode}")
    log.debug("Smart House's zip code is ${zip}")
    log.debug("The sun is currently ${sunMode}")
    log.debug("Sunset today is at ${sunsetToday.format("HH:mm z", location.timeZone)}")
	log.trace("Weekday Morning ${timeToday(time1, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekday Day ${timeToday(time2, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekday Evening ${timeToday(time3, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekday Night ${timeToday(time4, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend Morning ${timeToday(time11, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend Day ${timeToday(time21, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend Evening ${timeToday(time31, location.timeZone).format("HH:mm z", location.timeZone)}")
   	log.trace("Weekend Night ${timeToday(time41, location.timeZone).format("HH:mm z", location.timeZone)}") 

// This section is where the system mode is set based on day of the week, current sun state, and time.
  switch (today) {
	case Calendar.MONDAY:
	case Calendar.TUESDAY:
	case Calendar.WEDNESDAY:
	case Calendar.THURSDAY:
 	case Calendar.FRIDAY:

       	if (timeNow >=(midnightToday-1).time && timeNow < timeToday(time1, location.timeZone).time){homeMode = "Night"} //it's night
  			else if (timeNow >= timeToday(time1, location.timeZone).time && timeNow < timeToday(time2, location.timeZone).time){homeMode = "Morning"} //it's morning
  			else if (timeNow >= timeToday(time2, location.timeZone).time && timeNow < timeToday(time3, location.timeZone).time){homeMode = "Weekday"} //it's weekday
       		else if (timeNow >= timeToday(time3, location.timeZone).time && timeNow < sunsetToday.time){homeMode = "Evening"} //it's evening
  			else if (timeNow >= sunsetToday.time && timeNow < midnightToday.time){homeMode = "Night"} //it's night

//  Use the below code identified with comment marks if user would rather have the system mode set to Night based on a specific time and not based on sunset.
//  Ln 115	else if (timeNow >= timeToday(time3, location.timeZone).time && timeNow < timeToday(time4, location.timeZone).time){homeMode = "Evening"} //it's evening
//  Ln 116	else if (timeNow >= timeToday(time4, location.timeZone).time && timeNow < midnightToday.time){homeMode = "Night"} //it's night
 
 if (location.modes?.find{it.name == homeMode}) {
    		sendPush("Changing mode to ${homeMode}, welcome back!")
    		setLocationMode(homeMode)
  
			break
}
	case Calendar.SATURDAY:
	case Calendar.SUNDAY:

       	if (timeNow >=(midnightToday-1).time && timeNow < timeToday(time11, location.timeZone).time){homeMode = "Night"} //it's night
  			else if (timeNow >= timeToday(time11, location.timeZone).time && timeNow < timeToday(time21, location.timeZone).time){homeMode = "Morning"} //it's morning
  			else if (timeNow >= timeToday(time21, location.timeZone).time && timeNow < timeToday(time31, location.timeZone).time){homeMode = "Weekend"} //it's weekday
       		else if (timeNow >= timeToday(time31, location.timeZone).time && timeNow < sunsetToday.time){homeMode = "Evening"} //it's evening
  			else if (timeNow >= sunsetToday.time && timeNow < midnightToday.time){homeMode = "Night"} //it's night

//  Use the below code identified with comment marks if user would rather have the system mode set to Night based on a specific time and not based on sunset.
//  Ln 134	else if (timeNow >= timeToday(time31, location.timeZone).time && timeNow < timeToday(time41, location.timeZone).time){homeMode = "Evening"} //it's evening
//  Ln 135  else if (timeNow >= timeToday(time41, location.timeZone).time && timeNow < midnightToday.time){homeMode = "Night"} //it's night

  			if (location.modes?.find{it.name == homeMode}) {
    			sendPush("Changing mode to ${homeMode}, welcome back!")
    			setLocationMode(homeMode)
  }
			break
 }
}
