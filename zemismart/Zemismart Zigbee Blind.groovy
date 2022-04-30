/**
 *  Copyright 2021, 2022
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
 * This DTH is coded based on iquix's tuya-window-shade DTH and these other files:
 * https://github.com/iquix/Smartthings/blob/master/devicetypes/iquix/tuya-window-shade.src/tuya-window-shade.groovy
 * https://raw.githubusercontent.com/shin4299/XiaomiSJ/master/devicetypes/shinjjang/zemismart-zigbee-blind.src/zemismart-zigbee-blind.groovy
 * https://templates.blakadder.com/zemismart_YH002.html
 * https://github.com/zigpy/zha-device-handlers/blob/f3302257fbb57f9f9f99ecbdffdd2e7862cc1fd7/zhaquirks/tuya/__init__.py#L846
 *
 * VERSION HISTORY
 *
 *                                TODO: optimize debug/trace logs!
 * 3.1.2 (2022-04-30) [kkossev]   - added AdvancedOptions'
 * 3.1.1 (2022-04-26) [kkossev]   - added more TS0601 fingerprints; atomicState bug fix; added invertPosition option; added 'SwitchLevel' capability (Alexa); added POSITION_UPDATE_TIMEOUT timer
 * 3.1.0 (2022-04-07) [kkossev]   - added new devices fingerprints; blind position reporting; Tuya time synchronization;  
 * 3.0.0 (2021-06-18) [Amos Yuen] - Support new window shade command startPositionChange()
 *		- Rename stop() to stopPositionChange()
 *		- Handle ack and set time zigbee messages
 * 2.3.0 (2021-06-09) [Amos Yuen] - Add presence attribute to indicate whether device is responsive
 * 2.2.0 (2021-06-06) [Amos Yuen] - Add commands for stepping
 *       - Fix push command not sending zigbee commands
 * 2.1.0 (2021-05-01) [Amos Yuen] - Add pushable button capability
 *		- Add configurable close and open position thresholds
 * 2.0.0 (2021-03-09) [Amos Yuen] - Change tilt mode open()/close() commands to use set position
 *			to open/close all the way.
 *		- Rename pause() to stop()
 *		- Remove superfluous setDirection() setMode() functions
 * 1.0.0 (2021-03-09) [Amos Yuen] - Initial Commit
 */

import groovy.json.JsonOutput
import groovy.transform.Field
import hubitat.zigbee.zcl.DataType
import hubitat.helper.HexUtils

private def textVersion() {
	return "3.1.2 - 2022-04-30 11:57 AM"
}

private def textCopyright() {
	return "Copyright ©2021\nAmos Yuen, iquix, ShinJjang"
}

