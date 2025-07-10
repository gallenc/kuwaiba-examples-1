package org.entimoss.kuwaiba.input.tmp2;

import org.entimoss.kuwaiba.provisioning.KuwaibaTemplateDefinition;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
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
//KuawabaSimpleTestsXX kuwaibaImport = new KuawabaSimpleTestsXX(bem, aem, scriptParameters);
//return kuwaibaImport.runTask();

/**

 */

public class KuawabaSimpleTestsXX {
   static Logger LOG = LoggerFactory.getLogger(KuawabaSimpleTestsXX.class); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy

   GraphDatabaseService connectionHandler = null; //injected in groovy

   public KuawabaSimpleTestsXX(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;
      this.connectionHandler = connectionHandler;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      taskResult.getMessages().add(TaskResult.createInformationMessage(
               String.format("running Script " + KuawabaSimpleTestsXX.class.getName() + " with parameters:" + parameters)));

      LOG.info("running Script " + KuawabaSimpleTestsXX.class.getName() + " with parameters:" + parameters);

      String templateClassName = "FiberSplitter";

      //   String templateId=null;
      //  String templateElementClass;
      //  String templateElementId;

      /* TODO
       * create template with name for splitter etc 
       */
      try {

         try {

            String templateName = "WCTest1";
            int numberOfCables = 4;
            int numberOfFibers = 12;
            // createColouredOpticalFibreContainerTemplate(templateName, numberOfCables, numberOfFibers);

            //createOpticalFibreSplitterTemplate("fibre-split16", 16);

            //createOpticalSpliceBoxTemplate("splice-test1", 16);

            //            // create wirecontainer
            //            String templateId = aem.createTemplate("WireContainer", "WCTest1");
            //            String templateElementClassName = "WireContainer";
            //            String templateElementParentClassName = "WireContainer";
            //            String templateElementParentId = templateId;
            //            String templateElementNamePattern = "[sequence(1,4)]";
            //            // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
            //            List<String> childTemplateElementIds = Arrays
            //                     .asList(aem.createBulkSpecialTemplateElement(templateElementClassName, templateElementParentClassName, templateElementParentId, templateElementNamePattern));
            //
            //            for (String childId : childTemplateElementIds) {
            //               LOG.info("template child " + childId);
            //
            //               List<String> child2TemplateElementIds = Arrays.asList(aem.createBulkSpecialTemplateElement("OpticalLink",
            //                        "WireContainer", childId, "[sequence(1,12)]"));
            //               LOG.info("template child 2 " + childId);
            //
            //            }
            //
            //            // see if there is a template with the template name
            //            List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass(templateClassName);
            //            for (TemplateObjectLight tmplate : foundTemplates) {
            //               String tclassname = tmplate.getClassName();
            //               String templateElementId = tmplate.getId();
            //               String tname = tmplate.getName();
            //               LOG.debug("template: className=" + tclassname + " id=" + templateElementId + " name=" + tname);
            //
            //               //causes transaction fail
            //               // List<TemplateObjectLight> children = aem.getTemplateElementChildren(tclassname , templateElementId);
            //               // LOG.info("children:"+children);
            //            }

         } catch (Exception ex) {
            throw new IllegalArgumentException("problem creating template:", ex);
         }

      } catch (Exception ex) {
         LOG.error("error running task:", ex);
         taskResult.getMessages().add(TaskResult.createErrorMessage("error running task " + ex));
      }

      LOG.info("end of Script " + KuawabaSimpleTestsXX.class.getName());

      return taskResult;
   }

