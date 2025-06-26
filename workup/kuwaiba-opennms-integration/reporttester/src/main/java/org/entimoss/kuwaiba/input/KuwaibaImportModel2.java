package org.entimoss.kuwaiba.input;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObjectLight;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

//uncomment in groovy script
//KuwaibaImportModel2 kuwaibaImport = new KuwaibaImportModel2(bem, aem, scriptParameters);
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
   static Logger LOG = LoggerFactory.getLogger("KuwaibaImportModel2"); // remove static in groovy

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

   /*
    *  // https://www.thefoa.org/tech/ColCodes.htm
    *  Inside the cable or inside each tube in a loose tube cable, individual fibers will be color coded for identification
    *  1  Blue,    2  Orange, 3  Green, 4  Brown ,5  Slate, 6  White, 7  Red, 8  Black, 9  Yellow, 10    Violet, 11    Rose, 12    Aqua
    */
   static final List<String> orderedFibreColours = Arrays.asList("Blue", "Orange", "Green", "Brown", "Slate", "White", "Red", "Black", "Yellow", "Violet", "Rose", "Aqua");

   public static String getColourForStrand(int no) {
      if (no < 1 || no > orderedFibreColours.size()) {
         throw new IllegalArgumentException("strand size out of range: " + no);
      }
      return orderedFibreColours.get(no - 1);
   }

   public static int getStrandForColour(String colour) {
      int no = orderedFibreColours.indexOf(colour);
      if (no < 0)
         throw new IllegalArgumentException("unknown fibre colour: " + colour);
      return no + 1;
   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      String templateId = null;
      try {

         // create primary splice box
         // create secondary splice box
         // create 
         

      } catch (Exception ex) {
         taskResult.getMessages().add(TaskResult.createErrorMessage(
                  String.format("error running task " + ex)));
      }

      taskResult.getMessages().add(TaskResult.createInformationMessage(
               String.format("created temptemplate templateId= " + templateId)));

      return taskResult;
   }
   
   
   

   public String createWireContainer(String containerName, int sections, int tubes) throws MetadataObjectNotFoundException, OperationNotPermittedException {
      String templateClass = Constants.CLASS_WIRECONTAINER;

      List<TemplateObjectLight> templatesForClass = aem.getTemplatesForClass(templateClass);
      if (!templatesForClass.isEmpty()) {
         for (TemplateObjectLight t : templatesForClass) {
            LOG.error("object already exists:" + t);
         }
      }

      return aem.createTemplate(templateClass, containerName);

   }

}