metadata {
	definition(name: "ZemiSmart Zigbee Blind", namespace: "amosyuen", author: "Amos Yuen",
			ocfDeviceType: "oic.d.blind", vid: "generic-shade") {
		capability "Actuator"
		capability "Configuration"
		capability "PresenceSensor"
		capability "PushableButton"
		capability "WindowShade"
        capability "SwitchLevel"      // level - NUMBER, unit:%; setLevel(level, duration); level required (NUMBER) - Level to set (0 to 100); duration optional (NUMBER) - Transition duration in seconds
        //capability "ChangeLevel"    // startLevelChange(direction); direction required (ENUM) - Direction for level change request; stopLevelChange()

		attribute "speed", "integer"

		command "push", [[
			name: "button number*",
			type: "NUMBER",
			description: "1: Open, 2: Close, 3: Stop, 4: Step Open, 5: Step Close"]]
		command "stepClose", [[
			name: "step",
			type: "NUMBER",
			description: "Amount to change position towards close. Defaults to defaultStepAmount if not set."]]
		command "stepOpen", [[
			name: "step",
			type: "NUMBER",
			description: "Amount to change position towards open. Defaults to defaultStepAmount if not set."]]
		command "setSpeed", [[
			name: "speed*",
			type: "NUMBER",
			description: "Motor speed (0 to 100). Values below 5 may not work."]]

		fingerprint(endpointId: "01", profileId: "0104",
					inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019",
					manufacturer: "_TYST11_wmcdj3aq", model: "mcdj3aq",
					deviceJoinName: "Zemismart Zigbee Blind")
		fingerprint(endpointId: "01", profileId: "0104",
					inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019",
					manufacturer: "_TYST11_cowvfr", model: "owvfni3",
					deviceJoinName: "Zemismart Zigbee Blind")
		fingerprint(endpointId: "01", profileId: "0104",
					inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0019",
					deviceJoinName: "Zemismart Zigbee Blind")
		// AM43-0.45/40-ES-EZ
		fingerprint(endpointId: "01", profileId: "0104",
					inClusters: "0000, 0004, 0005, EF00", outClusters: "0019, 000A",
					manufacturer: "_TZE200_zah67ekd", model: "TS0601",
					deviceJoinName: "Zemismart Zigbee Blind Motor")
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_xuzcvlku" ,deviceJoinName: "Zemismart Zigbee Blind Motor M515EGBZTN"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_rddyvrci" ,deviceJoinName: "Zemismart Zigbee Blind Motor AM43"    //!!! close: 1, open: 2, stop: 0
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_gubdgai2" ,deviceJoinName: "Zemismart Zigbee Blind Motor" 
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_iossyxra" ,deviceJoinName: "Zemismart Tubular Roller Blind Motor AM15" 
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_uzinxci0" ,deviceJoinName: "Zignito Tubular Roller Blind Motor AM15" 
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_nueqqe6k" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_yenbr4om" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_5sbebbzs" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_xaabybja" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_hsgrhjpf" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_68nvbio9" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_zuz7f94z" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_ergbiejo" ,deviceJoinName: "Tuya Zigbee Blind Motor"
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_wmcdj3aq" ,deviceJoinName: "Tuya Zigbee Blind Motor"    // !!! close: 0, open: 2, stop: 1
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TZE200_cowvfni3" ,deviceJoinName: "Tuya Zigbee Blind Motor"    // !!! close: 0, open: 2, stop: 1
        fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0004,0005,EF00", outClusters:"0019,000A", model:"TS0601", manufacturer:"_TYST11_cowvfni3" ,deviceJoinName: "Tuya Zigbee Blind Motor"    // !!! close: 0, open: 2, stop: 1
        // defaults are : close: 2, open: 0, stop: 1
	}

	preferences {
		input("mode", "enum", title: "Mode",
			description: "<li><b>lift</b> - motor moves until button pressed again</li>"
					+ "<li><b>tilt</b> - pressing button < 1.5s, movement stops on release"
					+ "; pressing button > 1.5s, motor moves until button pressed again</li>",
			options: MODE_MAP, required: true, defaultValue: "1")
		input("direction", "enum", title: "Direction",
			options: DIRECTION_MAP, required: true, defaultValue: 0)
		input("maxClosedPosition", "number", title: "Max Closed Position",
			description: "The max position value that window shade state should be set to closed",
			required: true, defaultValue: 1)
		input("minOpenPosition", "number", title: "Min Open Position",
			description: "The min position value that window shade state should be set to open",
			required: true, defaultValue: 99)
		input("defaultStepAmount", "number", title: "Default Step Amount",
			description: "The default step amount",
			required: true, defaultValue: 5)
		input("enableDebugLog", "bool", title: "Enable debug logging", required: true, defaultValue: false)
		input("enableTraceLog", "bool", title: "Enable trace logging", required: true, defaultValue: false)
		input("enableUnexpectedMessageLog", "bool", title: "Log unexpected messages", required: true, defaultValue: false)   
		input("advancedOptions", "bool", title: "Show Advanced options", required: true, defaultValue: false)

        if (advancedOptions == true) {
    		input ("invertPosition", "bool", title: "Invert position reporting", required: true, defaultValue: true)
        }
        
	}
}

