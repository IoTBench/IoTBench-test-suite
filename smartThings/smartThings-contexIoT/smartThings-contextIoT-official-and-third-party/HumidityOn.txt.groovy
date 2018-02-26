/**
 *  Humidity On
 *
 *  Copyright 2014 Juhani Naskali
 *
 *	Created 2014-05-02
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
    name: "Humidity On",
    namespace: "fi.naskali.humidityon",
    author: "Juhani Naskali",
    description: "Get a notification or switch a humidifier (or some other switch) on/off, when the humidity crosses a limit. Icon by hitbit-pa.deviantart.com.",
    category: "",
    iconUrl: "http://i.imgur.com/86f4aFM.png",
    iconX2Url: "http://i.imgur.com/lrKc9Ui.png"
)


preferences {
	section("When humidity gets...") {
		input"comparison", "enum", title: "lower/higher than", required: true, multiple: false,
        metadata:[
          	values:["lower", "higher"]
        ]
        input"limit", "number", title: "Limit (in percentage)", description:"15"
		input "humid", "capability.relativeHumidityMeasurement", title: "with sensor", required: true, multiple: true
	}
    section("React...") {
		input "switches", "capability.switch", title: "Turn a thing", required: false
		input"onoff", "enum", title: "on/off", required: false, multiple: false,
        metadata:[
          	values:["on", "off"]
        ]
        input"notify", "bool", title: "Push notification", required: false
        
    
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
	subscribe(humid, "humidity", humidityHandler)
}

def humidityHandler(evt) {
	log.debug "Humidity: $evt.value, $evt.unit, $evt.name, $evt"
    BigDecimal humidityNum = new BigDecimal(evt.value.trim().replace("%", ""))
	if(comparison == "lower" && humidityNum <= limit) {
    	log.debug "Humidity lower than limit,"
        if(switches) {
        	turnSwitch()
        }
        if(notify) {
        	sendPush("Alert: Current humidity is $evt.value")
        }

	} else if(comparison == "higher" && humidityNum >= limit) {
    	log.debug "Humidity higher than limit,"
        if(switches) {
        	turnSwitch()
        }
        if(notify) {
        	sendPush("Alert: Current humidity is $evt.value")
        }
    } else {
    	log.debug "Humidity within parameters."
    }
    
}

def turnSwitch() {
	if(onoff == "on") {
    	log.debug "Switching thing on."
        switches.on()
    } else {
    	log.debug "Switching thing off."
    	switches.off()
    }
}