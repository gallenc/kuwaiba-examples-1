package org.entimoss.kuwaiba.input.tmp1;

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
//KuwaibaImportModel3 kuwaibaImport = new KuwaibaImportModel3(bem, aem, scriptParameters);
//return kuwaibaImport.runTask();

/**
 * A simple script that processes a CSV file in order to bulk import houses. 

 */

public class KuwaibaImportModel4 {
   static Logger LOG = LoggerFactory.getLogger(KuwaibaImportModel4.class); // remove static in groovy

   public String SEPARATOR = ",";
   public int LATITUDE_COLUMN = 0;
   public int LONGITUDE_COLUMN = 1;
   public int UPRN_COLUMN = 2;

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = null; // injected in groovy
   
   int lineCount = 0; 
   int objectCount = 0;
   int errorlineCount = 0;

   public KuwaibaImportModel4(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> scriptParameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = (scriptParameters == null) ? new HashMap<String, String>() : scriptParameters;
   }

   public TaskResult runTask() {
      TaskResult taskResult = new TaskResult();

      /*
       * latitude longitude UPRN 
       * note UPRN may have leading ' to avoid interpretation as number
       */
      String csvFileName = parameters.getOrDefault("csvFileName", "/external-data/uprnCsv.csv");

      /*
       * parentLocationValue
       * The parentLocationValue can be the name property of the object or the Kuwaiba objectID of the object.
       * If the parentLocationValue is not set, all devices will be included in the tree.
       * If the parentLocationValue is not found, an exception will be thrown and the report will not complete
       * Finds the parent visible object in which all of the model will be created. e.g Neighbourhood BitternePk
       */
      String parentLocationValue = parameters.getOrDefault("parentLocationValue", "");
      
      /*
       * parentLocationObjectPrefixValue
       * unique prefix to attach to all objects created in model below the parent location
       * eg SO18BPK1
       */
      String  parentLocationObjectPrefixValue = parameters.getOrDefault("parentLocationObjectPrefixValue", "PREFIX_NOT_SET");

      /* parentFexName
       * name of the parent fibre exchange for all objects in range
       * Four letter abbreviation followed by three figures e.g SOTN001
       */
      String parentFexName = parameters.getOrDefault("parentFexName", "FEX_NOT_SET");
      
      // print out startup message
      String msg = String.format("Running Script  %s : Parameters: parentFexName %s parentLocationValue: %s parentLocationObjectPrefixValue: %s", 
               KuwaibaImportModel4.class.getSimpleName(), parentFexName, parentLocationValue, parentLocationObjectPrefixValue);
      taskResult.getMessages().add(TaskResult.createInformationMessage(msg ));
      LOG.info(msg);

      // check can read file
      File importFile = new File(csvFileName);
      if (!importFile.exists())
         return TaskResult.createErrorResult(String.format("File %s does not exist", csvFileName));

      if (!importFile.canRead())
         return TaskResult.createErrorResult(String.format("File %s exists, but it's not readable", csvFileName));

      // Parses and processes every line

      BufferedReader br = null;
      try {
         br = new BufferedReader(new FileReader(importFile));
      
         String line;
         while ((line = br.readLine()) != null) {
            
            lineCount++;

            String[] tokens = line.split(SEPARATOR);
            if (tokens.length != 3) { // All columns are mandatory, even if they're just empty
               String errormsg = String.format("Line %s does not have 3 columns as expected but %s", line, tokens.length);
               LOG.warn(errormsg);
               taskResult.getMessages().add(TaskResult.createErrorMessage(errormsg));
            }

            else {

               try {
                  LOG.warn("processing line " + line);
                  
                  // create house UPRN , ONT if doesnt exist splice 2 fibres
                  // Building / House              UPRN_<uprn>  UPRN_200001919492
                  // CSP (Customer Splice Point)   CSP_<uprn>   CSP_200001919492 note CSP has two splices IN-0 OUT-0 
                  // Optical Network Terminator    ONT_<uprn>   ONT_200001919492   eth0, pon 
                  
                  
                  // create pole
                  // parentLocationObjectPrefixValue_POLE_<NUMBER>   SO18BPK1_POLE_001
                  
                  // create pole splitters 2 splitters per pole 1:16 
                  // parentLocationObjectPrefixValue_POLE_<NUMBER>_<SPTYPE>_<NUMBER>   SO18BPK1_POLE_001_SPL16_001
                  
                  // Cabinet parentLocationObjectPrefixValue_CAB_<NUMBER>   SO18BPK1_CAB_001
                  
                  // cabinet splitters 10 splitters per cabinet 1:8 
                  // parentLocationObjectPrefixValue_CAB_<NUMBER>_<SPTYPE>_<NUMBER>     SO18BPK1_CAB_001_SPL8_001
                  
                  // create fibre container pole to house 2 fibres
                  // 1_2BFU_SO18BPK1_POLE_001_UPRN_200001919492
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  
                  // create fibre container pole to cabinet 4x12 fibres
                  // 4_12BFU_SO18BPK1_CAB_001_SO18BPK1_POLE_001
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  

                  

               } catch (Exception ie) {
                  errorlineCount++;
                  String errormsg = String.format("Error processing line %s: %s", line, ie.getMessage());
                  LOG.warn(errormsg);
                  taskResult.getMessages().add(TaskResult.createErrorMessage(errormsg));
               }
            }

         }

      } catch (Exception e) {
         LOG.error("problem running script ", e);
         taskResult.getMessages().add(TaskResult.createErrorMessage("problem running script " + e.getMessage()));
      } finally {
         if (br!=null) {
            try {
               br.close();
            } catch (IOException e) { }
         }
      }

      msg = String.format("task complete:  read  %s lines  created %s objects", lineCount, objectCount);
      taskResult.getMessages().add(TaskResult.createInformationMessage(msg));
      LOG.info(msg);
      
      return taskResult;
   }

}
