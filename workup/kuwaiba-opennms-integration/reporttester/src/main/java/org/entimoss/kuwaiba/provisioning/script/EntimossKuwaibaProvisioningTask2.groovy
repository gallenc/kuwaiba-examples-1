package org.entimoss.kuwaiba.provisioning.script;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
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

/**
 * provisioning task to read provisioning file in order.
 * if multipleNewLineObjects is set true, each non blank line is read as a separate json object.
 * THis is to allow very large files which are not stored in memory.
 * (This will not work if the Json is pretty printed)
 * if multipleNewLineObjects is set false, only one json object is read which can contain multiple objects
 */
// note use COMMIT ON EXECUTE
// uncomment in groovy script
EntimossKuwaibaProvisioningTask2 kuwaibaImport = new EntimossKuwaibaProvisioningTask2(bem, aem, scriptParameters, connectionHandler);
return kuwaibaImport.runTask();

public class EntimossKuwaibaProvisioningTask2 {
   static Logger LOG = LoggerFactory.getLogger(EntimossKuwaibaProvisioningTask2.class); 

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy
   GraphDatabaseService connectionHandler = null; //injected in groovy

   int kuwaibaTemplatesExisting = 0;
   int kuwaibaTemplatesNew = 0;
   int kuwaibaClassesExisting = 0;
   int kuwaibaClassesNew = 0;
   
   public EntimossKuwaibaProvisioningTask2(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters,GraphDatabaseService connectionHandler) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;
      this.connectionHandler = connectionHandler;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      taskResult.getMessages().add(TaskResult.createInformationMessage(
               String.format("running Script " + EntimossKuwaibaProvisioningTask2.class.getName() + " with parameters:" + parameters)));

      LOG.info("running Script " + EntimossKuwaibaProvisioningTask2.class.getName() + " with parameters:" + parameters);

      /*
       * file name and location of kuwaibaProvisioningRequisition
       * Defaults to
       */
      String kuwaibaProvisioningRequisitionFileName = parameters.getOrDefault("kuwaibaProvisioningRequisitionFileName", "/external-data/kuwaibaProvisioningRequisition-data.json");

      try {

         // TODO file with multiple separate objects
         File kuwaibaProvisioningFile = new File(kuwaibaProvisioningRequisitionFileName);
         if (!kuwaibaProvisioningFile.exists()) {
            throw new IllegalArgumentException("sctipt cannot find file:" + kuwaibaProvisioningFile);
         }

         ObjectMapper om = new ObjectMapper();

         KuwaibaProvisioningRequisition kuwaibaProvisioningRequisition = om.readValue(kuwaibaProvisioningFile, KuwaibaProvisioningRequisition.class);
         LOG.info("Starting to load requistionFile " + kuwaibaProvisioningFile.getAbsolutePath() + " containing " + kuwaibaProvisioningRequisition.getKuwaibaTemplateList().size() +
                  " templates and" + kuwaibaProvisioningRequisition.getKuwaibaClassList().size() + " classes");



         //create new templates
         createTemplates(kuwaibaProvisioningRequisition.getKuwaibaTemplateList());

         // create new objects
         for (KuwaibaClass kuwaibaClass : kuwaibaProvisioningRequisition.getKuwaibaClassList()) {
            LOG.info("creating kuwaibaClass: " + kuwaibaClass);

            String createObjectClassName = kuwaibaClass.getClassName();
            String createObjectName = kuwaibaClass.getName();
            String parentObjectName = kuwaibaClass.getParentName();
            String parentClassName = kuwaibaClass.getParentClassName();
            String templateName = kuwaibaClass.getTemplateName();
            HashMap<String, String> initialAttributes = kuwaibaClass.getAttributes();

            BusinessObject businessObject = createClassIfDoesntExist(createObjectClassName, createObjectName,
                      parentClassName, parentObjectName, templateName, initialAttributes);

            LOG.info("created business object: " + businessObjectToString(businessObject));

         }


         LOG.info("Templates new: " + kuwaibaTemplatesExisting + " existing: " + kuwaibaTemplatesNew +
                  " Classes: new: " + kuwaibaClassesExisting + " existing: " + kuwaibaClassesNew);

      } catch (Exception ex) {
         LOG.error("problem running task",ex);
         taskResult.getMessages().add(TaskResult.createErrorMessage(
                  String.format("error running task " + ex)));
      }

      
      String msg = "End of task Script " + EntimossKuwaibaProvisioningTask2.class.getName() + " used existing templates: " + kuwaibaTemplatesExisting +
               " newTemplates: "+ kuwaibaTemplatesNew +" existingClasses: "+ kuwaibaClassesExisting +" new Classes:"+ kuwaibaClassesNew;

