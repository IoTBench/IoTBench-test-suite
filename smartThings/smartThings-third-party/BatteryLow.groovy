/**
 *  Low Battery Notification
 *
 */

definition(
    name: "Battery Low",
    namespace: "com.sudarkoff",
    author: "George Sudarkoff",
    description: "Notify when battery charge drops below the specified level.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
    section ("When battery change in these devices") {
        input "devices", "capability.battery", title:"Battery Operated Devices", multiple: true
    }
    section ("Drops below this level") {
        input "level", "number", title:"Battery Level (%)"
    }
    section ("Notify") {
        input "sendPushMessage", "bool", title: "Send a push notification?", required: false
        input "phone", "phone", title: "Send a Text Message?", required: false
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    if (level < 5 || level > 90) {
        sendPush("Battery level should be between 5 and 90 percent")
        return false
    }
    subscribe(devices, "battery", batteryHandler)

    state.lowBattNoticeSent = [:]
    updateBatteryStatus()
}

def batteryHandler(evt) {
    updateBatteryStatus()
}

private send(message) {
    if (phone) {
        sendSms(phone, message)
    }
    if (sendPushMessage) {
        sendPush(message)
    }
}

private updateBatteryStatus() {
    for (device in devices) {
        if (device.currentBattery < level) {
            if (!state.lowBattNoticeSent.containsKey(device.id)) {
                send("${device.displayName}'s battery is at ${device.currentBattery}%.")
            }
            state.lowBattNoticeSent[(device.id)] = true
        }
        else {
            if (state.lowBattNoticeSent.containsKey(device.id)) {
                state.lowBattNoticeSent.remove(device.id)
            }
        }
    }
}

