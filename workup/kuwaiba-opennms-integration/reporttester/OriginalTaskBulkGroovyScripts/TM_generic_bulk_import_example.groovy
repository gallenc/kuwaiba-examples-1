/**
 * A simple script that processes a CSV file in order to bulk import cities and sites inside the cities. 
 * This script does not use templates and creates all the objects from scratch. It assumes the following 
 * containment structure: Country -> State-> City -> Building -> Room.
 * The structure of the CSV is: STATE_NAME;STATE_ACRONYM;CITY_NAME;CENTRAL_OFFICE_NAME:CENTRAL_OFFICE_ADRESS;RACK_ROOM_NAME
 * Cities will be created in a country named "United States", unless the parameter defaultCountry is set. Note that the separator 
 * is a semicolon ";". Class "State" must have a string type attribute named "acronym" and class "Building" must have a string attribute named "address".
 * Note: Use the sample file "sample_co_import.csv" in the "assets" directory within the epository folder this script is hosted.
 * Neotropic SAS - version 1.0
 * Parameters: -fileName: The location of the upload file. Mandatory.
 *             -defaultCountry: The default country where the cities will be created. Optional. Default value:"United States"
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

//Creates the task result instance using reflection
def taskResult = new TaskResult();
def countryName = "United States";
def separator = ";"

//Check if the parameters exist and are set
if (scriptParameters.get("fileName") == null || scriptParameters.get("fileName").isEmpty()) 
	return TaskResult.createErrorResult("Parameter fileName not set");
	
if (scriptParameters.get("defaultCountry") != null && !scriptParameters.get("defaultCountry").isEmpty()) 
	countryName = scriptParameters.get("defaultCountry");

def fileName = scriptParameters.get("fileName");


// Check if the file exists and it's readable
def importFile = new File(fileName);
if (!importFile.exists())
    return TaskResult.createErrorResult(String.format("File %s does not exist", fileName));

if (!importFile.canRead())
    return TaskResult.createErrorResult(String.format("File %s exists, but it's not readable", fileName));


try {
    matchingCountries = bem.getObjectsWithFilterLight("Country", "name", countryName);
    if (matchingCountries.isEmpty())
        return TaskResult.createErrorResult(String.format("Default country %s could not be found", countryName));
        
    def defaultCountry = matchingCountries.get(0);
    
    // Parses and processes every line
    importFile.eachLine() { line, number ->
        def tokens = line.split(separator, -1);
        if (tokens.length != 6) // All columns are mandatory, even if they're just empty
            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Line %s does not have 6 columns as expected but %s", number, tokens.length)));
        else {
            try {
                def stateName = tokens[0];
                
                // Get or create the state
                def matchingStates = bem.getObjectsWithFilterLight("State", "name", stateName);
                def currentState
                if (matchingStates.isEmpty()) {// If the state does not exist, create one
                    def stateProperties = new HashMap<String, String>();
                    stateProperties.put("name", tokens[0]);
                    stateProperties.put("acronym", tokens[1]);
                    def newStateId = bem.createObject("State", "Country", defaultCountry.getId(), stateProperties, null);
                    currentState = new BusinessObjectLight("State", newStateId, tokens[0]);
                    taskResult.getMessages().add(TaskResult.createInformationMessage(
                        String.format("State %s created in line %s", tokens[0], number)));
                    
                } else
                    currentState = matchingStates.get(0);
                
                // Get or create the city
                def matchingCities = bem.getObjectsWithFilterLight("City", "name", tokens[2]);
                def currentCity;
                if (matchingCities.isEmpty()) {// If the city does not exist, create one
                    def cityProperties = new HashMap<String, String>();
                    cityProperties.put("name", tokens[2]);
                    def newCityId = bem.createObject("City", "State", currentState.getId(), cityProperties, null);
                    currentCity = new BusinessObjectLight("City", newCityId, tokens[2]);
                    taskResult.getMessages().add(TaskResult.createInformationMessage(
                        String.format("City %s created in line %s", tokens[2], number)));
                } else
                    currentCity = matchingCities.get(0);
                
                // New central office. No previous existence checks made
                def centralOfficeProperties = new HashMap<String, String>();
                centralOfficeProperties.put("name", tokens[3]);
                centralOfficeProperties.put("address", tokens[4]);
                newCentraOfficeId = bem.createObject("Building", "City", currentCity.getId(), centralOfficeProperties, null);
                taskResult.getMessages().add(TaskResult.createInformationMessage(
                        String.format("Central office %s created in line %s", tokens[3], number)));
                
                // New rack room. No previous existence checks made. This might be improved by using a CO template 
                def rackRoomProperties = new HashMap<String, String>();
                rackRoomProperties.put("name", tokens[5]);
                bem.createObject("Room", "Building", newCentraOfficeId, rackRoomProperties, null);
                taskResult.getMessages().add(TaskResult.createInformationMessage(
                        String.format("Rack room %s created in line %s", tokens[5], number)));
            } catch (InventoryException ie) {
                taskResult.getMessages().add(TaskResult.createErrorMessage(
                    String.format("Error processing line %s: %s", number, ie.getMessage())));
            }
        }        
    }
} catch (Exception e) {
    return TaskResult.createErrorResult(String.format("Unexpected error: %s", e.getMessage()));
}

taskResult 
