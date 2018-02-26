/**
 *  cuando se mueva el sensor avisa y manda la temperatura
 *
 *  Author: Orlando Zuiga
 */
preferences {
	section("Cuando se detecte movimiento...") {
		input "accelerationSensor", "capability.accelerationSensor", title: "Donde?"
	                                           }
    section("Monitor de temperatura...") {
		input "temperatureSensor1", "capability.temperatureMeasurement"
	                                     }
		       }
	
def installed() {
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
    subscribe(temperatureSensor1, "temperature", temperatureHandler)
}

def updated() {
	unsubscribe()
	subscribe(accelerationSensor, "acceleration.active", accelerationActiveHandler)
    subscribe(temperatureSensor1, "temperature", temperatureHandler)
}


// Para que no mandae y mande mensajes
   	def escaladesegundos = 5
	def CuantoTiempoHace = new Date(now() - (1000 * escaladesegundos))
	def EventosRecientes = accelerationSensor.eventsSince(CuantotiempoHace)
	log.trace "Found ${EventosRecientes?.size() ?: 0} eventos en los ultimos $escaladesegundos segundos"
	def Yamandepopup = EventosRecientes.count { it.value && it.value == "active" } > 1

	if (Yamandepopup) {
		log.debug "ya se mando un Popup en los ultimos $deltaSeconds segundos"
        
                      } else {
        
		log.debug "$accelerationSensor fue movido"
		sendPush("${accelerationSensor.label ?: accelerationSensor.name} fue movido esa madre y esta a $temperature grados farenheit")
         	               
       

}