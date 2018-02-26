/**
 *  Hue Lights and Groups and Scenes (OH MY) - new Hue Service Manager
 *
 *  Version 1.4:  	Added ability to create / modify / delete Hue Hub Scenes directly from SmartApp
 *					Added ability to create / modify / delete Hue Hub Groups directly from SmartApp
 *					Overhauled communications to / from Hue Hub
 *					Revised child app functions		
 *
 *  Authors: Anthony Pastor (infofiend) and Clayton (claytonjn)
 *
 */

definition(
    name: "Hue Lights and Groups and Scenes (OH MY)",
    namespace: "info_fiend",
    author: "Anthony Pastor",
	description: "Allows you to connect your Philips Hue lights with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app. Adjust colors by going to the Thing detail screen for your Hue lights (tap the gear on Hue tiles).\n\nPlease update your Hue Bridge first, outside of the SmartThings app, using the Philips Hue app.",
	category: "SmartThings Labs",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/hue.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/hue@2x.png",
	singleInstance: true
)

preferences {
	page(name:"mainPage", title:"Hue Device Setup", content:"mainPage", refreshTimeout:5)
	page(name:"bridgeDiscovery", title:"Hue Bridge Discovery", content:"bridgeDiscovery", refreshTimeout:5)
	page(name:"bridgeBtnPush", title:"Linking with your Hue", content:"bridgeLinking", refreshTimeout:5)
	page(name:"bulbDiscovery", title:"Bulb Discovery", content:"bulbDiscovery", refreshTimeout:5)
	page(name:"groupDiscovery", title:"Group Discovery", content:"groupDiscovery", refreshTimeout:5)
	page(name:"sceneDiscovery", title:"Scene Discovery", content:"sceneDiscovery", refreshTimeout:5)
    page(name:"defaultTransition", title:"Default Transition", content:"defaultTransition", refreshTimeout:5)
    page(name:"createScene", title:"Create A New Scene", content:"createScene")
    page(name:"changeSceneName", title:"Change Name Of An Existing Scene", content:"changeSceneName")
    page(name:"modifyScene", title:"Modify An Existing Scene", content:"modifyScene")
	page(name:"removeScene", title:"Delete An Existing Scene", content:"removeScene")
    page(name:"createGroup", title:"Create A New Group", content:"createGroup")
    page(name:"modifyGroup", title:"Modify An Existing Group", content:"modifyGroup")
	page(name:"removeGroup", title:"Delete An Existing Group", content:"removeGroup")
}

def mainPage() {
	def bridges = bridgesDiscovered()
	if (state.username && bridges) {
		return bulbDiscovery()
	} else {
		return bridgeDiscovery()
	}
}

def bridgeDiscovery(params=[:])
{
	def bridges = bridgesDiscovered()
	int bridgeRefreshCount = !state.bridgeRefreshCount ? 0 : state.bridgeRefreshCount as int
	state.bridgeRefreshCount = bridgeRefreshCount + 1
	def refreshInterval = 3

	def options = bridges ?: []
	def numFound = options.size() ?: 0

	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	//bridge discovery request every 15 //25 seconds
	if((bridgeRefreshCount % 5) == 0) {
		discoverBridges()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((bridgeRefreshCount % 3) == 0) && ((bridgeRefreshCount % 5) != 0)) {
		verifyHueBridges()
	}

	return dynamicPage(name:"bridgeDiscovery", title:"Discovery Started!", nextPage:"bridgeBtnPush", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bridge. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedHue", "enum", required:false, title:"Select Hue Bridge (${numFound} found)", multiple:false, options:options
		}
	}
}

def bridgeLinking()
{
	int linkRefreshcount = !state.linkRefreshcount ? 0 : state.linkRefreshcount as int
	state.linkRefreshcount = linkRefreshcount + 1
	def refreshInterval = 3

	def nextPage = ""
	def title = "Linking with your Hue"
	def paragraphText = "Press the button on your Hue Bridge to setup a link."
	if (state.username) { //if discovery worked
		nextPage = "bulbDiscovery"
		title = "Success! - click 'Next'"
		paragraphText = "Linking to your hub was a success! Please click 'Next'!"
	}

	if((linkRefreshcount % 2) == 0 && !state.username) {
		sendDeveloperReq()
	}

	return dynamicPage(name:"bridgeBtnPush", title:title, nextPage:nextPage, refreshInterval:refreshInterval) {
		section("Button Press") {
			paragraph """${paragraphText}"""
		}
	}
}

def bulbDiscovery() {

	if (selectedHue) {
        def bridge = getChildDevice(selectedHue)
        subscribe(bridge, "bulbList", bulbListHandler)
    }
    
	int bulbRefreshCount = !state.bulbRefreshCount ? 0 : state.bulbRefreshCount as int
	state.bulbRefreshCount = bulbRefreshCount + 1
	def refreshInterval = 3

	def optionsBulbs = bulbsDiscovered() ?: []
    state.optBulbs = optionsBulbs
	def numFoundBulbs = optionsBulbs.size() ?: 0
    
	if((bulbRefreshCount % 3) == 0) {
        log.debug "START BULB DISCOVERY"
        discoverHueBulbs()
        log.debug "END BULB DISCOVERY"
	}

	return dynamicPage(name:"bulbDiscovery", title:"Bulb Discovery Started!", nextPage:"groupDiscovery", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Hue Bulbs. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedBulbs", "enum", required:false, title:"Select Hue Bulbs (${numFoundBulbs} found)", multiple:true, options:optionsBulbs
		}
		section {
			def title = bridgeDni ? "Hue bridge (${bridgeHostname})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
	}
}

def groupDiscovery() {

	if (selectedHue) {
        def bridge = getChildDevice(selectedHue)
        subscribe(bridge, "groupList", groupListHandler)
    }
    
	int groupRefreshCount = !state.groupRefreshCount ? 0 : state.groupRefreshCount as int
	state.groupRefreshCount = groupRefreshCount + 1
	
    def refreshInterval = 3
    if (state.gChange) { 
    	refreshInterval = 1
        state.gChange = false
    }

	def optionsGroups = []
	def numFoundGroups = []
    optionsGroups = groupsDiscovered() ?: []
    numFoundGroups = optionsGroups.size() ?: 0
    
//    if (numFoundGroups == 0)
//        app.updateSetting("selectedGroups", "")

	if((groupRefreshCount % 3) == 0) {
	    log.debug "START GROUP DISCOVERY"
		discoverHueGroups()
        log.debug "END GROUP DISCOVERY"
	}

	return dynamicPage(name:"groupDiscovery", title:"Group Discovery Started!", nextPage:"sceneDiscovery", refreshInterval:refreshInterval, install: false, uninstall: isInitComplete) {
		section("Please wait while we discover your Hue Groups. Discovery can take a few minutes, so sit back and relax! Select your device below once discovered.") {
			input "selectedGroups", "enum", required:false, title:"Select Hue Groups (${numFoundGroups} found)", multiple:true, options:optionsGroups

		}
		section {
			def title = bridgeDni ? "Hue bridge (${bridgeHostname})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
        if (state.initialized) {
        
			section {
				href "createGroup", title: "Create a Group", description: "Create A New Group On Hue Hub", state: selectedHue ? "complete" : "incomplete" 

	    	}
			section {
				href "modifyGroup", title: "Modify a Group", description: "Modify The Lights Contained In Existing Group On Hue Hub", state: selectedHue ? "complete" : "incomplete" 

	    	}
			section {
				href "removeGroup", title: "Delete a Group", description: "Delete An Existing Group From Hue Hub", state: selectedHue ? "complete" : "incomplete" 
	    	}

		}             
	}
}


def createGroup(params=[:]) { 
    
	def theBulbs = state.bulbs
    def creBulbsNames = []
    def bulbID 
    def theBulbName     
   	theBulbs?.each {
       	bulbID = it.key
        theBulbName = state.bulbs[bulbID].name as String
	    creBulbsNames << [name: theBulbName, id:bulbID]        
    }		    
    
    state.creBulbNames = creBulbsNames
    log.trace "creBulbsNames = ${creBulbsNames}."

	def creTheLightsID = []
    if (creTheLights) {
			       	
		creTheLights.each { v ->
       		creBulbsNames.each { m ->
            	if (m.name == v) {
                	creTheLightsID << m.id 
        		}
            }
        }
	}
    
	if (creTheLightsID) {log.debug "The Selected Lights ${creTheLights} have ids of ${creTheLightsID}."}
        
    if (creTheLightsID && creGroupName) {

       	def body = [name: creGroupName, lights: creTheLightsID]

		log.debug "***************The body for createNewGroup() will be ${body}."

    	if ( creGroupConfirmed == "Yes" ) { 
        	
            // create new group on Hue Hub
	        createNewGroup(body)
            
            // reset page
			app.updateSetting("creGroupConfirmed", null)
			app.updateSetting("creGroupName", null)
			app.updateSetting("creTheLights", null)
            app.updateSetting("creTheLightsID", null)
			app.updateSetting("creGroup", null)

            
            state.gChange = true
             
        }
    }
        	
          

		return dynamicPage(name:"createGroup", title:"Create Group", nextPage:"groupDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Choose Name for New Group") {
				input "creGroupName", "text", title: "Group Name: (hit enter when done)", required: true, submitOnChange: true
        
       			if (creGroupName) {
	        	
					input "creTheLights", "enum", title: "Choose The Lights You Want In This Scene", required: true, multiple: true, submitOnChange: true, options:creBulbsNames.name.sort() 
				
                	if (creTheLights) {
                		
	                    paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY create a new group on the Hue Hub called ${creGroupName} using the selected lights." 
                
                		input "creGroupConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
                	} 
                }
			}
    	}
}

