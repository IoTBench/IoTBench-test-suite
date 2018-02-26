/**
 *  Humidity Trend Sample
 *
 *  Copyright 2015 Brian Lowrance
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
    name: "Humidity Trend Sample",
    namespace: "rayzurbock",
    author: "Brian Lowrance; brian@rayzurbock.com",
    description: "Sample on how to check for a humidity trend between readings and act if the trend rises or falls by threshold percentage points",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("How much (percentage) must the humidity rise to trigger a mode change?") {
        input "humidity", "capability.relativeHumidityMeasurement"
        input "humidity1", "number", title: "How much up?"
        input "newModeUp", "mode", title: "Change to this mode when Humidty rises by 'Up' points?"
        input "humidity2", "number", title: "How much down?"
        input "newModeDown", "mode", title: "Change to this mode when Humidity lowers by 'Down' points?"
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
	// TODO: subscribe to attributes, devices, locations, etc.
    subscribe(humidity, "humidity", humidityEvent)
    state.lastHumidity = 0
    state.currentTrend = "NONE"
    state.currentTrendPts = 0
    state.humidityThresholdActivated = false
}

def humidityEvent(evt){
    log.trace "Current Humidity is ${evt.value} as of ${evt.date}"
    log.trace "Last Humidity is ${state.lastHumidity}"
    log.trace "set point ${humidity1}"
    // This example keeps the current and last 2 readings for averaging
    if (state.lastHumidity < evt.value.toInteger()) { 
        state.currentTrend = "UP"
        state.currentTrendPts = evt.value.toInteger() - state.lastHumidity
    }
    if (state.lastHumidity > evt.value.toInteger()) {
        state.currentTrend = "DOWN"
        state.currentTrendPts = evt.value.toInteger() - state.lastHumidity  //This will be a negative number showing how much Humidity has dropped.
    }
    if (state.lastHumidity == evt.value.toInteger()) {
        state.currentTrend = "NONE"
        state.currentTrendPts = 0
    }
    state.lastHumidity = evt.value.toInteger()
    log.trace "Trend: ${state.currentTrend}  TrendPoints: ${state.currentTrendPts}"

    //Check trend and do stuff
    if (((state.currentTrend == "UP") || (state.currentTrend == "NONE")) && (state.currentTrendPts >= settings.humidity1) && (!(state.humidityThresholdActivated))) { 
        //Humidity is trending up and is at or above our threshold.
        // Do stuff
        log.trace "Passed Threshold.. Do Stuff!"
        if (location.mode != newModeUp) { setLocationMode(newModeUp) }
        state.humidityThresholdActivated = true
     } else {
            // Humidity Average is not at or above the threshold.
            log.trace "state.currentTrendPts:${state.currentTrendPts} ; 0 - settings.humidity2:${0 - settings.humidity2}"
            if (state.humidityThresholdActivated && (state.currentTrendPts <= (0 - settings.humidity2))) { 
                // Humidity threshold was previously met but is no longer and has fallen by our threshold...
                // Undo Stuff or do other stuff
                log.trace "Dropped below threshold.. Undo Stuff!"
                if (location.mode != newModeDown) { setLocationMode(newModeDown) }
                state.humidityThresholdActivated = false
            }
     }
}

// TODO: implement event handlers