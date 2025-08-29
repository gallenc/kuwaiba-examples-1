package org.entimoss.misc.test;

import static org.junit.Assert.*;
/*
 *  Copyright 2025 Entimoss Ltd (craig.gallen@entimoss.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import org.entimoss.kuwaiba.provisioning.ContainerColour;
import org.entimoss.kuwaiba.provisioning.KuwaibaClass;
import org.entimoss.kuwaiba.provisioning.KuwaibaProvisioningRequisition;
import org.entimoss.kuwaiba.provisioning.KuwaibaTemplateDefinition;
import org.entimoss.kuwaiba.provisioning.KuwaibaConnection;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * This test class creates an kuwaiba requisition direct from UPRN data
 */
public class KuwaibaRequisitionFromGponDataTest {
   static Logger LOG = LoggerFactory.getLogger(KuwaibaRequisitionFromGponDataTest.class); // remove static in groovy

   public static final String DUMMY_IP_ADDRESS = "254.0.0.1";

   String requisitionOutputFileLocation = "./target/requisitions/";

   /*
    * latitude longitude UPRN 
    * note UPRN may have leading ' to avoid interpretation as number
    */
   //String csvFileName = "./src/test/resources/modelimportCsv/uprnBitternePk1.csv";

   String csvFileName = "./src/test/resources/modelimportCsv/uprnBitternePk1nominatum-modified.csv";

   /*
    * parentLocationValue
    * The parentLocationValue can be the name property of the object or the Kuwaiba objectID of the object.
    * If the parentLocationValue is not set, all devices will be included in the tree.
    * If the parentLocationValue is not found, an exception will be thrown and the report will not complete
    * Finds the parent visible object in which all of the model will be created. e.g Neighbourhood BitternePk
    */
   String parentLocationValue = "BitternePk";

   /*
    * parentLocationObjectPrefixValue
    * unique prefix to attach to all objects created in model below the parent location
    * eg SO18BPK1
    */
   String parentLocationObjectPrefixValue = "SO18BPK1";

   /* parentFexName
    * name of the parent fibre exchange for all objects in range
    * Four letter abbreviation followed by three figures e.g SOTN001
    */
   String parentFexName = "SOTN001";

   String vendor = "NOKIA";

   Double fexLatitude = Double.valueOf("50.9178581");

   Double fexLongitude = Double.valueOf("-1.3762576");

   /*
    * Foreign source name used in naming the file and also in parent foreign source references
    */
   String foreignSource = "testGponRequisition1";

   /*
    * used as default location for minion gathering data
    */
   String defaultLocation = "Default";

   /*
    * UPRN_limitLines defines range in file to read
    * set to number of lines to read or null if read to end.
    */
   //TODO - REMOVE limit lines for full file
   Integer UPRN_limitLines = 100;

   /*
    * set true if WireContainers should be created
    */
   Boolean createContainers = true;

   /*
    * set true if optical circuits should be generated
    */
   Boolean generateCircuits = true;

   /* lteRangeStartNumber
    * number to start range of lte in FEX (e.g one lte per region)
    */
   int oltRangeStartNumber = 100;

   int PRIMARY_SPLIT_RATIO = 8; // PRIMARY SPLITTERS IN CABINETS
   int SECONDARY_SPLIT_RATIO = 16; // SECONDARY SPLITTERS ON POLES split 16

   int SPLITTERS_PER_CAB = 10;
   int SPLITTERS_TO_USE_PER_CAB = 8;// room for 10 in cabinet 
   int SPLITTERS_PER_POLE = 2; // room for 2 on pole
   int SPLITTERS_TO_USE_PER_POLE = 2;

   int PORTS_PER_OLT_CARD = 8;
   int CARDS_PER_OLT = 2;

   // csv columns
   public String SEPARATOR = ",";
   public int LATITUDE_COLUMN = 0;
   public int LONGITUDE_COLUMN = 1;
   public int UPRN_COLUMN = 2;
   public int ROAD_COLUMN = 3;
   public int NUMBER = 4;
   public int FULL_ADDRESS = 5;

