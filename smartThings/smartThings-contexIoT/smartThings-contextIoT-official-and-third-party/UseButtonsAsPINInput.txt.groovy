private def myVersion() { return "v0.1.3-develop+001-unstable" }
// Version Numbering: vMajor.Minor.VisibleFix[-branch]+BuildNo[-State]. For master, branch=beta or null.
// In non-release branches, version number is pre-incremented (i.e., branch version always > base released version).
/**
 *  Use Buttons As PIN Input
 *
 *  Assign a multi-button controller (e.g., Aeon Labs Minimote) to be a security 'PIN code' input pad,
 *    which triggers a switch, lock, mode, or Hello Home action.
 *  More details on GitHub: <https://github.com/CosmicPuppy/SmartThings-ButtonsAsPIN>
 *    and on SmartThings Community Forum: <http://community.smartthings.com/t/SmartApps/8378?u=tgauchat>
 *
 *  Filename: ButtonsAsPIN.app.groovy
 *  Version: see myVersion(), above.
 *  Date: 2015-01-16
 *  Status:
 *    - Beta release to Community for testing, feedback, feature requests.
 *    - Currently hard limited to 1-9 digits,
 *          from a choice of 1 to (number of buttons reported by device, max 9, default 4).
 *    - Tested only with 4-button Aeon Labs Minimote, button-push only, no support for button-hold.
 *    - Testing with "ZWN-SC7 Enerwave 7 Button Scene Controller" (by @mattjfrank) in progress.
 *
 *  Summary Changelog (See github for full Release Notes)
 *    - tbd.
 *
 *  Author: Terry Gauchat (CosmicPuppy)
 *  Email: terry@cosmicpuppy.com
 *  SmartThings Community: @tgauchat -- <http://community.smartthings.com/users/tgauchat>
 *  Latest versions on GitHub at: <https://github.com/CosmicPuppy/SmartThings-ButtonsAsPIN>
 *
 *  There is no charge for this software:
 *  Optional "encouragement funding" is accepted to PayPal address: info@CosmicPuppy.com
 *  (Contributions help cover endless vet bills for Buddy & Deuce, the official CosmicPuppy beagles.)
 *
 *  ----------------------------------------------------------------------------------------------------------------
 *  Copyright 2014 Terry Gauchat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */


import groovy.json.JsonSlurper

/**
 * Frequently edited options, parameters, constants.
 */
/**
 * Disable specific level of logging by commenting out log.* expressions as desired.
 * NB: Someday SmartThings's live log viewer front-end should provide dynamic filter-by-level, right?
 */
private def myDebug(text) {
    // log.debug myLogFormat(text) // NB: Debug level messages including the PIN number! Keep debug off mostly.
}
private def myTrace(text) {
    log.trace myLogFormat(text) // NB: Trace messages are farely minimal. Still helpful even if debug on.
}
private def myInfo(text) {
    log.info myLogFormat(text)  // NB: No usages in this program. TODO: Should some Trace be Info?
}
private def myLogFormat(text) {
    return "\"${app.label}\".(\"${app.name}\"): ${text}"
}


/**
 * Definiton
 */
definition(
    name: "Use Buttons As PIN Input",
    namespace: "CosmicPuppy",
    author: "Terry Gauchat",
    description: "Assign a multi-button controller (e.g., Aeon Labs Minimote) to be a security 'PIN code' input pad, " +
        "which triggers a switch, lock, mode, or Hello Home action.",
    category: "Safety & Security",
    iconUrl:     "http://cosmicpuppy.com/SmartThingsAssets/ButtonsAsPIN_icon_ComboLock.jpg",
    iconX2Url:   "http://cosmicpuppy.com/SmartThingsAssets/ButtonsAsPIN_icon_ComboLock.jpg",
    iconX3Url:   "http://cosmicpuppy.com/SmartThingsAssets/ButtonsAsPIN_icon_ComboLock.jpg",
) /* definition */


preferences {
    page(name: "pageSelectButtonDev")
    page(name: "pageSetPinSequence")
    page(name: "pageSelectActions")
} /* preferences */