@Field final String MODE_TILT = "0"
@Field final Map MODE_MAP = [1: "lift", 0: "tilt"]
@Field final Map MODE_MAP_REVERSE = MODE_MAP.collectEntries { [(it.value): it.key] }
@Field final List MODES = MODE_MAP.collect { it.value }
@Field final Map DIRECTION_MAP = [0: "forward", 1: "reverse"]
@Field final Map DIRECTION_MAP_REVERSE = DIRECTION_MAP.collectEntries { [(it.value): it.key] }
@Field final List DIRECTIONS = DIRECTION_MAP.collect { it.value }
@Field final int CHECK_FOR_RESPONSE_INTERVAL_SECONDS = 60
@Field final int HEARTBEAT_INTERVAL_SECONDS = 4000 // a little more than 1 hour
@Field final int POSITION_UPDATE_TIMEOUT = 5000    //  in milliseconds 

//
// Life Cycle
//

def installed() {
	configure()
}

def updated() {
	configure()
}

def configure() {
	logDebug("configure")
	state.version = textVersion()
	state.copyright = textCopyright()

	if (state.lastHeardMillis == null) {
		state.lastHeardMillis = 0
	}

	sendEvent(name: "numberOfButtons", value: 5)
	if (device.currentValue("position") != null
        && (device.currentValue("windowShade") == "closed"
            || device.currentValue("windowShade") == "open"
            || device.currentValue("windowShade") == "partially open")) {
		updateWindowShadeArrived(device.currentValue("position"))
	}

	// Must run async otherwise, one will block the other
	runIn(1, setMode)
	runIn(2, setDirection)

	if (maxClosedPosition < 0 || maxClosedPosition > 100) {
		throw new Exception("Invalid maxClosedPosition \"${maxClosedPosition}\" should be between"
			+ " 0 and 100 inclusive.")
	}
	if (minOpenPosition < 0 || minOpenPosition > 100) {
		throw new Exception("Invalid minOpenPosition \"${minOpenPosition}\" should be between 0"
			+ " and 100 inclusive.")
	}
	if (maxClosedPosition >= minOpenPosition) {
		throw new Exception("maxClosedPosition \"${minOpenPosition}\" must be less than"
			+ " minOpenPosition \"${minOpenPosition}\".")
	}
    if (settings.advancedOptions == null) device.updateSetting("advancedOptions", [value: false, type: "bool"]) 
}

def setDirection() {
	def directionValue = direction as int
	logDebug("setDirection: directionText=${DIRECTION_MAP[directionValue]}, directionValue=${directionValue}")
	sendTuyaCommand(DP_ID_DIRECTION, DP_TYPE_ENUM, directionValue, 2)
}

def setMode() {
	def modeValue = mode as int
	logDebug("setMode: modeText=${MODE_MAP[mode]}, modeValue=${modeValue}")
	sendTuyaCommand(DP_ID_MODE, DP_TYPE_ENUM, modeValue, 2)
}

//
// Messages
//

@Field final int CLUSTER_TUYA = 0xEF00

@Field final int ZIGBEE_COMMAND_SET_DATA = 0x00
@Field final int ZIGBEE_COMMAND_REPORTING = 0x01
@Field final int ZIGBEE_COMMAND_SET_DATA_RESPONSE = 0x02
@Field final int ZIGBEE_COMMAND_ACK = 0x0B
@Field final int ZIGBEE_COMMAND_SET_TIME = 0x24

@Field final int DP_ID_COMMAND = 0x01
@Field final int DP_ID_TARGET_POSITION = 0x02
@Field final int DP_ID_CURRENT_POSITION = 0x03
@Field final int DP_ID_DIRECTION = 0x05
@Field final int DP_ID_COMMAND_REMOTE = 0x07
@Field final int DP_ID_MODE = 0x65
@Field final int DP_ID_SPEED = 0x69

@Field final int DP_TYPE_BOOL = 0x01
@Field final int DP_TYPE_VALUE = 0x02
@Field final int DP_TYPE_ENUM = 0x04

@Field final int DP_COMMAND_OPEN = 0x00
@Field final int DP_COMMAND_STOP = 0x01
@Field final int DP_COMMAND_CLOSE = 0x02
@Field final int DP_COMMAND_CONTINUE = 0x03

