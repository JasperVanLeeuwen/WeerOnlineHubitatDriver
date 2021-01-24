  
/*
 * Import URL: https://raw.githubusercontent.com/JasperVanLeeuwen/WeerOnlineHubitatDriver/master/WeerOnline-Driver.groovy
 *
 *	Copyright 2021 Jasper van Leeuwen
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 *
 *
 */
 	public static String version()      {  return "v???"  }

 
import groovy.transform.Field

metadata 
{
	definition(name: "WeerOnline", namespace: "JasperVanLeeuwen", author: "JasperVanLeeuwen", importUrl: "https://raw.githubusercontent.com/JasperVanLeeuwen/WeerOnlineHubitatDriver/master/WeerOnline-Driver.groovy")
	{
 		capability "Sensor"
        capability "TemperatureMeasurement"
        attribute "visibleDistance", "number"
        attribute "latestMessage", "string"
        attribute "datetime", "string"
	}

      preferences 
      {
          //standard logging options
          input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
          input name: "apikey", type: "text", title: "apikey", description: "Enter apikey from http://weerlive.nl/", required: true, defaultValue : "demo"
          input name: "gpslocation", type: "text", title: "location", description: "Enter location, e.g. deg,deg or name", required: true,defaultValue: "Amsterdam"
      }
}


/*
	updated
    
	Doesn't do much other than call initialize().
*/
def updated()
{
    unschedule()
	initialize()	
      if (debugOutput) runIn(1800,logsOff) //disable debug logs after 30 min
	log.trace "Msg: updated ran"
}

/*

	generic driver stuff

*/


/*
	installed
    
	Doesn't do much other than call initialize().
*/
def installed()
{
	initialize()
	log.trace "Msg: installed ran"
}

def updateState()
{       
    log.trace "http://weerlive.nl/api/json-data-10min.php?key=${apikey}&locatie=${gpslocation}"
    Map requestParams = [ uri: "http://weerlive.nl/api/json-data-10min.php?key=${apikey}&locatie=${gpslocation}" , timeout: 20 ]
    log.trace "Msg: weerOnlineResponseHandler async request"
    asynchttpGet("weerOnlineResponseHandler",requestParams)
    log.trace "updateState ran"
}

/*
	initialize
    
	Doesn't do anything.
*/
def initialize()
{
    runIn(1,updateState)
    runEvery15Minutes(updateState)    
	log.trace "Msg: initialize ran"    
}

/*
   eventhandler that does things
*/

void weerOnlineResponseHandler(resp, data) { 
    log.trace "Msg: weerOnlineResponseHandler async response processing"
    if(resp.getStatus() == 200 || resp.getStatus() == 207) {
		Map weerResults = resp.getJson().results
        sendEvent(name:'latestMessage', value: resp.data.toString())
        sendEvent(name:'temperature', value: resp.getJson().liveweer[0].temp)
        sendEvent(name:'visibleDistance', value: resp.getJson().liveweer[0].zicht)   
        
    }
    else {
        sendEvent(name:'latestMessage', value: "http response status: ${resp.getStatus()}")
    }

}

def getThisCopyright(){"&copy; 2021 JasperVanLeeuwen "}