      taskResult.getMessages().add(TaskResult.createInformationMessage(msg));

      LOG.info(msg);

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

      String existingChildId = null;
      
      HashMap<String, String> newAttributes = (initialAttributes == null) ? new HashMap<String, String>() : new HashMap<String, String>(initialAttributes);
      newAttributes.put(Constants.PROPERTY_NAME, createObjectName);

      // check if template exists if not null
      if (templateName != null && !templateName.isEmpty()) {
         try {

            // check if child of parent has been made with the same template (i.e. its name starts with the template name). 
            // If it does, then we want to use this object and update it's properties rather than create a new object
            List<BusinessObject> childObjects = bem.getChildrenOfClass(parentObject.getId(), parentObject.getClassName(), createObjectClassName, 0, 0);
            for (BusinessObject child : childObjects) {
               if (child.getName().startsWith(templateName)) {
                  existingChildId = child.getId();
                  createdObject = child;
                  
                  LOG.info("matched templateName=" + templateName + " child object="+businessObjectToString(child) + " will be updated in parent "+
                               businessObjectToString(parentObject));
                  
                  bem.updateObject(child.getClassName(), existingChildId, newAttributes);
                  
                  break;
               }
            }

            // if there is no existing candidate object, find the template to create a new object 
            if (existingChildId == null) {
               
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
            }
         } catch (Exception ex) {
            LOG.error("problem finding template:", ex);
         }
      }

