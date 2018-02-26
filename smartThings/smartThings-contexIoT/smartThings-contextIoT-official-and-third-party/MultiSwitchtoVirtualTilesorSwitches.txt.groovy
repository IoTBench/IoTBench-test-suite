/**
 *  Multi Switch to Virtual Tiles or Switches
 *
 *  Copyright 2014 Cooper Lee
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
    name: "Multi Switch to Virtual Tiles or Switches",
    namespace: "ms_2_vt",
    author: "Cooper Lee",
    description: "Use this app to map multiple switches embedded in a device type to a variety of virtual tiles or real switches.",
    category: "My Apps",
    iconUrl: "http://www.scimonocesoftware.com/seefinance/images/icons_as_jpg/Switch.jpg",
    iconX2Url: "http://www.scimonocesoftware.com/seefinance/images/icons_as_jpg/Switch.jpg",
    iconX3Url: "http://www.scimonocesoftware.com/seefinance/images/icons_as_jpg/Switch.jpg")


preferences {
	page(name: "mainDevice", uninstall: true, install:false)
	page(name: "virtualDetails", uninstall: true, install:true)
}

def mainDevice() {
	dynamicPage(name: "mainDevice", title: "Setup virtual app and multi-switch device", nextPage: "virtualDetails", uninstall: true, install:false) {
        section {
            input "master", "capability.switch", multiple: false, required: true, title: "Choose the device with multiple switches"
//            input "num_sw", "number", multiple: false, required: true, title: "How many switches?"

            label title: "Assign a name for this virtual tile handler", required: false
            icon title: "Choose an icon for $app.label", required: false, defaultValue: "st.Lighting.light13-icn"
            paragraph: "Assign switches to virtual tiles or real switches on next page"
        }
	}
}

def virtualDetails() {
	dynamicPage(name: "virtualDetails", title: "How to map switches:", uninstall: true, install:true) {
        section {
            input "switch1", "capability.switch", multiple: true, required: false, title: "Switch Mapping 1"
            input "switch2", "capability.switch", multiple: true, required: false, title: "Switch Mapping 2"
            input "switch3", "capability.switch", multiple: true, required: false, title: "Switch Mapping 3"
            input "switch4", "capability.switch", multiple: true, required: false, title: "Switch Mapping 4"
            input "switch5", "capability.switch", multiple: true, required: false, title: "Switch Mapping 5"
            input "switch6", "capability.switch", multiple: true, required: false, title: "Switch Mapping 6"
            input "switch7", "capability.switch", multiple: true, required: false, title: "Switch Mapping 7"
            input "switch8", "capability.switch", multiple: true, required: false, title: "Switch Mapping 8"
            input "switch9", "capability.switch", multiple: true, required: false, title: "Switch Mapping 9"
            input "switch10", "capability.switch", multiple: true, required: false, title: "Switch Mapping 10"

        }
	}
}

def installed() {
    	subscribe(switch1, "switch", swch1)
		subscribe(switch2, "switch", swch2)
		subscribe(switch3, "switch", swch3)
		subscribe(switch4, "switch", swch4)
		subscribe(switch5, "switch", swch5)
		subscribe(switch6, "switch", swch6)
		subscribe(switch7, "switch", swch7)
		subscribe(switch8, "switch", swch8)
		subscribe(switch9, "switch", swch9)
		subscribe(switch10, "switch", swch10)
}

def updated() {
        unsubscribe()
    	subscribe(switch1, "switch", swch1)
		subscribe(switch2, "switch", swch2)
		subscribe(switch3, "switch", swch3)
		subscribe(switch4, "switch", swch4)
		subscribe(switch5, "switch", swch5)
		subscribe(switch6, "switch", swch6)
		subscribe(switch7, "switch", swch7)
		subscribe(switch8, "switch", swch8)
		subscribe(switch9, "switch", swch9)
		subscribe(switch10, "switch", swch10)
}

def swch1(evt) { master."${evt.value}1"() }
def swch2(evt) { master."${evt.value}2"() }
def swch3(evt) { master."${evt.value}3"() }
def swch4(evt) { master."${evt.value}4"() }
def swch5(evt) { master."${evt.value}5"() }
def swch6(evt) { master."${evt.value}6"() }
def swch7(evt) { master."${evt.value}7"() }
def swch8(evt) { master."${evt.value}8"() }
def swch9(evt) { master."${evt.value}9"() }
def swch10(evt) { master."${evt.value}10"() }

