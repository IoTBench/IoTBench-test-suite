/**
 *  Darken Behind Me
 *
 *  Author: SmartThings
 */
def preferences() {
	[
		sections: [
			[
				title: "PIR Sensor",
				input: [
					[
						name: "motion",
						title: "Where?",
						type: "device.ArduinoPIR",
						description: "Tap to set",
						multiple: false
					]
				]
			]
        ]
	]
}

def installed()
{
	log.debug "installed()";
	subscribe();
}

def updated()
{
	log.debug "updated";

	unsubscribe();
	subscribe();
}


def subscribe(){
	subscribe(motion.pirStatus);

}	



def pirStatus(evt) {
	log.debug "$evt.name: $evt.value"
    
	if (evt.value == "active") {
    	
        log.debug("Arduino PIR Sensor is Active");
    	
        httpPost("http://httpbin.org/post", evt.value) {
        	response -> if ("true".equals( response.data.toString() ) ) { 
            	log.debug("response!");
                log.debug(response);
                //doSomething() 
           }
       	}
        
        /**monitor.distract()
       	
        if (switch1) {
        	schedule(util.cronExpression(now() + 5000), "turnOnLight")
        }
        
        schedule(util.cronExpression(now() + 10000), "notifyParents")
        
        state.originalSwitchState = switch1?.currentSwitch**/
    }
    
    else {
    
    	log.debug("Arduino PIR Sensor is InActive - huzzah!");
    	/**unschedule()
        if (state.originalSwitchState == "off") {
        	switch1.off()
        }
        if (state.notified) {
            def message = "Your child stopped crying"
            sendPush(message)
            if (phone) {
                sendSms(phone, message)
            }
            state.notified = false
        }**/
        
        
    }
}