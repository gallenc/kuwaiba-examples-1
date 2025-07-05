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
//KuawabaSimpleTestsXX kuwaibaImport = new KuawabaSimpleTestsXX(bem, aem, scriptParameters);
//return kuwaibaImport.runTask();

/**

 */

public class KuawabaSimpleTestsXX {
   static Logger LOG = LoggerFactory.getLogger(KuawabaSimpleTestsXX.class); // remove static in groovy

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
      
      taskResult.getMessages().add(TaskResult.createInformationMessage(
             String.format("running Script "+KuawabaSimpleTestsXX.class.getName()+" with parameters:" +parameters)));
      
      LOG.debug("running Script "+KuawabaSimpleTestsXX.class.getName()+" with parameters:" +parameters);

      try {
         
         BusinessObject parentObject = null;
         
         List<BusinessObject> foundObjects = bem.getObjectsWithFilter("House", Constants.PROPERTY_NAME, "6burnett");
         if (!foundObjects.isEmpty()) {
            parentObject  = foundObjects.get(0);
            
         }
         
         LOG.debug(" found parent Object "+businessObjectToString(parentObject));
         
         String createObjectClassName ="OpticalNetworkTerminal";
         String createObjectName ="testOnt2";
         String parentOid = parentObject.getId();
         String parentClassName = parentObject.getClassName();
         HashMap<String, String> initialAttributes =null ;
         
         createIfDoesntExist(createObjectClassName, createObjectName, parentOid, parentClassName, initialAttributes);

//         // find parent region
//         BusinessObjectLight rangeParent = findObjectByIdOrName(Constants.CLASS_VIEWABLEOBJECT, rangeParentValue);
//         
//         
//         LOG.info("found range parent =" + rangeParent.getId()+" classname="+rangeParent.getClassName());
//         taskResult.getMessages().add(TaskResult.createInformationMessage(
//                  String.format("found range parent =" + rangeParent.getId()+" classname="+rangeParent.getClassName())));
//
//         String createObjectClass = "Pole";
//         String createObjectName = "bpk001";
//         String parentOid = rangeParent.getId();
//         String parentClassName = rangeParent.getClassName();
//         HashMap<String, String> initialAttributes = null;
//         
//         // create primary splice box
//         BusinessObject pole = createIfDoesntExist(createObjectClass, createObjectName, parentOid, parentClassName, initialAttributes);
//         
//         taskResult.getMessages().add(TaskResult.createInformationMessage(
//                  String.format("created new pole poleId= " + pole.getId() +" name:"+pole.getName() )));
//         LOG.warn("created new pole poleId= " + pole.getId() +" name:"+pole.getName() );
//         
//
//         // create secondary splice box
//         // create ont
//         // create lte

      } catch (Exception ex) {
         taskResult.getMessages().add(TaskResult.createErrorMessage(
                  String.format("error running task " + ex)));
      }



      LOG.debug("end of Script "+KuawabaSimpleTestsXX.class.getName());
      
      return taskResult;
   }
   
 

   
   BusinessObject findOrCreateIfDoesntExist(String className  , String classTemplate , String name, String parentClass, String parentClassName , 
            String latitude, String longitude, String IpAddress  , String Comment , String serialNumber  , String assetNumber  ) {
      
      return null;
   }

   /**
    * creates new object with parent if object doesn't exist
    * return BusinessObject of existing object or new object if does already exist
    * @param createObjectClassName
    * @param createObjectName
    * @param parentObjectId
    * @param parentObjectClass
    * @return
    */
   public BusinessObject createIfDoesntExist(String createObjectClassName, String createObjectName, String parentOid, String parentClassName, HashMap<String, String> initialAttributes) {
      
      BusinessObject createdObject = null;
      
      try {
         // see if there is an object with the same name
         List<BusinessObject> foundObjects = bem.getObjectsWithFilter(createObjectClassName, Constants.PROPERTY_NAME, createObjectName);
         if (!foundObjects.isEmpty()) {
            createdObject = foundObjects.get(0);
            LOG.info("createIfDoesntExist - object already exists id "+createdObject.getId()
                     + "createObjectClass " + createObjectClassName + 
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
            
            String createdObjectId = bem.createObject(createObjectClassName, parentClassName, parentOid, attributes, templateId);
            
            createdObject = bem.getObject(createObjectClassName, createdObjectId);

            LOG.info("createIfDoesntExist - created new object "+ businessObjectToString(createdObject));
            
         } catch (Exception e) {
            LOG.error("problem creating object createObjectClass " + createObjectClassName + 
                     ", createObjectName:" + createObjectName + ", parentOid:" + parentOid + ", parentClassName " + parentClassName, e);
         }
      }

      return createdObject;

   }
   
   // overloaded toString methods for BusinessObjects
   String businessObjectToString(BusinessObject bo) {
      return (bo==null) ? "BusinessObject[ null ]" : "BusinessObject[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()="+ 
               bo.getClassDisplayName() + " getAttributes()=" + bo.getAttributes() + "]";
   }

   String businessObjectToString(BusinessObjectLight bo) {
      return (bo==null) ? "BusinessObject[ null ]" : "BusinessObjectLight[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" + 
              bo.getClassDisplayName() + "]";
   }


}
