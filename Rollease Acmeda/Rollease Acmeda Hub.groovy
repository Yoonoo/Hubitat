

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
	
    input ("debug", "bool",
		title: "Enable debug logging", description: "", defaultValue: false,required: false, displayDuringSetup: false )
    }
    
}



def initialize(){
	telnetConnect([termChars:[59]],hubAddress, 1487, null, null)
	logDebug "Opening telnet connection"
  
}


def sendTelnetCommand(String commandString) {
    logDebug "Sending Telnet Command: ${commandString}"
    return new hubitat.device.HubAction(commandString,hubitat.device.Protocol.TELNET)
    
}


def telnetStatus(String status){
	logWarning "telnetStatus: error: " + status
	
   /*
    if (status != "receive error: Stream is closed")
	{
		log.error "Connection was dropped."
		initialize()
	} 
    */
    
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

def requestStatus() {
    sendCommand("r","?")
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


def installed(){
	initialize()
    
}

def updated(){
    telnetClose()
	initialize()
}
