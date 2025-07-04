package org.entimoss.kuwaiba.input.tmp2;

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

// note use COMMIT ON EXECUTE
//uncomment in groovy script
//KuawabaSimpleTests1 kuwaibaImport = new KuawabaSimpleTests1(bem, aem, scriptParameters);
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

public class KuawabaSimpleTestsXX {
   static Logger LOG = LoggerFactory.getLogger("KuawabaSimpleTests1"); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy

   String countryName = "United States";
   String SEPARATOR = ";";

   public KuawabaSimpleTestsXX(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      /*
       * rangeParentValue
       * The rangeParentValue can be the name property of the object or the kuwaiba objectID of the object.
       * If the rangeParentValue is not set, all devices will be included in the tree.
       * If the rangeParentValue is not found, an exception will be thrown and the report will not complete
       * Finds the parent visable object of the devices to include in the device list. 
       * If a device has this parent somewhere in their parent object tree, the device will be a candidate to be included in the requisition for OpenNMS.
       */
      String rangeParentValue = parameters.getOrDefault("rangeParentValue", "");

      try {

         // find parent region
         BusinessObjectLight rangeParent = findObjectByIdOrName(Constants.CLASS_VIEWABLEOBJECT, rangeParentValue);
         
         
         LOG.info("found range parent =" + rangeParent.getId()+" classname="+rangeParent.getClassName());
         taskResult.getMessages().add(TaskResult.createInformationMessage(
                  String.format("found range parent =" + rangeParent.getId()+" classname="+rangeParent.getClassName())));

         String createObjectClass = "Pole";
         String createObjectName = "bpk001";
         String parentOid = rangeParent.getId();
         String parentClassName = rangeParent.getClassName();
         HashMap<String, String> initialAttributes = null;
         
         // create primary splice box
         BusinessObject pole = createIfDoesntExist(createObjectClass, createObjectName, parentOid, parentClassName, initialAttributes);
         
         taskResult.getMessages().add(TaskResult.createInformationMessage(
                  String.format("created new pole poleId= " + pole.getId() +" name:"+pole.getName() )));
         LOG.warn("created new pole poleId= " + pole.getId() +" name:"+pole.getName() );
         

         // create secondary splice box
         // create ont
         // create lte

      } catch (Exception ex) {
         taskResult.getMessages().add(TaskResult.createErrorMessage(
                  String.format("error running task " + ex)));
      }



      return taskResult;
   }

   /**
    * creates new object with parent if object doesn't exist
    * return BusinessObject of existing object or new object if does already exist
    * @param createObjectClass
    * @param createObjectName
    * @param parentObjectId
    * @param parentObjectClass
    * @return
    */
   public BusinessObject createIfDoesntExist(String createObjectClass, String createObjectName, String parentOid, String parentClassName, HashMap<String, String> initialAttributes) {
      
      BusinessObject createdObject = null;
      
      try {
         // see if there is an object with the same name
         List<BusinessObject> foundObjects = bem.getObjectsWithFilter(createObjectClass, Constants.PROPERTY_NAME, createObjectName);
         if (!foundObjects.isEmpty()) {
            createdObject = foundObjects.get(0);
            LOG.info("createIfDoesntExist - object already exists id "+createdObject.getId()
                     + "createObjectClass " + createObjectClass + 
                     ", createObjectName:" + createObjectName + ", parentOid:" + parentOid + ", parentClassName " + parentClassName);
         }
      } catch (Exception ex) {
         LOG.error("problem finding object:",ex);
      }

      if (createdObject== null ) {
         // create new object with parent
         try {
            HashMap<String, String> attributes = (initialAttributes == null) ? new HashMap<String, String>() : new HashMap<String, String>(initialAttributes);
            attributes.put(Constants.PROPERTY_NAME, createObjectName);

            // not using templates
            String templateId = null;
            
            String createdObjectId = bem.createObject(createObjectClass, parentClassName, parentOid, attributes, templateId);
            
            createdObject = bem.getObject(createObjectClass, createdObjectId);

            LOG.info("createIfDoesntExist - created new ooject id "+createdObject.getId()
            + "createObjectClass " + createObjectClass + 
            ", createObjectName:" + createObjectName + ", parentOid:" + parentOid + ", parentClassName " + parentClassName);
            
         } catch (Exception e) {
            LOG.error("problem creating object createObjectClass " + createObjectClass + 
                     ", createObjectName:" + createObjectName + ", parentOid:" + parentOid + ", parentClassName " + parentClassName, e);
         }
      }

      return createdObject;

   }

