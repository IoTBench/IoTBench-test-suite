/**
 *  Big Switch for Hello Home Phrases
 *
 *  Current Version: 1.0
 *
 *
 *
 *  Copyright 2015 Tim Slagle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *	The original licensing applies, with the following exceptions:
 *		1.	These modifications may NOT be used without freely distributing all these modifications freely
 *			and without limitation, in source form.	 The distribution may be met with a link to source code
 *			with these modifications.
 *		2.	These modifications may NOT be used, directly or indirectly, for the purpose of any type of
 *			monetary gain.	These modifications may not be used in a larger entity which is being sold,
 *			leased, or anything other than freely given.
 *		3.	To clarify 1 and 2 above, if you use these modifications, it must be a free project, and
 *			available to anyone with "no strings attached."	 (You may require a free registration on
 *			a free website or portal in order to distribute the modifications.)
 *		4.	The above listed exceptions to the original licensing do not apply to the holder of the
 *			copyright of the original work.	 The original copyright holder can use the modifications
 *			to hopefully improve their original work.  In that event, this author transfers all claim
 *			and ownership of the modifications to "SmartThings."
 *
 *	Original Copyright information:
 *
 *	Copyright 2015 SmartThings
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Big Switch for Hello Home Phrases",
    namespace: "tslagle13",
    author: "Tim Slagle",
    description: "Uses a virtual/or physical switch to run hello home phrases.",
    category: "Convenience",
    iconUrl: "http://icons.iconarchive.com/icons/icons8/windows-8/512/User-Interface-Switch-On-icon.png",
    iconX2Url: "http://icons.iconarchive.com/icons/icons8/windows-8/512/User-Interface-Switch-On-icon.png"
)


preferences {
	page(name: "selectPhrases")

    page( name:"Settings", title:"Settings", uninstall:true, install:true ) {
    section("Settings") {
    	label title: "Assign a name", required: false
  	}
    section(title: "More options", hidden: hideOptionsSection(), hideable: true) {

			def timeLabel = timeIntervalLabel()

			href "timeIntervalInput", title: "Only during a certain time", description: timeLabel ?: "Tap to set", state: timeLabel ? "complete" : null

			input "days", "enum", title: "Only on certain days of the week", multiple: true, required: false,
				options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

			input "modes", "mode", title: "Only when mode is", multiple: true, required: false
		}
  }
}



def selectPhrases() {
	def configured = (settings.HHPhraseOff && settings.HHPhraseOn)
    dynamicPage(name: "selectPhrases", title: "Configure", nextPage:"Settings", uninstall: true) {		
		section("When this switch is turned on or off") {
			input "master", "capability.switch", title: "Where?"
		}
        def phrases = location.helloHome?.getPhrases()*.label
		if (phrases) {
        	phrases.sort()
		section("Run These Hello Home Phrases When...") {
			log.trace phrases
			input "HHPhraseOn", "enum", title: "The Switch Turns On", required: false, options: phrases, refreshAfterSelection:true
			input "HHPhraseOff", "enum", title: "The Switch Turns Off", required: false, options: phrases, refreshAfterSelection:true

		}
		}
    }
}    


def installed(){
subscribe(master, "switch.on", onHandler)
subscribe(master, "switch.off", offHandler)
}

def updated(){
unsubscribe()
subscribe(master, "switch.on", onHandler)
subscribe(master, "switch.off", offHandler)
}

def onHandler(evt) {
if(allOk){
log.debug evt.value
log.info("Running Light On Event")
sendNotificationEvent("Performing \"${HHPhraseOn}\" because ${master} turned on.")
location.helloHome.execute(settings.HHPhraseOn)
}
}

def offHandler(evt) {
if(allOk){
log.debug evt.value
log.info("Running Light Off Event")
sendNotificationEvent("Performing \"${HHPhraseOff}\" because ${master} turned off.")
location.helloHome.execute(settings.HHPhraseOff)
}
}

private getAllOk() {
	modeOk && daysOk && timeOk
}

private getModeOk() {
	def result = !modes || modes.contains(location.mode)
	log.trace "modeOk = $result"
	result
}

private getDaysOk() {
	def result = true
	if (days) {
		def df = new java.text.SimpleDateFormat("EEEE")
		if (location.timeZone) {
			df.setTimeZone(location.timeZone)
		}
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		def day = df.format(new Date())
		result = days.contains(day)
	}
	log.trace "daysOk = $result"
	result
}

private getTimeOk() {
	def result = true
	if (starting && ending) {
		def currTime = now()
		def start = timeToday(starting).time
		def stop = timeToday(ending).time
		result = start < stop ? currTime >= start && currTime <= stop : currTime <= stop || currTime >= start
	}
	log.trace "timeOk = $result"
	result
}

private hhmm(time, fmt = "h:mm a")
{
	def t = timeToday(time, location.timeZone)
	def f = new java.text.SimpleDateFormat(fmt)
	f.setTimeZone(location.timeZone ?: timeZone(time))
	f.format(t)
}

private hideOptionsSection() {
	(starting || ending || days || modes) ? false : true
}

private timeIntervalLabel() {
	(starting && ending) ? hhmm(starting) + "-" + hhmm(ending, "h:mm a z") : ""
}