   BusinessObject findOrCreateIfDoesntExist(String className, String classTemplate, String name, String parentClass, String parentClassName,
            String latitude, String longitude, String IpAddress, String Comment, String serialNumber, String assetNumber) {

      return null;
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

   // List<KuwaibaTemplateElement> kuwaibaTemplateList = new ArrayList<KuwaibaTemplateElement>();

   public void createTemplates(List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList, String parentTemplateElementId) {

      for (KuwaibaTemplateDefinition kuwaibaTemplateDefinition : kuwaibaTemplateDefinitionList) {

         String className = kuwaibaTemplateDefinition.getClassName();
         String templateName = kuwaibaTemplateDefinition.getTemplateName();
         String templateElementName = kuwaibaTemplateDefinition.getTemplateElementName();
         HashMap<String, String> functionAttributes = kuwaibaTemplateDefinition.getTemplateFunctionAttributes();

         List<KuwaibaTemplateDefinition> childKuwaibaTemplateElements = kuwaibaTemplateDefinition.getChildKuwaibaTemplateDefinitions();

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
               LOG.info("template " + templateName + "already exists, will not create new templateId=" + templateId);
               
            // if template doesn't exist
            } else {
            
               LOG.info("trying to create new template " + templateName);

               String function = kuwaibaTemplateDefinition.getTemplateFunction();

               if (function == null || function.isEmpty()) {
                  // no function so create a simple template for this class
                  templateId = aem.createTemplate(className, templateName);

               } else {

                  switch (function) {

                  case "fiberSplitter":
                     templateId = createOpticalFiberSplitterTemplate(className, templateName, functionAttributes);
                     break;

                  case "opticalSpliceBox":
                     templateId = createOpticalSpliceBoxTemplate(className, templateName, functionAttributes);
                     break;

                  case "coloredFiberContainer":
                     templateId = createColoredOpticalFiberContainerTemplate(className, templateName, functionAttributes);
                     break;

                  default:
                     throw new IllegalArgumentException("tempalate function does not exist: " + function);

                  }
               }

            }

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating template name " + templateName, e);
         }
      }

   }

   public String createOpticalFiberSplitterTemplate(String className, String templateName, HashMap<String, String> functionAttributes) {

      if (!"FiberSplitter".equals(className)) {
         throw new IllegalArgumentException("cannot run FiberSplitter function for class=" + className);
      }

      try {
         Integer numberOfPorts = Integer.parseInt(functionAttributes.get("numberOfPorts"));

         LOG.info("creating optical splitter template templateName=" + templateName + " number of ports=" + numberOfPorts);

         // check if template name exists
         List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass("FiberSplitter");
         for (TemplateObjectLight tmplate : foundTemplates) {
            if (templateName.equals(tmplate.getName())) {
               throw new IllegalArgumentException("template already exists " + tmplate.getClassName() + " template name: " + tmplate.getName() +
                        " template id: " + tmplate.getId());
            }
         }

         // create FibreSplitter template
         String templateId = aem.createTemplate("FiberSplitter", templateName);

         String templateElementNamePattern = "[multiple-mirror(1," + numberOfPorts + ")]";

         // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
         List<String> childTemplateElementIds = Arrays
                  .asList(aem.createBulkTemplateElement("OpticalPort", "FiberSplitter", templateId, templateElementNamePattern));

         for (String childId : childTemplateElementIds) {
            LOG.info("created splitter optical port OUT template element  id=" + childId);

            // fails because getTemplateElement does not close transaction
            //TemplateObject templateObject = aem.getTemplateElement("OpticalPort", childId);
            // LOG.info("created splitter optical port name "+templateObject.getName()+" id=" +templateObject.getId());
         }

         return templateId;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating template name " + templateName, e);
      }

   }

   public String createOpticalSpliceBoxTemplate(String className, String templateName, HashMap<String, String> functionAttributes) {

      if (!"SpliceBox".equals(className)) {
         throw new IllegalArgumentException("cannot run SpliceBox function for class=" + className);
      }

      try {
         Integer numberOfPorts = Integer.parseInt(functionAttributes.get("numberOfPorts"));

         LOG.info("creating optical splice box template templateName=" + templateName + " number of ports=" + numberOfPorts);
         // check if template name exists

         List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass("SpliceBox");
         for (TemplateObjectLight tmplate : foundTemplates) {
            if (templateName.equals(tmplate.getName())) {
               throw new IllegalArgumentException("template already exists " + tmplate.getClassName() + " template name: " + tmplate.getName() +
                        " template id: " + tmplate.getId());
            }
         }

         // create splice box template
         String templateId = aem.createTemplate("SpliceBox", templateName);

         //String templateElementNamePattern = "OUT-[sequence(1,"+numberOfPorts+ ")]";  
         String templateElementNamePattern = "[mirror(1," + numberOfPorts + ")]";

         // String templateElementClassName, String templateElementParentClassName, String templateElementParentId, String templateElementNamePattern
         List<String> childTemplateElementIds = Arrays
                  .asList(aem.createBulkTemplateElement("OpticalPort", "SpliceBox", templateId, templateElementNamePattern));

         for (String childId : childTemplateElementIds) {
            LOG.info("created SpliceBox optical port template element  id=" + childId);

            // fails because getTemplateElement does not close transaction
            //            TemplateObject templateObject = aem.getTemplateElement("OpticalPort", childId);
            //            LOG.info("created splitter optical port name "+templateObject.getName()+" id=" +templateObject.getId());
         }

         return templateId;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating template name " + templateName, e);
      }

   }

   public String createColoredOpticalFiberContainerTemplate(String className, String templateName, HashMap<String, String> functionAttributes) {

      if (!"WireContainer".equals(className)) {
         throw new IllegalArgumentException("cannot run FiberSplitter function for class=" + className);
      }

      Integer numberOfCables = Integer.parseInt(functionAttributes.get("numberOfCables"));
      Integer numberOfFibers = Integer.parseInt(functionAttributes.get("numberOfFibers"));

      LOG.info("creating wire container template templateName " + templateName + " number of cables " + numberOfCables + " number of fibers " + numberOfFibers);

      try {
         // check if template name exists

         List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass("WireContainer");
         for (TemplateObjectLight tmplate : foundTemplates) {
            if (templateName.equals(tmplate.getName())) {
               throw new IllegalArgumentException("template already exists " + tmplate.getClassName() + " template name: " + tmplate.getName() +
                        " template id: " + tmplate.getId());
            }
         }

         // create wire container template
         String templateId = aem.createTemplate("WireContainer", templateName);

         for (int cableNo = 1; cableNo <= numberOfCables; cableNo++) {
            String cableName = String.format("%02d", cableNo) + "-" + getColourForStrand(cableNo);

            // .createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, String tsElementParentId, String tsElementName)
            String cableObjectId = aem.createTemplateSpecialElement("WireContainer", "WireContainer", templateId, cableName);
            LOG.info("created cable id=" + cableObjectId + " cable name=" + cableName);

            // create fibers inside cable
            for (int fiberNo = 1; fiberNo <= numberOfFibers; fiberNo++) {
               String fiberName = String.format("%02d", fiberNo) + "-" + getColourForStrand(fiberNo);
               String opticalLinkObjectId = aem.createTemplateSpecialElement("OpticalLink", "WireContainer", cableObjectId, fiberName);
               LOG.info("created optical link id=" + opticalLinkObjectId + " cable name=" + fiberName);
            }

         }

         return templateId;

      } catch (Exception e) {
         throw new IllegalArgumentException("problem creating template name " + templateName, e);
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

}