def modifyGroup(params=[:]) { 
     
    
   	def theGroups = []
    theGroups = state.groups 								// scenesDiscovered() ?: [] 
   
 	def modGroupOptions = []        
    def grID
    def theGroupName
	theGroups?.each {
       	grID = it.key
        theGroupName = state.groups[grID].name as String
	    modGroupOptions << [name:theGroupName, id:grID]
   	}
    
    state.modGroupOptions = modGroupOptions
	log.trace "modGroupOptions = ${modGroupOptions}."

	def modGroupID
	if (modGroup) {
 		state.modGroupOptions.each { m ->
    	    if (modGroup == m.name) {
   	    		modGroupID = m.id
				log.debug "The selected group ${modGroup} has an id of ${modGroupID}."        
   			}
	    }		                
	}	

	def theBulbs = state.bulbs
    def modBulbsNames = []
    def bulbID 
    def theBulbName     
   	theBulbs?.each {
       	bulbID = it.key
        theBulbName = state.bulbs[bulbID].name as String
	    modBulbsNames << [name: theBulbName, id:bulbID]        
    }		    
    
    state.modBulbNames = modBulbsNames
    log.trace "modBulbsNames = ${modBulbsNames}."

	def modTheLightsID = []
    if (modTheLights) {
			       	
		modTheLights.each { v ->
       		modBulbsNames.each { m ->
//            	log.debug "m.name is ${m.name} and v is ${v}."
            	if (m.name == v) {
//                	log.debug "m.id is ${m.id}."
                	modTheLightsID << m.id // myBulbs.find{it.name == v}
        		}
            }
        }
	}
    
	if (modTheLightsID) {log.debug "The Selected Lights ${modTheLights} have ids of ${modTheLightsID}."}
        
    if (modTheLightsID && modGroupID) {

       	def body = [groupID: modGroupID]
        if (modGroupName) {body.name = modGroupName}
        if (modTheLightsID) {body.lights = modTheLightsID}

		log.debug "***************The body for updateGroup() will be ${body}."

    	if ( modGroupConfirmed == "Yes" ) {
			
            // modify Group lights (and optionally name) on Hue Hub
	        updateGroup(body)
            
            // modify Group lights within ST
            def dni = app.id + "/" + modGroupID + "g"            
	        if (modTheLightsID) { sendEvent(dni, [name: "lights", value: modTheLights]) }
            
            // reset page
            app.updateSetting("modGroupConfirmed", null)
			app.updateSetting("modGroupName", "")
			app.updateSetting("modTheLights", null)
			app.updateSetting("modTheLightsID", null)            
			app.updateSetting("modGroup", null)           
            app.updateSetting("modGroupID", null) 
            
            state.gChange = true 
        }
    }
        	
          

		return dynamicPage(name:"modifyGroup", title:"Modify Group", nextPage:"groupDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Choose Group to Modify") {
				input "modGroup", "enum", title: "Modify Group:", required: true, multiple: false, submitOnChange: true, options:modGroupOptions.name.sort() {it.value}
        
       			if (modGroup) {
	        	
					input "modTheLights", "enum", title: "Choose The Lights You Want In This Group (reflected in Hue Hub and within ST device)", required: true, multiple: true, submitOnChange: true, options:modBulbsNames.name.sort() 
				
                	if (modTheLights) {
                		input "modGroupName", "text", title: "Change Group Name (Reflected ONLY within Hue Hub - ST only allows name / label change via mobile app or IDE ). ", required: false, submitOnChange: true

	                    paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY set the ${modGroup} group to the selected lights." 
                
                		input "modGroupConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
                	} 
                }
			}
    	}
}




def removeGroup(params=[:]) {     
    
   	def theGroups = []
    theGroups = state.groups
   
 	def remGroupOptions = []        
    def grID
    def theGroupName
	theGroups?.each {
       	grID = it.key
        theGroupName = state.groups[grID].name as String
	    remGroupOptions << [name:theGroupName, id:grID]
   	}
    
    state.remGroupOptions = remGroupOptions
	log.trace "remGroupOptions = ${remGroupOptions}."

	def remGroupID
	if (remGroup) {
 		state.remGroupOptions.each { m ->
    	    if (remGroup == m.name) {
   	    		remGroupID = m.id
				log.debug "The selected group ${remGroup} has an id of ${remGroupID}."        
   			}
	    }		                
	}	
        
    if (remGroupID) {

       	def body = [groupID: remGroupID]

		log.debug "***************The body for deleteGroup() will be ${body}."

    	if ( remGroupConfirmed == "Yes" ) { 
        	                  
			// delete group from hub                              
	        deleteGroup(body)             
            
            // try deleting group from ST (if exists)            
            def dni = app.id + "/" + remGroupID + "g"
            log.debug "remGroup: dni = ${dni}."
            try {
            	deleteChildDevice(dni)
                log.trace "${remGroup} found and successfully deleted from ST."
			} catch (e) {
                log.debug "${remGroup} not found within ST - no action taken."            	
			}
            
            // reset page
            app.updateSetting("remGroupConfirmed", null)
			app.updateSetting("remGroup", null)
            app.updateSetting("remGroupID", null)
            
            state.gChange = true
        }
    }
        	
          

		return dynamicPage(name:"removeGroup", title:"Delete Group", nextPage:"groupDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Choose Group to DELETE") {
				input "remGroup", "enum", title: "Delete Group:", required: true, multiple: false, submitOnChange: true, options:remGroupOptions.name.sort() {it.value}
        
       			if (remGroup) {
	        	
	                paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY DELETE the ${remGroup} group FOREVER!!!" 
                
                	input "remGroupConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
				} 
                
			}
    	}
}




def sceneDiscovery() {

	def isInitComplete = initComplete() == "complete"
	
    state.inItemDiscovery = true

    if (selectedHue) {
        def bridge = getChildDevice(selectedHue)
        subscribe(bridge, "sceneList", sceneListHandler)
    }

    def toDo = ""
     
	int sceneRefreshCount = !state.sceneRefreshCount ? 0 : state.sceneRefreshCount as int
	state.sceneRefreshCount = sceneRefreshCount + 1
	
    def refreshInterval = 3
    if (state.sChange) {
    	refreshInterval = 1
        state.sChange = false
    }    

	def optionsScenes = scenesDiscovered() ?: []
	def numFoundScenes = optionsScenes.size() ?: 0

//	if (numFoundScenes == 0)
//        app.updateSetting("selectedScenes", "")
	
	if((sceneRefreshCount % 3) == 0) {
        log.debug "START HUE SCENE DISCOVERY"
        discoverHueScenes()
        log.debug "END HUE SCENE DISCOVERY"
	}

	return dynamicPage(name:"sceneDiscovery", title:"Scene Discovery Started!", nextPage:toDo, refreshInterval:refreshInterval, install: true, uninstall: isInitComplete) {
		section("Please wait while we discover your Hue Scenes. Discovery can take a few minutes, so sit back and relax! Select your device below once discovered.") {
			input "selectedScenes", "enum", required:false, title:"Select Hue Scenes (${numFoundScenes} found)", multiple:true, options:optionsScenes.sort {it.value}
		}
		section {
			def title = getBridgeIP() ? "Hue bridge (${getBridgeIP()})" : "Find bridges"
			href "bridgeDiscovery", title: title, description: "", state: selectedHue ? "complete" : "incomplete", params: [override: true]

		}
		if (state.initialized) {
        
			section {
				href "createScene", title: "Create a Scene", description: "Create A New Scene On Hue Hub", state: selectedHue ? "complete" : "incomplete" 

	    	}
			section {
				href "changeSceneName", title: "Change the Name of a Scene", description: "Change Scene Name On Hue Hub", state: selectedHue ? "complete" : "incomplete" 

	    	}
            section {
				href "modifyScene", title: "Modify a Scene", description: "Modify The Lights And / Or The Light Settings For An Existing Scene On Hue Hub", state: selectedHue ? "complete" : "incomplete" 

	    	}
			section {
				href "removeScene", title: "Delete a Scene", description: "Delete An Existing Scene From Hue Hub", state: selectedHue ? "complete" : "incomplete" 
	    	}

		}              
	}
}

