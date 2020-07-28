/**
 *  Copyright 2015 SmartThings
 *            2020 Spiros Papadimitriou
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
 *  Z-Wave RGBW Light
 *
 *  Author: SmartThings & Spiros Papadimitriou
 *  Date: 2015-7-12
 */

// XXX - Groovy/SmartThings will convert keys to String !!
private getINPUT_MODE_OPTIONS() { [
    1: "Normal / momentary",
    2: "Normal / toggle",
    3: "Normal / memory toggle",
    4: "Brightness / momentary",
    5: "Brightness / toggle",
    6: "Brightness / memory toggle",
    7: "Rainbow / momentary",
    8: "Scene / momentary",
    9: "Scene / toggle",
    10: "Scene / memory toggle"
] }

private getAUTO_SCENE_MODE_OPTIONS() { [
    0: "Off",
    1: "Ocean",
    2: "Lightning",
    3: "Rainbow",
    4: "Snow",
    5: "Sun",
    6: "Dancing"
] }

// private getDIMMER_MODE_OPTIONS() { [
//     0: "Disabled",
//     1: "Enabled / momentary",
//     2: "Enabled / toggle",
//     3: "Enabled / memory toggle"
//  ] }

metadata {
    definition (name: "Monoprice RGBW Controller", namespace: "spapadim", author: "Spiros Papadimitriou", ocfDeviceType: "oic.d.light", mnmn: "SmartThings", vid: "monoprice-rgbw") {
        capability "Switch Level"
        capability "Color Control"
        capability "Switch"
        capability "Refresh"
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"
        capability "Configuration"

        // Manufacturer and model-specific fingerprints.
        fingerprint mfr: "0068", prod: "0003", model: "00B", deviceJoinName: "Monoprice RGBW", mnmn:"SmartThings", vid: "monoprice-rgbw"
    }

    simulator {
    }

    tiles(scale: 2) {
        multiAttributeTile(name:"switch", type: "lighting", width: 1, height: 1, canChangeIcon: true) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("on", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
                attributeState("off", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
                attributeState("turningOn", label:'${name}', action:"switch.off", icon:"st.lights.philips.hue-single", backgroundColor:"#00a0dc", nextState:"turningOff")
                attributeState("turningOff", label:'${name}', action:"switch.on", icon:"st.lights.philips.hue-single", backgroundColor:"#ffffff", nextState:"turningOn")
            }

            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }

            tileAttribute ("device.color", key: "COLOR_CONTROL") {
                attributeState "color", action:"setColor"
            }
        }

        main(["switch"])
        details(["switch", "levelSliderControl"])
   }

 
    preferences {
        input "modeIn1", "enum", title: "Input 1 mode", options: INPUT_MODE_OPTIONS, required: false
        input "modeIn2", "enum", title: "Input 2 mode", options: INPUT_MODE_OPTIONS, required: false
        input "modeIn3", "enum", title: "Input 3 mode", options: INPUT_MODE_OPTIONS, required: false
        input "modeIn4", "enum", title: "Input 4 mode", options: INPUT_MODE_OPTIONS, required: false
        input "modeAutoScene", "enum", title: "Auto scene mode", options: AUTO_SCENE_MODE_OPTIONS, required: false
        input "durationAutoScene", "number", title: "Auto scene duration (sec)", range: "1..7620", required: false
        input "memorizePowerStatus", "bool", title: "Memorize power status", defaultValue: true, required: false
        input "maxDimValue", "decimal", title: "Max dimming value", range: "2..99", required: false
        input "minDimValue", "decimal", title: "Min dimming value", range: "1..98", required: false
        input "dimmingTime", "decimal", title: "Soft on/off time (sec)", range: "0.5..2.5", required: false
        input "keyDimmingTime", "number", title: "Keypress dimming time (sec)", range: "1..127", required: false
//       input "modeDimmer", "enum", title: "Four dimmer mode", options: DIMMER_MODE_OPTIONS, required: false, 
//           displayDuringSetup: true, description: "IMPORTANT - Can ONLY be set during device inclusion!"
    }
}

