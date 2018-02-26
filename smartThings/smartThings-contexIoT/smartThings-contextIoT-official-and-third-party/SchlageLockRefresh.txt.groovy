/**
 *  Scheduled Mode Change
 *
 *  Author: Cory Simpson
 
 *  Date: 2013-05-04
 */
preferences {
        section("At this time every day") {
                input "time1", "time", title: "Time of Day"
        }
/*      section("Select locks") {
                input "lock1", "capability.lock", multiple: true
        }*/
    section("Select locks"){
        input "refresh1","capability.refresh", multiple: true
        }
 
 
}
 
def installed()
{
        schedule(time1, "scheduleCheck")
}
 
def updated()
{
        unschedule()
        schedule(time1, "scheduleCheck")
}
 
def scheduleCheck(evt){
    log.trace "scheduledCheck"
        log.debug refresh1?.lock
    refresh1.refresh()
}