def createScene(params=[:]) { 
    
	def theBulbs = state.bulbs
    def creBulbsNames = []
    def bulbID 
    def theBulbName     
   	theBulbs?.each {
       	bulbID = it.key
        theBulbName = state.bulbs[bulbID].name as String
	    creBulbsNames << [name: theBulbName, id:bulbID]        
    }		    
    
    state.creBulbNames = creBulbsNames
    log.trace "creBulbsNames = ${creBulbsNames}."

	def creTheLightsID = []
    if (creTheLights) {
			       	
		creTheLights.each { v ->
       		creBulbsNames.each { m ->
            	if (m.name == v) {
                	creTheLightsID << m.id // myBulbs.find{it.name == v}
        		}
            }
        }
	}
    
	if (creTheLightsID) {log.debug "The Selected Lights ${creTheLights} have ids of ${creTheLightsID}."}
        
    if (creTheLightsID && creSceneName) {

       	def body = [name: creSceneName, lights: creTheLightsID]

		log.debug "***************The body for createNewScene() will be ${body}."

    	if ( creSceneConfirmed == "Yes" ) { 
        	 
            // create new scene on Hue Hub 
	        createNewScene(body)
            
            // refresh page
			app.updateSetting("creSceneConfirmed", null)
			app.updateSetting("creSceneName", "")
			app.updateSetting("creTheLights", null)
            app.updateSetting("creTheLightsID", null)
			app.updateSetting("creScene", null)
            
            state.sChange = true 
        }
    }
        	
          

		return dynamicPage(name:"createScene", title:"Create Scene", nextPage:"sceneDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Choose Name for New Scene") {
				input "creSceneName", "text", title: "New Scene Name:", required: true, submitOnChange: true
        
       			if (creSceneName) {
	        	
					input "creTheLights", "enum", title: "Choose The Lights You Want In This Scene", required: true, multiple: true, submitOnChange: true, options:creBulbsNames.name.sort() 
				
                	if (creTheLights) {
                		
	                    paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY create a new scene on the Hue Hub called ${creSceneName} using the selected lights' current configuration." 
                
                		input "creSceneConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
                	} 
                }
			}
    	}
}



def modifyScene(params=[:]) {     
    
   	def theScenes = []
    theScenes = state.scenes 								// scenesDiscovered() ?: [] 
   
 	def modSceneOptions = []        
    def scID
    def theSceneName
	theScenes?.each {
       	scID = it.key
        theSceneName = state.scenes[scID].name as String
	    modSceneOptions << [name:theSceneName, id:scID]
   	}
    
    state.modSceneOptions = modSceneOptions
	log.trace "modSceneOptions = ${modSceneOptions}."

	def modSceneID
	if (modScene) {
 		state.modSceneOptions.each { m ->
    	    if (modScene == m.name) {
   	    		modSceneID = m.id
				log.debug "The selected scene ${modScene} has an id of ${modSceneID}."        
   			}
	    }		                
	}	

	def theBulbs = state.bulbs
    def modBulbsNames = []
    def bulbID 
    def theBulbName     
   	theBulbs?.each {
       	bulbID = it.key
        theBulbName = state.bulbs[bulbID].name as String
	    modBulbsNames << [name: theBulbName, id:bulbID]        
    }		    
    
    state.modBulbNames = modBulbsNames
    log.trace "modBulbsNames = ${modBulbsNames}."

	def modTheLightsID = []
    if (modTheLights) {
			       	
		modTheLights.each { v ->
       		modBulbsNames.each { m ->
            	if (m.name == v) {
                	modTheLightsID << m.id 
        		}
            }
        }
	}
    
	if (modTheLightsID) {log.debug "The Selected Lights ${modTheLights} have ids of ${modTheLightsID}."}
        
    if (modTheLightsID && modSceneID) {

       	def body = [sceneID: modSceneID]
        if (modSceneName) {body.name = modSceneName}
        if (modTheLightsID) {body.lights = modTheLightsID}

		log.debug "***************The body for updateScene() will be ${body}."

    	if ( modSceneConfirmed == "Yes" ) { 
        	                  
	        // update Scene lights and settings on Hue Hub
            updateScene(body)
            
            // update Scene lights on ST device
			def dni = app.id + "/" + modSceneID + "s"            
	        if (modTheLightsID) { sendEvent(dni, [name: "lights", value: modTheLightsID]) }
            
            // reset page
            
            app.updateSetting("modSceneConfirmed", null)
			app.updateSetting("modSceneName", "")
			app.updateSetting("modTheLights", null)           
            app.updateSetting("modScene", null)    
            // pause(300)
            // app.updateSetting("modSceneID", null)    
			// app.updateSetting("modTheLightsID", null)           
            
            state.sChange = true
        }
    }
        	
          

		return dynamicPage(name:"modifyScene", title:"Modify Scene", nextPage:"sceneDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Choose Scene to Modify") {
				input "modScene", "enum", title: "Modify Scene:", required: true, multiple: false, submitOnChange: true, options:modSceneOptions.name.sort() {it.value}
        
       			if (modScene) {
	        	
					input "modTheLights", "enum", title: "Choose The Lights You Want In This Scene (reflected within Hue Hub and on ST Scene device", required: true, multiple: true, submitOnChange: true, options:modBulbsNames.name.sort() 
				
                	if (modTheLights) {
                		input "modSceneName", "text", title: "Change Scene Name (Reflected ONLY within Hue Hub - ST only allows name / label change via mobile app or IDE ).", required: false, submitOnChange: true

	                    paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY set the ${modScene} scene to selected lights' current configuration." 
                
                		input "modSceneConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
                	} 
                }
			}
    	}
}

def changeSceneName(params=[:]) { 
    
    
   	def theScenes = []
    theScenes = state.scenes 				 
   
 	def renameSceneOptions = []        
    def scID
    def theSceneName
	theScenes?.each {
       	scID = it.key
        theSceneName = state.scenes[scID].name as String
	    renameSceneOptions << [name:theSceneName, id:scID]
   	}
    
    state.renameSceneOptions = renameSceneOptions
	log.trace "renameSceneOptions = ${renameSceneOptions}."

	def renameSceneID
	if (oldSceneName) {
 		state.renameSceneOptions.each { m ->
    	    if (oldSceneName == m.name) {
   	    		renameSceneID = m.id
				log.debug "The selected scene ${oldSceneName} has an id of ${renameSceneID}."        
   			}
	    }		                
	}	
        
    if (renameSceneID && newSceneName) {

       	def body = [sceneID: renameSceneID, name: newSceneName]

		log.debug "***************The body for renameScene() will be ${body}."

    	if ( renameSceneConfirmed == "Yes" ) { 
        	
            // rename scene on Hue Hub 
	        renameScene(body)

			// refresh page           
            app.updateSetting("renameSceneConfirmed", null)
			app.updateSetting("newSceneName", "")  
            app.updateSetting("oldSceneName", null)  
            app.updateSetting("renameSceneID", null)  
            
            state.sChange = true  
        }
    }
        	
          

		return dynamicPage(name:"changeSceneName", title:"Change Scene Name", nextPage:"sceneDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Change NAME of which Scene ") {
				input "oldSceneName", "enum", title: "Change Scene:", required: true, multiple: false, submitOnChange: true, options:renameSceneOptions.name.sort() {it.value}
        
       			if (oldSceneName) {
                
                	input "newSceneName", "text", title: "Change Scene Name to (click enter when done): ", required: false, submitOnChange: true
	        	
	                paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY CHANGE the name of ${oldSceneName} to ${newSceneName}!!!" 
                
                	input "renameSceneConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
				} 
                
			}
    	}
}





def removeScene(params=[:]) { 
    
    
   	def theScenes = []
    theScenes = state.scenes 								// scenesDiscovered() ?: [] 
   
 	def remSceneOptions = []        
    def scID
    def theSceneName
	theScenes?.each {
       	scID = it.key
        theSceneName = state.scenes[scID].name as String
	    remSceneOptions << [name:theSceneName, id:scID]
   	}
    
    state.remSceneOptions = remSceneOptions
	log.trace "remSceneOptions = ${remSceneOptions}."

	def remSceneID
	if (remScene) {
 		state.remSceneOptions.each { m ->
    	    if (remScene == m.name) {
   	    		remSceneID = m.id
				log.debug "The selected scene ${remScene} has an id of ${remSceneID}."        
   			}
	    }		                
	}	
        
    if (remSceneID) {

       	def body = [sceneID: remSceneID]

		log.debug "***************The body for deleteScene() will be ${body}."

    	if ( remSceneConfirmed == "Yes" ) { 
        	
            // delete scene on Hue Buh
	        deleteScene(body)
			log.trace "${remScene} found and deleted from Hue Hub."
            
            // try deleting child scene from ST
            def dni = app.id + "/" + renameSceneID + "s"
            try {
            	deleteChildDevice(dni)
                log.trace "${remScene} found and successfully deleted from ST."
			} catch (e) {
                log.debug "${remScene} not found within ST - no action taken."            	
			}
            
			// refresh page 
            app.updateSetting("remSceneConfirmed", null)
			app.updateSetting("remScene", null)            
			app.updateSetting("remSceneID", null)            
            
            state.sChange = true  
        }
    }
        	
          

		return dynamicPage(name:"removeScene", title:"Delete Scene", nextPage:"sceneDiscovery", refreshInterval:3, install:false, uninstall: false) {
			section("Choose Scene to DELETE") {
				input "remScene", "enum", title: "Delete Scene:", required: true, multiple: false, submitOnChange: true, options:remSceneOptions.name.sort() {it.value}
        
       			if (remScene) {
	        	
	                paragraph "ATTENTION: Clicking Yes below will IMMEDIATELY DELETE the ${remScene} scene FOREVER!!!" 
                
                	input "remSceneConfirmed", "enum", title: "Are you sure?", required: true, options: ["Yes", "No"], defaultValue: "No", submitOnChange: true
			            
				} 
                
			}
    	}
}



