/**
 *  Unlock It When I Come In
 *
 *  Author: MattRobell
 *  Date: 2014-01-28
 */
preferences {
	section("When there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("Unlock the lock..."){
		input "lock1", "capability.lock", multiple: true
	}

}

def installed()
{
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def updated()
{
	unsubscribe()
	subscribe(motion1, "motion.active", motionActiveHandler)
}

def motionActiveHandler(evt)
{
	def anyLocked = lock1.count{it.currentLock == "unlocked"} != lock1.size()
	if (anyLocked) {
		sendPush "Unlocked door due to arrival of $evt.displayName"
		lock1.unlock()
	}
}