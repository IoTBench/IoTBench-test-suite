/**
 *  StatusThing 
 *
 *  Author: alexking@me.com
 *  Date: 2013-11-01
 * 
 *  Download the Mac App at https://github.com/alexking/StatusThing
 *
 */
preferences {
    section("Settings") {
        input "switches", "capability.switch", title : "Switches", multiple : true, required : true
        input "temperatures", "capability.temperatureMeasurement", title : "Temperature", multiple : true, required : true
    }
}

mappings 
{
    path("/updateItemsAndTemperature") 
    {
        action : 
        [
            GET : "updateItemsAndTemperatures"
        ] 
    }
    
    path("/itemChangeToState/:id/:state")
    {
        action : 
        [
            GET : "itemChangeToState"
        ]
    }
}


def updateItemsAndTemperatures()
{
    def items = []
    for (item in switches)
    {
        items << [ 'id' : item.id , 'state' : item.currentState('switch').value , 'name' : item.displayName ]
    }

    def temperatureItems = []
    for (temperature in temperatures)
    {
        temperatureItems << [ 'id' : temperature.id, 'name' : temperature.displayName, 'value' : temperature.currentValue('temperature') ]
    }

    [ 'temperatures' : temperatureItems , 'items' : items ]

}

def itemChangeToState()
{

    def item = switches.find { it.id == params.id }

    if (params.state == "on")
    {
        item.on();
    } else {
        item.off();
    }


    def data = updateItemsAndTemperatures()

    for(dataItem in data['items']) 
    {
        if (dataItem.id == params.id)
        {
            dataItem.state = params.state; 
        }

    }

    data

}

def installed() { }

def updated() { }