def pageSelectButtonDev() {
    myTrace("Version: ${myVersion()}. Running preferences pages.")
    dynamicPage(name: "pageSelectButtonDev", title: "General Configuration",
            nextPage: "pageSetPinSequence", uninstall: true) {
        section(title: "About This App") {
            paragraph "Version ${myVersion()}"
                //"Version ${myVersion()}\nhttps://github.com/CosmicPuppy/SmartThings-ButtonsAsPIN"
            href title: "GitHub Link",
                 style: "external",
                 url: "https://github.com/CosmicPuppy/SmartThings-ButtonsAsPIN",
                 description: "https://github.com/CosmicPuppy/SmartThings-ButtonsAsPIN"
                 required: false
        }
        section {
            input name: "buttonDevice", type: "capability.button", title: "Button Device:", multiple: false, required: true
        }
        section {
            input name: "pinLength", type: "enum", title: "PIN length (1 to 9 digits):", multiple: false,
                required: true, options: "1" .. "9", defaultValue: "4";
        }
        section(mobileOnly:true, hideable: true, hidden: true) {
            //icon title: "Custom Icon (optional)", required: false
            label title: "Assign a name to this SmartApp instance?", required: false
            mode title: "Activate for specific mode(s)?", required: false
        }
    }
} /* pageSelectButtonDev() */


/**
 * TODO: In progress... Handling a variable number of buttons.
 *       Was hardcoded to 4 buttons (based on Aeotec Minimote), so that will be default;
 *          but "ZWN-SC7 Enerwave 7 Button Scene Controller" (@mattjfrank) has 7 buttons,
 *          and that Device Type currently has a "numButtons" attribute; so we read it.
 * NB: Not sure this SmartApp can handle more than 9 buttons, so it is max for now (and 2 is min!).
 */
def pageSetPinSequence() {
    //Experimental, not required, numButtons can (should) be set in configure() of buttonDevice: buttonDevice.getNumButtons()
    /* TODO: Need to do error avoid or more handling on next statement, if attribute does not exist? */
    state.buttonDeviceName = "buttonDevice"
    int numButtons = 0
    def numButtonsString = ""

    myDebug("Attemping buttonDevice.currentValue(\"numButtons\"). Value on next line...")
    try {
        state.buttonDeviceName = buttonDevice.displayName
        numButtonsString = buttonDevice.currentValue("numButtons")
    } catch (all) {
        myDebug("Caught Exception from buttonDevice... No worries!")
        state.buttonDeviceName = "buttonDevice.(undefined name)"
        numButtonsString = ""
    }
    try {
        numButtons = Integer.parseInt(numButtonsString)
    } catch (all) {
        myDebug("Caught Exception from parseInt... No worries!")
        numButtons = 0
    }
    myDebug(numButtons)

    if (numButtonsString == null || numButtons <= 1) { // Perhaps some buttonDevice returns 0 or 1 buttons which would be useless.
        numButtons = 4
        myTrace("numButtons of '${state.buttonDeviceName}' not valid. Using default: $numButtons.")
    } else if (numButtons > 9)  {
        myTrace("numButtons of '${state.buttonDeviceName}' is too big: $numButtons. Using max setting: 9.")
        numButtons = 9
    } else {
        myTrace("numButtons of '${state.buttonDeviceName}' is: $numButtons");
    }
    dynamicPage(name: "pageSetPinSequence", title: "Set PIN (Security Code)", nextPage: "pageSelectActions",
        install: false, uninstall: true) {
        section("PIN Code Buttons in Desired Sequence Order") {
            L:{ for( i in 1 .. pinLength.toInteger() ) {
                    input name: "comb_${i}", type: "enum", title: "Sequence $i:", mulitple: false, required: true,
                        options: 1 .. numButtons;
                }
            }
            href "pageSelectButtonDev", title:"Go Back", description:"Tap to go back."
        }
    }
} /* pageSetPinSequence() */


