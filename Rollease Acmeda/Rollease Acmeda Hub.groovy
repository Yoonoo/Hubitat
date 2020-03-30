/* 

Version 2020.03.30.1

Revision History:

Version 2020.03.30.1
- Retry telnet connection when connection is dropped
- Moved telnetClose to Initialize
- Close telnet connection in case of telnet error
- Added logError

Version 2020.03.30
- Initial Release

*/

metadata {
	definition (name: "Rollease Acmeda Hub", namespace: "arcautomate", author: "Younes Oughla", vid: "generic-shade") {
	capability "Initialize"
    capability "Telnet"
    capability "Refresh"
    capability "Configuration" 
        
    command  "sendTelnetCommand", ["commandString"]
        
	}
    
    preferences {
	input ("hubAddress", "STRING",
		title: "Hub Address", description: "IP Address of the Hub", defaultValue: "",
                required: true, displayDuringSetup: true )
        
    input ("connectionRetryInterval", "Number",
		title: "Connection Retry Interval", description: "Number of seconds to wait before re-attempting the connection. 0=Do Not Retry", defaultValue: "300",
                required: false, displayDuringSetup: false )
	
    input ("debug", "bool",
		title: "Enable debug logging", description: "", defaultValue: false,required: false, displayDuringSetup: false )
    }
    
}


def initialize () {
    telnetClose()    
    logInfo "Opening telnet connection"
    telnetConnect([termChars:[59]],hubAddress, 1487, null, null)
}



def sendTelnetCommand(String commandString) {
    logDebug "Sending Telnet Command: ${commandString}"
    return new hubitat.device.HubAction(commandString,hubitat.device.Protocol.TELNET)
    
}


def telnetStatus(String status){
	logError "telnetStatus: error: " + status

    if (connectionRetryInterval > 1 ){
        logInfo "Will try to reconnect in ${connectionRetryInterval} seconds"
        runIn(connectionRetryInterval, initialize)
    }
    
    
    
}

private parse(String msg) 
{
	logDebug("Event Received: " + msg)
    
    motorAddress = msg[1..3]
    

    //Ignore Telnet Errors and Hub Events
    if (motorAddress == "EUC" || motorAddress == "BR1" ) {
        return;
    }
    
    String thisId = device.deviceNetworkId
    
    def cd = getChildDevice("${thisId}-${motorAddress}")
    
    if (!cd) {
        logInfo "Found New Shade: " + motorAddress
        cd = addChildDevice("arcautomate", "Rollease Acmeda Shade", "${thisId}-${motorAddress}", [name: "Rollease Acmeda Shade - ${motorAddress}", isComponent: false])
      
         cd.updateSetting("motorAddress",[type:"STRING",value:motorAddress])      
    }
    
    
    cd.parse(msg)
	
}


def configure(){
    log.info "Scanning for shades..."
    sendTelnetCommand "!000v?"
}


def refresh() {
     
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

private def logError(message) {
     log.error "${device.name} ${message}"  
}



def installed(){
	initialize()
    
}

def updated(){
	initialize()
}