def initComplete(){
	if (state.initialized){
    	return "complete"
    } else {
    	return null
    }
}


def defaultTransition()
{
	int sceneRefreshCount = !state.sceneRefreshCount ? 0 : state.sceneRefreshCount as int
	state.sceneRefreshCount = sceneRefreshCount + 1
	def refreshInterval = 3

	return dynamicPage(name:"defaultTransition", title:"Default Transition", nextPage:"", refreshInterval:refreshInterval, install:true, uninstall: true) {
		section("Choose how long bulbs should take to transition between on/off and color changes. This can be modified per-device.") {
			input "selectedTransition", "number", required:true, title:"Transition Time (seconds)", value: 1
		}
	}
}






private discoverBridges() {
	sendHubCommand(new physicalgraph.device.HubAction("lan discovery urn:schemas-upnp-org:device:basic:1", physicalgraph.device.Protocol.LAN))
}

private sendDeveloperReq() {
	def token = app.id
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "POST",
		path: "/api",
		headers: [
			HOST: bridgeHostnameAndPort
		],
		body: [devicetype: "$token-0"]], bridgeDni))
}

private discoverHueBulbs() {
	log.trace "discoverHueBulbs REACHED"
    def host = getBridgeIP()
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/lights",
		headers: [
			HOST: bridgeHostnameAndPort
		]], bridgeDni))
}

private discoverHueGroups() {
	log.trace "discoverHueGroups REACHED"
    def host = getBridgeIP()
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/groups",
		headers: [
			HOST: bridgeHostnameAndPort
		]], "${selectedHue}"))
}

private discoverHueScenes() {
	log.trace "discoverHueScenes REACHED"
    def host = getBridgeIP()
    sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/api/${state.username}/scenes",
		headers: [
			HOST: bridgeHostnameAndPort
		]], "${selectedHue}"))
}

private verifyHueBridge(String deviceNetworkId) {
	log.trace "verifyHueBridge($deviceNetworkId)"
	sendHubCommand(new physicalgraph.device.HubAction([
		method: "GET",
		path: "/description.xml",
		headers: [
			HOST: ipAddressFromDni(deviceNetworkId)
		]], "${selectedHue}"))
}

private verifyHueBridges() {
	def devices = getHueBridges().findAll { it?.value?.verified != true }
	log.debug "UNVERIFIED BRIDGES!: $devices"
	devices.each {
		verifyHueBridge((it?.value?.ip + ":" + it?.value?.port))
	}
}

Map bridgesDiscovered() {
	def vbridges = getVerifiedHueBridges()
	def map = [:]
	vbridges.each {
		def value = "${it.value.name}"
		def key = it.value.ip + ":" + it.value.port
		map["${key}"] = value
	}
	map
}

Map bulbsDiscovered() {
	def bulbs =  getHueBulbs()
	def map = [:]
	if (bulbs instanceof java.util.Map) {
		bulbs.each {
			def value = "${it?.value?.name}"
			def key = app.id +"/"+ it?.value?.id
			map["${key}"] = value
		}
	} else { //backwards compatable
		bulbs.each {
			def value = "${it?.name}"
			def key = app.id +"/"+ it?.id
			map["${key}"] = value
		}
	}
	map
}

Map groupsDiscovered() {
	def groups =  getHueGroups()
	def map = [:]
	if (groups instanceof java.util.Map) {
		groups.each {
			def value = "${it?.value?.name}"
			def key = app.id +"/"+ it?.value?.id + "g"
			map["${key}"] = value
		}
	} else { //backwards compatable
		groups.each {
			def value = "${it?.name}"
			def key = app.id +"/"+ it?.id + "g"
			map["${key}"] = value
		}
	}
	map
}

Map scenesDiscovered() {
	def scenes =  getHueScenes()
	def map = [:]
	if (scenes instanceof java.util.Map) {
		scenes.each {
			def value = "${it?.value?.name}"
			def key = app.id +"/"+ it?.value?.id + "s"
			map["${key}"] = value
		}
	} else { //backwards compatable
		scenes.each {
			def value = "${it?.name}"
			def key = app.id +"/"+ it?.id + "s"
			map["${key}"] = value
		}
	}
	map
}

def getHueBulbs() {

	log.debug "HUE BULBS:"
	state.bulbs = state.bulbs ?: [:]  	// discoverHueBulbs() //
}

def getHueGroups() {

    log.debug "HUE GROUPS:"
	state.groups = state.groups ?: [:]		// discoverHueGroups() // 
}

def getHueScenes() {

    log.debug "HUE SCENES:"
	state.scenes = state.scenes ?: [:]		// discoverHueScenes()   //
}

def getHueBridges() {
	state.bridges = state.bridges ?: [:]
}

def getVerifiedHueBridges() {
	getHueBridges().findAll{ it?.value?.verified == true }
}

def installed() {
	log.trace "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.trace "Updated with settings: ${settings}"
	unsubscribe()
   unschedule()
	initialize()
}

def initialize() {
	// remove location subscription aftwards
	log.debug "INITIALIZE"
	state.initialized = true
    state.subscribe = false
	state.bridgeSelectedOverride = false

	if (selectedHue) {
		addBridge()
	}
	if (selectedBulbs) {
		addBulbs()
	}
	if (selectedGroups)
	{
		addGroups()
	}
    if (selectedScenes)
	{
		addScenes()
	}
/**	if (selectedHue) {
      def bridge = getChildDevice(selectedHue)
      subscribe(bridge, "bulbList", bulbListHandler)
      subscribe(bridge, "groupList", groupListHandler)
      subscribe(bridge, "sceneList", sceneListHandler)
   }
**/   
   runEvery5Minutes("doDeviceSync")
   doDeviceSync()
}

def manualRefresh() {
   unschedule()
   unsubscribe()
   doDeviceSync()
   runEvery5Minutes("doDeviceSync")
}

def uninstalled(){
	state.bridges = [:]
    state.username = null
}

// Handles events to add new bulbs
def bulbListHandler(evt) {
	def bulbs = [:]
	log.trace "Adding bulbs to state..."
	//state.bridgeProcessedLightList = true
	evt.jsonData.each { k,v ->
		log.trace "$k: $v"
		if (v instanceof Map) {
				bulbs[k] = [id: k, name: v.name, type: v.type, hub:evt.value]
		}
	}
	state.bulbs = bulbs
	log.info "${bulbs.size()} bulbs found"
}

def groupListHandler(evt) {
	def groups =[:]
	log.trace "Adding groups to state..."
	state.bridgeProcessedGroupList = true
	evt.jsonData.each { k,v ->
		log.trace "$k: $v"
		if (v instanceof Map) {
				groups[k] = [id: k, name: v.name, type: v.type, lights: v.lights, hub:evt.value]
		}
	}
	state.groups = groups
	log.info "${groups.size()} groups found"
}

def sceneListHandler(evt) {
	def scenes =[:]
	log.trace "Adding scenes to state..."
	state.bridgeProcessedSceneList = true
	evt.jsonData.each { k,v ->
		log.trace "$k: $v"
		if (v instanceof Map) {
				scenes[k] = [id: k, name: v.name, type: "Scene", lights: v.lights, hub:evt.value]
		}
	}
	state.scenes = scenes
	log.info "${scenes.size()} scenes found"
}

def addBulbs() {
	def bulbs = getHueBulbs()
	selectedBulbs.each { dni ->
		def d = getChildDevice(dni)
		if(!d) {
			def newHueBulb
			if (bulbs instanceof java.util.Map) {
				newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light")) {
					d = addChildDevice("info_fiend", "AP Hue Lux Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
					d.initialize(newHueBulb?.value.id)
				} else {
					d = addChildDevice("info_fiend", "AP Hue Bulb", dni, newHueBulb?.value.hub, ["label":newHueBulb?.value.name])
					d.initialize(newHueBulb?.value.id)
				}
				d.refresh()
			} else {
            	//backwards compatable
				newHueBulb = bulbs.find { (app.id + "/" + it.id) == dni }
				d = addChildDevice("info_fiend", "AP Hue Bulb", dni, newHueBulb?.hub, ["label":newHueBulb?.name])
				d.initialize(newHueBulb?.id)
			}

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		} else {
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
			if (bulbs instanceof java.util.Map) {
            	def newHueBulb = bulbs.find { (app.id + "/" + it.value.id) == dni }
				if (newHueBulb?.value?.type?.equalsIgnoreCase("Dimmable light") && d.typeName == "Hue Bulb") {
					d.setDeviceType("AP Hue Lux Bulb")
                    d.initialize(newHueBulb?.value.id)
				}
			}
		}
	}
}

def addGroups() {
	def groups = getHueGroups()
	selectedGroups.each { dni ->
		def d = getChildDevice(dni)
		if(!d)
		{
			def newHueGroup
			if (groups instanceof java.util.Map)
			{
				newHueGroup = groups.find { (app.id + "/" + it.value.id + "g") == dni }
				d = addChildDevice("info_fiend", "AP Hue Group", dni, newHueGroup?.value.hub, ["label":newHueGroup?.value.name, "groupID":newHueGroup?.value.id])
				d.initialize(newHueGroup?.value.id)
			}

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists, type: '$d.typeName'"
		}
	}
}

