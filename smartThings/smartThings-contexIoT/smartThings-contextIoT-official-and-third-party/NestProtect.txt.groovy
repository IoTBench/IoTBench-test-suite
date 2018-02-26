/**
 * Nest Protect
 *
 * Author: nick@nickhbailey.com
 * Date: 01.03.14
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type (https://graph.api.smartthings.com/ide/devices)
 * Name: Nest Protect
 * Author: nick@nickhbailey.com
 * Capabilities:
 * Battery
 * Carbon Monoxide Detector
 * Polling
 * Smoke Detector
 *
 * 2) Create a new device (https://graph.api.smartthings.com/device/list)
 * Name: Your Choice
 * Device Network Id: Your Choice
 * Type: Nest Protect (should be the last option)
 * Location: Choose the correct location
 * Hub/Group: Leave blank
 *
 * 3) Update device preferences
 * Click on the new device to see the details.
 * Click the edit button next to Preferences
 * Fill in your Nest login information.
 * To find your MAC Address, login to http://home.nest.com. Click the Protect
 * icon. Open the right-hand side bar, open Nest Protect Settings, choose the Nest Protect
 * you are adding, open Technical Info, copy the value next to 802.15.4 MAC.
 *
 *
 * Copyright (C) 2014 Nick Bailey <nick@nickhbailey.com>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Overall inspiration and Authentication methods originally developed by Brian Steere
 * as part of the Nest thermostat device type: https://gist.github.com/Dianoga/6055918
 * and are subject to the following:
 *
 * Copyright (C) 2013 Brian Steere <dianoga7@3dgo.net>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
 
preferences {
    input("username", "text", title: "Username", description: "Your Nest username (usually an email address)")
    input("password", "password", title: "Password", description: "Your Nest password")
    input("mac", "text", title: "MAC Address", description: "The MAC Address of your Nest Protect")
}
 
 // for the UI
metadata {
    simulator {
        // TODO: define status and reply messages here
    }
 
    tiles {

        valueTile("smoke", "device.smoke", width: 2, height: 2){
         //state ("clear", label: "OK", unit: "Smoke", backgroundColor:"#44B621")
//state ("detected", icon:"st.alarm.smoke.smoke", label:"SMOKE", backgroundColor:"#e86d13", unit: "Smoke")
//state ("tested", label:"TEST", backgroundColor:"#003CEC", unit: "Smoke")
            state "smoke", label: 'Smoke ${currentValue}', unit:"smoke",
             backgroundColors: [
                    [value: "clear", color: "#44B621"],
                    [value: "detected", color: "#e86d13"],
                    [value: "tested", color: "#003CEC"]
                ]
}
valueTile("carbonMonoxide", "device.carbonMonoxide"){
         //state("clear", backgroundColor:"#44B621", unit: "CO")
//state("detected", label:"CO", icon:"st.alarm.smoke.smoke", backgroundColor:"#e86d13", unit: "CO")
//state("tested", label:"TEST", icon:"st.alarm.smoke.test", backgroundColor:"#003CEC", unit: "CO")
            state("carbonMonoxide", label: 'CO ${currentValue}', unit:"CO", backgroundColors: [
                    [value: "clear", color: "#44B621"],
                    [value: "detected", color: "#e86d13"],
                    [value: "tested", color: "#003CEC"]
                ]
            )
}
        valueTile("battery", "device.battery"){
         //state("OK", backgroundColor:"#44B621", unit: "Batt")
//state("Low", label:"Low", backgroundColor:"#e86d13")
            state("battery", label: 'Battery ${currentValue}', unit:"battery", backgroundColors: [
                    [value: "OK", color: "#44B621"],
                    [value: "Low", color: "#e86d13"]
                ]
            )
}
        standardTile("refresh", "device.smoke", inactiveLabel: false, decoration: "flat") {
            state "default", action:"polling.poll", icon:"st.secondary.refresh"
        }
main "smoke"
        details(["smoke", "carbonMonoxide", "battery", "refresh"])
}
}
 
// parse events into attributes
def parse(String description) {
    
}

 
 
def auto() {
    log.debug "Executing 'auto'"
}
 
def poll() {
    log.debug "Executing 'poll'"
    api('status', []) {
        data.topaz = it.data.topaz.getAt(settings.mac.toUpperCase())

        data.topaz.smoke_status = data.topaz.smoke_status == 0? "clear" : "detected"
        data.topaz.co_status = data.topaz.co_status == 0? "clear" : "detected"
        data.topaz.battery_health_state = data.topaz.battery_health_state == 0 ? "OK" : "Low"
             
        sendEvent(name: 'smoke', value: data.topaz.smoke_status)
        sendEvent(name: 'carbonMonoxide', value: data.topaz.co_status)
        sendEvent(name: 'battery', value: data.topaz.battery_health_state )
        log.debug settings.mac
        log.debug data.topaz.wifi_mac_address
        log.debug data.topaz.smoke_status
        log.debug data.topaz.co_status
        log.debug data.topaz.battery_health_state
    }
}
 
def api(method, args = [], success = {}) {
    if(!isLoggedIn()) {
        log.debug "Need to login"
        login(method, args, success)
        return
    }
 
    def methods = [
        'status': [uri: "/v2/mobile/${data.auth.user}", type: 'get']
    ]
    
    def request = methods.getAt(method)
    
    log.debug "Logged in"
    doRequest(request.uri, args, request.type, success)
}
 
// Need to be logged in before this is called. So don't call this. Call api.
def doRequest(uri, args, type, success) {
    log.debug "Calling $type : $uri : $args"
    
    if(uri.charAt(0) == '/') {
        uri = "${data.auth.urls.transport_url}${uri}"
    }
    
    def params = [
        uri: uri,
        headers: [
            'X-nl-protocol-version': 1,
            'X-nl-user-id': data.auth.userid,
            'Authorization': "Basic ${data.auth.access_token}"
        ],
        body: args
    ]
    
    if(type == 'post') {
        httpPostJson(params, success)
    } else if (type == 'get') {
        httpGet(params, success)
    }
}
 
def login(method = null, args = [], success = {}) {
    def params = [
        uri: 'https://home.nest.com/user/login',
        body: [username: settings.username, password: settings.password]
    ]
    
    httpPost(params) {response ->
        data.auth = response.data
        data.auth.expires_in = Date.parse('EEE, dd-MMM-yyyy HH:mm:ss z', response.data.expires_in).getTime()
        log.debug data.auth
        
        api(method, args, success)
    }
}
 
def isLoggedIn() {
    if(!data.auth) {
        log.debug "No data.auth"
        return false
    }
    
    def now = new Date().getTime();
    return data.auth.expires_in > now
}