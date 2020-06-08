/* 

Version 2020.06.08.01
Release Notes:
- Added "opening" and "closing" states to the windowShade attribute
- Added "toggle" command


Revision History
Version 2020.03.30 
- Initial Release

*/

metadata {
	definition (name: "Rollease Acmeda Shade", namespace: "arcautomate", author: "Younes Oughla", vid: "generic-shade") {
	capability "Initialize"
    capability "Refresh"
    capability "Switch Level"
    capability "Switch"
    capability "Window Shade"
        
    command  "stop"
    command  "toggle"
        
    attribute "open", "bool"
    attribute "closed", "bool"
    attribute "position", "int"
    attribute "moving","bool"
    attribute "voltage","int"
        
	}
    
   
    preferences {
	input ("motorAddress", "STRING",
		title: "Motor Address", description: "", defaultValue: "000",
                required: true, displayDuringSetup: true )
        
    input ("debug", "bool",
		title: "Enable debug logging", description: "", defaultValue: false,required: false, displayDuringSetup: false )
	}
   
    
}



def initialize(){
	logDebug "Motor Address: ${settings?.motorAddress}"
}

def on() {
    open()    
}

def off() {
    close()    
}

def setLevel(level) {
    setPosition(level)
   
}

def open(){
	logDebug "Opening Shade"
    sendCommand("m","000")
    
}

def close(){
	logDebug "Closing Shade"
    sendCommand("m","100")
}

def stop() {
  logDebug "Stopping Shade"
  sendCommand("s","")  
}

def toggle() {
    logDebug "Toggling Shade"
  
    currentStatus = device.currentValue("windowShade")
  
    switch (currentStatus) {
    
        case "opening":
        case "closing":
            stop()
            break;
  
        case "open":
            close()
            break;
        case "closed":
            open()
            break;
        case "partially open":
            if (state.lastDirection == "closing") {
                open()    
            }
            else{
                close()
            }    
            break;
        default:
            open()
            break;
        
            
    }
    
    
  
    
}


def setPosition(position) {
    logDebug "Set Position: ${position}"
    
    positionString = 100-position
    positionString = "000${positionString}"
    positionString = positionString[-3..-1]
        
    sendCommand("m",positionString)
}


def sendCommand(String command, String commandData) {
    
    commandString = "!${motorAddress}${command}${commandData}"
    logDebug "Send Command: ${commandString}"
    parent.sendTelnetCommand(commandString)
}

def parse(String msg) {
    
    if(msg.startsWith("!${motorAddress}r"))
	{
		positionStr = msg.substring(5, 8)

        
        position = 100 - Integer.parseInt(positionStr)

        
        positionUpdated(position)
		
   
	}
    
    if(msg.startsWith("!${motorAddress}m"))
	{
		sendEvent(name: "moving", value: true)
        
        positionStr = msg.substring(5, 8)
        
        targetPosition = 100 - Integer.parseInt(positionStr)
        currentPosition = device.currentValue("position")
     
        
        
        if (targetPosition > currentPosition) {
            sendEvent(name: "windowShade", value: "opening")
            state.lastDirection = "opening"
            
        }
        
        if (targetPosition < currentPosition) {
            sendEvent(name: "windowShade", value: "closing")
            state.lastDirection = "closing"
        }
     
        
	}
    
    if(msg.startsWith("!${motorAddress}pVc"))
	{
        voltageStr = msg.substring(7)
		sendEvent(name: "voltage", value: voltageStr)
	}
    
}


def positionUpdated(position) {
    
    logDebug "Position Updated: ${position}"
        if (position == 100) {
            sendEvent(name: "open", value: true)
            sendEvent(name: "closed", value: false)
            sendEvent(name: "windowShade", value: "open")
        }
        
        if (position == 0) {
            sendEvent(name: "closed", value: true)  
            sendEvent(name: "open", value: false)  
            sendEvent(name: "windowShade", value: "closed")
        }
        
        if (position !=0 && position !=100) {
            sendEvent(name: "closed", value: false)   
            sendEvent(name: "open", value: false)
            sendEvent(name: "windowShade", value: "partially open")
        }
        
        
        sendEvent(name: "position", value: position)
        sendEvent(name: "moving", value: false)
}



def requestStatus() {
    sendCommand("r","?")
}


def refresh() {
   logDebug "Refreshing"
   requestStatus()
    
}

private def logDebug(message) {
    if (!debug) {
        return;   
    }
     log.debug "${device.name} ${message}"  
}

private def logInfo(message) {
     log.info "${device.name} ${message}"  
}

private def logWarning(message) {
     log.warn "${device.name} ${message}"  
}


def installed(){
	initialize()
}

def updated(){
	initialize()
}
