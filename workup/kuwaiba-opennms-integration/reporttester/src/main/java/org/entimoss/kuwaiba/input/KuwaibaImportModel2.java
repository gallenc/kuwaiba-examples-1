package org.entimoss.kuwaiba.input;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

//uncomment in groovy script
//KuwaibaImportModel1 kuwaibaImport = new KuwaibaImportModel1(bem, aem, scriptParameters);
//return kuwaibaImport.runTask();

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

public class KuwaibaImportModel2 {
   static Logger LOG = LoggerFactory.getLogger("KuwaibaImportModel1"); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> scriptParameters = null; // injected in groovy

   String countryName = "United States";
   String SEPARATOR = ";";

   public KuwaibaImportModel2(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.scriptParameters = scriptParameters;
   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      //Check if the parameters exist and are set
      if (scriptParameters.get("fileName") == null || scriptParameters.get("fileName").isEmpty())
         return TaskResult.createErrorResult("Parameter fileName not set");

      if (scriptParameters.get("defaultCountry") != null && !scriptParameters.get("defaultCountry").isEmpty())
         countryName = scriptParameters.get("defaultCountry");

      // Check if the file exists and it's readable
      String fileName = scriptParameters.get("fileName");

      File importFile = new File(fileName);
      if (!importFile.exists())
         return TaskResult.createErrorResult(String.format("File %s does not exist", fileName));

      if (!importFile.canRead())
         return TaskResult.createErrorResult(String.format("File %s exists, but it's not readable", fileName));

      try {
         
         List<BusinessObjectLight> matchingCountries = bem.getObjectsWithFilterLight("Country", "name", countryName);
         if (matchingCountries.isEmpty())
            return TaskResult.createErrorResult(String.format("Default country %s could not be found", countryName));

         BusinessObjectLight defaultCountry = matchingCountries.get(0);

         // Parses and processes every line

         try (BufferedReader br = new BufferedReader(new FileReader(importFile))) {
            String line;
            while ((line = br.readLine()) != null) {

               String[] tokens = line.split(SEPARATOR);
               if (tokens.length != 6) // All columns are mandatory, even if they're just empty
                  taskResult.getMessages().add(TaskResult.createErrorMessage(String.format("Line %s does not have 6 columns as expected but %s", line, tokens.length)));

               else {

                  try {

                     String stateName = tokens[0];

                     // Get or create the state
                     List<BusinessObjectLight> matchingStates = bem.getObjectsWithFilterLight("State", "name", stateName);
                     BusinessObjectLight currentState = null;

                     if (matchingStates.isEmpty()) {// If the state does not exist, create one
                        HashMap<String, String> stateProperties = new HashMap<String, String>();
                        stateProperties.put("name", tokens[0]);
                        stateProperties.put("acronym", tokens[1]);

                        String newStateId = bem.createObject("State", "Country", defaultCountry.getId(), stateProperties, null);

                        currentState = new BusinessObjectLight("State", newStateId, tokens[0]);
                        taskResult.getMessages().add(TaskResult.createInformationMessage(
                                 String.format("State %s created in line %s", tokens[0], line)));

                     } else {
                        currentState = matchingStates.get(0);

                        // Get or create the city
                        List<BusinessObjectLight> matchingCities = bem.getObjectsWithFilterLight("City", "name", tokens[2]);
                        BusinessObjectLight currentCity;

                        if (matchingCities.isEmpty()) {// If the city does not exist, create one
                           HashMap<String, String> cityProperties = new HashMap<String, String>();
                           cityProperties.put("name", tokens[2]);
                           String newCityId = bem.createObject("City", "State", currentState.getId(), cityProperties, null);
                           currentCity = new BusinessObjectLight("City", newCityId, tokens[2]);
                           taskResult.getMessages().add(TaskResult.createInformationMessage(
                                    String.format("City %s created in line %s", tokens[2], line)));

                        } else {
                           currentCity = matchingCities.get(0);

                           // New central office. No previous existence checks made
                           HashMap<String, String> centralOfficeProperties = new HashMap<String, String>();
                           centralOfficeProperties.put("name", tokens[3]);
                           centralOfficeProperties.put("address", tokens[4]);
                           String newCentraOfficeId = bem.createObject("Building", "City", currentCity.getId(), centralOfficeProperties, null);
                           taskResult.getMessages().add(TaskResult.createInformationMessage(
                                    String.format("Central office %s created in line %s", tokens[3], line)));

                           // New rack room. No previous existence checks made. This might be improved by using a CO template 
                           HashMap<String, String> rackRoomProperties = new HashMap<String, String>();
                           rackRoomProperties.put("name", tokens[5]);
                           bem.createObject("Room", "Building", newCentraOfficeId, rackRoomProperties, null);
                           taskResult.getMessages().add(TaskResult.createInformationMessage(
                                    String.format("Rack room %s created in line %s", tokens[5], line)));
                        }
                     }

                  } catch (InventoryException ie) {
                     taskResult.getMessages().add(TaskResult.createErrorMessage(
                              String.format("Error processing line %s: %s", line, ie.getMessage())));
                  }
               }

            }
         }

      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (InvalidArgumentException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return taskResult;
   }

}