      if (createdObject == null) {
         // create new object with parent
         try {

            String createdObjectId = bem.createObject(createObjectClassName, parentClassName, parentObject.getId(), newAttributes, templateId);

            // this is added because the created object takes the name of the template and not the name we want to give it.
            bem.updateObject(createObjectClassName, createdObjectId, newAttributes);

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
   
   public int createChildTemplateElements(List<KuwaibaTemplateDefinition> kuwaibaChildTemplateElementList, String elementParentClassName, String elementParentId) {
      int templateElementsCreated = 0;

      LOG.warn("creating " + kuwaibaChildTemplateElementList.size() + " child template elements");
      for (KuwaibaTemplateDefinition templateElement : kuwaibaChildTemplateElementList) {

         String function = templateElement.getTemplateFunction();

         if (function == null || function.isEmpty()) {

            try {
               String clildId = null;
               if (templateElement.getSpecial()) {
                  //.createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName)
                  clildId = aem.createTemplateSpecialElement(templateElement.getClassName(), elementParentClassName, elementParentId,
                           templateElement.getTemplateElementName());
               } else {
                  clildId = aem.createTemplateElement(templateElement.getClassName(), elementParentClassName, elementParentId, templateElement.getTemplateElementName());
               }
               LOG.warn("created child:" + templateElement + ", clildId=" + clildId + " template element for elementParentClassName=" +
                        elementParentClassName + ", elementParentId=" + elementParentId);
               templateElementsCreated++;

               if (templateElement.getChildKuwaibaTemplateDefinitions() != null && !templateElement.getChildKuwaibaTemplateDefinitions().isEmpty()) {
                  templateElementsCreated = templateElementsCreated + createChildTemplateElements(templateElement.getChildKuwaibaTemplateDefinitions(), templateElement.getClassName(), clildId);
               }

            } catch (Exception ex) {
               throw new IllegalArgumentException("problem creating child template element for elementParentClassName" +
                        elementParentClassName + ", elementParentId" + elementParentId + " child:" + templateElement, ex);
            }

         } else {
            try {

               HashMap<String, String> functionAttributes = templateElement.getTemplateFunctionAttributes();

               LOG.info("trying to create template elements from function=" + function + " with functionAttributes=" + functionAttributes + " className=" +
                        templateElement.getClassName() + "  templateElementParentClassName=" + elementParentClassName + "  templateElementParentId=" + elementParentId);

               templateElementsCreated = templateElementsCreated + createTemplateElementsFromFunction(null, templateElement.getTemplateElementName(), elementParentClassName, elementParentId, function, functionAttributes);

            } catch (Exception ex) {
               throw new IllegalArgumentException("problem creating child template element from function for elementParentClassName=" +
                        elementParentClassName + ", elementParentId=" + elementParentId + " child:" + templateElement, ex);
            }

         }
      }

      return templateElementsCreated;

   }

   public int createTemplateElementsFromFunction(String templateName, String templateElementName, String templateElementParentClassName, String templateElementParentId, String function,
            HashMap<String, String> functionAttributes) {
      int elementsCreated = 0;

      LOG.info("trying to create new template element (templateName="+templateName+ ") for parent class=" + templateElementParentClassName +
               " id=" + templateElementParentId +
               " from function=" + function + " with functionAttributes=" + functionAttributes);

      try {
         switch (function) {

         case "FiberSplitterFunction":
            elementsCreated = elementsCreated + createOpticalFiberSplitterTemplateElements(templateName, templateElementName, templateElementParentClassName, templateElementParentId, functionAttributes);
            break;

         case "OpticalSpliceBoxFunction":
            elementsCreated = elementsCreated + createOpticalSpliceBoxTemplateElements(templateName, templateElementName, templateElementParentClassName, templateElementParentId, functionAttributes);
            break;

         case "ColoredFiberWireContainerFunction":
            elementsCreated = elementsCreated + createColoredOpticalFiberContainerTemplateElements(templateName, templateElementName, templateElementParentClassName, templateElementParentId, functionAttributes);
            break;

         default:
            throw new IllegalArgumentException("template function does not exist: " + function);
         }
      } catch (Exception ex) {
         throw new IllegalArgumentException("problem creating child template element from function", ex);
      }

      return elementsCreated;
   }

   public int createTemplates(List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList) {

      int templateElementsCreated = 0;

      for (KuwaibaTemplateDefinition kuwaibaTemplateDefinition : kuwaibaTemplateDefinitionList) {

         String className = kuwaibaTemplateDefinition.getClassName();

         HashMap<String, String> functionAttributes = kuwaibaTemplateDefinition.getTemplateFunctionAttributes();

         List<KuwaibaTemplateDefinition> childKuwaibaTemplateElements = kuwaibaTemplateDefinition.getChildKuwaibaTemplateDefinitions();

         // if templateName is set, this is a top level template and templateElementName must not be set
         // template functions can only be called for top level templates
         String templateName = kuwaibaTemplateDefinition.getTemplateName();

         String templateElementName = kuwaibaTemplateDefinition.getTemplateElementName();

         // templateName  must be set
         if ((templateName == null || !templateName.isEmpty())) {
            new IllegalArgumentException("templateName must be set");
         }
         if ((className == null || !className.isEmpty())) {
            new IllegalArgumentException("className must be set");
         }

         // create new template
         String templateId = null;

         try {
            // check if template already exists 
            List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass(className);
            for (TemplateObjectLight tmplate : foundTemplates) {
               if (templateName.equals(tmplate.getName())) {
                  templateId = tmplate.getId();
                  break;
               }
            }

            if (templateId != null) {
               LOG.info("template " + templateName + " already exists, will not create a new template with templateId=" + templateId);

            } else {
               // if template doesn't exist

               String function = kuwaibaTemplateDefinition.getTemplateFunction();

               if (function == null || function.isEmpty()) {
                  LOG.info("trying to create without function new template name = " + templateName);

                  // no function so create a simple template for this class
                  templateId = aem.createTemplate(className, templateName);
                  templateElementsCreated++;
                  LOG.info("template " + templateName + " Was created. New templateId=" + templateId);

                  if (childKuwaibaTemplateElements != null && !childKuwaibaTemplateElements.isEmpty()) {
                     // recursively create child templates
                     templateElementsCreated = templateElementsCreated + createChildTemplateElements(childKuwaibaTemplateElements, className, templateId);

                  }

               } else {

                  LOG.info("trying to create new template " + templateName + " with class name=" + className + " from function=" + function + " with functionAttributes=" + functionAttributes);

                  templateElementsCreated = templateElementsCreated + createTemplateElementsFromFunction(templateName, templateElementName, null, null,  function, functionAttributes);

                  LOG.info("template " + templateName + " was created using function " + function + " new templateId=" + templateId);

               }

            }

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating template name " + templateName, e);
         }

      }

      return templateElementsCreated;

   }

   public int createOpticalFiberSplitterTemplateElements(String templateName, String templateElementName, String templateElementParentClassName, String templateElementParentId,
            HashMap<String, String> functionAttributes) {

      int elementsCreated = 0;

      if (functionAttributes.get("numberOfPorts") == null)
         throw new IllegalArgumentException("OpticalSplitterFunction number of ports not set ");

      try {
         Integer numberOfPorts = Integer.parseInt(functionAttributes.get("numberOfPorts"));
         
         // if templateName!= null then this is the top of the tree so create a template 
         String elementId = templateElementParentId;
         
         if (templateName!=null) {
            elementId = aem.createTemplate("FiberSplitter", templateName);
         } else {
            String nameOfTemplateElement = (templateElementName !=null ) ? templateElementName : "Splitter";
            elementId = aem.createTemplateElement("FiberSplitter", templateElementParentClassName, templateElementParentId, nameOfTemplateElement);
         }

         
         String templateElementNamePattern = "[multiple-mirror(1," + numberOfPorts + ")]";

         List<String> childTemplateElementIds = Arrays
                  .asList(aem.createBulkTemplateElement("OpticalPort", "FiberSplitter", elementId, templateElementNamePattern));

         elementsCreated = childTemplateElementIds.size();

         for (String childId : childTemplateElementIds) {
            LOG.info("created splitter optical port template element  id=" + childId);

            // fails because getTemplateElement does not close transaction
            //TemplateObject templateObject = aem.getTemplateElement("OpticalPort", childId);
            // LOG.info("created splitter optical port name "+templateObject.getName()+" id=" +templateObject.getId());
         }

         return elementsCreated;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating splitter ports for parentClassName=" +
                  templateElementParentClassName + " parentId=" + templateElementParentId, e);
      }

   }

   public int createOpticalSpliceBoxTemplateElements(String templateName, String templateElementName, String templateElementParentClassName, String templateElementParentId,
            HashMap<String, String> functionAttributes) {

      int elementsCreated = 0;

      if (functionAttributes.get("numberOfPorts") == null)
         throw new IllegalArgumentException("SpliceBoxFunction number of ports not set ");

      try {
         Integer numberOfPorts = Integer.parseInt(functionAttributes.get("numberOfPorts"));
         
         // if templateName!= null then this is the top of the tree so create a template 
         String elementId = templateElementParentId;
         
         if (templateName!=null) {
            elementId = aem.createTemplate("SpliceBox", templateName);
         } else {
            String nameOfTemplateElement = (templateElementName !=null ) ? templateElementName : "SpliceBox";
            elementId = aem.createTemplateElement("SpliceBox", templateElementParentClassName, templateElementParentId, nameOfTemplateElement);
         }

         String templateElementNamePattern = "[mirror(1," + numberOfPorts + ")]";

         // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
         List<String> childTemplateElementIds = Arrays
                  .asList(aem.createBulkTemplateElement("OpticalPort", "SpliceBox", elementId , templateElementNamePattern));

         elementsCreated = childTemplateElementIds.size();

         for (String childId : childTemplateElementIds) {
            LOG.info("created SpliceBox optical port template element  id=" + childId);

            // fails because getTemplateElement does not close transaction
            //            TemplateObject templateObject = aem.getTemplateElement("OpticalPort", childId);
            //            LOG.info("created splitter optical port name "+templateObject.getName()+" id=" +templateObject.getId());
         }

         return elementsCreated;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating splice ports for parentClassName=" +
                  templateElementParentClassName + " parentId=" + templateElementParentId, e);
      }

   }

