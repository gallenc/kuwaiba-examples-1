package org.entimoss.kuwaiba.provisioning.script;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.util.Constant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
//EntimossKuwaibaProvisioningTask kuwaibaImport = new EntimossKuwaibaProvisioningTask(bem, aem, scriptParameters);
//return kuwaibaImport.runTask();

/**

 */

public class EntimossKuwaibaProvisioningTask {
   static Logger LOG = LoggerFactory.getLogger(EntimossKuwaibaProvisioningTask.class); 

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy

   int kuwaibaTemplatesExisting = 0;
   int kuwaibaTemplatesNew = 0;
   int kuwaibaClassesExisting = 0;
   int kuwaibaClassesNew = 0;

   public EntimossKuwaibaProvisioningTask(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      taskResult.getMessages().add(TaskResult.createInformationMessage(
               String.format("running Script " + EntimossKuwaibaProvisioningTask.class.getName() + " with parameters:" + parameters)));

      LOG.debug("running Script " + EntimossKuwaibaProvisioningTask.class.getName() + " with parameters:" + parameters);

      /*
       * file name and location of kuwaibaProvisioningRequisition
       * Defaults to
       */
      String kuwaibaProvisioningRequisitionFileName = parameters.getOrDefault("kuwaibaProvisioningRequisitionFileName", "/external-data/kuwaibaProvisioningRequisition.json");

      try {

         File kuwaibaProvisioningFile = new File(kuwaibaProvisioningRequisitionFileName);
         if (!kuwaibaProvisioningFile.exists()) {
            throw new IllegalArgumentException("sctipt cannot find file:" + kuwaibaProvisioningFile);
         }

         ObjectMapper om = new ObjectMapper();

         KuwaibaProvisioningRequisition kuwaibaProvisioningRequisition = om.readValue(kuwaibaProvisioningFile, KuwaibaProvisioningRequisition.class);
         LOG.info("Starting to load requistionFile " + kuwaibaProvisioningFile.getAbsolutePath() + " containing " + kuwaibaProvisioningRequisition.getKuwaibaTemplateList().size() +
                  " templates and" + kuwaibaProvisioningRequisition.getKuwaibaClassList().size() + " classes");

         // create new tempates
         for (KuwaibaClass kuwaibaTemplate : kuwaibaProvisioningRequisition.getKuwaibaTemplateList()) {
            LOG.info("creating kuwaibaTemplate: " + kuwaibaTemplate);

         }

         // create new classes
         for (KuwaibaClass kuwaibaClass : kuwaibaProvisioningRequisition.getKuwaibaClassList()) {
            LOG.info("creating kuwaibaClass: " + kuwaibaClass);

            String createObjectClassName = kuwaibaClass.getClassName();
            String createObjectName = kuwaibaClass.getName();
            String parentObjectName = kuwaibaClass.getParentName();
            String parentClassName = kuwaibaClass.getParentClassName();
            String templateName = null; //TODO
            HashMap<String, String> initialAttributes = kuwaibaClass.getAttributes();

            BusinessObject businessObject = createClassIfDoesntExist(createObjectClassName, createObjectName,
                      parentClassName, parentObjectName, templateName, initialAttributes);

            LOG.debug("created business object: " + businessObjectToString(businessObject));

         }

         //         BusinessObject parentObject = null;

         //         List<BusinessObject> foundObjects = bem.getObjectsWithFilter("House", Constants.PROPERTY_NAME, "6burnett");
         //         if (!foundObjects.isEmpty()) {
         //            parentObject = foundObjects.get(0);
         //
         //         }
         //
         //         LOG.debug(" found parent Object " + businessObjectToString(parentObject));
         //
         //         String createObjectClassName = "OpticalNetworkTerminal";
         //         String createObjectName = "testOnt2";
         //         String parentOid = parentObject.getId();
         //         String parentClassName = parentObject.getClassName();
         //         HashMap<String, String> initialAttributes = null;
         //
         //         String templateClass = null;
         //         String templateName = null;
         //
         //         createIfDoesntExist(createObjectClassName, createObjectName, parentOid, parentClassName, templateClass, templateName, initialAttributes);

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

         LOG.debug("Templates new: " + kuwaibaTemplatesExisting + " existing: " + kuwaibaTemplatesNew
                  + " Classes: new: " + kuwaibaClassesExisting + " existing: " + kuwaibaClassesNew);

      } catch (Exception ex) {
         taskResult.getMessages().add(TaskResult.createErrorMessage(
                  String.format("error running task " + ex)));
      }

      
      String msg = "End of task Script " + EntimossKuwaibaProvisioningTask.class.getName() + " used existing templates: " + kuwaibaTemplatesExisting +
               " newTemplates: "+ kuwaibaTemplatesNew +" existingClasses: "+ kuwaibaClassesExisting +" new Classes:"+ kuwaibaClassesNew;

      taskResult.getMessages().add(TaskResult.createInformationMessage(msg));

      LOG.debug(msg);

      return taskResult;
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
   public BusinessObject createClassIfDoesntExist(String createObjectClassName, String createObjectName, String parentClassName, 
            String parentObjectName, String templateName, HashMap<String, String> initialAttributes) {

      BusinessObject createdObject = null;
      BusinessObject parentObject = null;

      // check if object already exists
      try {
         // see if there is an object with the same name
         List<BusinessObject> foundObjects = bem.getObjectsWithFilter(createObjectClassName, Constants.PROPERTY_NAME, createObjectName);
         if (!foundObjects.isEmpty()) {
            createdObject = foundObjects.get(0);
            LOG.info("createIfDoesntExist - object already exists " + businessObjectToString(createdObject));
            kuwaibaClassesExisting++;
            return createdObject;
         }
      } catch (Exception ex) {
         LOG.error("problem finding object:", ex);
      }

      // check if parent object exists
      try {
         // see if there is an object with the same name
         List<BusinessObject> foundObjects = bem.getObjectsWithFilter(parentClassName, Constants.PROPERTY_NAME, parentObjectName);
         if (!foundObjects.isEmpty()) {
            parentObject = foundObjects.get(0);
            LOG.info("createIfDoesntExist - parentObject exists " + businessObjectToString(parentObject));
         }
      } catch (Exception ex) {
         LOG.error("problem finding parent object:", ex);
      }

      if (parentObject == null)
         throw new IllegalArgumentException("parent object does not exist for createObjectClassName=" + createObjectClassName + "  createObjectName =" +
                  createObjectName + " parentObjectName=" + parentObjectName + " parentClassName=" + parentClassName);

      TemplateObjectLight template = null;
      String templateId = null;

      // check if template exists if not null
      if (templateName != null && !templateName.isEmpty()) {
         try {
            // see if there is a template with the template name
            List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass(createObjectClassName);
            for (TemplateObjectLight tmplate : foundTemplates) {
               if (templateName.equals(tmplate.getName())) {
                  template = tmplate;
                  templateId = template.getId();
                  LOG.info("creating object " + template.getClassName() + " with template: " + template.getName() + " templateId: " + template.getId());
                  break;
               }
            }
            if (template == null) {
               throw new IllegalArgumentException("cannot find template for createObjectClassName " + createObjectClassName + " template name: " + templateName);
            }
         } catch (Exception ex) {
            LOG.error("problem finding template:", ex);
         }
      }

      if (createdObject == null) {
         // create new object with parent
         try {
            HashMap<String, String> attributes = (initialAttributes == null) ? new HashMap<String, String>() : new HashMap<String, String>(initialAttributes);
            attributes.put(Constants.PROPERTY_NAME, createObjectName);

            String createdObjectId = bem.createObject(createObjectClassName, parentClassName, parentObject.getId(), attributes, templateId);

            createdObject = bem.getObject(createObjectClassName, createdObjectId);

            LOG.info("createIfDoesntExist - created new object " + businessObjectToString(createdObject));

            kuwaibaClassesNew++;
         } catch (Exception e) {
            LOG.error("problem creating object createObjectClass " + createObjectClassName +
                     ", createObjectName:" + createObjectName + ", parentOid:" + parentObject.getId() + ", parentClassName " + parentClassName, e);
         }
      }

      return createdObject;

   }

   // overloaded toString methods for BusinessObjects
   String businessObjectToString(BusinessObject bo) {
      return (bo == null) ? "BusinessObject[ null ]"
               : "BusinessObject[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" +
                        bo.getClassDisplayName() + " getAttributes()=" + bo.getAttributes() + "]";
   }

   String businessObjectToString(BusinessObjectLight bo) {
      return (bo == null) ? "BusinessObject[ null ]"
               : "BusinessObjectLight[ getId()=" + bo.getId() + ", getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" +
                        bo.getClassDisplayName() + "]";
   }

   public static class KuwaibaProvisioningRequisition {

      private List<KuwaibaClass> kuwaibaTemplateList = new ArrayList<KuwaibaClass>();

      private List<KuwaibaClass> kuwaibaClassList = new ArrayList<KuwaibaClass>();

      public KuwaibaProvisioningRequisition() {
         super();
      }

      public List<KuwaibaClass> getKuwaibaTemplateList() {
         return kuwaibaTemplateList;
      }

      public void setKuwaibaTemplateList(List<KuwaibaClass> kuwaibaTemplateList) {
         this.kuwaibaTemplateList = kuwaibaTemplateList;
      }

      public List<KuwaibaClass> getKuwaibaClassList() {
         return kuwaibaClassList;
      }

      public void setKuwaibaClassList(List<KuwaibaClass> kuwaibaClassList) {
         this.kuwaibaClassList = kuwaibaClassList;
      }

      @Override
      public String toString() {
         return "ProvisioningRecord [kuwaibaTemplateList=" + kuwaibaTemplateList + ", kuwaibaClassList=" + kuwaibaClassList + "]";
      }

   }

   public static class KuwaibaClass {

      private String className = null;
      private String templateName = null;
      private String name = null;

      private String parentClassName = null;
      private String parentName = null;

      private HashMap<String, String> attributes = new HashMap();

      public KuwaibaClass() {
         super();
      }

      public String getClassName() {
         return className;
      }

      public void setClassName(String className) {
         this.className = className;
      }

      public String getTemplateName() {
         return templateName;
      }

      public void setTemplateName(String templateName) {
         this.templateName = templateName;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getParentClassName() {
         return parentClassName;
      }

      public void setParentClassName(String parentClassName) {
         this.parentClassName = parentClassName;
      }

      public String getParentName() {
         return parentName;
      }

      public void setParentName(String parentName) {
         this.parentName = parentName;
      }

      public HashMap<String, String> getAttributes() {
         return attributes;
      }

      public void setAttributes(HashMap<String, String> attributes) {
         this.attributes = attributes;
      }

      @Override
      public String toString() {
         return "KuwaibaClass [className=" + className + ", name=" + name + ", templateName=" + templateName + ", parentClassName=" + parentClassName + ", parentName=" + parentName + ", attributes="
                  + attributes + "]";
      }

   }

}