private getRED() { "red" }
private getGREEN() { "green" }
private getBLUE() { "blue" }
private getWARM_WHITE() { "warmWhite" }
private getRGB_NAMES() { [RED, GREEN, BLUE] }
private getWHITE_NAMES() { [WARM_WHITE] }
private getCOLOR_NAMES() { RGB_NAMES + WHITE_NAMES }
private getWHITE_SATURATION_THRESHOLD() { 1 }   // Switch to white LEDs, if saturation below this threshold
private getSWITCH_VALUE_ON() { 0xFF }  // Per Z-Wave, this multilevel switch value commands state-transition to on.  This will restore the most-recent non-zero value cached in the device.
private getSWITCH_VALUE_OFF() { 0 }  // Per Z-Wave, this multilevel switch value commands state-transition to off.  This will not clobber the most-recent non-zero value cached in the device.
private BOUND(x, floor, ceiling) { Math.max(Math.min(x, ceiling), floor) }

private getCONFIGURATION_INFO() {
    // XXX - Seems Groovy doesn't allow reference to function or bound method (a-la Java)... ugh!
    def atoi = { s -> s as Integer }
    def str = { i -> i as String }

    // Map of paramNum : info pairs (zwave configuration parameter number to associated information)
    [
    1: [prefName: "modeIn1", paramSize: 1, prefToParam: atoi, paramToPref: str],
    2: [prefName: "modeIn2", paramSize: 1, prefToParam: atoi, paramToPref: str],
    3: [prefName: "modeIn3", paramSize: 1, prefToParam: atoi, paramToPref: str],
    4: [prefName: "modeIn4", paramSize: 1, prefToParam: atoi, paramToPref: str],
    5: [prefName: "modeAutoScene", paramSize: 1, prefToParam: atoi, paramToPref: str],
    6: [prefName: "durationAutoScene", paramSize: 1, prefToParam: { pref -> pref <= 127 ? pref : 1000 + Math.round(pref/60.0) as int }, paramToPref: { param -> param <= 127 ? param : 60*(param - 1000) }],
    7: [prefName: "memorizePowerStatus", paramSize: 1, prefToParam: { pref -> pref ? 1 : 0 }, paramToPref: { param -> param == 1 }],
    10: [prefName: "maxDimValue", paramSize: 1],
    11: [prefName: "minDimValue", paramSize: 1],
    12: [prefName: "dimmingTime", paramSize: 1, prefToParam: { pref -> Math.round(pref*10) as int }, paramToPref: { param -> param / 10.0 }],
    13: [prefName: "keyDimmingTime", paramSize: 1],

    // No plans to support this setting; when enabled, the device appears as four separate on/off switch devices
    //   Therefore, it needs to be excluded and re-included into the ZWave mesh, and this device handler
    //   will probably not work correctly (never tested, don't need it, so no plan to do ever test this either)
    // 14: prefName: "modeDimmer", paramSize: 1, prefToParam: atoi, paramToPref: str],

    // TODO - Qubino docs include a couple of additional parameters, e.g., for auto-on / auto-off after a timeout
    //   Perhaps we could include them here, if this version of the device also supports them 
    //   (Monoprice docs dont list these configuration parameters)
    ] 
}
private DEVSTATE_FROM_PREFNAME(prefName) { "dev" + prefName.capitalize() }  // See installed() below for explanation

def updated() {
    log.debug "updated()"
    // TODO - Ensure that queryAllConfigurations() responses were all received?
    //   Maybe kludge to first queryAllConfigurations(), and then use runIn 
    //   to emitConfigurations() after some magic hardcoded delay 
    //   (other drivers seem to employ such hacks)
    response(emitConfiguration() + [ "delay 1000" ] + refresh())
}