   /**
    * tries to find an object based on className or object id
    * the first found object will be used
    *  note className should be one of Constants.CLASS_VIEWABLEOBJECT or POSSIBLY Constants.CLASS_GENERICCONNECTION
    * @param findObjectValue value can be an absolute object id or an object name. Search for object name first.
    * @return the object id of the first object found 
    */
   public BusinessObjectLight findObjectByIdOrName(String findObjectClassName, String findObjectValue) {

      BusinessObjectLight rangeParent = null;
      String rangeParentClassName = null;
      String rangeParentId = null;

      String searchErrorMsg = null;
      if (findObjectValue != null && !findObjectValue.isEmpty()) {
         try {
            // see if there is an object with the same name
            List<BusinessObjectLight> rangeParents = bem.getObjectsWithFilterLight(findObjectClassName, Constants.PROPERTY_NAME, findObjectValue);
            if (!rangeParents.isEmpty())
               rangeParent = rangeParents.get(0);
         } catch (Exception ex) {
            searchErrorMsg = ex.getMessage();
         }
         if (rangeParent == null)
            // else see if there is an object with the object id = viewable object
            try {
               // see if there is an object with the same id
               List<BusinessObjectLight> rangeParents = bem.getObjectsWithFilterLight(findObjectClassName, Constants.PROPERTY_UUID, findObjectValue);
               if (!rangeParents.isEmpty())
                  rangeParent = rangeParents.get(0);
            } catch (Exception ex) {
               searchErrorMsg = ex.getMessage();
            }
         if (rangeParent == null) {
            throw new IllegalArgumentException("cannot find findObjectClassName:"+findObjectClassName+ " with findObjectValue=" + findObjectValue + " search error:" + searchErrorMsg);
         }
         rangeParentClassName = rangeParent.getClassName();
         rangeParentId = rangeParent.getId();
         LOG.warn("found object with parent object rangeParentClassName=" + rangeParentClassName + " rangeParentId=" + rangeParentId + " " + rangeParent);
      }

      return rangeParent;
   }

   /**
    * tests if business objectId is a parent of child object
    * @param parentObjectId
    * @param childBusinessObject
    * @return 
    * @throws BusinessObjectNotFoundException
    * @throws MetadataObjectNotFoundException
    * @throws InvalidArgumentException
    */
   public boolean objectIsParent(String parentObjectId, BusinessObjectLight childBusinessObject) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {

      boolean isParent = false;

      // if rangeParent is set do not proceed if device is not a child of rangeParent
      // TODO this is correct method but doesn't work because transaction is not closed in BusinessEntityManagerImpl.isParent (no txSuccess())
      // bem.isParent(rangeParentClassName, rangeParentId, device.getClassName(), device.getId())) {

      // TODO work around
      if (parentObjectId != null) {
         List<BusinessObjectLight> parents = bem.getParents(childBusinessObject.getClassName(), childBusinessObject.getId());

         LOG.warn("parents of child name: " + childBusinessObject.getClassName() + " Id: " + childBusinessObject.getId() + " : " + parents);

         for (BusinessObjectLight parent : parents) {
            if (parent.getId().equals(parentObjectId)) {
               isParent = true;
               break;
            }
         }
      }
      return isParent;
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