   public int createColoredOpticalFiberContainerTemplateElements(String templateName, String templateElementName, String templateElementParentClassName, String templateElementParentId,
            HashMap<String, String> functionAttributes) {

      int elementsCreated = 0;

      if (functionAttributes.get("numberOfCables") == null)
         throw new IllegalArgumentException("OpticalFiberContainerTemplateFunction numberOfCables not set ");
      if (functionAttributes.get("numberOfFibers") == null)
         throw new IllegalArgumentException("OpticalFiberContainerTemplateFunction numberOfFibers not set ");

      Integer numberOfCables = Integer.parseInt(functionAttributes.get("numberOfCables"));
      Integer numberOfFibers = Integer.parseInt(functionAttributes.get("numberOfFibers"));

      try {
         
         // if templateName!= null then this is the top of the tree so create a template 
         String elementId = templateElementParentId;
         
         if (templateName!=null) {
            elementId = aem.createTemplate("WireContainer", templateName);
         } else {
            String nameOfTemplateElement = (templateElementName !=null ) ? templateElementName :  "WireContainer";
            elementId = aem.createTemplateElement("WireContainer", templateElementParentClassName, templateElementParentId, nameOfTemplateElement);
         }
         
         for (int cableNo = 1; cableNo <= numberOfCables; cableNo++) {
            String cableName = String.format("%02d", cableNo) + "-" + getColourForStrand(cableNo);

            // .createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName)
            String cableObjectId = aem.createTemplateSpecialElement("WireContainer", "WireContainer", elementId, cableName);
            LOG.info("created cable id=" + cableObjectId + " cable name=" + cableName);

            // create fibers inside cable
            for (int fiberNo = 1; fiberNo <= numberOfFibers; fiberNo++) {
               String fiberName = String.format("%02d", fiberNo) + "-" + getColourForStrand(fiberNo);
               String opticalLinkObjectId = aem.createTemplateSpecialElement("OpticalLink", "WireContainer", cableObjectId, fiberName);
               LOG.info("created optical link id=" + opticalLinkObjectId + " cable name=" + fiberName);
               elementsCreated++;
            }

         }

         return elementsCreated;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating wire container for parentClassName=" +
                  templateElementParentClassName + " parentId=" + templateElementParentId, e);
      }

   }

   /*
    *  // https://www.thefoa.org/tech/ColCodes.htm
    *  Inside the cable or inside each tube in a loose tube cable, individual fibers will be color coded for identification
    *  1  Blue,  2  Orange, 3  Green, 4  Brown ,5  Slate, 6  White, 7  Red, 8  Black, 9  Yellow, 10    Violet, 11    Rose, 12    Aqua
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

   
   /*
    * These classes could be in separate java classes if not in a groovy script
    */
   public static class KuwaibaClass {
      
      private String className = null;
      private String templateName = null;
      private String name = null;
      private Boolean special = false;
   
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
   
      public Boolean getSpecial() {
         return special;
      }
   
      public void setSpecial(Boolean special) {
         this.special = special;
      }
   
      @Override
      public String toString() {
         return "KuwaibaClass [className=" + className + ", name=" + name + ", templateName=" + templateName + ", special=" + special +
                  ", parentClassName=" + parentClassName + ", parentName=" + parentName + ", attributes=" + attributes + "]";
      }
   
   }
   
   
   
   public static class KuwaibaProvisioningRequisition {
      
      private List<KuwaibaTemplateDefinition> kuwaibaTemplateList = new ArrayList<KuwaibaTemplateDefinition>();
   
      private List<KuwaibaClass> kuwaibaClassList = new ArrayList<KuwaibaClass>();
      
      public KuwaibaProvisioningRequisition() {
         super();
      }
      
      public List<KuwaibaTemplateDefinition> getKuwaibaTemplateList() {
         return kuwaibaTemplateList;
      }
   
      public void setKuwaibaTemplateList(List<KuwaibaTemplateDefinition> kuwaibaTemplateList) {
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


   
   public static class KuwaibaTemplateDefinition {
      
      private String templateName = null;
      
      private String templateElementName = null;
      
      private String className = null;
   
      private String templateFunction = null;
      
      private Boolean special = false;
      
      private List<KuwaibaTemplateDefinition> childKuwaibaTemplateDefinitions = new ArrayList<KuwaibaTemplateDefinition>();
      
      private HashMap<String,String> templateFunctionAttributes = new HashMap<String,String>();
   
      public KuwaibaTemplateDefinition() {
         super();
      }
   
      public String getTemplateName() {
         return templateName;
      }
   
      public void setTemplateName(String templateName) {
         this.templateName = templateName;
      }
   
      public String getTemplateElementName() {
         return templateElementName;
      }
   
      public void setTemplateElementName(String templateElementName) {
         this.templateElementName = templateElementName;
      }
   
      public List<KuwaibaTemplateDefinition> getChildKuwaibaTemplateDefinitions() {
         return childKuwaibaTemplateDefinitions;
      }
   
      public void setChildKuwaibaTemplateDefinitions(List<KuwaibaTemplateDefinition> childKuwaibaTemplateDefinitions) {
         this.childKuwaibaTemplateDefinitions = childKuwaibaTemplateDefinitions;
      }
   
      public String getClassName() {
         return className;
      }
   
      public void setClassName(String className) {
         this.className = className;
      }
   
      public String getTemplateFunction() {
         return templateFunction;
      }
   
      public void setTemplateFunction(String templateFunction) {
         this.templateFunction = templateFunction;
      }
   
      public HashMap<String, String> getTemplateFunctionAttributes() {
         return templateFunctionAttributes;
      }
   
      public void setTemplateFunctionAttributes(HashMap<String, String> templateFunctionAttributes) {
         this.templateFunctionAttributes = templateFunctionAttributes;
      }
      
      public Boolean getSpecial() {
         return special;
      }
   
      public void setSpecial(Boolean special) {
         this.special = special;
      }
   
      @Override
      public String toString() {
         return "KuwaibaTemplateDefinition [templateName=" + templateName + ", templateElementName=" + templateElementName +
                  ", className=" + className + ", templateFunction=" + templateFunction + ", special=" + special +
                  ", childKuwaibaTemplateDefinitions=" + childKuwaibaTemplateDefinitions + ", templateFunctionAttributes=" +
                  templateFunctionAttributes + "]";
      }

   }

}
