/**
 *  Feed My Pet
 *
 *  Author: SmartThings
 */
preferences {
	section("Choose your pet feeder...") {
		input "feeder", "device.PetFeederShield", title: "Where?"
	}
	section("Feed my pet at...") {
		input "time1", "time", title: "When?"
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

def scheduleCheck()
{
	log.trace "scheduledFeeding"
	feeder?.feed()
}