def installed() {
    // XXX - For each *possible* settings.XXX value, there should be a corresponding state.devXXX value
    //   (the aux function DEVSTATE_FROM_PREFNAME should be used).
    //   I originally grouped all these in a nested map (i.e., state.devSettings.XXX), but because state
    //   is persisted at the top-level (with maps serialized as JSON), this would always cause race conditions
    //   since ConfigurationReport events are often handled by different SmartThings executions, hence only the
    //   keys of state.devSettings that were written by the last execution would survive.
    //   The use of atomicState rather than state is even hairier than un-nesting these keys.

    // Clear all state.devXXX values
    CONFIGURATION_INFO.each { pNum, pInf ->
        state.remove(DEVSTATE_FROM_PREFNAME(pInf.prefName))
    }

    log.debug "installed()"
    sendEvent(name: "checkInterval", value: 1860, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
    sendEvent(name: "level", value: 100, unit: "%", displayed: false)
    sendEvent(name: "color", value: "#000000", displayed: false)
    sendEvent(name: "hue", value: 0, displayed: false)
    sendEvent(name: "saturation", value: 0, displayed: false)
}

def parse(description) {
    def result = null
    if (description.startsWith("Err 106")) {
        state.sec = 0
    } else if (description != "updated") {
        def cmd = zwave.parse(description)
        if (cmd) {
            result = zwaveEvent(cmd)
            log.debug("'$description' parsed to $result")
        } else {
            log.debug("Couldn't zwave.parse '$description'")
        }
    }
    result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
    dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchcolorv3.SwitchColorReport cmd) {
    log.debug "got SwitchColorReport: $cmd"
    def result = []
    if (state.staged != null) {
        // We use this as a callback from our color setter.
        // Emit our color update event with our staged state.
        state.staged.subMap("hue", "saturation", "color").each{ k, v -> 
            result << createEvent(name: k, value: v) 
        }
    }
    result
}

private queryAllConfigurations() {
    def cmds = []
    CONFIGURATION_INFO.each { paramNum, paramInfo ->
        cmds << zwave.configurationV2.configurationGet(parameterNumber: paramNum)
    }
    cmds
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
    log.debug "got ConfigurationReport: $cmd"

    // XXX - Groovy type system sucks.. apparently cmd.parameterNumber isn't an Integer,
    //   even though equality tests with integer literals work (maybe Perl-like semantics..?)
    //   However, without "as Integer", map key lookup doesnt work...
    def paramNum = cmd.parameterNumber as Integer
    def paramVal = cmd.scaledConfigurationValue
    def paramInfo = CONFIGURATION_INFO[paramNum]

    if (!paramInfo) {
        log.debug("Received unknown configuration parameter number $paramNum")
        return
    }

    def prefVal = paramInfo.paramToPref ? paramInfo.paramToPref(paramVal) : paramVal
    
    state[DEVSTATE_FROM_PREFNAME(paramInfo.prefName)] = prefVal

    log.debug "state.dev* is ${state.findAll {k, v -> k.startsWith('dev')} }"
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
    def stateValue = (cmd.value ? "on" : "off")
    def result = [createEvent(name: "switch", value: stateValue, descriptionText: "$device.displayName was turned $stateValue")]
    if (cmd.value) {
        result << createEvent(name: "level", value: cmd.value == 99 ? 100 : cmd.value , unit: "%")
    }
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    def encapsulatedCommand = cmd.encapsulatedCommand()
    if (encapsulatedCommand) {
        state.sec = 1
        def result = zwaveEvent(encapsulatedCommand)
        result = result.collect {
            if (it instanceof physicalgraph.device.HubAction && !it.toString().startsWith("9881")) {
                response(cmd.CMD + "00" + it.toString())
            } else {
                it
            }
        }
        result
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    def linkText = device.label ?: device.name
    [linkText: linkText, descriptionText: "$linkText: $cmd", displayed: false]
}

private emitConfiguration() {
    def cfgSetCmds = []
    def cfgGetCmds = []
    CONFIGURATION_INFO.each { paramNum, paramInfo -> 
        def prefName = paramInfo.prefName
        def devStatePrefName = DEVSTATE_FROM_PREFNAME(prefName)
        def prefVal = settings[prefName]
        if (prefVal != null && prefVal != state[devStatePrefName]) {
            def paramVal = paramInfo.prefToParam ? paramInfo.prefToParam(prefVal) : prefVal
            cfgSetCmds << zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: paramInfo.paramSize, scaledConfigurationValue: paramVal)
            cfgGetCmds << zwave.configurationV2.configurationGet(parameterNumber: paramNum)
        }
    }

    def rv = commands(cfgSetCmds + cfgGetCmds, 500)
    log.debug "emitConfiguration() -> $rv"
    rv
}

private emitMultiLevelSet(level, duration=1) {
    log.debug "setLevel($level, $duration)"
    duration = duration < 128 ? duration : 127 + Math.round(duration / 60) // See Z-Wave duration encodinbg
    duration = Math.min(duration, 0xFE) // 0xFF is a special code for factory default; bound to 0xFE
    def tcallback = Math.min(duration * 1000 + 2500, 12000) // how long should we wait to read back?  we can't wait forever
    commands([
        zwave.switchMultilevelV3.switchMultilevelSet(value: level, dimmingDuration: duration),
        zwave.switchMultilevelV3.switchMultilevelGet(),
    ], tcallback)
}

def on() {
    emitMultiLevelSet(SWITCH_VALUE_ON)
}

def off() {
    emitMultiLevelSet(SWITCH_VALUE_OFF)
}

def setLevel(level, duration=1) {
    level = BOUND(level, 1, 99) // See Z-Wave level encoding
    emitMultiLevelSet(level, duration)
}

def refresh() {
    log.debug "refresh()"
    commands([zwave.switchMultilevelV3.switchMultilevelGet()] + queryAllColors())
}

def ping() {
    log.debug "ping()"
    refresh()
}

def configure() {
    // log.debug "configure()"
    def rv = commands(queryAllConfigurations(), 500)
    log.debug "configure() -> $rv"
    rv
}

def setSaturation(percent) {
    log.debug "setSaturation($percent)"
    setColor(saturation: percent)
}

def setHue(value) {
    log.debug "setHue($value)"
    setColor(hue: value)
}

def setColor(value) {
    log.debug "setColor($value)"
    def rgb
    def hsv
    if (state.staged == null) {
        state.staged = [:]
    }
    if (value.hex) {
        state.staged << [color: value.hex]  // stage ST RGB color attribute
        hsv = colorUtil.hexToHsv(value.hex)  // convert to HSV
        state.staged << [hue: hsv[0], saturation: hsv[1]] // stage ST hue and saturation attributes
        rgb = value.hex.findAll(/[0-9a-fA-F]{2}/).collect { Integer.parseInt(it, 16) } // separate RGB elements for zwave setter
    } else {
        state.staged << value.subMap("hue", "saturation") // stage ST hue and saturation attributes
        def hex = colorUtil.hsvToHex(Math.round(value.hue) as int, Math.round(value.saturation) as int) // convert to hex
        state.staged << [color: hex]  // stage ST RGB color attribute
        rgb = colorUtil.hexToRgb(hex)  // separate RGB elements for zwave setter
        hsv = colorUtil.hexToHsv(hex)  // Not quite sure if colorUtil uses current level/hue/saturation values.. so, playing it safe
    }
    log.debug "RGB is $rgb / HSV is $hsv"
    def cmds
    if (hsv[1] < WHITE_SATURATION_THRESHOLD) {
        def currentLevel = device.currentValue("level")
        // XXX - Not sure if it's possible for current level to be undefined, but.. playing it safe.
        currentLevel = currentLevel != null ? currentLevel as Integer : hsv[2]
        cmds = [
            // XXX - Setting RGB values does not affect level (as required by ZWave standard), but setting
            //   W value does also change level (to the same value).  Not sure what ZWave standard requires (and,
            //   hence, whether the controller violates that or not), but... it is what it is, empirically.
            //zwave.switchColorV3.switchColorSet(red: 0, green: 0, blue: 0, warmWhite: 255*hsv[2]/100),
            zwave.switchColorV3.switchColorSet(red: 0, green: 0, blue: 0, warmWhite: 255*currentLevel/100),
            zwave.switchColorV3.switchColorGet(colorComponent: WHITE_NAMES[0]), // event-publish callback is on any of the RGB responses, so only need to GET one of these
        ]
    } else {
        cmds = [
            zwave.switchColorV3.switchColorSet(red: rgb[0], green: rgb[1], blue: rgb[2], warmWhite: 0),
            zwave.switchColorV3.switchColorGet(colorComponent: RGB_NAMES[0]), // event-publish callback is on any of the RGB responses, so only need to GET one of these
        ]
    }
    commands(cmds, 3500)
}

private queryAllColors() {
    COLOR_NAMES.collect { zwave.switchColorV3.switchColorGet(colorComponent: it) }
}

private secEncap(physicalgraph.zwave.Command cmd) {
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private crcEncap(physicalgraph.zwave.Command cmd) {
    zwave.crc16EncapV1.crc16Encap().encapsulate(cmd).format()
}

private command(physicalgraph.zwave.Command cmd) {
    if (zwaveInfo.zw.contains("s") || state.sec == 1) {
        secEncap(cmd)
    } else if (zwaveInfo?.cc?.contains("56")){
        crcEncap(cmd)
    } else {
        cmd.format()
    }
}

private commands(commands, delay=200) {
    delayBetween(commands.collect{ command(it) }, delay)
}
