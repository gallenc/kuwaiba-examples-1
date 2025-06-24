/**
 * Imports DWDM muxes and internal boards from a CSV file in the format exported by Huawei's U2000 NMS. The format of each line should look as follows:
 * NE_NAME;BOARDNAME;BOARD_TYPE;NE_TYPE;SUBRACK_ID;SLOT_ID;SERIAL_NUMBER;PART_NUMBER
 *
 * Note: Make sure that your data model contains the classes DWDMMux, DWDMBoard, EquipmentModel and DWDMBoardType. Also, make sure that the class City has an attribute named acronym and DWDMBoard the attribute partNumber.
 * Additionally, allow DWDMMux instances to be created directly under cities.
 *         
 * Neotropic SAS - version 1.1 <contact@neotropic.co>
 * Parameters: -fileName: The location of the  file that should be imported at server side.
 *             -defaultLocation: The id of the city to place the multiplexers if no suitable city is found.
 */
import java.util.HashMap;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.kuwaiba.apis.persistence.PersistenceService;

//Creates the task result instance using reflection
def taskResult = TaskResult.newInstance();

//Check if the parameters exist and are set
if (scriptParameters.get("fileName") == null || scriptParameters.get("fileName").isEmpty()) 
	return TaskResult.createErrorResult("Parameter fileName must be set first");

if (scriptParameters.get("defaultLocation") == null || scriptParameters.get("defaultLocation").isEmpty()) 
	return TaskResult.createErrorResult("Parameter defaultLocation must be set first");

def fileName = scriptParameters.get("fileName");
def columnSeparator = ";";

// Check if the file exists and it's readable
def importFile = new File(fileName);

if (!importFile.exists())
    return TaskResult.createErrorResult(String.format("File %s does not exist", fileName));

if (!importFile.canRead())
    return TaskResult.createErrorResult(String.format("File %s exists, but it's not readable", fileName));

try {
    // First let's try to find the default city
    def defaultLocation = bem.getObjectLight("City", scriptParameters.get("defaultLocation"));

    // Now we cache the existing DWDMBoardTypes and EquipmentModels
    def cachedBoardTypes = aem.getListTypeItems("DWDMBoardType");
    def cachedEquipmentModels = aem.getListTypeItems("EquipmentModel");

    // Optimize the script by not looking muxes and cities that we already found
    def lastMux, lastLocation;
    
    //Parses and processes every line
    importFile.eachLine() { line, number ->
        try {
            def tokens = line.split(columnSeparator, -1);
            if (tokens.length != 8) //Remember that all columns are mandatory, even if they're just empty
                taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Line %s does not have 8 columns as expected: %s", number, tokens.length)));
            else {
                def mux;

                if (lastMux != null && lastMux.getName().equals(tokens[0]))
                    mux = lastMux;
                else {
                    //Get all DWDM muxes with the given name
                    def muxes = bem.getObjectsWithFilterLight("DWDMMux", "name", tokens[0]);

                    if (muxes.isEmpty()) { //If the device does not exist, create it, and use 
                        //First, we try to find a place to the new mux. To do this, we use their naming convention, and if no city is found, the default location is used.
                        def muxNameTokens = tokens[0].split("-", -1); //Theoretically, the last part of the name of the mux is the acronym of the city
                        def location;
                        if (muxNameTokens.length == 3) {
                            def locations = bem.getObjectsWithFilterLight("City", "acronym", muxNameTokens[2]);
                            if (locations.isEmpty()) {
                                taskResult.getMessages().add(TaskResult.createWarningMessage(String.
                                    format("A City with acronym %s could not be found. Using default location instead", muxNameTokens[2])));
                                location = defaultLocation;
                            } else
                                location = locations.get(0);
                        } else {
                            location = defaultLocation; // The mux name does not comply with the convention
                            taskResult.getMessages().add(TaskResult.createWarningMessage(String.
                                    format("The name of the mux %s does not seem to comply with the naming convention to determine its location. The default one will be used", tokens[0])));
                        }
                        lastLocation = location;

                        // First we check if the mux model exists, otherwise, we create it
                        def muxModel = cachedEquipmentModels.find { aMuxModel ->
                            aMuxModel.getName().equals(tokens[3]);
                        }

                        if (muxModel == null) { // The list type item does not exist
                            def muxModelId = aem.createListTypeItem("EquipmentModel", tokens[3], tokens[3]);
                            muxModel = new BusinessObjectLight("EquipmentModel", muxModelId, tokens[3]);
                            cachedEquipmentModels.add(muxModel);
                            taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("The mux model %s was created successfully", tokens[3])));
                        }

                        // Initialize the structure containing the list of properties to be set 
                        def muxProperties = new HashMap<String, String>();
                        muxProperties.put("name", tokens[0]);
                        muxProperties.put("model", muxModel.getId());

                        // Now let's create the mux in the designated location
                        def newMuxId = bem.createObject("DWDMMux", location.getClassName(), location.getId(), muxProperties, "" /* don't use a template */);
                        mux = new BusinessObjectLight("DWDMMux", newMuxId, muxProperties.get("name"));
                        taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("DWDMMux %s created successfully", tokens[0])));
                    } else
                        //Use the first occurrence
                        mux = muxes.get(0);
                    
                    lastMux = mux;                    
                }

                // Now we create the slot where the board is installed. Instead of creating a slot and a subslot with the columns "Slot ID" and "Subrack ID", 
                // we will create a single slot whose name will be composed by these two numbers
                def slotProperties = new HashMap<String, String>();
                slotProperties.put("name", tokens[4] + "." + tokens[5]);
                def slotId = bem.createObject("Slot", mux.getClassName(), mux.getId(), slotProperties, "" /* don't use a template */);
                slot = new BusinessObjectLight("Slot", slotId, slotProperties.get("name"))
                // Now the board itself
                // First we check if the model exists, or else, we create it
                def realBoardType = tokens[2].split(" ", -1)[0]; // The model of the board has this format "MODEL (SPECIFIC_MODEL)". We will use the first part only
                def boardType = cachedBoardTypes.find { aBoardType ->
                    aBoardType.getName().equals(realBoardType);
                }

                if (boardType == null) { // The list type item does not exist
                    def boardTypeId = aem.createListTypeItem("DWDMBoardType", realBoardType, tokens[2]);
                    boardType = new BusinessObjectLight("DWDMBoardType", boardTypeId, realBoardType);
                    cachedBoardTypes.add(boardType);
                    taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("The board model %s was created successfully", realBoardType)));
                }
                def boardProperties = new HashMap<String, String>();
                boardProperties.put("name", tokens[1]);
                boardProperties.put("model", boardType.getId());
                boardProperties.put("serialNumber", tokens[6]);
                boardProperties.put("partNumber", tokens[7]);

                def boardId = bem.createObject("DWDMBoard", slot.getClassName(), slot.getId(), boardProperties, "" /* don't use a template */);

                // Now let's create a single port. We could have used a template with the same results
                def portProperties = new HashMap<String, String>();
                portProperties.put("name", "01");
                bem.createObject("OpticalPort", "DWDMBoard", boardId, portProperties, "");

                taskResult.getMessages().add(TaskResult.createInformationMessage(String.format("The board %s was created successfully in %s:%s", tokens[1], tokens[0], slot.getName())));
            }
        } catch (Exception e) {
            taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Error in line %s: %s", number, e.getMessage())));
        }
    }    
} catch (Exception e) { //Boiler-plate code to make sure we catch every possible situation gracefully.
    return TaskResult.createErrorResult(String.format("Unexpected error: %s", e.getMessage()));
}

//Returns the result
taskResult;