def parse(String description) {
	if (description == null || (!description.startsWith('catchall:') && !description.startsWith('read attr -'))) {
		logUnexpectedMessage("parse: Unhandled description=${description}")
		return
	}
	updatePresence(true)
	Map descMap = zigbee.parseDescriptionAsMap(description)
	if (descMap.clusterInt != CLUSTER_TUYA) {
		logUnexpectedMessage("parse: Not a Tuya Message descMap=${descMap}")
		return
	}
	def command = zigbee.convertHexToInt(descMap.command)
	switch (command) {
		case ZIGBEE_COMMAND_SET_DATA_RESPONSE: // 0x02
        case ZIGBEE_COMMAND_REPORTING : // 0x01
			if (!descMap?.data || descMap.data.size() < 7) {
                logUnexpectedMessage("parse: Invalid data size for SET_DATA_RESPONSE descMap=${descMap}")
				return
			}
			parseSetDataResponse(descMap)
			return
		case ZIGBEE_COMMAND_ACK: // 0x0B
			if (!descMap?.data || descMap.data.size() < 2) {
				logUnexpectedMessage("parse: Invalid data size for ACK descMap=${descMap}")
				return
			}
			def ackCommand = zigbee.convertHexToInt(descMap.data.join())
	        logTrace("parse: ACK command=${ackCommand}")
			return
		case ZIGBEE_COMMAND_SET_TIME: // 0x24
			// Data payload seems to increment every hour but doesn't seem to be an absolute value
	        logTrace("parse: SET_TIME data=${descMap.data}")
            processTuyaSetTime()
			return
		default:
			logUnexpectedMessage("parse: Unhandled command=${command} descMap=${descMap}")
			return
	}
}

/*
 * Data (sending and receiving) generally have this format:
 * [2 bytes] (packet id)
 * [1 byte] (dp ID)
 * [1 byte] (dp type)
 * [2 bytes] (fnCmd length in bytes)
 * [variable bytes] (fnCmd)
 */
