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

import io.netty.util.Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;

// NOTE COMMIT ON EXECUTE BUTTON
//uncomment in groovy script
//KuawabaSimpleTests2 kuwaibaImport = new KuawabaSimpleTests2(bem, aem, scriptParameters);
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

public class KuawabaSimpleTests2 {
   static Logger LOG = LoggerFactory.getLogger("KuawabaSimpleTests1"); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy

   String countryName = "United States";
   String SEPARATOR = ";";

   public KuawabaSimpleTests2(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();
      
      LOG.warn("**************** start of test ");

      try {

         BusinessObject parentObject = null;
         BusinessObject foundObject = null;

         // see if there is an object with the same name
         List<BusinessObject> parentObjects = bem.getObjectsWithFilter("Neighborhood", Constants.PROPERTY_NAME, "BitternePk");
         LOG.info("parent objects size: " + parentObjects.size());
         if (!parentObjects.isEmpty()) {
            parentObject = parentObjects.get(0);
            LOG.info("parentObject:" + businessObjectToString(parentObject));
         }

         // see if object already exists
         List<BusinessObject> foundObjects = bem.getObjectsWithFilter("Pole", Constants.PROPERTY_NAME, "poleXXXX");
         LOG.info("found objects size: " + foundObjects.size());
         if (!foundObjects.isEmpty()) {
            foundObject = foundObjects.get(0);
            LOG.info("foundObject:" + businessObjectToString(foundObject));
         }

         if (foundObject == null) {
            // create object if it doesnt exist
            HashMap<String, String> attributes = new HashMap<String, String>();
            attributes.put(Constants.PROPERTY_NAME, "poleXXXX");

            String parentClassName = parentObject.getClassName();
            String parentOid = parentObject.getId();
            String className = "Pole";

            // not using templates
            String templateId = null;

            String createdObjectId = bem.createObject(className, parentClassName, parentOid, attributes, templateId);
            LOG.info("createdObjectId:" + createdObjectId );

            // find created object
            BusinessObject createdObject = bem.getObject(className, createdObjectId);

            LOG.info("createdObject:" + businessObjectToString(createdObject));
         }

      } catch (Exception ex) {
         taskResult.getMessages().add(TaskResult.createErrorMessage(
                  String.format("error running task " + ex)));
      }
      
      LOG.warn("**************** end test ");

      return taskResult;
   }

   // overloaded to string methods for BusinessObjects
   String businessObjectToString(BusinessObject bo) {
      return "BusinessObject[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()="+ 
               bo.getClassDisplayName() + " getAttributes()=" + bo.getAttributes() + "]";
   }

   String businessObjectToString(BusinessObjectLight bo) {
      return "BusinessObjectLight[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" + 
              bo.getClassDisplayName() + "]";
   }

}