def addScenes() {
	def scenes = getHueScenes()
	selectedScenes.each { dni ->
		def d = getChildDevice(dni)
		if(!d)
		{
			def newHueScene
			if (scenes instanceof java.util.Map)
			{
				newHueScene = scenes.find { (app.id + "/" + it.value.id + "s") == dni }
				d = addChildDevice("info_fiend", "AP Hue Scene", dni, newHueScene?.value.hub, ["label":newHueScene?.value.name, "sceneID":newHueScene?.value.id])
			}

			log.debug "created ${d.displayName} with id $dni"
			d.refresh()
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists, type: 'Scene'"
		}
	}
}

def addBridge() {
	def vbridges = getVerifiedHueBridges()
	def vbridge = vbridges.find {(it.value.ip + ":" + it.value.port) == selectedHue}

	if(vbridge) {
		def d = getChildDevice(selectedHue)
		if(!d) {
			d = addChildDevice("info_fiend", "AP Hue Bridge", selectedHue, vbridge.value.hub, ["data":["mac": vbridge.value.mac]]) // ["preferences":["ip": vbridge.value.ip, "port":vbridge.value.port, "path":vbridge.value.ssdpPath, "term":vbridge.value.ssdpTerm]]

			log.debug "created ${d.displayName} with id ${d.deviceNetworkId}"

			sendEvent(d.deviceNetworkId, [name: "networkAddress", value: convertHexToIP(vbridge.value.ip) + ":" +  convertHexToInt(vbridge.value.port)])
			sendEvent(d.deviceNetworkId, [name: "serialNumber", value: vbridge.value.serialNumber])
		}
		else
		{
			log.debug "found ${d.displayName} with id $dni already exists"
		}
	}
}

def locationHandler(evt) {
	log.info "LOCATION HANDLER: $evt.description"
	def description = evt.description
	def hub = evt?.hubId

	def parsedEvent = parseEventMessage(description)
	parsedEvent << ["hub":hub]

	if (parsedEvent?.ssdpTerm?.contains("urn:schemas-upnp-org:device:basic:1"))
	{ //SSDP DISCOVERY EVENTS
		log.trace "SSDP DISCOVERY EVENTS"
		def bridges = getHueBridges()

		if (!(bridges."${parsedEvent.ssdpUSN.toString()}"))
		{ //bridge does not exist
			log.trace "Adding bridge ${parsedEvent.ssdpUSN}"
			bridges << ["${parsedEvent.ssdpUSN.toString()}":parsedEvent]
		}
		else
		{ // update the values

			log.debug "Device was already found in state..."

			def d = bridges."${parsedEvent.ssdpUSN.toString()}"
			def host = parsedEvent.ip + ":" + parsedEvent.port
			if(d.ip != parsedEvent.ip || d.port != parsedEvent.port || host != state.hostname) {

				log.debug "Device's port or ip changed..."
				state.hostname = host
				d.ip = parsedEvent.ip
				d.port = parsedEvent.port
				d.name = "Philips hue ($bridgeHostname)"

				app.updateSetting("selectedHue", host)

				childDevices.each {
					if (it.getDeviceDataByName("mac") == parsedEvent.mac) {
						log.debug "updating dni for device ${it} with mac ${parsedEvent.mac}"
						it.setDeviceNetworkId((parsedEvent.ip + ":" + parsedEvent.port)) //could error if device with same dni already exists
                  doDeviceSync()
					}
				}
			}
		}
	}
	else if (parsedEvent.headers && parsedEvent.body)
	{ // HUE BRIDGE RESPONSES
		log.trace "HUE BRIDGE RESPONSES"
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		def type = (headerString =~ /Content-type:.*/) ? (headerString =~ /Content-type:.*/)[0] : null
		def body

		if (type?.contains("xml"))
		{ // description.xml response (application/xml)
			body = new XmlSlurper().parseText(bodyString)

			if (body?.device?.modelName?.text().startsWith("Philips hue bridge"))
			{
				def bridges = getHueBridges()
				def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
				if (bridge)
				{
					bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
				}
				else
				{
					log.error "/description.xml returned a bridge that didn't exist"
				}
			}
		}
		else if(type?.contains("json") && isValidSource(parsedEvent.mac))
		{ //(application/json)
			body = new groovy.json.JsonSlurper().parseText(bodyString)

			if (body?.success != null)
			{ //POST /api response (application/json)
				if (body?.success?.username)
				{
					state.username = body.success.username[0]
					state.hostname = selectedHue
				}
			}
			else if (body.error != null)
			{
				//TODO: handle retries...
				log.error "ERROR: application/json ${body.error}"
			}
			else
			{ //GET /api/${state.username}/lights response (application/json)
            	log.debug "HERE"
				if (!body.action)
				{ //check if first time poll made it here by mistake

                	if(!body?.type?.equalsIgnoreCase("LightGroup") || !body?.type?.equalsIgnoreCase("Room"))
					{
						log.debug "LIGHT GROUP!!!"
					}

					def bulbs = getHueBulbs()
					def groups = getHueGroups()
                    def scenes = getHueScenes()

					log.debug "Adding bulbs, groups, and scenes to state!"
					body.each { k,v ->
                    	log.debug v.type
						if(v.type == "LightGroup" || v.type == "Room")
						{
							groups[k] = [id: k, name: v.name, type: v.type, hub:parsedEvent.hub]
						}
						else if (v.type == "Extended color light" || v.type == "Color light" || v.type == "Dimmable light" )
						{
							bulbs[k] = [id: k, name: v.name, type: v.type, hub:parsedEvent.hub]
						}
                        else
                        {
                        	scenes[k] = [id: k, name: v.name, type: "Scene", hub:parsedEvent.hub]
                        }
					}
				}
			}
		}
	}
	else {
		log.trace "NON-HUE EVENT $evt.description"
	}
}

private def parseEventMessage(Map event) {
	//handles bridge attribute events
	return event
}

private def parseEventMessage(String description) {
	def event = [:]
	def parts = description.split(',')
	parts.each { part ->
		part = part.trim()
		if (part.startsWith('devicetype:')) {
			def valueString = part.split(":")[1].trim()
			event.devicetype = valueString
		}
		else if (part.startsWith('mac:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.mac = valueString
			}
		}
		else if (part.startsWith('networkAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ip = valueString
			}
		}
		else if (part.startsWith('deviceAddress:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.port = valueString
			}
		}
		else if (part.startsWith('ssdpPath:')) {
			def valueString = part.split(":")[1].trim()
			if (valueString) {
				event.ssdpPath = valueString
			}
		}
		else if (part.startsWith('ssdpUSN:')) {
			part -= "ssdpUSN:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpUSN = valueString
			}
		}
		else if (part.startsWith('ssdpTerm:')) {
			part -= "ssdpTerm:"
			def valueString = part.trim()
			if (valueString) {
				event.ssdpTerm = valueString
			}
		}
		else if (part.startsWith('headers')) {
			part -= "headers:"
			def valueString = part.trim()
			if (valueString) {
				event.headers = valueString
			}
		}
		else if (part.startsWith('body')) {
			part -= "body:"
			def valueString = part.trim()
			if (valueString) {
				event.body = valueString
			}
		}
	}

	event
}

def doDeviceSync(){
	log.trace "Doing Hue Device Sync!"

	//shrink the large bulb lists
	convertBulbListToMap()
	convertGroupListToMap()
	convertSceneListToMap()

   poll()
   try {
		subscribe(location, null, locationHandler, [filterEvents:false])
   } catch (all) {
      log.trace "Subscription already exist"
   }
	discoverBridges()
}

def isValidSource(macAddress) {
	def vbridges = getVerifiedHueBridges()
	return (vbridges?.find {"${it.value.mac}" == macAddress}) != null
}

/////////////////////////////////////
//CHILD DEVICE METHODS
/////////////////////////////////////

