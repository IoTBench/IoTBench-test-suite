 /**
 *  There are ways to trick static analysis, one method is reflection through strings
 *  The example code block shows how static analysis system may fail (over-approximate)
 *  Author: Z. Berkay Celik
 *  Reflection through string
 *  Email: zbc102@cse.psu.edu
 */

definition(
    name: "Soteria",
    namespace: "Soteria",
    author: "IoTBench",
    description: "When there is smoke alarm goes off",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_outlet@2x.png")

 def initialize() {
     log.debug "initialize configured"
     subscribe(presenceSensor, "present", presenceChanged) 
     subscribe(presenceSensor, "not present", presenceChanged) 
 }
 
 def presenceChanged(evt){
     String s = ""
     if (evt.value == "not present") {
       s = "closeAll"
     }else{
       s = "openAll"
     }
   performAction(s)
 }

 //Soteria cannot analyze, string analysis is required or run-time instrumentation is required.
 def performAction(String f){
     log.debug "Executing $f at ${new Date()}"
     "$f()"  // reflection  object."${mystring}"()
 }

 def openAll(){
     // turns on all switches
 }

 def closeAll(){
     // turns off all switches    
 }

// these methods are not invoked for sure.
 def takeAction(){
    
 }

 def takeAction2() {
    
 }