def parseSetDataResponse(descMap) {
	logTrace("parseSetDataResponse: descMap=${descMap}")
	def data = descMap.data
	def dp = zigbee.convertHexToInt(data[2])
	def dataValue = zigbee.convertHexToInt(data[6..-1].join())
	switch (dp) {
		case DP_ID_COMMAND: // 0x01 Command
			switch (dataValue) {
				case DP_COMMAND_OPEN: // 0x00
					logDebug("parse: opening")
                    runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
					updateWindowShadeOpening()
					return
				case DP_COMMAND_STOP: // 0x01
                    runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
					logDebug("parse: stopping")
					return
				case DP_COMMAND_CLOSE: // 0x02
                    runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
					logDebug("parse: closing")
					updateWindowShadeClosing()
					return
				case DP_COMMAND_CONTINUE: // 0x03
                    runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
					logDebug("parse: continuing")
					return
				default:
					logUnexpectedMessage("parse: Unexpected DP_ID_COMMAND dataValue=${dataValue}")
					return
			}
		
		case DP_ID_TARGET_POSITION: // 0x02 Target position    // for new blinds models - this is the actual/current position !
			if (dataValue >= 0 && dataValue <= 100) {
                if ( invertPosition == true ) {
                    dataValue = 100 - dataValue
                }
				logDebug("parse: moved to position ${dataValue}")
                runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
				updateWindowShadeMoving(dataValue)
				updatePosition(dataValue)
			} else {
				logUnexpectedMessage("parse: Unexpected DP_ID_TARGET_POSITION dataValue=${dataValue}")
			}
			return
		
		case DP_ID_CURRENT_POSITION: // 0x03 Current Position
			if (dataValue >= 0 && dataValue <= 100) {
                if ( invertPosition == true ) {
                    dataValue = 100 - dataValue
                }
				logDebug("parse: arrived at position ${dataValue}")
                runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
				updateWindowShadeArrived(dataValue)
				updatePosition(dataValue)
			} else {
				logUnexpectedMessage("parse: Unexpected DP_ID_CURRENT_POSITION dataValue=${dataValue}")
			}
			return
		
		case DP_ID_DIRECTION: // 0x05 Direction
			def directionText = DIRECTION_MAP[dataValue]
			if (directionText != null) {
				logDebug("parse: direction=${directionText}")
				updateDirection(dataValue)
			} else {
				logUnexpectedMessage("parse: Unexpected DP_ID_DIRECTION dataValue=${dataValue}")
			}
			return
		
		case DP_ID_COMMAND_REMOTE: // 0x07 Remote Command
			if (dataValue == 0) {
				logDebug("parse: opening from remote")
                runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
				updateWindowShadeOpening()
			} else if (dataValue == 1) {
				logDebug("parse: closing from remote")
                runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
				updateWindowShadeClosing()
			} else {
				logUnexpectedMessage("parse: Unexpected DP_ID_COMMAND_REMOTE dataValue=${dataValue}")
			}
			return
		
		case DP_ID_MODE: // 0x65 Mode
			def modeText = MODE_MAP[dataValue]
			if (modeText != null) {
				logDebug("parse: mode=${modeText}")
				updateMode(dataValue)
			} else {
				logUnexpectedMessage("parse: Unexpected DP_ID_MODE dataValue=${dataValue}")
			}
			return
		
		case DP_ID_SPEED: // 0x69 Motor speed
			if (dataValue >= 0 && dataValue <= 100) {
				logDebug("parse: speed=${dataValue}")
				updateSpeed(dataValue)
			} else {
				logUnexpectedMessage("parse: Unexpected DP_ID_SPEED dataValue=${dataValue}")
			}
			return
		
		default:
			logUnexpectedMessage("parse: Unknown DP_ID dp=0x${data[2]}, dataType=0x${data[3]} dataValue=${dataValue}")
			return
	}
}

def processTuyaSetTime() {
    logDebug("${device.displayName} time synchronization request")    
    def offset = 0
    try {
        offset = location.getTimeZone().getOffset(new Date().getTime())
    }
    catch(e) {
        log.error "${device.displayName} cannot resolve current location. please set location in Hubitat location setting. Setting timezone offset to zero"
    }
    def cmds = zigbee.command(CLUSTER_TUYA, ZIGBEE_COMMAND_SET_TIME, "0008" +zigbee.convertToHexString((int)(now()/1000),8) +  zigbee.convertToHexString((int)((now()+offset)/1000), 8))
    logDebug("${device.displayName} sending time data : ${cmds}")
    cmds.each{ sendHubCommand(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE)) }
}

private ignorePositionReport(position) {
	def lastPosition = device.currentValue("position")
	logDebug("ignorePositionReport: position=${position}, lastPosition=${lastPosition}")
	if (lastPosition == "undefined" || isWithinOne(position)) {
		logTrace("Ignore invalid reports")
		return true
	}
	return false
}

private isWithinOne(position) {
	def lastPosition = device.currentValue("position")
	if (lastPosition != "undefined" && Math.abs(position - lastPosition) <= 1) {
    	logTrace("isWithinOne:true (position=${position}, lastPosition=${lastPosition})")
		return true
	}
 	logTrace("isWithinOne:false (position=${position}, lastPosition=${lastPosition})")
	return false
}

private updateDirection(directionValue) {
	def directionText = DIRECTION_MAP[directionValue]
	logDebug("updateDirection: directionText=${directionText}, directionValue=${directionValue}")
	if (directionValue != (direction as int)) {
		setDirection()
	}
}

private updateMode(modeValue) {
	def modeText = MODE_MAP[modeValue]
	logDebug("updateMode: modeText=${modeText}, modeValue=${modeValue}")
	if (modeValue != (mode as int)) {
		setMode()
	}
}