def parse(childDevice, description) {
	def parsedEvent = parseEventMessage(description)

	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		childDevice?.log "parse() - ${bodyString}"
		def body = new groovy.json.JsonSlurper().parseText(bodyString)
//		childDevice?.log "BODY - $body"
        
		if (body instanceof java.util.HashMap) {   // POLL RESPONSE
		  
			def devices = getChildDevices() 
            
            // BULBS
            for (bulb in body) {
                def d = devices.find{it.deviceNetworkId == "${app.id}/${bulb.key}"}
                 if (d) {
                 	if (bulb.value.type == "Extended color light" || bulb.value.type == "Color light" || bulb.value.type == "Dimmable light") {
	                		log.trace "Reading Poll for Lights"
		                    if (bulb.value.state.reachable) {
								sendEvent(d.deviceNetworkId, [name: "switch", value: bulb.value?.state?.on ? "on" : "off"])
		                        sendEvent(d.deviceNetworkId, [name: "level", value: Math.round(bulb.value?.state?.bri * 100 / 255)])
		                        if (bulb.value.state.sat) {
		                        	def hue = Math.min(Math.round(bulb.value?.state?.hue * 100 / 65535), 65535) as int
		                            def sat = Math.round(bulb.value?.state?.sat * 100 / 255) as int
		                            def hex = colorUtil.hslToHex(hue, sat)
		                            sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                    sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                    sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
		                        }
                                    if (bulb.value.state.ct) {
                                    	def ct = mireksToKelvin(bulb.value?.state?.ct) as int
                                        sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: ct])
                                    }
                                    if (bulb.value.state.effect) { sendEvent(d.deviceNetworkId, [name: "effect", value: bulb.value?.state?.effect]) }
									if (bulb.value.state.colormode) { sendEvent(d.deviceNetworkId, [name: "colormode", value: bulb.value?.state?.colormode]) }
                                    
		                        } else {		// Bulb not reachable
		                            sendEvent(d.deviceNetworkId, [name: "switch", value: "off"])
		                            sendEvent(d.deviceNetworkId, [name: "level", value: 100])
		                            def hue = 23
		                            def sat = 56
		                            def hex = colorUtil.hslToHex(23, 56)
                                    sendEvent(d.deviceNetworkId, [name: "color", value: hex])
                                    sendEvent(d.deviceNetworkId, [name: "hue", value: hue])
                                    sendEvent(d.deviceNetworkId, [name: "saturation", value: sat])
		                            def ct = 2710
                                    sendEvent(d.deviceNetworkId, [name: "colorTemperature", value: ct])
                                    sendEvent(d.deviceNetworkId, [name: "effect", value: "none"]) 
                                    sendEvent(d.deviceNetworkId, [name: "colormode", value: hs] )
		                    	}
		            }
	            }	            
            }    

//	        devices = getChildDevices()

			// GROUPS
            for (bulb in body) {
                def g = devices.find{it.deviceNetworkId == "${app.id}/${bulb.key}g"}
                if (g) {
	                if(bulb.value.type == "LightGroup" || bulb.value.type == "Room" || bulb.value.type == "Luminaire" || bulb.value.type == "Lightsource" ) {
                		log.trace "Reading Poll for Groups"
                        
                        sendEvent(g.deviceNetworkId, [name: "name", value: bulb.value?.name ])
						sendEvent(g.deviceNetworkId, [name: "switch", value: bulb.value?.action?.on ? "on" : "off"])
                  		sendEvent(g.deviceNetworkId, [name: "level", value: Math.round(bulb.value?.action?.bri * 100 / 255)])
                        sendEvent(g.deviceNetworkId, [name: "lights", value: bulb.value?.lights ])
                        
                        if (bulb.value.action.sat) {
                            def hue = Math.min(Math.round(bulb.value?.action?.hue * 100 / 65535), 65535) as int
                            def sat = Math.round(bulb.value?.action?.sat * 100 / 255) as int
                            def hex = colorUtil.hslToHex(hue, sat)
                            sendEvent(g.deviceNetworkId, [name: "color", value: hex])
                            sendEvent(g.deviceNetworkId, [name: "hue", value: hue])
                            sendEvent(g.deviceNetworkId, [name: "saturation", value: sat])
                        }
                        
                        if (bulb.value.action.ct) {
                             def ct = mireksToKelvin(bulb.value?.action?.ct) as int
                             sendEvent(g.deviceNetworkId, [name: "colorTemperature", value: ct])
                        }
                        
                        if (bulb.value.action.effect) { sendEvent(g.deviceNetworkId, [name: "effect", value: bulb.value?.action?.effect]) }
						if (bulb.value.action.alert) { sendEvent(g.deviceNetworkId, [name: "alert", value: bulb.value?.action?.alert]) }
                        if (bulb.value.action.transitiontime) { sendEvent(g.deviceNetworkId, [name: "transitiontime", value: bulb.value?.action?.transitiontime ?: 0]) }
						if (bulb.value.action.colormode) { sendEvent(g.deviceNetworkId, [name: "colormode", value: bulb.value?.action?.colormode]) }
                   }
               }         
           }
           
		// SCENES
          for (bulb in body) {
        	def sc = devices.find{it.deviceNetworkId == "${app.id}/${bulb.key}s"}    
            if (sc) {
	           	if ( !bulb.value.type || bulb.value.type == "Scene" || bulb.value.recycle ) {
                	log.trace "Reading Poll for Scene"
                	sendEvent(sc.deviceNetworkId, [ name: "name", value: bulb.value?.name ])
                  	sendEvent(sc.deviceNetworkId, [ name: "lights", value: bulb.value?.lights ])
               	}
	        }
   		  }
          
        } else { 		// PUT RESPONSE
			def hsl = [:]
			body.each { payload ->
				childDevice?.log $payload
				if (payload?.success) {

					def childDeviceNetworkId = app.id + "/"
					def eventType
					body?.success[0].each { k,v ->
						log.trace "********************************************************"
						log.trace "********************************************************"
						if (k.split("/")[1] == "groups") {
							childDeviceNetworkId += k.split("/")[2] + "g"
						} else if (k.split("/")[1] == "scenes") {
							childDeviceNetworkId += k.split("/")[2] + "s"                        
                        } else {
							childDeviceNetworkId += k.split("/")[2]
						}
                        
						if (!hsl[childDeviceNetworkId]) hsl[childDeviceNetworkId] = [:]
						
                        eventType = k.split("/")[4]
						childDevice?.log "eventType: $eventType"
						switch(eventType) {
							case "on":
								sendEvent(childDeviceNetworkId, [name: "switch", value: (v == true) ? "on" : "off"])
								break
							case "bri":
								sendEvent(childDeviceNetworkId, [name: "level", value: Math.round(v * 100 / 255)])
								break
							case "sat":
								hsl[childDeviceNetworkId].saturation = Math.round(v * 100 / 255) as int
								break
							case "hue":
								hsl[childDeviceNetworkId].hue = Math.min(Math.round(v * 100 / 65535), 65535) as int
								break
                            case "ct":
                           		sendEvent(childDeviceNetworkId, [name: "colorTemperature", value: mireksToKelvin(v)])
                               	break
	                        case "effect":
    	                       	sendEvent(childDeviceNetworkId, [name: "effect", value: v])
        	                    break
							case "colormode":
								sendEvent(childDeviceNetworkId, [name: "colormode", value: v])
								break
                            case "lights":
								sendEvent(childDeviceNetworkId, [name: "lights", value: v])
								break
                            case "transitiontime":
                                sendEvent(childDeviceNetworkId, [name: "transitiontime", value: v ]) // ?: getSelectedTransition()
      	                        break    
						}
					}

				} else if (payload.error) {
					childDevice?.error "JSON error - ${body?.error}"
				}

			}

			hsl.each { childDeviceNetworkId, hueSat ->
				if (hueSat.hue && hueSat.saturation) {
					def hex = colorUtil.hslToHex(hueSat.hue, hueSat.saturation)
					childDevice?.log "sending ${hueSat} for ${childDeviceNetworkId} as ${hex}"
					sendEvent(hsl.childDeviceNetworkId, [name: "color", value: hex])
				}
			}
		}
	} else {   // SOME OTHER RESPONSE
		childDevice?.log "parse - got something other than headers,body..."
		return []
	}	
}


def hubVerification(bodytext) {
	childDevice?.trace "Bridge sent back description.xml for verification"
    def body = new XmlSlurper().parseText(bodytext)
    if (body?.device?.modelName?.text().startsWith("Philips hue bridge")) {
        def bridges = getHueBridges()
        def bridge = bridges.find {it?.key?.contains(body?.device?.UDN?.text())}
        if (bridge) {
            bridge.value << [name:body?.device?.friendlyName?.text(), serialNumber:body?.device?.serialNumber?.text(), verified: true]
        } else {
            childDevice?.error "/description.xml returned a bridge that didn't exist"
        }
    }
}
def setTransitionTime ( childDevice, transitionTime, deviceType ) {
	childDevice?.log "HLGS:  Executing 'setTransitionTime'"
    def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = "groups" 
        deviceID = deviceID - "g"
    }
	def path = dType + "/" + deviceID + "/" + api
    childDevice?.log "HLGS: 'transitionTime' path is $path"
    
	def value = [transitiontime: transitionTime * 10]

	childDevice?.log "HLGS: sending ${value}."	
	put( path, value )

}

def on( childDevice, percent, transitionTime, deviceType ) {
	childDevice?.log "HLGS:  Executing 'on'"
    def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = "groups" 
        deviceID = deviceID - "g"
    }
  	def level = Math.min(Math.round(percent * 255 / 100), 255)
    
	def path = dType + "/" + deviceID + "/" + api
    childDevice?.log "HLGS: 'on' path is $path"
    
	def value = [on: true, bri: level, transitiontime: transitionTime * 10 ]

	childDevice?.log "HLGS:  sending 'on' using ${value}."	
	put( path, value )
}

def off( childDevice, transitionTime, deviceType ) {
	childDevice?.log "HLGS:  Executing 'off'"
    def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = "groups" 
        deviceID = deviceID - "g"
    }
	def path = dType + "/" + deviceID + "/" + api
    childDevice?.log "HLGS:  'off' path is ${path}."	
	def value = [on: false, transitiontime: transitionTime * 10 ]
	childDevice?.log "HLGS:  sending 'off' using ${value}."	

	put( path, value )
}

