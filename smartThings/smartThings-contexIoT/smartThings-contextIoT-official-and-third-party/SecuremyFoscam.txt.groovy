/**
 *  Secure My Foscam
 *
 *  Author: brian@bevey.org
 *  Date: 9/25/13
 *
 *  Simply turns on the alarm for any Foscam custom device.  This is intended
 *  to be used with the Foscam custom device type with a camera set up for FTP
 *  or email uploading based on motion detection.
 */

preferences {
  section("Change to this mode to...") {
    input "newMode", "enum", metadata:[values:["Alarm On", "Alarm Off"]]
  }

  section("Move to this preset...") {
    input "newPreset", "enum", metadata:[values:["1", "2", "3"]], required: false
  }

  section("Change these Foscam modes...") {
    input "foscams", "capability.imageCapture", multiple: true
  }
}

def installed() {
  subscribe(location, changeMode)
  subscribe(app, changeMode)
}

def updated() {
  unsubscribe()
  subscribe(location, changeMode)
  subscribe(app, changeMode)
}

def changeMode(evt) {
  if(newPreset) {
    def preset = new Integer(newPreset)

    log.info("Preset: ${preset}")
    foscams?.preset(preset)
  }

  if(newMode == "Alarm On") {
    log.info("Alarm: on")

    foscams?.alarmOn()
  }

  else {
    log.info("Alarm: off")

    foscams?.alarmOff()
  }
}