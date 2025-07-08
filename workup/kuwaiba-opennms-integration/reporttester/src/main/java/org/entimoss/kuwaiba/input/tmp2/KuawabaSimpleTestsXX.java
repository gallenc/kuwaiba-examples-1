package org.entimoss.kuwaiba.input.tmp2;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
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
//KuawabaSimpleTestsXX kuwaibaImport = new KuawabaSimpleTestsXX(bem, aem, scriptParameters, connectionHandler);
//return kuwaibaImport.runTask();

/**

 */

public class KuawabaSimpleTestsXX {
   static Logger LOG = LoggerFactory.getLogger(KuawabaSimpleTestsXX.class); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy
   
   GraphDatabaseService connectionHandler = null; //injected in groovy


   public KuawabaSimpleTestsXX(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters, GraphDatabaseService connectionHandler) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;
      this.connectionHandler =  connectionHandler;

   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();
      
      taskResult.getMessages().add(TaskResult.createInformationMessage(
             String.format("running Script "+KuawabaSimpleTestsXX.class.getName()+" with parameters:" +parameters)));
      
      LOG.debug("running Script "+KuawabaSimpleTestsXX.class.getName()+" with parameters:" +parameters);

      String templateClassName="FiberSplitter";
      String templateName = null;
   //   String templateId=null;
    //  String templateElementClass;
    //  String templateElementId;

      
      try {
         
         // this is needed because tx.success not invoked in aem
         try (Transaction tx = connectionHandler.beginTx()){
            // see if there is a template with the template name
            List<TemplateObjectLight> foundTemplates = aem.getTemplatesForClass(templateClassName);
            for (TemplateObjectLight tmplate : foundTemplates) {
               String tclassname = tmplate.getClassName();
               String templateElementId = tmplate.getId();
               String tname = tmplate.getName();
               LOG.debug("template: className="+tclassname+" id="+templateElementId+" name="+tname);
               
               List<TemplateObjectLight> children = aem.getTemplateElementChildren(tclassname , templateElementId);
               LOG.info("children:"+children);
            }
            
            tx.success();

         } catch (Exception ex) {
            LOG.error("problem finding template:", ex);
         }



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