private updatePosition(position) {
	logDebug("updatePosition: position=${position}")
	if (isWithinOne(position)) {
    	logDebug("updatePosition: <b>arrived!</b>")
        updateWindowShadeArrived(position)
        return
	}
	sendEvent(name: "position", value: position, unit: "%")
	sendEvent(name: "level", value: position, unit: "%")
}

private updatePresence(present) {
	logDebug("updatePresence: present=${present}")
	if (present) {
		state.lastHeardMillis = now()
		checkHeartbeat()
	}
	state.waitingForResponseSinceMillis = null
	sendEvent(name: "presence", value: present ? "present" : "not present")
}

private updateSpeed(speed) {
	logDebug("updateSpeed: speed=${speed}")
	sendEvent(name: "speed", value: speed)
}

private updateWindowShadeMoving(position) {
	def lastPosition = device.currentValue("position")
	logDebug("updateWindowShadeMoving: position=${position} (lastPosition=${lastPosition})")

	if (lastPosition < position) {
		updateWindowShadeOpening()
	} else if (lastPosition > position) {
		updateWindowShadeClosing()
	}
}

private updateWindowShadeOpening() {
	logTrace("updateWindowShadeOpening")
	sendEvent(name:"windowShade", value: "opening")
}

private updateWindowShadeClosing() {
	logTrace("updateWindowShadeClosing")
	sendEvent(name:"windowShade", value: "closing")
}

private updateWindowShadeArrived(position) {
	logDebug("updateWindowShadeArrived: position=${position}")
	if (position < 0 || position > 100) {
		log.warn("updateWindowShadeArrived: Need to setup limits on device")
		sendEvent(name: "windowShade", value: "unknown")
	} else if (position <= maxClosedPosition) {
		sendEvent(name: "windowShade", value: "closed")
	} else if (position >= minOpenPosition) {
		sendEvent(name: "windowShade", value: "open")
	} else {
		sendEvent(name: "windowShade", value: "partially open")
	}
}

//
// Actions
//

def close() {
    logDebug("close, direction = ${direction as int}")
	//sendEvent(name: "position", value: 0)
	if (mode == MODE_TILT) {
        	logDebug("close mode == MODE_TILT")
		setPosition(0)
	} else {
        runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
        sendTuyaCommand(DP_ID_COMMAND, DP_TYPE_ENUM, DP_COMMAND_CLOSE, 2)
	}
}

def open() {
	logDebug("open, direction = ${direction as int}")
	//sendEvent(name: "position", value: 100)
	if (mode == MODE_TILT) {
		setPosition(100)
	} else {
        runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
        sendTuyaCommand(DP_ID_COMMAND, DP_TYPE_ENUM, DP_COMMAND_OPEN, 2)
	}
}

def startPositionChange(state) {
	logDebug("startPositionChange")
	switch (state) {
		case "close":
			close()
			return
		case "open":
			open()
			return
		default:
			throw new Exception("Unsupported startPositionChange state \"${state}\"")
	}
}

def stopPositionChange() {
	logDebug("stopPositionChange")
    runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
	sendTuyaCommand(DP_ID_COMMAND, DP_TYPE_ENUM, DP_COMMAND_STOP, 2)
}

def setLevel( level )
{
    setPosition(level)
}

def setPosition(position) {
	if (position < 0 || position > 100) {
		throw new Exception("Invalid position ${position}. Position must be between 0 and 100 inclusive.")
	}
	if (isWithinOne(position)) {
	    // Motor is off by one sometimes, so set it to desired value if within one
	    //	sendEvent(name: "position", value: position)
        logDebug("setPosition: no need to move!")
        updateWindowShadeArrived(position)
        return
	}
    Integer currentPosition = device.currentValue("position")
    if(position > currentPosition) {
        sendEvent(name: "windowShade", value: "opening")
    } 
    else if(position < currentPosition) {
        sendEvent(name: "windowShade", value: "closing")
    }    
    logDebug("setPosition: position=${position}, currentPosition=${device.currentValue('position')}")
    if ( invertPosition == true ) {
        position = 100 - position
    }
    runInMillis(POSITION_UPDATE_TIMEOUT, endOfMovement, [overwrite: true])
	sendTuyaCommand(DP_ID_TARGET_POSITION, DP_TYPE_VALUE, position.intValue(), 8)
}