   @Test
   public void test1() {
      LOG.info("**** start of test1");

      int lineCount = 0;
      int objectCount = 0;
      int errorlineCount = 0;
      int uprnCount = 0;

      //name , latitude (0),longitude(1)
      Map<String, List<Double>> cabinets = new HashMap<String, List<Double>>();
      Map<String, List<Double>> poles = new HashMap<String, List<Double>>();

      BufferedReader br = null;

      KuwaibaProvisioningRequisition pr = null;

      try {

         KuwaibaGponProvisoner kuwaibaGponProvisoner = new KuwaibaGponProvisoner();

         kuwaibaGponProvisoner.addTemplatesToProvisioningRequisition();

         kuwaibaGponProvisoner.addStaticObjectsToProvisioningRequisition();

         br = new BufferedReader(new FileReader(csvFileName));

         String line;

         br.readLine(); // skip header line

         while ((line = br.readLine()) != null) {

            // note you want to maintain the line count even if you cant read a line
            lineCount++;

            if (UPRN_limitLines != null && lineCount > UPRN_limitLines) {
               break;
            }

            List<String> csvColumns = Arrays.asList(line.split(SEPARATOR));
            if (csvColumns.size() < 3) { // All columns are mandatory, even if they're just empty
               String errormsg = String.format("Line %s does not have at least 3 columns as expected but %s", line, csvColumns.size());
               LOG.warn(errormsg);
            } else {

               try {

                  LOG.warn("processing line lineCount=" + lineCount + ", line=" + line);

                  // remove leading quote on uprn '
                  String uprn = csvColumns.get(UPRN_COLUMN).replaceFirst("'", "");

                  long uprnNo = Long.parseUnsignedLong(uprn);

                  // may not have 5 columns
                  String ontAddress = (csvColumns.size() > FULL_ADDRESS) ? csvColumns.get(FULL_ADDRESS) : "";

                  String ontStreet = (csvColumns.size() > FULL_ADDRESS) ? csvColumns.get(this.ROAD_COLUMN) : "";

                  int poleNo = uprnCount / (SPLITTERS_TO_USE_PER_POLE * SECONDARY_SPLIT_RATIO); // pole number
                  int localPolePortNo = uprnCount % (SPLITTERS_TO_USE_PER_POLE * SECONDARY_SPLIT_RATIO);
                  int poleSplitterNo = localPolePortNo / SECONDARY_SPLIT_RATIO; // splitter number on pole
                  int poleSplitterOutPortNo = localPolePortNo % SECONDARY_SPLIT_RATIO; // port on splitter
                  
                  int secondarySplitterCount = uprnCount / SECONDARY_SPLIT_RATIO;
                  int primarySplitterCount = secondarySplitterCount / PRIMARY_SPLIT_RATIO;
                  
                  // each cabinet splitter port associated with one poleSplitter
                  int cabinetNo = primarySplitterCount / SPLITTERS_TO_USE_PER_CAB ; // cabinet number
                  int localCabinetPortNo = secondarySplitterCount % (SPLITTERS_TO_USE_PER_CAB * PRIMARY_SPLIT_RATIO); 
                  int cabinetSplitterNo = localCabinetPortNo / PRIMARY_SPLIT_RATIO; // splitter number in cabinet
                  int cabinetSplitterOutPortNo = localCabinetPortNo % PRIMARY_SPLIT_RATIO; // port on primary splitter
 
                  //int cabinetSplitterNo = (uprnCount / SECONDARY_SPLIT_RATIO) / (SPLITTERS_TO_USE_PER_CAB * PRIMARY_SPLIT_RATIO);
                 // int cabinetSplitterOutPortNo = (uprnCount / SECONDARY_SPLIT_RATIO) % (SPLITTERS_TO_USE_PER_CAB * PRIMARY_SPLIT_RATIO);
                  //int cabinetSplitterOutPortNo = (uprnCount / SECONDARY_SPLIT_RATIO) % PRIMARY_SPLIT_RATIO;

                  // each olt has multiple shelves each with separate ports (2 x 8 port shelves = 16 possible splitters)
                  int oltShelfNo = cabinetSplitterNo / PORTS_PER_OLT_CARD;
                  int oltPortNo = cabinetSplitterNo % PORTS_PER_OLT_CARD; // one port per primary splitter

                  // allocate one olt per cabinet (2 x 8 port shelves = 16 possible splitters)
                  int oltNo = oltRangeStartNumber + cabinetNo;

                  LOG.debug("***********");
                  LOG.debug("calculations:  uprnCount=" + uprnCount + ", poleNo=" + poleNo + ", poleSplitterNo=" + poleSplitterNo + ", poleSplitterOutPortNo=" + poleSplitterOutPortNo +
                           ", cabinetNo=" + cabinetNo +
                           ", cabinetSplitterNo=" + cabinetSplitterNo + ", cabinetSplitterOutPortNo=" + cabinetSplitterOutPortNo + ", oltNo=" + oltNo + ", oltShelfNo=" + oltShelfNo +
                           ", oltPortNo=" + oltPortNo);

                  if (cabinetSplitterOutPortNo>=9) throw new RuntimeException("error cabinet splitter number="+cabinetSplitterOutPortNo);
                  // create house UPRN , ONT if doesn't exist splice 2 fibres
                  // Building / House              UPRN_<uprn>  UPRN_200001919492
                  // CSP (Customer Splice Point)   CSP_<uprn>   CSP_200001919492 note CSP has two splices IN-0 OUT-0 
                  // Optical Network Terminator    ONT_<uprn>   ONT_200001919492   eth0, pon

                  String buildingName = "UPRN_" + uprn;
                  String ontName = "ONT_" + uprn;
                  String cspName = "CSP_" + uprn;

                  // create pole
                  // parentLocationObjectPrefixValue_POLE_<NUMBER>   SO18BPK1_POLE_0001
                  String poleName = parentLocationObjectPrefixValue + "_POLE_" + String.format("%04d", poleNo + 1);

                  // create pole splitters 2 splitters per pole 1:16 
                  // parentLocationObjectPrefixValue_POLE_<NUMBER>_<SPTYPE>_<NUMBER>   SO18BPK1_POLE_0001_SPL16_0001
                  String poleSplitterName = poleName + "_SPL16_" + String.format("%04d", poleSplitterNo + 1);

                  // Cabinet parentLocationObjectPrefixValue_CAB_<NUMBER>   SO18BPK1_CAB_0001
                  String cabinetName = parentLocationObjectPrefixValue + "_CAB_" + String.format("%04d", cabinetNo + 1);

                  // cabinet splitters 10 splitters per cabinet 1:8 
                  // parentLocationObjectPrefixValue_CAB_<NUMBER>_<SPTYPE>_<NUMBER>     SO18BPK1_CAB_0001_SPL8_0001
                  String cabinetSplitterName = cabinetName + "_SPL8_" + String.format("%04d", cabinetSplitterNo + 1);

                  // create fibre container pole to house 2 fibres
                  // BFU_1_2_SO18BPK1_POLE_001_UPRN_200001919492
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  String poleToBuildingContainerName = "BFU_1_2_" + buildingName + "_" + poleName;

                  // create fibre container pole to cabinet 4x12 fibres
                  // BFU_4_12_SO18BPK1_CAB_001_SO18BPK1_POLE_001
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  String cabinetToPoleContainerName = "BFU_4_12_" + cabinetName + "_" + poleName;

                  // 2 strands depending on which splitter
                  Integer cabinetToPoleStrandNo = poleSplitterNo + 1;

                  // create olt in fex
                  // parentFexName_OLT_<NUMBER>   SOTN001_OLT_0001
                  String oltName = parentFexName + "_OLT_" + String.format("%04d", oltNo);

                  // create fibre container fex to cabinet 4x12 fibres  (10 + 2 spare)
                  // BFU_4_12_SOTN001_SO18BPK1_CAB_001
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  String cabinetToFexContainerName = "BFU_4_12_" + parentFexName + "_" + cabinetName;

                  // 2 strands depending on which splitter
                  Integer fexToCabinetStrandNo = cabinetSplitterNo + 1;

                  // create circuit ont to olt

                  Double latitude = Double.valueOf(csvColumns.get(LATITUDE_COLUMN));
                  Double longitude = Double.valueOf(csvColumns.get(LONGITUDE_COLUMN));

                  Double poleLatitude;
                  Double poleLongitude;
                  Double cabinetLatitude;
                  Double cabinetLongitude;

                  // only the first calculated pole and cabinet values will be used for all splitters in the container
                  if (!poles.containsKey(poleName)) {
                     poleLatitude = latitude - 0.000040;
                     poleLongitude = longitude - 0.000040; // small offset
                     List<Double> coords = Arrays.asList(poleLatitude, poleLongitude);
                     poles.put(poleName, coords);
                  } else {
                     List<Double> coords = poles.get(poleName);
                     poleLatitude = coords.get(0);
                     poleLongitude = coords.get(1);
                  }

                  if (!cabinets.containsKey(cabinetName)) {
                     cabinetLatitude = latitude - 0.000080;
                     cabinetLongitude = longitude - 0.000080; // small offset
                     List<Double> coords = Arrays.asList(cabinetLatitude, cabinetLongitude);
                     cabinets.put(cabinetName, coords);
                  } else {
                     List<Double> coords = cabinets.get(cabinetName);
                     cabinetLatitude = coords.get(0);
                     cabinetLongitude = coords.get(1);
                  }

                  String ontSerialNo = "NOT_SET";
                  String ontAssetNo = "NOT_SET";

                  if ("NOKIA".equals(vendor)) {
                     // e.g. sn ALCLFCA40FFF an 691558

                     // same as uprn but add
                     String hex = Long.toHexString(uprnNo).toUpperCase();

                     ontSerialNo = "ALCLF" + hex;

                     // same as uprn but add '
                     ontAssetNo = "'" + uprn;
                  }

                  if ("CALEX".equals(vendor)) {
                     //  
                     // e.g. sn '372106041266  an 151029
                     //           10001304957
                     //          100000000000

                     long ontsn = uprnNo + 100000000000L;
                     ontSerialNo = "'" + Long.toUnsignedString(ontsn);

                     //same as uprn but add '
                     ontAssetNo = "'" + uprn;
                  }

                  LOG.debug(String.format(
                           "vendor: %s, ontName: %s, ontAssetNo: %s, ontSerialNo: %s, cspName: %s, buildingName: %s, poleName: %s, poleSplitterName: %s, cabinetName: %s, cabinetSplitterName: %s, lteName %s",
                           vendor, ontName, ontAssetNo, ontSerialNo, cspName, buildingName, poleName, poleSplitterName, cabinetName, cabinetSplitterName, oltName));

                  // POPULATE REQUISITION
                  String ontLabelName = ontName;
                  String ontContainerName = buildingName;
                  Double ontContainerLatitude = latitude;
                  Double ontContainerLongitude = longitude;
                  String ontIpAddress = DUMMY_IP_ADDRESS;
                  String ontComment = ontAddress;
                  String ontSerialNumber = ontSerialNo;
                  String ontAssetNumber = ontAssetNo;

                  String secondarySplitterName = poleSplitterName; //  (splitters on poles)
                  String secondarySplitterContainerName = poleName;
                  Double secondarySplitterContainerLatitude = poleLatitude;
                  Double secondarySplitterContainerLongitude = poleLongitude;
                  String secondarySplitterComment = "";
                  String secondarySplitterSerialNumber = "";
                  String secondarySplitterAssetNumber = "";
                  String secondarySplitterOutPortName = String.format("%03d", poleSplitterOutPortNo + 1) + "-OUT";

                  String primarySplitterName = cabinetSplitterName; //  (splitters in cabinets)
                  String primarySplitterContainerName = cabinetName;
                  Double primarySplitterContainerLatitude = cabinetLatitude;
                  Double primarySplitterContainerLongitude = cabinetLongitude;
                  String primarySplitterComment = "";
                  String primarySplitterSerialNumber = "";
                  String primarySplitterAssetNumber = "";
                  String primarySplitterOutPortName = String.format("%03d", cabinetSplitterOutPortNo + 1) + "-OUT";

                  Integer primaryToSecondaryStrandNo = cabinetToPoleStrandNo;
                  Integer fexToPrimaryStrandNo = fexToCabinetStrandNo;

                  String oltLabelName = oltName; // (ltes in cabinets)
                  String oltFexName = parentFexName;
                  Double oltFexLatitude = fexLatitude;
                  Double oltFexLongitude = fexLongitude;
                  String oltIpAddress = DUMMY_IP_ADDRESS;
                  String oltComment = "";
                  String oltSerialNumber = "";
                  String oltAssetNumber = "";
                  String oltCardName = "card-" + String.format("%03d", oltShelfNo + 1); // TODO
                  String oltCardPortName = "PON-" + String.format("%03d", oltPortNo + 1); // TODO

                  String cspLabelName = cspName;

                  String oltRackName = "SOTNOO1_RACK001"; //TODO

                  kuwaibaGponProvisoner.addLineToKuwaibaRequisition(ontLabelName, ontContainerName, ontContainerLatitude, ontContainerLongitude, ontIpAddress,
                           ontComment, ontSerialNumber, ontAssetNumber,

                           ontAddress, ontStreet,

                           cspLabelName,

                           secondarySplitterName, secondarySplitterContainerName, secondarySplitterContainerLatitude, secondarySplitterContainerLongitude,
                           secondarySplitterComment, secondarySplitterSerialNumber, secondarySplitterAssetNumber, secondarySplitterOutPortName, primaryToSecondaryStrandNo,

                           primarySplitterName, primarySplitterContainerName, primarySplitterContainerLatitude, primarySplitterContainerLongitude,
                           primarySplitterComment, primarySplitterSerialNumber, primarySplitterAssetNumber, primarySplitterOutPortName, fexToPrimaryStrandNo,

                           oltLabelName, oltRackName, oltFexLatitude, oltFexLongitude, oltIpAddress, oltComment, oltSerialNumber, oltAssetNumber,
                           oltCardName, oltCardPortName);

                  uprnCount++;

               } catch (Exception ie) {
                  errorlineCount++;
                  String errormsg = String.format("Error processing line %s: %s", line, ie.getMessage());
                  LOG.warn(errormsg);

               }

            }
         }

         pr = kuwaibaGponProvisoner.finalise();

         ObjectMapper om = new ObjectMapper();
         om.enable(SerializationFeature.INDENT_OUTPUT);

         File outputDirectory = new File("./target/external-data");
         System.out.println("output directory: " + outputDirectory.getAbsolutePath());
         outputDirectory.mkdirs();

         File provisioningFile = new File(outputDirectory, "kuwaibaProvisioningRequisition-data.json");
         provisioningFile.delete();

         om.writeValue(provisioningFile, pr);
         System.out.println("Provisioning File saved to: " + provisioningFile.getAbsolutePath());

         File metadataTemplateFile = new File(outputDirectory, "kuwaibaProvisioningRequisition-metadata.json");
         metadataTemplateFile.delete();

         KuwaibaProvisioningRequisition metadataTemplates = new KuwaibaProvisioningRequisition();

         metadataTemplates.setKuwaibaTemplateList(pr.getKuwaibaTemplateList());

         om.writeValue(metadataTemplateFile, metadataTemplates);
         System.out.println("Metadata File saved to: " + metadataTemplateFile.getAbsolutePath());

         // check you can read the file
         // KuwaibaProvisioningRequisition pr2 = om.readValue(file, KuwaibaProvisioningRequisition.class);
         // System.out.println("read file: " + pr2);

      } catch (Exception e) {
         LOG.error("problem running script ", e);

      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException e) {
            }
         }
      }

      LOG.info("**** test1 finished");

   }

   public class KuwaibaGponProvisoner {

      // used to avoid duplicate definitions
      Set<String> streetNames = new HashSet<String>();
      Set<String> secondarySplitterContainerNames = new HashSet<String>(); //pole
      Set<String> secondarySplitterNames = new HashSet<String>();
      Set<String> primarySplitterContainerNames = new HashSet<String>(); // cabinet
      Set<String> primarySplitterNames = new HashSet<String>();
      Set<String> connectionNames = new HashSet<String>();
      Set<String> oltNames = new HashSet<String>();

      KuwaibaProvisioningRequisition pr = new KuwaibaProvisioningRequisition();

      public KuwaibaProvisioningRequisition finalise() {
         return pr;
      }

      public KuwaibaClass createEndpointOpticalLinkContainerClass(KuwaibaClass connectionClass, Integer circuitNo, Integer deapth) {

         KuwaibaClass endpointLinkContainerClass = new KuwaibaClass();

         endpointLinkContainerClass.getParentClasses().add(connectionClass);

         String compoundContainerName="";
         List<String> colourList = ContainerColour.getNestedContainerColourList(circuitNo, deapth);
         Iterator<String> colListIterator = colourList.iterator();
         while (colListIterator.hasNext()) {
            String color = colListIterator.next();
            String name = String.format("%02d", ContainerColour.getStrandForColour(color)) + "-" + color;
            KuwaibaClass newContainerClass = new KuwaibaClass();
            if (colListIterator.hasNext()) {
               newContainerClass.setClassName("WireContainer");
               newContainerClass.setName(name);
               newContainerClass.setSpecial(true);
               endpointLinkContainerClass.getParentClasses().add(newContainerClass);
               compoundContainerName=compoundContainerName+name+"_";
            } else {
               endpointLinkContainerClass.setClassName("OpticalLink");
               endpointLinkContainerClass.setTemplateName(name);
               endpointLinkContainerClass.setSpecial(true);
               compoundContainerName=compoundContainerName+name;
            }
         }

         String opticalConnectionName = compoundContainerName+"_"+connectionClass.getName() ;
         endpointLinkContainerClass.setName(opticalConnectionName);
         
         return endpointLinkContainerClass;

      }

      public void addLineToKuwaibaRequisition(
               String ontLabelName, String ontContainerName, Double ontContainerLatitude, Double ontContainerLongitude, String ontIpAddress,
               String ontComment, String ontSerialNumber, String ontAssetNumber,

               String ontAddress, String ontStreet,

               String cspLabelName,

               String secondarySplitterName, String secondarySplitterContainerName, Double secondarySplitterContainerLatitude, Double secondarySplitterContainerLongitude,
               String secondarySplitterComment, String secondarySplitterSerialNumber, String secondarySplitterAssetNumber, String secondarySplitterOutPortName, Integer primaryToSecondaryStrandNo,

               String primarySplitterName, String primarySplitterContainerName, Double primarySplitterContainerLatitude, Double primarySplitterContainerLongitude,
               String primarySplitterComment, String primarySplitterSerialNumber, String primarySplitterAssetNumber, String primarySplitterOutPortName, Integer fexToPrimaryStrandNo,

               String oltLabelName, String oltFexName, Double oltFexLatitude, Double oltFexLongitude, String oltIpAddress,
               String oltComment, String oltSerialNumber, String oltAssetNumber, String oltCardName, String oltCardPortName) {

         LOG.info("**** addLineToKuwaibaRequisition (ontLabelName=" + ontLabelName + ", ontContainerName=" + ontContainerName +
                  ", ontContainerLatitude=" + ontContainerLatitude + ", ontContainerLongitude=" + ontContainerLongitude + ", ontIpAddress=" + ontIpAddress
                  + ", ontComment=" + ontComment + ", ontSerialNumber=" + ontSerialNumber + ", ontAssetNumber=" + ontAssetNumber + ", ontAddress=" + ontAddress
                  + ", ontStreet=" + ontStreet + ", cspLabelName=" + cspLabelName

                  + ",\n secondarySplitterName=" + secondarySplitterName + ", secondarySplitterContainerName=" + secondarySplitterContainerName
                  + ", secondarySplitterContainerLatitude=" + secondarySplitterContainerLatitude + ", secondarySplitterContainerLongitude="
                  + secondarySplitterContainerLongitude + ", secondarySplitterComment=" + secondarySplitterComment
                  + ", secondarySplitterSerialNumber=" + secondarySplitterSerialNumber + ", secondarySplitterAssetNumber=" + secondarySplitterAssetNumber
                  + ", secondarySplitterOutPortName=" + secondarySplitterOutPortName + ", primaryToSecondaryStrandNo=" + primaryToSecondaryStrandNo +

                  ",\n primarySplitterName=" + primarySplitterName + ", primarySplitterContainerName="
                  + primarySplitterContainerName + ", primarySplitterContainerLatitude=" + primarySplitterContainerLatitude + ", primarySplitterContainerLongitude=" + primarySplitterContainerLongitude
                  + ", primarySplitterComment=" + primarySplitterComment + ", primarySplitterSerialNumber=" + primarySplitterSerialNumber + ", primarySplitterAssetNumber=" + primarySplitterAssetNumber
                  + ", primarySplitterOutPortName=" + primarySplitterOutPortName

                  + ",\n fexToPrimaryStrandNo=" + fexToPrimaryStrandNo + ", oltLabelName=" + oltLabelName + ", oltFexName=" + oltFexName + ", oltFexLatitude=" + oltFexLatitude
                  + ", oltFexLongitude=" + oltFexLongitude + ", oltIpAddress=" + oltIpAddress
                  + ", oltComment=" + oltComment + ", oltSerialNumber=" + oltSerialNumber + ", oltAssetNumber=" + oltAssetNumber + ", oltCardName=" + oltCardName + ", oltCardPortName=" + oltCardPortName + ")");

         // Street container 
         if (!streetNames.contains(ontStreet)) {
            streetNames.add(ontStreet);

            KuwaibaClass streetContainer = new KuwaibaClass();
            pr.getKuwaibaClassList().add(streetContainer);

            streetContainer.setName(ontStreet);
            streetContainer.setClassName(GponConstants.STREET_CONTAINER_CLASS_NAME);

            KuwaibaClass parent1 = new KuwaibaClass();
            streetContainer.getParentClasses().add(parent1);
            parent1.setName(GponConstants.PARENT_LOCATION_VALUE); // bitterne park
            parent1.setClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);

            HashMap<String, String> streetContainerAttributes = new HashMap<String, String>();
            streetContainer.getAttributes().putAll(streetContainerAttributes);
         }

         // only ever one house so no need to de-duplicate house, ont, csp
         // House ont container 
         try {

            KuwaibaClass ontContainer = new KuwaibaClass();
            pr.getKuwaibaClassList().add(ontContainer);

            ontContainer.setName(ontContainerName);
            ontContainer.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
            ontContainer.setTemplateName(GponConstants.ONT_CONTAINER_TEMPLATE_NAME); // house

            KuwaibaClass parent1 = new KuwaibaClass();
            ontContainer.getParentClasses().add(parent1);
            parent1.setName(ontStreet);
            parent1.setClassName(GponConstants.STREET_CONTAINER_CLASS_NAME);

            HashMap<String, String> ontContainerAttributes = new HashMap<String, String>();
            ontContainerAttributes.put("latitude", String.format("%.8f", ontContainerLatitude));
            ontContainerAttributes.put("longitude", String.format("%.8f", ontContainerLongitude));
            ontContainerAttributes.put("address", ontAddress.replace(",", " "));
            ontContainer.getAttributes().putAll(ontContainerAttributes);

         } catch (Exception ex) {
            LOG.error("problem configurion", ex);
         }

         // ont
         try {
            KuwaibaClass ont = new KuwaibaClass();
            pr.getKuwaibaClassList().add(ont);

            ont.setName(ontLabelName);
            ont.setClassName(GponConstants.ONT_CLASS_NAME);
            ont.setTemplateName(GponConstants.ONT_TEMPLATE_NAME);

            KuwaibaClass parent1 = new KuwaibaClass();
            ont.getParentClasses().add(parent1);
            parent1.setName(ontContainerName);
            parent1.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);

            HashMap<String, String> ontAttributes = new HashMap<String, String>();

            ontAttributes.put("serialNumber", ontSerialNumber);
            ont.getAttributes().putAll(ontAttributes);
         } catch (Exception ex) {
            LOG.error("problem configurion", ex);
         }

         // csp 
         try {
            KuwaibaClass csp = new KuwaibaClass();
            pr.getKuwaibaClassList().add(csp);

            csp.setName(cspLabelName);
            csp.setClassName(GponConstants.CSP_CLASS_NAME);
            csp.setTemplateName(GponConstants.CSP_TEMPLATE_NAME); // house

            KuwaibaClass parent1 = new KuwaibaClass();
            csp.getParentClasses().add(parent1);
            parent1.setName(ontContainerName);
            parent1.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);

         } catch (Exception ex) {
            LOG.error("problem configurion", ex);
         }

         if (!secondarySplitterContainerNames.contains(secondarySplitterContainerName)) {
            secondarySplitterContainerNames.add(secondarySplitterContainerName);
            // pole secondary splitter container 
            KuwaibaClass secondarySplitterContainer = new KuwaibaClass();
            pr.getKuwaibaClassList().add(secondarySplitterContainer);

            secondarySplitterContainer.setName(secondarySplitterContainerName);
            secondarySplitterContainer.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
            secondarySplitterContainer.setTemplateName(GponConstants.SECONDARY_SPLITTER_CONTAINER_TEMPLATE_NAME); // house

            KuwaibaClass parent1 = new KuwaibaClass();
            secondarySplitterContainer.getParentClasses().add(parent1);
            parent1.setName(ontStreet);
            parent1.setClassName(GponConstants.STREET_CONTAINER_CLASS_NAME);

            HashMap<String, String> secondarySplitterContainerAttributes = new HashMap<String, String>();
            secondarySplitterContainerAttributes.put("latitude", String.format("%.8f", secondarySplitterContainerLatitude));
            secondarySplitterContainerAttributes.put("longitude", String.format("%.8f", secondarySplitterContainerLongitude));
            secondarySplitterContainer.getAttributes().putAll(secondarySplitterContainerAttributes);
         }

         if (!secondarySplitterNames.contains(secondarySplitterName)) {
            secondarySplitterNames.add(secondarySplitterName);
            // secondarySplitter 
            KuwaibaClass secondarySplitter = new KuwaibaClass();
            pr.getKuwaibaClassList().add(secondarySplitter);

            secondarySplitter.setClassName(GponConstants.SECONDARY_SPLITTER_CLASS_NAME);
            secondarySplitter.setTemplateName(GponConstants.SECONDARY_SPLITTER_TEMPLATE_NAME);

            KuwaibaClass parent1 = new KuwaibaClass();
            secondarySplitter.getParentClasses().add(parent1);
            parent1.setName(secondarySplitterContainerName);
            parent1.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);

            secondarySplitter.setName(secondarySplitterName);
         }

         // cab primarySplitter container
         if (!primarySplitterContainerNames.contains(primarySplitterContainerName)) {
            secondarySplitterNames.add(primarySplitterContainerName);

            KuwaibaClass primarySplitterContainer = new KuwaibaClass();
            pr.getKuwaibaClassList().add(primarySplitterContainer);

            primarySplitterContainer.setName(primarySplitterContainerName);
            primarySplitterContainer.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
            primarySplitterContainer.setTemplateName(GponConstants.PRIMARY_SPLITTER_CONTAINER_TEMPLATE_NAME);

            KuwaibaClass parent1 = new KuwaibaClass();
            primarySplitterContainer.getParentClasses().add(parent1);
            parent1.setName(GponConstants.PARENT_LOCATION_VALUE);
            parent1.setClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);

            HashMap<String, String> primarySplitterContainerAttributes = new HashMap<String, String>();
            primarySplitterContainerAttributes.put("latitude", String.format("%.8f", primarySplitterContainerLatitude));
            primarySplitterContainerAttributes.put("longitude", String.format("%.8f", primarySplitterContainerLongitude));
            primarySplitterContainer.getAttributes().putAll(primarySplitterContainerAttributes);
         }

         // primarySplitter 
         if (!primarySplitterNames.contains(primarySplitterName)) {
            secondarySplitterNames.add(primarySplitterName);

            KuwaibaClass primarySplitter = new KuwaibaClass();
            pr.getKuwaibaClassList().add(primarySplitter);

            primarySplitter.setName(primarySplitterName);
            primarySplitter.setClassName(GponConstants.PRIMARY_SPLITTER_CLASS_NAME);
            primarySplitter.setTemplateName(GponConstants.PRIMARY_SPLITTER_TEMPLATE_NAME);

            KuwaibaClass parent1 = new KuwaibaClass();
            primarySplitter.getParentClasses().add(parent1);
            parent1.setName(primarySplitterContainerName);
            parent1.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);

         }

         // olt 
         if (!oltNames.contains(oltLabelName)) {
            oltNames.add(oltLabelName);

            KuwaibaClass olt = new KuwaibaClass();
            pr.getKuwaibaClassList().add(olt);

            olt.setName(oltLabelName);
            olt.setClassName(GponConstants.OLT_CLASS_NAME);
            olt.setTemplateName(GponConstants.OLT_TEMPLATE_NAME); // fex

            KuwaibaClass parent1 = new KuwaibaClass();
            olt.getParentClasses().add(parent1);
            parent1.setName(GponConstants.OLT_CONTAINER_NAME);
            parent1.setClassName(GponConstants.OLT_CONTAINER_CLASS_NAME);

            HashMap<String, String> oltAttributes = new HashMap<String, String>();

            oltAttributes.put("serialNumber", oltSerialNumber);
            olt.getAttributes().putAll(oltAttributes);
         }

         /* 
          * WiredContainer connections
          */
         if (createContainers) {

            // csp to ont
            // e.g "BFU_1_2_CSP_200001919492_ONT_200001919492"
            // block to isolate repeat variables

            try {
               KuwaibaConnection connection1 = new KuwaibaConnection();

               // csp
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(cspLabelName);
               endpointA.setClassName(GponConstants.CSP_CLASS_NAME);
               connection1.setEndpointA(endpointA);

               // ont
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(ontLabelName);
               endpointB.setClassName(GponConstants.ONT_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();

               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_1_2");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // only add to list if doesn't exist
               if (!connectionNames.contains(connectionClass.getName())) {
                  connectionNames.add(connectionClass.getName());
                  pr.getKuwaibaConnectionList().add(connection1);
               }

            } catch (Exception e) {
               e.printStackTrace();
            }

            // pole to house
            // e.g "BFU_1_2_SO18BPK1_POLE_001_UPRN_200001919492"
            // block to isolate repeat variables
            try {
               KuwaibaConnection connection1 = new KuwaibaConnection();

               // pole 
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(secondarySplitterContainerName);
               endpointA.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointA(endpointA);

               //house
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(ontContainerName);
               endpointB.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();

               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_1_2");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // only add to list if doesn't exist
               if (!connectionNames.contains(connectionClass.getName())) {
                  connectionNames.add(connectionClass.getName());
                  pr.getKuwaibaConnectionList().add(connection1);
               }

            } catch (Exception e) {
               e.printStackTrace();
            }

            // cabinet to pole
            // BFU_1_4_SO18BPK1_CAB_001_SO18BPK1_POLE_001
            // block to isolate repeat variables
            try {
               KuwaibaConnection connection1 = new KuwaibaConnection();

               // cabinet
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(primarySplitterContainerName);
               endpointA.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointA(endpointA);

               // pole
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(secondarySplitterContainerName);
               endpointB.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();

               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_1_4");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // only add to list if doesn't exist
               if (!connectionNames.contains(connectionClass.getName())) {
                  connectionNames.add(connectionClass.getName());
                  pr.getKuwaibaConnectionList().add(connection1);
               }

            } catch (Exception e) {
               e.printStackTrace();
            }

            // fex to cabinet
            // e.g "BFU_4_12_SOTN001_SO18BPK1_CAB_001"
            // block to isolate repeat variables
            try {
               KuwaibaConnection connection1 = new KuwaibaConnection();

               // fex rack
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(GponConstants.OLT_CONTAINER_NAME); //TODO make dynamic
               endpointA.setClassName("Rack");
               connection1.setEndpointA(endpointA);

               // pole
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(primarySplitterContainerName);
               endpointB.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();
               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_4_12");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // only add to list if doesn't exist
               if (!connectionNames.contains(connectionClass.getName())) {
                  connectionNames.add(connectionClass.getName());
                  pr.getKuwaibaConnectionList().add(connection1);
               }

            } catch (Exception e) {
               e.printStackTrace();
            }

         }

         // csp to ont optical link
         if (generateCircuits) {

            // csp ont circuit
            try {

               KuwaibaConnection connection1 = new KuwaibaConnection();

               // csp
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(cspLabelName);
               endpointA.setClassName(GponConstants.CSP_CLASS_NAME);
               connection1.setEndpointA(endpointA);

               // ont
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(ontLabelName);
               endpointB.setClassName(GponConstants.ONT_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();

               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_1_2");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               KuwaibaConnection endpointLink1 = new KuwaibaConnection();

               // csp front optical terminal
               KuwaibaClass opticalEndpointA = new KuwaibaClass();
               opticalEndpointA.setName("001-front");
               opticalEndpointA.setClassName("OpticalPort");
               opticalEndpointA.getParentClasses().add(endpointA);
               endpointLink1.setEndpointA(opticalEndpointA);

               // ont optical terminal
               KuwaibaClass opticalEndpointB = new KuwaibaClass();
               opticalEndpointB.setName("IN-01");
               opticalEndpointB.setClassName("OpticalPort");
               opticalEndpointB.getParentClasses().add(endpointB);
               endpointLink1.setEndpointB(opticalEndpointB);

               // containerClass - WireContainer strand
               Integer circuitNo = 1;
               Integer deapth = 2;
               KuwaibaClass endpointLinkContainerClass = createEndpointOpticalLinkContainerClass(connectionClass, circuitNo, deapth);
               endpointLink1.setConnectionClass(endpointLinkContainerClass);

               pr.getKuwaibaConnectionList().add(endpointLink1);

            } catch (Exception e) {
               e.printStackTrace();
            }

            // pole splitter to house csp circuit
            try {

               KuwaibaConnection connection1 = new KuwaibaConnection();

               // pole to house WireContainer

               // pole 
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(secondarySplitterContainerName);
               endpointA.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointA(endpointA);

               //house
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(ontContainerName);
               endpointB.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();

               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_1_2");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // splitter to csp circuit
               KuwaibaConnection endpointLink1 = new KuwaibaConnection();

               // pole secondary splitter
               KuwaibaClass endpointA_A = new KuwaibaClass();
               endpointA_A.setName(secondarySplitterName);
               endpointA_A.setClassName(GponConstants.SECONDARY_SPLITTER_CLASS_NAME);
               connection1.setEndpointA(endpointA_A);

               // secondary splitter port
               KuwaibaClass opticalEndpointA = new KuwaibaClass();
               opticalEndpointA.setName(secondarySplitterOutPortName);
               opticalEndpointA.setClassName("OpticalPort");
               opticalEndpointA.getParentClasses().add(endpointA_A);
               endpointLink1.setEndpointA(opticalEndpointA);

               // csp
               KuwaibaClass endpointB_B = new KuwaibaClass();
               endpointB_B.setName(cspLabelName);
               endpointB_B.setClassName(GponConstants.CSP_CLASS_NAME);
               connection1.setEndpointA(endpointB_B);

               // csp rear optical terminal
               KuwaibaClass opticalEndpointB = new KuwaibaClass();
               opticalEndpointB.setName("001-back");
               opticalEndpointB.setClassName("OpticalPort");
               opticalEndpointB.getParentClasses().add(endpointB_B);
               endpointLink1.setEndpointB(opticalEndpointB);

               // containerClass - WireContainer strand
               Integer circuitNo = 1;
               Integer deapth = 2;
               KuwaibaClass endpointLinkContainerClass = createEndpointOpticalLinkContainerClass(connectionClass, circuitNo, deapth);
               endpointLink1.setConnectionClass(endpointLinkContainerClass);

               pr.getKuwaibaConnectionList().add(endpointLink1);

            } catch (Exception e) {
               e.printStackTrace();
            }

            // cabinet to pole csp circuit
            try {

               KuwaibaConnection connection1 = new KuwaibaConnection();

               // cabinet to pole WireContainer

               // cabinet
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(primarySplitterContainerName);
               endpointA.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointA(endpointA);

               // pole
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(secondarySplitterContainerName);
               endpointB.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();

               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_1_4");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // primary splitter to secondary splitter circuit
               KuwaibaConnection endpointLink1 = new KuwaibaConnection();

               // cabinet primary splitter
               KuwaibaClass endpointA_A = new KuwaibaClass();
               endpointA_A.setName(primarySplitterName);
               endpointA_A.setClassName(GponConstants.PRIMARY_SPLITTER_CLASS_NAME);
               connection1.setEndpointA(endpointA_A);

               // cabinet primary splitter out port
               KuwaibaClass opticalEndpointA = new KuwaibaClass();
               opticalEndpointA.setName(primarySplitterOutPortName);
               opticalEndpointA.setClassName("OpticalPort");
               opticalEndpointA.getParentClasses().add(endpointA_A);
               endpointLink1.setEndpointA(opticalEndpointA);

               // pole secondary splitter
               KuwaibaClass endpointB_B = new KuwaibaClass();
               endpointB_B.setName(secondarySplitterName);
               endpointB_B.setClassName(GponConstants.SECONDARY_SPLITTER_CLASS_NAME);
               connection1.setEndpointA(endpointB_B);

               // csp rear optical terminal
               KuwaibaClass opticalEndpointB = new KuwaibaClass();
               opticalEndpointB.setName("IN-001");
               opticalEndpointB.setClassName("OpticalPort");
               opticalEndpointB.getParentClasses().add(endpointB_B);
               endpointLink1.setEndpointB(opticalEndpointB);

               // containerClass - WireContainer strand
               Integer circuitNo = primaryToSecondaryStrandNo;
               Integer deapth = 2;
               KuwaibaClass endpointLinkContainerClass = createEndpointOpticalLinkContainerClass(connectionClass, circuitNo, deapth);
               endpointLink1.setConnectionClass(endpointLinkContainerClass);

               pr.getKuwaibaConnectionList().add(endpointLink1);

            } catch (Exception e) {
               e.printStackTrace();
            }

            // fex rack to cabinet
            try {

               // fex rack to cabinet WireContainer
               KuwaibaConnection connection1 = new KuwaibaConnection();

               // fex rack
               KuwaibaClass endpointA = new KuwaibaClass();
               endpointA.setName(GponConstants.OLT_CONTAINER_NAME); //TODO make dynamic
               endpointA.setClassName("Rack");
               connection1.setEndpointA(endpointA);

               // cabinet
               KuwaibaClass endpointB = new KuwaibaClass();
               endpointB.setName(primarySplitterContainerName);
               endpointB.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
               connection1.setEndpointB(endpointB);

               KuwaibaClass connectionClass = new KuwaibaClass();
               connectionClass.setClassName("WireContainer");
               connectionClass.setTemplateName("BFU_4_12");

               String connectionClassName = connectionClass.getTemplateName() + "_" + endpointA.getName() + "_" + endpointB.getName();
               connectionClass.setName(connectionClassName);

               connection1.setConnectionClass(connectionClass);

               // fex rack to primary splitter circuit
               KuwaibaConnection endpointLink1 = new KuwaibaConnection();

               /*
                *                String oltLabelName, String oltFexName, Double oltFexLatitude, Double oltFexLongitude, String oltIpAddress,
               String oltComment, String oltSerialNumber, String oltAssetNumber, String oltCardName, String oltCardPortName) {
                */
               // fex rack olt 
               KuwaibaClass endpointA_A = new KuwaibaClass();
               endpointA_A.setName(oltLabelName);
               endpointA_A.setClassName(GponConstants.OLT_CLASS_NAME);
                              
               // fex rack olt card
               KuwaibaClass endpointA_A_A = new KuwaibaClass();
               endpointA_A_A.setName(oltCardName);
               endpointA_A_A.setClassName("OLTBoard");
               
               // olt card-port
               KuwaibaClass opticalEndpointA = new KuwaibaClass();
               opticalEndpointA.setName(oltCardPortName);
               opticalEndpointA.setClassName("OpticalPort");
               
               opticalEndpointA.getParentClasses().add(endpointA_A);
               opticalEndpointA.getParentClasses().add(endpointA_A_A);
               
               endpointLink1.setEndpointA(opticalEndpointA);

               // cabinet primary splitter
               KuwaibaClass endpointB_B = new KuwaibaClass();
               endpointB_B.setName(primarySplitterName);
               endpointB_B.setClassName(GponConstants.PRIMARY_SPLITTER_CLASS_NAME);
               connection1.setEndpointA(endpointB_B);

               // cabinet primary splitter port
               KuwaibaClass opticalEndpointB = new KuwaibaClass();
               opticalEndpointB.setName("IN-001");
               opticalEndpointB.setClassName("OpticalPort");
               opticalEndpointB.getParentClasses().add(endpointB_B);
               endpointLink1.setEndpointB(opticalEndpointB);

               // containerClass - WireContainer strand
               Integer circuitNo = fexToPrimaryStrandNo;
               Integer deapth = 2;
               KuwaibaClass endpointLinkContainerClass = createEndpointOpticalLinkContainerClass(connectionClass, circuitNo, deapth);
               endpointLink1.setConnectionClass(endpointLinkContainerClass);

               pr.getKuwaibaConnectionList().add(endpointLink1);

            } catch (Exception e) {
               e.printStackTrace();
            }

         }

      }

      /*
       * STATIC PROVISIONING REQUISITIONS
       */
      public void addStaticObjectsToProvisioningRequisition() {

         // create Southampton if doesn't exist
         // block to isolate repeat variables
         try {
            KuwaibaClass kuwaibaClass1 = new KuwaibaClass();
            pr.getKuwaibaClassList().add(kuwaibaClass1);

            kuwaibaClass1.setName(GponConstants.PARENT_CITY); // southampton
            kuwaibaClass1.setClassName("City");

            KuwaibaClass parent1 = new KuwaibaClass();
            kuwaibaClass1.getParentClasses().add(parent1);
            parent1.setName(GponConstants.PARENT_STATE); //hampshire
            parent1.setClassName("State");

         } catch (Exception e) {
            e.printStackTrace();
         }

         // create bitterne park neighborhood  if doesn't exist
         // block to isolate repeat variables
         try {
            KuwaibaClass kuwaibaClass1 = new KuwaibaClass();
            pr.getKuwaibaClassList().add(kuwaibaClass1);

            kuwaibaClass1.setClassName(GponConstants.PARENT_LOCATION_CLASS_NAME); // Neighborhood
            kuwaibaClass1.setName(GponConstants.PARENT_LOCATION_VALUE); // bitterne pk

            KuwaibaClass parent1 = new KuwaibaClass();
            kuwaibaClass1.getParentClasses().add(parent1);
            parent1.setName(GponConstants.PARENT_CITY); //hampshire
            parent1.setClassName("City");

         } catch (Exception e) {
            e.printStackTrace();
         }

         // FEX Facility  if doesn't exist
         // block to isolate repeat variables
         try {
            KuwaibaClass kuwaibaClass1 = new KuwaibaClass();
            pr.getKuwaibaClassList().add(kuwaibaClass1);

            kuwaibaClass1.setName(GponConstants.PARENT_FACILITY); // fex
            kuwaibaClass1.setClassName("Facility");

            KuwaibaClass parent1 = new KuwaibaClass();
            kuwaibaClass1.getParentClasses().add(parent1);
            parent1.setName(GponConstants.PARENT_CITY);
            parent1.setClassName("City");

            HashMap<String, String> kuwaibaClass1Attributes = new HashMap<String, String>();
            kuwaibaClass1Attributes.put("latitude", String.format("%.8f", -1.393999)); // hampton telephone exchange portswood
            kuwaibaClass1Attributes.put("longitude", String.format("%.8f", 50.923873));
            kuwaibaClass1.getAttributes().putAll(kuwaibaClass1Attributes);

         } catch (Exception e) {
            e.printStackTrace();
         }

         // Rack if doesn't exist
         // block to isolate repeat variables
         try {
            KuwaibaClass kuwaibaClass1 = new KuwaibaClass();
            pr.getKuwaibaClassList().add(kuwaibaClass1);

            kuwaibaClass1.setName(GponConstants.OLT_CONTAINER_NAME);
            kuwaibaClass1.setClassName(GponConstants.OLT_CONTAINER_CLASS_NAME);
            kuwaibaClass1.setTemplateName(GponConstants.OLT_CONTAINER_TEMPLATE_NAME); // FEX_RACK_001

            KuwaibaClass parent1 = new KuwaibaClass();
            kuwaibaClass1.getParentClasses().add(parent1);
            parent1.setName(GponConstants.PARENT_FACILITY);
            parent1.setClassName("Facility");

         } catch (Exception e) {
            e.printStackTrace();
         }

         // TODO REMOVE TEST OBJECTS - as will be created from data

      }

      public void addTemplatesToProvisioningRequisition() {

         List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList = new ArrayList<KuwaibaTemplateDefinition>();

         // ONT_CONTAINER_CLASS_NAME = "House";
         // ONT_CONTAINER_TEMPLATE_NAME = "House_01";
         //         
         // ONT_CLASS_NAME = "OpticalNetworkTerminal";
         // ONT_TEMPLATE_NAME = "ONT_NOKIA_01";
         // ONC_CLASS_NAME = "SpliceBox";
         // CSP_TEMPLATE_NAME = "CSP_BFU_1_2_01";

         // block to isolate local variables  
         // creating template from function definitions
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("House_01");
            definition1.setTemplateElementName("House_01");
            definition1.setClassName("House");
            definition1.setSpecial(false);

            // ONT
            KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
            childDefinition1.setTemplateElementName("ONT_NOKIA_01");
            childDefinition1.setClassName("OpticalNetworkTerminal");
            childDefinition1.setSpecial(false);
            definition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1);

            KuwaibaTemplateDefinition childDefinition1_1 = new KuwaibaTemplateDefinition();
            childDefinition1_1.setTemplateElementName("IN-01");
            childDefinition1_1.setClassName("OpticalPort");
            childDefinition1_1.setSpecial(false);
            childDefinition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1_1);

            KuwaibaTemplateDefinition childDefinition1_2 = new KuwaibaTemplateDefinition();
            childDefinition1_2.setTemplateElementName("eth0");
            childDefinition1_2.setClassName("ElectricalPort");
            childDefinition1_2.setSpecial(false);
            childDefinition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1_2);

            // CSP
            KuwaibaTemplateDefinition childDefinition2 = new KuwaibaTemplateDefinition();
            childDefinition2.setTemplateElementName("CSP_BFU_1_2_01");
            childDefinition2.setClassName("SpliceBox");
            childDefinition2.setSpecial(false);
            // build ports using function
            childDefinition2.setTemplateFunction("OpticalSpliceBoxFunction");
            HashMap<String, String> attributes1 = new HashMap<String, String>();
            attributes1.put("numberOfPorts", "2");
            childDefinition2.setTemplateFunctionAttributes(attributes1);

            definition1.getChildKuwaibaTemplateDefinitions().add(childDefinition2);

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         //  SECONDARY_SPLITTER_CONTAINER_CLASS_NAME = "Pole";
         //  SECONDARY_SPLITTER_CONTAINER_TEMPLATE_NAME = "POLE_2_16drop";
         //  SECONDARY_SPLITTER_CLASS_NAME = "FiberSplitter";
         //  SECONDARY_SPLITTER_TEMPLATE_NAME = "SPL16";

         // block to isolate local variables  
         // creating template from function definitions
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("POLE_2_16drop");
            definition1.setTemplateElementName("POLE_2_16drop");
            definition1.setClassName("Pole");
            definition1.setSpecial(false);

            // 2 x 8 way splitters in template
            for (int splitterNo = 1; splitterNo <= 2; splitterNo++) {
               KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
               childDefinition1.setTemplateElementName("SPL16_" + String.format("%03d", splitterNo));
               childDefinition1.setClassName("FiberSplitter");
               childDefinition1.setSpecial(false);
               // build ports using function
               childDefinition1.setTemplateFunction("FiberSplitterFunction");
               HashMap<String, String> attributes1 = new HashMap<String, String>();
               attributes1.put("numberOfPorts", "16");
               childDefinition1.setTemplateFunctionAttributes(attributes1);

               definition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1);
            }

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // PRIMARY_SPLITTER_CONTAINER_TEMPLATE_NAME = "CAB_10SPL8";
         // PRIMARY_SPLITTER_CONTAINER_CLASS_NAME = "OutdoorsCabinet";
         //         
         // PRIMARY_SPLITTER_CLASS_NAME = "FiberSplitter";
         // PRIMARY_SPLITTER_TEMPLATE_NAME = "SPL8";

         // block to isolate local variables  
         // creating template from function definitions
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("CAB_10SPL8");
            definition1.setTemplateElementName("CAB_10SPL8");
            definition1.setClassName("OutdoorsCabinet");
            definition1.setSpecial(false);

            // 10 splitters in template
            for (int splitterNo = 1; splitterNo <= 10; splitterNo++) {
               KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
               childDefinition1.setTemplateElementName("SPL8_" + String.format("%03d", splitterNo));
               childDefinition1.setClassName("FiberSplitter");
               childDefinition1.setSpecial(false);
               // build ports using function
               childDefinition1.setTemplateFunction("FiberSplitterFunction");
               HashMap<String, String> attributes1 = new HashMap<String, String>();
               attributes1.put("numberOfPorts", "8");
               childDefinition1.setTemplateFunctionAttributes(attributes1);

               definition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1);
            }

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // OLT_CONTAINER_TEMPLATE_NAME = "FEX_RACK_001";
         // OLT_CONTAINER_CLASS_NAME = "Rack";
         //         
         // OLT_TEMPLATE_NAME = "OLT_NOKIA_01";
         // OLT_CLASS_NAME = "OpticalLineTerminal";
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("FEX_RACK_001");
            definition1.setTemplateElementName("FEX_RACK_001");
            definition1.setClassName("Rack");
            definition1.setSpecial(false);

            HashMap<String, String> definition1Attributes = new HashMap<String, String>();
            definition1Attributes.put("rackUnits", "42");
            definition1Attributes.put("rackUnitsNumberingDescending", "true");
            definition1.getTemplateAttributes().putAll(definition1Attributes);

            // 10 OLT in rack
            for (int oltNo = 1; oltNo <= 10; oltNo++) {
               KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
               childDefinition1.setTemplateElementName("OLT_NOKIA_01_" + String.format("%03d", oltNo));
               childDefinition1.setClassName("OpticalLineTerminal");
               childDefinition1.setSpecial(false);

               HashMap<String, String> childDefinition1Attributes = new HashMap<String, String>();
               childDefinition1Attributes.put("rackUnits", "2");
               childDefinition1Attributes.put("position", Integer.toString(2 + oltNo * 2)); // top 2 slots free
               childDefinition1.getTemplateAttributes().putAll(childDefinition1Attributes);

               // 2 cards per olt
               for (int card = 1; card <= 2; card++) {
                  KuwaibaTemplateDefinition childDefinition1_1 = new KuwaibaTemplateDefinition();
                  childDefinition1_1.setTemplateElementName("card-" + String.format("%03d", card));
                  childDefinition1_1.setClassName("OLTBoard");
                  childDefinition1_1.setSpecial(false);
                  childDefinition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1_1);

                  // 16 pon ports per card
                  for (int opticalPort = 1; opticalPort <= 16; opticalPort++) {
                     KuwaibaTemplateDefinition childDefinition1_2 = new KuwaibaTemplateDefinition();
                     childDefinition1_2.setTemplateElementName("PON-" + String.format("%03d", opticalPort));
                     childDefinition1_2.setClassName("OpticalPort");
                     childDefinition1_2.setSpecial(false);
                     childDefinition1_1.getChildKuwaibaTemplateDefinitions().add(childDefinition1_2);
                  }

               }

               definition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1);
            }

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // wire containers

         // BFU_1_2 blown fiber unit 1 cable, 2 cores coloured
         // block to isolate local variables            
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("BFU_1_2");
            definition1.setClassName("WireContainer");
            definition1.setSpecial(false);
            definition1.setTemplateFunction("ColoredFiberWireContainerFunction");

            HashMap<String, String> attributes1 = new HashMap<String, String>();
            attributes1.put("numberOfCables", "1");
            attributes1.put("numberOfFibers", "2");
            definition1.setTemplateFunctionAttributes(attributes1);

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // BFU_1_4 blown fiber unit 1 cables, 4 cores  coloured
         // block to isolate local variables            
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("BFU_1_4");
            definition1.setClassName("WireContainer");
            definition1.setSpecial(false);
            definition1.setTemplateFunction("ColoredFiberWireContainerFunction");

            HashMap<String, String> attributes1 = new HashMap<String, String>();
            attributes1.put("numberOfCables", "4");
            attributes1.put("numberOfFibers", "12");
            definition1.setTemplateFunctionAttributes(attributes1);

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // BFU_4_12 blown fiber unit 4 cables, 12 cores  coloured
         // block to isolate local variables            
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("BFU_4_12");
            definition1.setClassName("WireContainer");
            definition1.setSpecial(false);
            definition1.setTemplateFunction("ColoredFiberWireContainerFunction");

            HashMap<String, String> attributes1 = new HashMap<String, String>();
            attributes1.put("numberOfCables", "4");
            attributes1.put("numberOfFibers", "12");
            definition1.setTemplateFunctionAttributes(attributes1);

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         pr.setKuwaibaTemplateList(kuwaibaTemplateDefinitionList);

      }

   }

   public static class GponConstants {

      public static final String PARENT_CONTINENT = "Europe";
      public static final String PARENT_COUNTRY = "Great Britain";
      public static final String PARENT_STATE = "Hampshire";
      public static final String PARENT_CITY = "Southampton";
      public static final String PARENT_FACILITY = "SOTNOO1"; // fex

      public static final String PARENT_LOCATION_CLASS_NAME = "Neighborhood";
      public static final String PARENT_LOCATION_VALUE = "BitternePk";

      public static final String STREET_CONTAINER_CLASS_NAME = "Neighborhood";

      public static final String ONT_CONTAINER_CLASS_NAME = "House";
      public static final String ONT_CONTAINER_TEMPLATE_NAME = "House_01";

      // ONT (Optical Network Terminal)
      public static final String ONT_CLASS_NAME = "OpticalNetworkTerminal";
      public static final String ONT_TEMPLATE_NAME = "ONT_NOKIA_01";

      // CSP (Customer Splice Point):
      // The CSP is where the external fiber cable (usually coming from the street or a local cabinet) is spliced to the internal fiber cable that runs to the ONT
      public static final String CSP_CLASS_NAME = "SpliceBox";
      public static final String CSP_TEMPLATE_NAME = "CSP_BFU_1_2_01";

      public static final String SECONDARY_SPLITTER_CONTAINER_CLASS_NAME = "Pole";
      public static final String SECONDARY_SPLITTER_CONTAINER_TEMPLATE_NAME = "POLE_2_16drop";

      public static final String SECONDARY_SPLITTER_CLASS_NAME = "FiberSplitter";
      public static final String SECONDARY_SPLITTER_TEMPLATE_NAME = "SPL16";

      public static final String PRIMARY_SPLITTER_CONTAINER_TEMPLATE_NAME = "CAB_10SPL8";
      public static final String PRIMARY_SPLITTER_CONTAINER_CLASS_NAME = "OutdoorsCabinet";

      public static final String PRIMARY_SPLITTER_CLASS_NAME = "FiberSplitter";
      public static final String PRIMARY_SPLITTER_TEMPLATE_NAME = "SPL8";

      public static final String OLT_CONTAINER_TEMPLATE_NAME = "FEX_RACK_001";
      public static final String OLT_CONTAINER_CLASS_NAME = "Rack";
      public static final String OLT_CONTAINER_NAME = "SOTN001_RACK001";

      public static final String OLT_TEMPLATE_NAME = "OLT_NOKIA_01";
      public static final String OLT_CLASS_NAME = "OpticalLineTerminal";

   }

}
