/**
 *  Notify and reset power data from energy meter
 *
 *  Author: Snoopyjoe
 *
 */

definition(
    name: "Report energy data",
    namespace: "SnoopyJoe",
    author: "Thompson Garner",
    description: "Notify and reset power data from energy meter via SMS or PUSH",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/text.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/text@2x.png"
		  )

preferences 
{
  page(name: "MainPage")
  page(name: "DailyPage")
  page(name: "WeeklyPage")
  page(name: "MonthlyPage")
  page(name: "FinalPage")
}


def MainPage() 
{
  dynamicPage(name: "MainPage", title: "Report usage from energy meter", nextPage: "FinalPage", uninstall: true) 
  {
   section()
    {
      input "meter","capability.powerMeter", title: "Send report from this meter", required: true, multiple: false, submitOnChange: true, refreshAfterSelection: true
    }
    
      section 
      {
        input(name: "repeater", type: "enum", title: "When to send report", options:["Daily","Weekly","Monthly"], refreshAfterSelection: true)

      	if(repeater == "Daily")
        {
        href(name: "toDailyPage", title: "Send report Daily", page: "DailyPage", description:"Every day at ${repeatDaily}00", refreshAfterSelection: true)
        }
        if(repeater == "Weekly")
        {
        href(name: "toWeeklyPage", page: "WeeklyPage", title: "Send report Weekly", description:"Every ${repeatWeekly} at ${repeatDaily}00", refreshAfterSelection: true)
        }
        if(repeater == "Monthly")
        {
        href(name: "toMonthlyPage", page: "MonthlyPage", title: "Send report Monthly", description:"Day ${repeatMonthly} of month at ${repeatDaily}00", refreshAfterSelection: true)
        }
      }
   }
}

    def FinalPage()
    {
    	dynamicPage(name: "FinalPage", title: "Chose notification method to complete", install: true)
    	{
		 section 
			{
        	input("recipients", "contact", title: "Send notifications to") 
        		{
        		input(name: "sms", type: "phone", title: "Send A SMS Text To", description: null, required: false)
       			input(name: "pushNotification", type: "bool", title: "Send a PUSH notification", description: null, defaultValue: true)
        		}
   	 		}
        }
    }
    
    def DailyPage()
    {
    	dynamicPage(name: "DailyPage", title: "Chose HOUR to send daily report", install: false, required: true)
    	{
        	section
            {
        	input(name: "repeatDaily", type: "number", title: "Daily at this (24HR hour only)", required: true, refreshAfterSelection: true)			
        	}
        }
    }
    
    def WeeklyPage()
    {
    	dynamicPage(name: "WeeklyPage", title: "Chose DAY to send weekly report")
    	{
        	section
            {
       		input(name: "repeatWeekly", type: "enum", title: "Weekly on this (DAY)",options:["MON","TUE","WED","THU","FRI","SAT","SUN"],required: true)
            input(name: "repeatDaily", type: "number", title: "At this (24HR hour only)", required: true)			
        	}
        }
    }
    
    def MonthlyPage()
    {
    	dynamicPage(name: "MonthlyPage", title: "Chose DATE to send monthly report")
    	{
        	section
            {
        	input(name: "repeatMonthly", type: "number", title: "Monthly on this (Date)",required: true)
            input(name: "repeatDaily", type: "number", title: "At this (24HR hour only)", required: true)			
        	}
        }
    }

def installed()
{
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() 
{
	log.debug "Updated with settings: ${settings}"
	unschedule()
    initialize()
}

def initialize()
{        

		if(repeater == "Daily")
        {
    		def RepeaterDaily = "0 0 ${repeatDaily} * * ?"
    		schedule(RepeaterDaily, meterHandler)
       	}
        
        if(repeater == "Weekly")
        {
    		def RepeaterWeekly = "0 0 ${repeatDaily} ? * ${repeatWeekly}"
        	schedule(RepeaterWeekly, meterHandler)

		}
        
        if(repeater == "Monthly")
        {
    		def RepeaterMonthly = "0 0 ${repeatDaily} ${repeatMonthly} * ?"
        	schedule(RepeaterMonthly, meterHandler)
		}
}

def meterHandler()
{
  	def msg = "${meter} used ${meter.latestValue("energy")}kWh during ${repeater} period."
    sendMessage(msg)
    meter.reset()
}

def sendMessage(msg) 
{
    if (location.contactBookEnabled)
    {
        sendNotificationToContacts(msg, recipients)
    }
    else 
    {
        if (sms)
        {
            sendSms(sms, msg)
        }
        if (pushNotification) 
        {
            sendPush(msg)
        }
    }
}