def stepClose(step) {
	if (!step) {
		step = defaultStepAmount
	}
	stepOpen(-step)
}

def stepOpen(step) {
	logDebug("stepOpen: step=${step}")
	if (!step) {
		step = defaultStepAmount
	}
	setPosition(Math.max( 0, Math.min(100, (device.currentValue("position") + step) as int)))
}


def setSpeed(speed) {
	logDebug("setSpeed: speed=${speed}")
	if (speed < 0 || speed > 100) {
		throw new Exception("Invalid speed ${speed}. Speed must be between 0 and 100 inclusive.")
	}
	sendTuyaCommand(DP_ID_SPEED, DP_TYPE_ENUM, speed.intValue(), 8)
}

def push(buttonNumber)		{
	logTrace("push: buttonNumber=${buttonNumber}")
	sendEvent(name: "pushed", value: buttonNumber, isStateChange: true)
	switch(buttonNumber) {
		case 1:
			open()
			break
		case 2:
			close()
			break
		case 3:
			stopPositionChange()
			break
		case 4:
			stepOpen()
			break
		case 5:
			stepClose()
			break
		default:
			throw new Exception("Unsupported buttonNumber \"${buttonNumber}\"")
	}
}


def endOfMovement() {
    updateWindowShadeArrived(device.currentValue("position"))
}

//
// Helpers
//

private sendTuyaCommand(int dp, int dpType, int fnCmd, int fnCmdLength) {
	state.waitingForResponseSinceMillis = now()
	checkForResponse()
    
	def dpHex = zigbee.convertToHexString(dp, 2)
	def dpTypeHex = zigbee.convertToHexString(dpType, 2)
	def fnCmdHex = zigbee.convertToHexString(fnCmd, fnCmdLength)
	logTrace("sendTuyaCommand: dp=0x${dpHex}, dpType=0x${dpTypeHex}, fnCmd=0x${fnCmdHex}, fnCmdLength=${fnCmdLength}")
	def message = (randomPacketId().toString()
				   + dpHex
				   + dpTypeHex
				   + zigbee.convertToHexString((fnCmdLength / 2) as int, 4)
				   + fnCmdHex)
	//logTrace("sendTuyaCommand: message=${message}")
	zigbee.command(CLUSTER_TUYA, ZIGBEE_COMMAND_SET_DATA, message)
}

private randomPacketId() {
	return zigbee.convertToHexString(new Random().nextInt(65536), 4)
}

// Must be non-private to use runInMillis
def checkForResponse() {
	//logTrace("checkForResponse: waitingForResponseSinceMillis=${state.waitingForResponseSinceMillis}")
	if (state.waitingForResponseSinceMillis == null) {
		return
	}
	def waitMillis = (CHECK_FOR_RESPONSE_INTERVAL_SECONDS * 1000
			- (now() - state.waitingForResponseSinceMillis))
	//logTrace("checkForResponse: waitMillis=${waitMillis}")
	if (waitMillis <= 0) {
		updatePresence(false)
	} else {
		runInMillis(waitMillis, checkForResponse, [overwrite: true])
	}
}

// Must be non-private to use runInMillis
def checkHeartbeat() {
	def waitMillis = (HEARTBEAT_INTERVAL_SECONDS * 1000
			- (now() - state.lastHeardMillis))
	//logTrace("checkHeartbeat: waitMillis=${waitMillis}")
	if (waitMillis <= 0) {
		updatePresence(false)
	} else {
		runInMillis(waitMillis, checkHeartbeat, [overwrite: true])
	}
}

private logDebug(text) {
	if (!enableDebugLog) {
		return
	}
	log.debug(text)
}

private logTrace(text) {
	if (!enableTraceLog) {
		return
	}
	log.trace(text)
}

private logUnexpectedMessage(text) {
	if (!enableUnexpectedMessageLog) {
		return
	}
	log.warn(text)
}