def setLevel( childDevice, percent, transitionTime, deviceType ) {
	def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = deviceType
        deviceID = deviceID - "g"
    }
  	def level = Math.min(Math.round(percent * 255 / 100), 255)
	def value = [bri: level, on: percent > 0, transitiontime: transitionTime * 10 ]		
   	def path = dType + "/" + deviceID + "/" + api
	childDevice?.log "HLGS: 'on' path is $path"
   	childDevice?.log "HLGS:  Executing 'setLevel($percent).'"
	put( path, value)
}

def setSaturation(childDevice, percent, transitionTime, deviceType) {
	def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    
    if(deviceType == "groups") { 
    	api = "action"
        dType = deviceType
        deviceID = deviceID - "g"
    }
   def path = dType + "/" + deviceID + "/" + api
    
	def level = Math.min(Math.round(percent * 255 / 100), 255)
    
   	childDevice?.log "HLGS:  Executing 'setSaturation($percent).'"
	put( path, [sat: level, transitiontime: transitionTime * 10 ])
}

def setHue(childDevice, percent, transitionTime, deviceType ) {
	def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = "groups" 
        deviceID = deviceID - "g"
    }

	def path = dType + "/" + deviceID + "/" + api

	childDevice?.log "HLGS: Executing 'setHue($percent)'"
	def level =	Math.min(Math.round(percent * 65535 / 100), 65535)
	put( path, [hue: level, transitiontime: transitionTime * 10 ])
}

def setColorTemperature(childDevice, huesettings, transitionTime, deviceType ) {
	def api = "state" 
    def dType = "lights" 
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = "groups" 
        deviceID = deviceID - "g"
    }
    
  	def path = dType + "/" + deviceID + "/" + api
    
	childDevice?.log "HLGS: Executing 'setColorTemperature($huesettings)'"
	def value = [on: true, ct: kelvinToMireks(huesettings), transitiontime: transitionTime * 10 ]
    
	put( path, value )
}

def setColor(childDevice, huesettings, transitionTime, deviceType ) {
	def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = deviceType
        deviceID = deviceID - "g"
    }
   	def path = dType + "/" + deviceID + "/" + api
    
	def value = [:]
	def hue = null
    def sat = null
    def xy = null

    if (huesettings.hex != null) {
        value.xy = getHextoXY(huesettings.hex)
    } else {
        if (huesettings.hue != null)
            value.hue = Math.min(Math.round(huesettings.hue * 65535 / 100), 65535)
        if (huesettings.saturation != null)
            value.sat = Math.min(Math.round(huesettings.saturation * 255 / 100), 255)
    }
   
	if (huesettings.level > 0 || huesettings.switch == "on") { 
    	value.on = true
    } else if (huesettings.switch == "off") { 
    	value.on = false
    }
    value.transitiontime = transitionTime * 10 
    value.bri = Math.min(Math.round(huesettings.level * 255 / 100), 255) 	
	value.alert = huesettings.alert ? huesettings.alert : "none"

	childDevice?.log "HLGS: Executing 'setColor($value).'"
	put( path, value)

//	return "Color set to $value"
}

def setGroupScene(childDevice, Number inGroupID) {
	childDevice?.log "HLGS: Executing setGroupScene with inGroupID of ${inGroupID}." 
	def sceneID = getId(childDevice) // - "s"
    def groupID = inGroupID ?: "0"
	childDevice?.log "HLGS: setGroupScene scene is ${sceneID} "
    String path = "groups/${groupID}/action/"

	childDevice?.log "Path = ${path} "

	put( path, [scene: sceneID]) 
}

def setToGroup(childDevice, Number inGroupID ) {
	childDevice?.log "HLGS: setToGroup with inGroupID of ${inGroupID}." 
	def sceneID = getId(childDevice) - "s"
    def groupID = inGroupID ?: "0"

	childDevice?.log "HLGS: setToGroup: sceneID = ${sceneID} "
    String gPath = "groups/${groupID}/action/"

	childDevice?.log "Group path = ${gPath} "

	put( gPath, [scene: sceneID])
}

/**
def nextLevel(childDevice) {
	def level = device.latestValue("level") as Integer ?: 0
	if (level < 100) {
		level = Math.min(25 * (Math.round(level / 25) + 1), 100) as Integer
	} else { level = 25	}
	setLevel(childDevice, level)
}
**/

def getId(childDevice) {
	childDevice?.log "HLGS: Executing getId"
	if (childDevice.device?.deviceNetworkId?.startsWith("Hue") || childDevice.device?.deviceNetworkId?.startsWith("AP Hue") ) {
		childDevice?.log "Device ID returned is ${childDevice.device?.deviceNetworkId[3..-1]}."
		return childDevice.device?.deviceNetworkId[3..-1]
	} else {
		childDevice?.log "Device ID returned (based on SPLIT '/') is ${childDevice.device?.deviceNetworkId.split("/")[-1]}."    
		return childDevice.device?.deviceNetworkId.split("/")[-1]
	}
}


def updateSceneFromDevice(childDevice) {
	childDevice?.log "HLGS: updateSceneFromDevice: Scene ${childDevice} requests scene use current light states."
	def sceneID = getId(childDevice) - "s"

	childDevice?.log "HLGS: updateScene: sceneID = ${sceneID} "
    String path = "scenes/${sceneID}/"

	def value = [storelightstate: true]
	childDevice?.log "Path = ${path} "

	put( path, value )
    
}

def updateScene(body) {
	log.trace "HLGS: updateScene "
	def sceneID = body.sceneID

    String path = "scenes/${sceneID}/"
	def value = [lights: body.lights, storelightstate: true]
    
    if (body.name) {
    	value.name = body.name
	}
    
	log.trace "HLGS: updateScene:  Path = ${path} & body = ${value}"

	put( path, value )
    
	app.updateSetting("modSceneConfirmed", null)
	app.updateSetting("modSceneName", "")
	app.updateSetting("modTheLights", [])
	app.updateSetting("modScene", null)
    state.updateScene == null
//    state.scenes = []

}

def renameScene(body) {
	log.trace "HLGS: renameScene "
	def sceneID = body.sceneID

    String path = "scenes/${sceneID}/"
	    
    def value = body.name
	
    
		log.trace "HLGS: renameScene:  Path = ${path} & body = ${value}"

		put( path, value )
    
		app.updateSetting("renameSceneConfirmed", null)
		app.updateSetting("renameSceneName", "")
		app.updateSetting("renameScene", null)
	    state.renameScene == null
    
//    state.scenes = []
}



def deleteScene(body) {
	log.trace "HLGS: deleteScene "
	def host = getBridgeIP()


	def sceneID = body.sceneID
    String path = "scenes/${sceneID}/"
	def uri = "/api/${state.username}/$path"

	log.trace "HLGS: deleteScene:  uri =  $uri"


	sendHubCommand(new physicalgraph.device.HubAction([
		method: "DELETE",
		path: uri,
		headers: [
			HOST: host
		]
    ],"${selectedHue}"))

	app.updateSetting("remSceneConfirmed", null)
	app.updateSetting("remScene", null)
    state.deleteScene == null 
//    state.scenes = []
    
}

def createNewScene(body) {
	log.trace "HLGS: createNewScene "
	def host = getBridgeIP()

    String path = "scenes/"
	def uri = "/api/${state.username}/$path"

	def newBody = [name: body.name, lights: body.lights]
    
	def bodyJSON = new groovy.json.JsonBuilder(newBody).toString()

	log.trace "HLGS: createNewScene:  POST:  $uri"
    log.trace "HLGS: createNewScene:  BODY: $bodyJSON"


	sendHubCommand(new physicalgraph.device.HubAction([
		method: "POST",
		path: uri,
		headers: [
			HOST: host
		], body: bodyJSON],"${selectedHue}"))
        
	app.updateSetting("creSceneConfirmed", null)
	app.updateSetting("creSceneName", "")
	app.updateSetting("creTheLights", [])
	app.updateSetting("creScene", null)
    state.createScene == null 
//  state.scenes = []

}

def updateGroup(body) {
	log.trace "HLGS: updateGroup "
	def groupID = body.groupID

    String path = "groups/${groupID}/"
	def value = [lights: body.lights]
    
    if (body.name) {
    	value.name = body.name
	}

	log.trace "HLGS: updateGroup:  Path = ${path} & body = ${value}"

	put( path, value )
    
	app.updateSetting("modGroupConfirmed", null)
	app.updateSetting("modGroupName", "")
	app.updateSetting("modTheLights", [])
	app.updateSetting("modGroup", null)
    state.updateGroup == null
//    state.groups = []
}

def renameGroup(body) {
	log.trace "HLGS: renameGroup "
	def groupID = body.groupID

    String path = "groups/${groupID}/"
	def value = body.name
	
    
		log.trace "HLGS: renameGroup:  Path = ${path} & body = ${value}"

		put( path, value )
    
		app.updateSetting("renameGroupConfirmed", null)
		app.updateSetting("renameGroupName", "")
		app.updateSetting("renameGroup", null)
	    state.renameGroup == null
    
//    state.groups = []
}