def pageSelectActions() {
    def pageProperties = [
        name: "pageSelectActions",
        title: "Confirm PIN & Select Action(s)",
        install: true,
        uninstall: true
    ]

    state.pinSeqList = []
    state.pinLength = pinLength.toInteger()
    for( i in 1 .. state.pinLength ) {
        state.pinSeqList << settings."comb_${i}"
    }
    myDebug("pinLength is $state.pinLength; pinSeqList is $state.pinSeqList")

    return dynamicPage(pageProperties) {
        section() {
            paragraph "PIN Code set to: " + "$state.pinSeqList"
            href "pageSetPinSequence", title:"Go Back", description:"Tap to change PIN Code sequence."
        }
        section("Actions, Mode, Locks, Switches") {
            def phrases = location.helloHome?.getPhrases()*.label
            if (phrases) {
                myDebug("Phrase list found: ${phrases}")
                /* NB: Customary to not allow multiple phrases. Complications due to sequencing, etc. */
                input "phrase", "enum", title: "Trigger Hello Home Action", required: false, options: phrases
            }
            input "mode", "mode", title: "Set Mode", required: false
            input "locks", "capability.lock", title: "Toggle Locks", multiple: true, required: false
            input "switches", "capability.switch", title: "Toggle Lights & Switches", multiple: true, required: false
        }
    }
} /* pageSelectActions() */


def installed() {
    myTrace("Version: ${myVersion()}; Installed.")
    myDebug("Installed; settings: ${settings}") // settings includes the PIN, so we should avoid logging except for Debug.

    initialize()
}

def updated() {
    myTrace("Version: ${myVersion()}; Updated.")
    myDebug("Updated; settings: ${settings}") // settings includes the PIN, so we should avoid logging except for Debug.

    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(buttonDevice, "button", buttonEvent)
    state.inputDigitsList = []

    myDebug("Initialized - state: ${state}")
}


/**
 * Watch for correct matching PIN input by rolling scan of last "pinLength" presses.
 *
 * TODO: Keep a count of the number of unsucessful sequences so that alarm or other alert action could be called.
 *       Such an alert could also (virtually) "disable" the buttons for a period of time.
 *
 * NB: It would be more secure to require a Start and/or End indicator, but complicates user interface.
 *     One possible improvement is to have a "timeout" on the input buffer.
 *
 * NB: On the Aeon Minimote, pressing the same key twice is "sort of" filtered unless
 *       you wait for the red LED confirmation response.
 *     The two presses are probably detectable by analyzing the buttonDevice.events log, but the stream seems inconsistent.
 *     Therefore the User "MUST" wait for confirmation after each keypress else input digits may be lost (undetected).
 *     NOT waiting will often still work, though, if there are no double presses (duplicate sequential digits in the PIN).
 */
def buttonEvent(evt){
    def allOK = true;
    if(allOK) {
        def value = evt.value
        def slurper = new JsonSlurper()
        def dataMap = slurper.parseText(evt.data)
        def buttonNumber = dataMap.buttonNumber
        myDebug("buttonEvent Device: [$buttonDevice.name], Name: [$evt.name], Value: [$evt.value], Data: [$evt.data], ButtonNumber: [$dataMap.buttonNumber]")

        /**
            Aeon Minimote returns "pushed" or "held". Enerwave 7 (by @mattjfrank) returns "button #"
            Yes ... the Device Handlers *should* be reconciled someday:
              I think "held" is nonsense and Minimote should just use buttonNumber 5 to 8 for helds.
        */
        if(value == "pushed" || value[0 .. 5] == "button") {
            state.inputDigitsList << buttonNumber.toString()
            if(state.inputDigitsList.size > state.pinLength) {
                state.inputDigitsList.remove(state.inputDigitsList.size - state.pinLength - 1)
            }
            myDebug("Current inputDigitsList: $state.inputDigitsList")
            if(state.inputDigitsList.equals(state.pinSeqList)) {
                myDebug("PIN Match Detected; found [$state.pinSeqList]. Clearing input digits buffer.")
                myTrace("PIN Match Detected. Clearing input digits buffer.")
                state.inputDigitsList.clear();
                executeHandlers()
            } else {
                myDebug("No PIN match yet: inputDigitsList is $inputDigitsList; looking for $state.pinSeqList")
                myTrace("No PIN match yet.")
            }
        }

    /**
     * TODO: (Experimental code for reference):
     *   If the above code misses button presses that occur too quickly,
     *   considering scanning back through the event log.
     * The behavior if this is a little confusing though: Repeated keys show up in the recentEvents().
     * Could we limit data entry to 10 or 20 seconds and limit the backscan to the length of the PIN?
     * The only time multiple event backscan seems to apply is for multi-presses of the same key. But then this is essential.
     * Yet eventsSince seems to only be reporting NEW events. Weird. Not critical for this App to work ok, though.
     */
    //	def recentEvents = buttonDevice.eventsSince(new Date(now() - 10000),
    //    	[max:pinLength.toInteger()]).findAll{it.value == evt.value && it.data == evt.data}
    //	myDebug("PIN Found ${recentEvents.size()?:0} events in past 10 seconds"
    //  recentEvents.eachWithIndex { it, i -> myDebug("PIN [$i] Value:$it.value Data:$it.data" }
    }
} /* buttonEvent() */


