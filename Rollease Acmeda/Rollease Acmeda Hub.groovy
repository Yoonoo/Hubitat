/* 

Version 2020.06.08.1

Release Notes:
- Added Maximum Idle Time
- Ignore invalid shade addresses


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
        
 
    input ("maxIdleTime", "Number",
		title: "Maximum Idle Time", description: "Reset connection to hub if no status reports are received within this time (Seconds). 0=Disabled", defaultValue: "3600",
                required: false, displayDuringSetup: false )
    /*
    input ("keepAliveInterval", "Number",
		title: "Keep-Alive Interval", description: "Send Keep-Alive packets to hub in order to detect when connection is interrupted (Seconds). 0=Disabled", defaultValue: "300",
                required: false, displayDuringSetup: false )
      */
        
    input ("debug", "bool",
		title: "Enable debug logging", description: "", defaultValue: false,required: false, displayDuringSetup: false )
    }
    

    
}


def initialize () {
    telnetClose()    
    logInfo "Opening telnet connection"
    telnetConnect([termChars:[59]],hubAddress, 1487, null, null)
    startMaxIdleTimer()
    
    /*
    if (keepAliveInterval > 0) 
        runIn(keepAliveInterval, "keepAlive");
    */
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
    lastThree = msg[-3..-1]
    

    //Ignore Telnet Errors and Hub Events
    if (motorAddress == "EUC" || motorAddress == "BR1" || lastThree == "Enp") {
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
	
    startMaxIdleTimer()
    
                          
}

def private startMaxIdleTimer() {
    
    //Reset Maximum Idle Time
    //Y.O. 5/4/2020 No need to unschedule all timers since this affects connection retry timer. Calling runIn with the same function overwrites any existing runIns for that function
    //unschedule() 
    if (maxIdleTime > 0 ){
        logDebug "Will reset connection if no events received in ${maxIdleTime} seconds"
        runIn(maxIdleTime, "resetConnection")
    }
    
}


def configure(){
    log.info "Scanning for shades..."
    sendTelnetCommand "!000v?"
}


def refresh() {
     
}

/*
//Y.O. 5/4/2020 Hubitat does not appear to send scheduled telnet commands in the background.
private def keepAlive() {
    if (keepAliveInterval > 0){
        log.info "Sending Keep-Alive"
        sendTelnetCommand "!BR1v?"
        runIn(keepAliveInterval, "keepAlive")
    }
    
}
*/


private def resetConnection() {
    log.info "Resetting Connection..."
    initialize()
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