def deleteGroup(body) {
	log.trace "HLGS: deleteGroup "
	def host = getBridgeIP()


	def groupID = body.groupID
    String path = "groups/${groupID}/"
	def uri = "/api/${state.username}/$path"

	log.trace "HLGS: deleteGroup:  uri =  $uri"


	sendHubCommand(new physicalgraph.device.HubAction([
		method: "DELETE",
		path: uri,
		headers: [
			HOST: host
		]
    ],"${selectedHue}"))

	app.updateSetting("remGroupConfirmed", null)
	app.updateSetting("remGroup", null)
    state.deleteGroup == null        
//    state.groups = []    
}

def createNewGroup(body) {
	log.trace "HLGS: createNewGroup "
	def host = getBridgeIP()

    String path = "groups/"
	def uri = "/api/${state.username}/$path"

	body.type = "LightGroup"
	def bodyJSON = new groovy.json.JsonBuilder(body).toString()

	log.trace "HLGS: createNewGroup:  POST:  $uri"
    log.trace "HLGS: createNewGroup:  BODY: $bodyJSON"


	sendHubCommand(new physicalgraph.device.HubAction([
		method: "POST",
		path: uri,
		headers: [
			HOST: host
		], body: bodyJSON],"${selectedHue}"))
        
	app.updateSetting("creGroupConfirmed", null)
	app.updateSetting("creGroupName", "")
	app.updateSetting("creTheLights", [])
	app.updateSetting("creGroup", null)
    state.createGroup == null        
//    state.groups = []
    
}


private poll() {
   def host = getBridgeIP()

   def uris = ["/api/${state.username}/lights/", "/api/${state.username}/groups/", "/api/${state.username}/scenes/"]
   for (uri in uris) {
      try {
         sendHubCommand(new physicalgraph.device.HubAction("""GET ${uri} HTTP/1.1
         HOST: ${host}
         """, physicalgraph.device.Protocol.LAN, selectedHue))
      } catch (all) {
         log.warn "Parsing Body failed - trying again..."
         doDeviceSync()
      }
   }
}



private put(path, body) {
	childDevice?.log "HLGS: put: path = ${path}."
	def host = getBridgeIP()
	def uri = "/api/${state.username}/$path"  // "lights"
	
    if ( path.startsWith("groups") || path.startsWith("scenes")) {
		uri = "/api/${state.username}/$path"[0..-1]
	}
    
//    if (path.startsWith("scenes")) {
//		uri = "/api/${state.username}/$path"[0..-1]
//	}

	def bodyJSON = new groovy.json.JsonBuilder(body).toString()

	childDevice?.log "PUT:  $uri"
	childDevice?.log "BODY: $bodyJSON"  // ${body} ?


	sendHubCommand(new physicalgraph.device.HubAction([
		method: "PUT",
		path: uri,
		headers: [
			HOST: host
		],
		body: bodyJSON], "${selectedHue}"))

}

def getBridgeDni() {
	state.hostname
}

def getBridgeHostname() {
	def dni = state.hostname
	if (dni) {
		def segs = dni.split(":")
		convertHexToIP(segs[0])
	} else {
		null
	}
}

def getBridgeHostnameAndPort() {
	def result = null
	def dni = state.hostname
	if (dni) {
		def segs = dni.split(":")
		result = convertHexToIP(segs[0]) + ":" +  convertHexToInt(segs[1])
	}
	log.trace "result = $result"
	result
}

private getBridgeIP() {
	def host = null
	if (selectedHue) {
        def d = getChildDevice(selectedHue)
    	if (d) {
        	if (d.getDeviceDataByName("networkAddress"))
            	host = d.getDeviceDataByName("networkAddress")
            else
        		host = d.latestState('networkAddress').stringValue
        }
        if (host == null || host == "") {
            def serialNumber = selectedHue
            def bridge = getHueBridges().find { it?.value?.serialNumber?.equalsIgnoreCase(serialNumber) }?.value
            if (!bridge) {
            	bridge = getHueBridges().find { it?.value?.mac?.equalsIgnoreCase(serialNumber) }?.value
            }
            if (bridge?.ip && bridge?.port) {
            	if (bridge?.ip.contains("."))
            		host = "${bridge?.ip}:${bridge?.port}"
                else
                	host = "${convertHexToIP(bridge?.ip)}:${convertHexToInt(bridge?.port)}"
            } else if (bridge?.networkAddress && bridge?.deviceAddress)
            	host = "${convertHexToIP(bridge?.networkAddress)}:${convertHexToInt(bridge?.deviceAddress)}"
        }
        log.trace "Bridge: $selectedHue - Host: $host"
    }
    return host
}

private getHextoXY(String colorStr) {
    // For the hue bulb the corners of the triangle are:
    // -Red: 0.675, 0.322
    // -Green: 0.4091, 0.518
    // -Blue: 0.167, 0.04

    def cred = Integer.valueOf( colorStr.substring( 1, 3 ), 16 )
    def cgreen = Integer.valueOf( colorStr.substring( 3, 5 ), 16 )
    def cblue = Integer.valueOf( colorStr.substring( 5, 7 ), 16 )

    double[] normalizedToOne = new double[3];
    normalizedToOne[0] = (cred / 255);
    normalizedToOne[1] = (cgreen / 255);
    normalizedToOne[2] = (cblue / 255);
    float red, green, blue;

    // Make red more vivid
    if (normalizedToOne[0] > 0.04045) {
       red = (float) Math.pow(
                (normalizedToOne[0] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        red = (float) (normalizedToOne[0] / 12.92);
    }

    // Make green more vivid
    if (normalizedToOne[1] > 0.04045) {
        green = (float) Math.pow((normalizedToOne[1] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        green = (float) (normalizedToOne[1] / 12.92);
    }

    // Make blue more vivid
    if (normalizedToOne[2] > 0.04045) {
        blue = (float) Math.pow((normalizedToOne[2] + 0.055) / (1.0 + 0.055), 2.4);
    } else {
        blue = (float) (normalizedToOne[2] / 12.92);
    }

    float X = (float) (red * 0.649926 + green * 0.103455 + blue * 0.197109);
    float Y = (float) (red * 0.234327 + green * 0.743075 + blue * 0.022598);
    float Z = (float) (red * 0.0000000 + green * 0.053077 + blue * 1.035763);

    float x = (X != 0 ? X / (X + Y + Z) : 0);
    float y = (Y != 0 ? Y / (X + Y + Z) : 0);

    double[] xy = new double[2];
    xy[0] = x;
    xy[1] = y;
    return xy;
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

def convertBulbListToMap() {
	log.debug "CONVERT BULB LIST"
    try {
		if (state.bulbs instanceof java.util.List) {
			def map = [:]
			state.bulbs.unique {it.id}.each { bulb ->
				map << ["${bulb.id}":["id":bulb.id, "name":bulb.name, "type": bulb.type, "hub":bulb.hub]]
			}
			state.bulbs = map
		}
	} catch(Exception e) {
		log.error "Caught error attempting to convert bulb list to map: $e"
	}
}

def convertGroupListToMap() {
	log.debug "CONVERT GROUP LIST"
	try {
		if (state.groups instanceof java.util.List) {
			def map = [:]
			state.groups.unique {it.id}.each { group ->
				map << ["${group.id}g":["id":group.id+"g", "name":group.name, "type": group.type, "lights": group.lights, "hub":group.hub]]
			}
			state.group = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert group list to map: $e"
	}
}

def convertSceneListToMap() {
	log.debug "CONVERT SCENE LIST"
	try {
		if (state.scenes instanceof java.util.List) {
			def map = [:]
			state.scenes.unique {it.id}.each { scene ->
				map << ["${scene.id}s":["id":scene.id+"s", "name":scene.name, "type": group.type, "lights": group.lights, "hub":scene.hub]]
			}
			state.scene = map
		}
	}
	catch(Exception e) {
		log.error "Caught error attempting to convert scene list to map: $e"
	}
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

def ipAddressFromDni(dni) {
	if (dni) {
		def segs = dni.split(":")
		convertHexToIP(segs[0]) + ":" +  convertHexToInt(segs[1])
	} else {
		null
	}
}

def getSelectedTransition() {
	return settings.selectedTransition
}

def setAlert(childDevice, effect, transitionTime, deviceType ) {
	def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = deviceType
        deviceID = deviceID - "g"
    }
   	def path = dType + "/" + deviceID + "/" + api


	if(effect != "none" && effect != "select" && effect != "lselect") { childDevice?.log "Invalid alert value!" }
    else {
		def value = [alert: effect, transitiontime: transitionTime * 10 ]
		childDevice?.log "setAlert: Alert ${effect}."
		put( path, value )
	}
}

def setEffect(childDevice, effect, transitionTime, deviceType) {
	def api = "state" 
    def dType = "lights"
    def deviceID = getId(childDevice) 
    if(deviceType == "groups") { 
    	api = "action"
        dType = deviceType
        deviceID = deviceID - "g"
    }
   	def path = dType + "/" + deviceID + "/" + api


	def value = [effect: effect, transitiontime: transitionTime * 10 ]
	childDevice?.log "setEffect: Effect ${effect}."
	put( path, value )
}


int kelvinToMireks(kelvin) {
	return 1000000 / kelvin //https://en.wikipedia.org/wiki/Mired
}

int mireksToKelvin(mireks) {
	return 1000000 / mireks //https://en.wikipedia.org/wiki/Mired
}