/**
 * Event handlers.
 * Most code copied from "Button Controller" by SmartThings, + slight modifications.
 */
private def executeHandlers() {
    myTrace("executeHandlers: phrase exec, mode set, locks/switches toggles.")

    def phrase = findPreferenceSetting('phrase')
    if (phrase != null)	{
        myTrace("helloHome.execute: \"${phrase}\"")
        location.helloHome.execute(phrase)
    }

    def mode = findPreferenceSetting('mode')
    if (mode != null) changeMode(mode)

    def switches = findPreferenceSetting('switches')
    myDebug("switches are ${switches}")
    if (switches != null) toggle(switches,'switch')

    def locks = findPreferenceSetting('locks')
    myDebug("locks are ${locks}")
    if (locks != null) toggle(locks,'lock')
} /* executeHandlers() */


private def findPreferenceSetting(preferenceName) {
    def pref = settings[preferenceName]
    if(pref != null) {
        myDebug("Found Pref Setting: $pref for $preferenceName")
    }
    return pref
}


/**
 * NB: This function only works properly if "devices" list passed are all of same capability.
 *     Shouldn't be a problem, because devices is from a preference setting list filtered by capability.
 * NB: "Toggle" is a misnomer as it sets all switches to off ANY are on (rather than check and toggle each one's state).
 *       (Similarly, all locks are unlocked if any are found locked.)
 *     Is toggling the most intuitive behavior since the resulting set of states is possibly uncertain to the user?
 *     A possible "improvement"?: Require two different PINs, for lock vs for unlock (could just be the last digit: 1 vs 2).
 *       This would also better accomodate two distinct Hello Home phrases (activating security vs deactiviting security).
 *       But: A toggle type action for Hello Home phrase is appropriate if reading and using mode or state is reliable.
 *     The current "Failsafe default" sections are a questionable design decision; Is there a better choice?
 */
private def toggle(devices,capabilityType) {
    if (capabilityType == 'switch') {
        myDebug("toggle switch Values: $devices = ${devices*.currentValue('switch')}")
        if (devices*.currentValue('switch').contains('on')) {
            myTrace("Set devices.off: ${devices}")
            devices.off()
        }
        else if (devices*.currentValue('switch').contains('off')) {
            myTrace("Set devices.on: ${devices}")
            devices.on()
        }
        else {
            myTrace("Set devices.on (Failsafe default action attempt.): ${devices}")
            devices.on()
        }
    }
    else if (capabilityType == 'lock') {
        myDebug("toggle lock Values: $devices = ${devices*.currentValue('lock')}")
        if (devices*.currentValue('lock').contains('locked')) {
            myTrace("Set devices.unlock: ${devices}")
            devices.unlock()
        }
        else if (devices*.currentValue('lock').contains('unlocked')) {
            myTrace("Set devices.lock: ${devices}")
            devices.lock()
        }
        else {
            myTrace("Set devices.unlock (Failsafe default action attempt.): ${devices}")
            devices.unlock()
        }
    }
} /* toggle() */


private def changeMode(mode) {
    myDebug("changeMode: $mode, location.mode = $location.mode, location.modes = $location.modes")

    if (location.mode != mode && location.modes?.find { it.name == mode }) {
        myTrace("setLocationMode: ${mode}")
        setLocationMode(mode)
    } else {
        if (location.mode == mode) {
            myTrace("Mode unchanged. Already set to: ${mode}")
        } else {
            myTrace("Mode unchanged. Unable to find defined mode named: ${mode}")
        }
    }
}


/* =========== */
/* End of File */
/* =========== */