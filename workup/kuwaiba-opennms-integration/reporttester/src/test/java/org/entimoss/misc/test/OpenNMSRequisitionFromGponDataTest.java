package org.entimoss.misc.test;

import static org.junit.Assert.*;

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
import java.util.ArrayList;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test class creates an opennms requisition direct from UPRN data
 */
public class OpenNMSRequisitionFromGponDataTest {
   static Logger LOG = LoggerFactory.getLogger(OpenNMSRequisitionFromGponDataTest.class); // remove static in groovy

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
   Integer UPRN_limitLines = null; 

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

      OpenNMSRequisitionPopulator openNMSRequisitionPopulator = new OpenNMSRequisitionPopulator(defaultLocation, foreignSource, requisitionOutputFileLocation);

      try {
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

                  int poleNo = uprnCount / (SPLITTERS_TO_USE_PER_POLE * SECONDARY_SPLIT_RATIO); // pole number
                  int localPolePortNo = uprnCount % (SPLITTERS_TO_USE_PER_POLE * SECONDARY_SPLIT_RATIO); 
                  int poleSplitterNo = localPolePortNo / SECONDARY_SPLIT_RATIO; // splitter number on pole
                  int poleSplitterPortNo = localPolePortNo % SECONDARY_SPLIT_RATIO; // port on splitter

                  // each cabinet splitter port associated with one poleSplitter
                  int cabinetNo = poleNo / (SPLITTERS_TO_USE_PER_POLE * SPLITTERS_TO_USE_PER_CAB);
                  int cabinetSplitterNo = (uprnCount / SECONDARY_SPLIT_RATIO) / (SPLITTERS_TO_USE_PER_CAB * PRIMARY_SPLIT_RATIO);
                  int cabinetSplitterPortNo = (uprnCount / SECONDARY_SPLIT_RATIO) % (SPLITTERS_TO_USE_PER_CAB * PRIMARY_SPLIT_RATIO);

                  // each olt has multiple shelves each with separate ports (2 x 8 port shelves = 16 possible splitters)
                  int oltShelfNo = cabinetSplitterNo / PORTS_PER_OLT_CARD;
                  int oltPortNo = cabinetSplitterNo % PORTS_PER_OLT_CARD; // one port per primary splitter

                  // allocate one olt per cabinet (2 x 8 port shelves = 16 possible splitters)
                  int oltNo = oltRangeStartNumber + cabinetNo;

                  LOG.debug("***********");
                  LOG.debug("calculations:  uprnCount=" + uprnCount + ", poleNo=" + poleNo + ", poleSplitterNo=" + poleSplitterNo + ", poleSplitterPortNo=" + poleSplitterPortNo + 
                           ", cabinetNo=" + cabinetNo +
                           ", cabinetSplitterNo=" + cabinetSplitterNo + ", cabinetSplitterPortNo=" + cabinetSplitterPortNo + ", oltNo=" + oltNo + ", oltShelfNo=" + oltShelfNo +
                           ", oltPortNo=" + oltPortNo);

                  // create house UPRN , ONT if doesn't exist splice 2 fibres
                  // Building / House              UPRN_<uprn>  UPRN_200001919492
                  // CSP (Customer Splice Point)   CSP_<uprn>   CSP_200001919492 note CSP has two splices IN-0 OUT-0 
                  // Optical Network Terminator    ONT_<uprn>   ONT_200001919492   eth0, pon

                  String buildingName = "UPRN_" + uprn;
                  String ontName = "ONT_" + uprn;
                  String cspName = "CSP_" + uprn;
                  

                  // create pole
                  // parentLocationObjectPrefixValue_POLE_<NUMBER>   SO18BPK1_POLE_001
                  String poleName = parentLocationObjectPrefixValue + "_POLE_" + poleNo;

                  // create pole splitters 2 splitters per pole 1:16 
                  // parentLocationObjectPrefixValue_POLE_<NUMBER>_<SPTYPE>_<NUMBER>   SO18BPK1_POLE_001_SPL16_001
                  String poleSplitterName = poleName + "_SPL16_" + poleSplitterNo;

                  // Cabinet parentLocationObjectPrefixValue_CAB_<NUMBER>   SO18BPK1_CAB_001
                  String cabinetName = parentLocationObjectPrefixValue + "_CAB_" + cabinetNo;

                  // cabinet splitters 10 splitters per cabinet 1:8 
                  // parentLocationObjectPrefixValue_CAB_<NUMBER>_<SPTYPE>_<NUMBER>     SO18BPK1_CAB_001_SPL8_001
                  String cabinetSplitterName = cabinetName + "_SPL8_" + cabinetSplitterNo;

                  // create fibre container pole to house 2 fibres
                  // BFU_1_2_SO18BPK1_POLE_001_UPRN_200001919492
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  String poleToBuildingContainerName = "BFU_1_2_"+buildingName+"_"+poleName;

                  // create fibre container pole to cabinet 4x12 fibres
                  // BFU_4_12_SO18BPK1_CAB_001_SO18BPK1_POLE_001
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  String cabinetToPoleContainerName = "BFU_4_12_"+cabinetName+"_"+poleName;

                  // create olt in fex
                  // parentFexName_OLT_<NUMBER>   SOTN001_OLT_001
                  String oltName = parentFexName + "_OLT_" + oltNo;
                  
                  // create fibre container fex to cabinet 4x12 fibres  (10 + 2 spare)
                  // BFU_4_12_SOTN001_SO18BPK1_CAB_001
                  // <CONTAINER_TYPE>_parentLocationObjectPrefixValue_CAB_<NUMBER>_parentLocationObjectPrefixValue_POLE_<NUMBER>
                  String cabinetToFexContainerName = "BFU_4_12_"+parentFexName+"_"+cabinetName;
                  
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

                  // POPULATE OPENNMS REQUISITION
                  String ontLabelName = ontName;
                  String ontContainerName = buildingName;
                  Double ontContainerLatitude = latitude;
                  Double ontContainerLongitude = longitude;
                  String ontIpAddress = OpenNMSRequisitionPopulator.DUMMY_IP_ADDRESS;
                  String ontComment = ontAddress ;
                  String ontSerialNumber = ontSerialNo;
                  String ontAssetNumber = ontAssetNo;

                  String secondarySplitterName = poleSplitterName; //  (splitters on poles)
                  String secondarySplitterContainerName = poleName;
                  Double secondarySplitterContainerLatitude = poleLatitude;
                  Double secondarySplitterContainerLongitude = poleLongitude;
                  String secondarySplitterComment = "";
                  String secondarySplitterSerialNumber = "";
                  String secondarySplitterAssetNumber = "";

                  String primarySplitterName = cabinetSplitterName; //  (splitters in cabinets)
                  String primarySplitterContainerName = cabinetName;
                  Double primarySplitterContainerLatitude = cabinetLatitude;
                  Double primarySplitterContainerLongitude = cabinetLongitude;
                  String primarySplitterComment = "";
                  String primarySplitterSerialNumber = "";
                  String primarySplitterAssetNumber = "";

                  String oltLabelName = oltName; // (ltes in cabinets)
                  String oltFexName = parentFexName;
                  Double oltFexLatitude = fexLatitude;
                  Double oltFexLongitude = fexLongitude;
                  String oltIpAddress = OpenNMSRequisitionPopulator.DUMMY_IP_ADDRESS;
                  String oltComment = "";
                  String oltSerialNumber = "";
                  String oltAssetNumber = "";

                  openNMSRequisitionPopulator.addLineToOpenNMSRequisition(
                           ontLabelName, ontContainerName, ontContainerLatitude, ontContainerLongitude, ontIpAddress, ontComment, ontSerialNumber, ontAssetNumber,

                           secondarySplitterName, secondarySplitterContainerName, secondarySplitterContainerLatitude, secondarySplitterContainerLongitude,
                           secondarySplitterComment, secondarySplitterSerialNumber, secondarySplitterAssetNumber,

                           primarySplitterName, primarySplitterContainerName, primarySplitterContainerLatitude, primarySplitterContainerLongitude,
                           primarySplitterComment, primarySplitterSerialNumber, primarySplitterAssetNumber,

                           oltLabelName, oltFexName, oltFexLatitude, oltFexLongitude, oltIpAddress, oltComment, oltSerialNumber, oltAssetNumber);

                  uprnCount++;

               } catch (Exception ie) {
                  errorlineCount++;
                  String errormsg = String.format("Error processing line %s: %s", line, ie.getMessage());
                  LOG.warn(errormsg);

               }

            }
         }

         List<List<String>> csvData = openNMSRequisitionPopulator.finaliseRequisition();

         File outputFile = new File(requisitionOutputFileLocation + foreignSource + ".csv");
         exportCsvFile(outputFile, csvData);

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

   public void exportCsvFile(File outputFile, List<List<String>> csvData) {

      PrintWriter printWriter = null;
      try {
         outputFile.delete();

         File parent = outputFile.getParentFile();
         if (!parent.exists())
            parent.mkdirs();

         printWriter = new PrintWriter(new FileWriter(outputFile));
         for (List<String> line : csvData) {
            Iterator<String> lineItr = line.iterator();
            StringBuilder linesb = new StringBuilder();
            while (lineItr.hasNext()) {
               linesb.append(lineItr.next());
               if (lineItr.hasNext()) {
                  linesb.append(",");
               }
            }
            printWriter.println(linesb.toString());
         }

      } catch (Exception ex) {
         ex.printStackTrace();
      } finally {
         if (printWriter != null)
            printWriter.close();
      }

   }

   public class OpenNMSRequisitionPopulator {

      public static final String ONT_LABEL = "ONT";
      public static final String OLT_LABEL = "OLT";
      public static final String PE_ROUTER_LABEL = "PE";
      public static final String PRIMARY_SPLITTER_LABEL = "PRIMARY_SPLITTER";
      public static final String SECONDARY_SPLITTER_LABEL = "SECONDARY_SPLITTER";
      public static final String DUMMY_IP_ADDRESS = "254.0.0.1";

      // used for opennms requisition
      private Map<String, List<String>> ontLines = new LinkedHashMap<String, List<String>>();
      private Map<String, List<String>> primarySplitterLines = new LinkedHashMap<String, List<String>>();
      private Map<String, List<String>> secondarySplitterLines = new LinkedHashMap<String, List<String>>();
      private Map<String, List<String>> oltLines = new LinkedHashMap<String, List<String>>();

      private List<List<String>> csvData = new ArrayList<List<String>>();

      private String defaultLocation;
      private String foreignSource;
      private String requisitionOutputFileLocation;

      public OpenNMSRequisitionPopulator(String defaultLocation, String foreignSource, String requisitionOutputFileLocation) {
         super();
         this.defaultLocation = defaultLocation;
         this.foreignSource = foreignSource;
         this.requisitionOutputFileLocation = requisitionOutputFileLocation;
      }

      public void addLineToOpenNMSRequisition(
               String ontLabelName, String ontContainerName, Double ontContainerLatitude, Double ontContainerLongitude, String ontIpAddress,
               String ontComment, String ontSerialNumber, String ontAssetNumber,

               String secondarySplitterName, String secondarySplitterContainerName, Double secondarySplitterContainerLatitude, Double secondarySplitterContainerLongitude,
               String secondarySplitterComment, String secondarySplitterSerialNumber, String secondarySplitterAssetNumber,

               String primarySplitterName, String primarySplitterContainerName, Double primarySplitterContainerLatitude, Double primarySplitterContainerLongitude,
               String primarySplitterComment, String primarySplitterSerialNumber, String primarySplitterAssetNumber,

               String oltLabelName, String oltFexName, Double oltFexLatitude, Double oltFexLongitude, String oltIpAddress, String oltComment, String oltSerialNumber, String oltAssetNumber) {

         LOG.debug("OpenNMSRequisitionPopulator addLineToOpenNMSRequisition [ontLabelName=" + ontLabelName + ", ontContainerName=" + ontContainerName + ", ontIpAddress=" + ontIpAddress +
                  ", ontContainerLatitude=" + ontContainerLatitude + ", ontContainerLongitude=" + ontContainerLongitude +
                  ", ontComment=" + ontComment + ", ontSerialNumber=" + ontSerialNumber + ", ontAssetNumber=" + ontAssetNumber +

                  ", secondarySplitterName=" + secondarySplitterName +
                  ", secondarySplitterContainerName=" + secondarySplitterContainerName + ", secondarySplitterContainerLatitude=" + secondarySplitterContainerLatitude +
                  ", secondarySplitterContainerLongitude=" + secondarySplitterContainerLongitude +
                  ", secondarySplitterComment=" + secondarySplitterComment + ", secondarySplitterSerialNumber=" + secondarySplitterSerialNumber +
                  ", secondarySplitterAssetNumber=" + secondarySplitterAssetNumber +

                  ", primarySplitterName=" + primarySplitterName + ", primarySplitterContainerName=" + primarySplitterContainerName +
                  ", primarySplitterContainerLatitude=" + primarySplitterContainerLatitude +
                  ", primarySplitterContainerLongitude=" + primarySplitterContainerLongitude +
                  ", primarySplitterComment=" + primarySplitterComment + ", primarySplitterSerialNumber=" + primarySplitterSerialNumber + ", primarySplitterAssetNumber=" + primarySplitterAssetNumber +

                  ", oltLabelName=" + oltLabelName + ", oltFexName=" + oltFexName +
                  ", oltFexLatitude=" + oltFexLatitude + ", oltFexLongitude=" + oltFexLongitude + ", oltIpAddresse=" + oltIpAddress +
                  ", oltComment=" + oltComment + ", oltSerialNumber=" + oltSerialNumber + ", oltAssetNumber=" + oltAssetNumber + "]");

         // POPULATE OPENNMS REQUISITION

         // ADD DATA TO LINES

         // ***********************
         // ONT
         // create and populate empty line

         try {
            List<String> ontLine = new ArrayList<>(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size());
            for (int i = 0; i < OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size(); i++)
               ontLine.add("");

            // add data
            String iP_Management = DUMMY_IP_ADDRESS;
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.IP_MANAGEMENT), iP_Management);
            String mgmtType_ = "N";
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MGMTTYPE_), mgmtType_);
            String svc_Forced = "";
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.SVC_FORCED), svc_Forced);
            String cat_ = ONT_LABEL;
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.CAT_), cat_);
            String asset_region = oltFexName;
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_REGION), asset_region);
            String location = defaultLocation;
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MINION_LOCATION), location);

            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.NODE_LABEL), ontLabelName);
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ID_), ontLabelName);

            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.PARENT_FOREIGN_ID), secondarySplitterName);
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.PARENT_FOREIGN_SOURCE), foreignSource);

            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LATITUDE), String.format("%.8f", ontContainerLatitude));
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LONGITUDE), String.format("%.8f", ontContainerLongitude));

            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_DESCRIPTION), "ONT in " + ontContainerName);
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_COMMENT), ontComment);

            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_SERIALNUMBER), ontSerialNumber);
            ontLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_ASSETNUMBER), ontAssetNumber);

            LOG.debug("putting ontName:" + ontLabelName + " ontLine: " + ontLine);
            ontLines.put(ontLabelName, ontLine);

         } catch (Exception ex) {
            throw new RuntimeException("problem creating ont requisition entry", ex);
         }

         // ***********************
         // SECONDARY SPLITTERS
         // create and populate empty line
         try {
            List<String> secondarySplitterLine = new ArrayList<>(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size());
            for (int i = 0; i < OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size(); i++)
               secondarySplitterLine.add("");

            // add data
            String iP_Management = DUMMY_IP_ADDRESS;
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.IP_MANAGEMENT), iP_Management);
            String mgmtType_ = "N";
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MGMTTYPE_), mgmtType_);
            String svc_Forced = "";
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.SVC_FORCED), svc_Forced);
            String cat_ = SECONDARY_SPLITTER_LABEL;
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.CAT_), cat_);
            String asset_region = oltFexName;
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_REGION), asset_region);
            String location = defaultLocation;
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MINION_LOCATION), location);

            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.NODE_LABEL), secondarySplitterName);
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ID_), secondarySplitterName);

            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.PARENT_FOREIGN_ID), primarySplitterName);
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.PARENT_FOREIGN_SOURCE), foreignSource);

            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LATITUDE),
                     String.format("%.8f", secondarySplitterContainerLatitude));
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LONGITUDE),
                     String.format("%.8f", secondarySplitterContainerLongitude));

            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_DESCRIPTION),
                     "Secondary splitter in " + secondarySplitterContainerName);
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_COMMENT), secondarySplitterComment);

            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_SERIALNUMBER), secondarySplitterSerialNumber);
            secondarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_ASSETNUMBER), secondarySplitterAssetNumber);

            LOG.debug("putting poleSplitterName:" + secondarySplitterName + " secondarySplitterLine: " + secondarySplitterLine);
            secondarySplitterLines.put(secondarySplitterName, secondarySplitterLine);

         } catch (Exception ex) {
            throw new RuntimeException("problem creating ont requisition entry", ex);
         }

         // ***********************
         // PRIMARY SPLITTERS
         // create and populate empty line

         try {
            List<String> primarySplitterLine = new ArrayList<>(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size());
            for (int i = 0; i < OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size(); i++)
               primarySplitterLine.add("");

            // add data
            String iP_Management = DUMMY_IP_ADDRESS;
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.IP_MANAGEMENT), iP_Management);
            String mgmtType_ = "N";
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MGMTTYPE_), mgmtType_);
            String svc_Forced = "";
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.SVC_FORCED), svc_Forced);
            String cat_ = PRIMARY_SPLITTER_LABEL;
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.CAT_), cat_);
            String asset_region = oltFexName;
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_REGION), asset_region);
            String location = defaultLocation;
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MINION_LOCATION), location);

            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.NODE_LABEL), primarySplitterName);
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ID_), primarySplitterName);

            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.PARENT_FOREIGN_ID), oltLabelName);
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.PARENT_FOREIGN_SOURCE), foreignSource);

            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LATITUDE),
                     String.format("%.8f", primarySplitterContainerLatitude));
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LONGITUDE),
                     String.format("%.8f", primarySplitterContainerLongitude));

            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_DESCRIPTION), "Primary splitter in " + primarySplitterContainerName);
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_COMMENT), primarySplitterComment);

            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_SERIALNUMBER), primarySplitterSerialNumber);
            primarySplitterLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_ASSETNUMBER), primarySplitterAssetNumber);

            LOG.debug("putting cabinetSplitterName:" + primarySplitterName + " primarySplitterLine: " + primarySplitterLine);
            primarySplitterLines.put(primarySplitterName, primarySplitterLine);

         } catch (Exception ex) {
            throw new RuntimeException("problem creating primary splitter requisition entry", ex);
         }

         // ***********************
         // OLTs in FEX
         // create and populate empty line
         try {
            List<String> oltLine = new ArrayList<>(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size());
            for (int i = 0; i < OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size(); i++)
               oltLine.add("");

            // add data
            String iP_Management = DUMMY_IP_ADDRESS;
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.IP_MANAGEMENT), iP_Management);
            String mgmtType_ = "N";
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MGMTTYPE_), mgmtType_);
            String svc_Forced = "";
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.SVC_FORCED), svc_Forced);
            String cat_ = OLT_LABEL;
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.CAT_), cat_);
            String asset_region = oltFexName;
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_REGION), asset_region);
            String location = defaultLocation;
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.MINION_LOCATION), location);

            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.NODE_LABEL), oltLabelName);
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ID_), oltLabelName);

            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LATITUDE), String.format("%.8f", oltFexLatitude));
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_LONGITUDE), String.format("%.8f", oltFexLongitude));

            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_DESCRIPTION), "OLT in " + oltFexName);
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_COMMENT), oltComment);

            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_SERIALNUMBER), oltSerialNumber);
            oltLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(OnmsRequisitionConstants.ASSET_ASSETNUMBER), oltAssetNumber);

            LOG.debug("putting oltname:" + oltLabelName + " oltLine" + oltLine);
            oltLines.put(oltLabelName, oltLine);

         } catch (Exception ex) {
            throw new RuntimeException("problem creating olt requisition entry", ex);
         }

      }

      public List<List<String>> finaliseRequisition() {

         // add header
         csvData.add(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS);

         // create single requisition
         csvData.addAll(oltLines.values());
         csvData.addAll(primarySplitterLines.values());
         csvData.addAll(secondarySplitterLines.values());
         csvData.addAll(ontLines.values());

         return csvData;

      }

   }

   public static class OnmsRequisitionConstants {

      public static final String NODE_LABEL = "Node_Label";
      public static final String ID_ = "ID_";
      public static final String MINION_LOCATION = "Location";
      public static final String PARENT_FOREIGN_ID = "Parent_Foreign_Id";
      public static final String PARENT_FOREIGN_SOURCE = "Parent_Foreign_Source";
      public static final String IP_MANAGEMENT = "IP_Management";
      public static final String MGMTTYPE_ = "MgmtType_";
      public static final String SVC_FORCED = "svc_Forced";
      public static final String CAT_ = "cat_";
      public static final String ASSET_CATEGORY = "Asset_category";
      public static final String ASSET_REGION = "Asset_region";
      public static final String ASSET_SERIALNUMBER = "Asset_serialNumber";
      public static final String ASSET_ASSETNUMBER = "Asset_assetNumber";
      public static final String ASSET_LATITUDE = "Asset_latitude";
      public static final String ASSET_LONGITUDE = "Asset_longitude";
      public static final String ASSET_THRESHOLDCATEGORY = "Asset_thresholdCategory";
      public static final String ASSET_NOTIFYCATEGORY = "Asset_notifyCategory";
      public static final String ASSET_POLLERCATEGORY = "Asset_pollerCategory";
      public static final String ASSET_DISPLAYCATEGORY = "Asset_displayCategory";
      public static final String ASSET_MANAGEDOBJECTTYPE = "Asset_managedObjectType";
      public static final String ASSET_MANAGEDOBJECTINSTANCE = "Asset_managedObjectInstance";
      public static final String ASSET_CIRCUITID = "Asset_circuitId";
      public static final String ASSET_DESCRIPTION = "Asset_description";

      public static final String ASSET_MANUFACTURER = "Asset_manufacturer";
      public static final String ASSET_VENDOR = "Asset_vendor";
      public static final String ASSET_MODELNUMBER = "Asset_modelnumber";
      public static final String ASSET_OPERATINGSYSTEM = "Asset_operatingsystem";
      public static final String ASSET_RACK = "Asset_rack";
      public static final String ASSET_SLOT = "Asset_slot";
      public static final String ASSET_PORT = "Asset_port";
      public static final String ASSET_DIVISION = "Asset_division";
      public static final String ASSET_DEPARTMENT = "Asset_department";
      public static final String ASSET_ADDRESS1 = "Asset_address1";
      public static final String ASSET_ADDRESS2 = "Asset_address2";
      public static final String ASSET_CITY = "Asset_city";
      public static final String ASSET_STATE = "Asset_state";
      public static final String ASSET_ZIP = "Asset_zip";
      public static final String ASSET_BUILDING = "Asset_building";
      public static final String ASSET_FLOOR = "Asset_floor";
      public static final String ASSET_ROOM = "Asset_room";
      public static final String ASSET_VENDORPHONE = "Asset_vendorphone";
      public static final String ASSET_VENDORFAX = "Asset_vendorfax";
      public static final String ASSET_VENDORASSETNUMBER = "Asset_vendorassetnumber";
      public static final String ASSET_USERLASTMODIFIED = "Asset_userlastmodified";
      public static final String ASSET_LASTMODIFIEDDATE = "Asset_lastmodifieddate";
      public static final String ASSET_DATEINSTALLED = "Asset_dateinstalled";
      public static final String ASSET_LEASE = "Asset_lease";
      public static final String ASSET_LEASEEXPIRES = "Asset_leaseexpires";
      public static final String ASSET_SUPPORTPHONE = "Asset_supportphone";
      public static final String ASSET_MAINTCONTRACT = "Asset_maintcontract";
      public static final String ASSET_MAINTCONTRACTEXPIRES = "Asset_maintcontractexpires";
      public static final String ASSET_COMMENT = "Asset_comment";
      public static final String ASSET_USERNAME = "Asset_username";
      public static final String ASSET_PASSWORD = "Asset_password";
      public static final String ASSET_ENABLE = "Asset_enable";
      public static final String ASSET_AUTOENABLE = "Asset_autoenable";
      public static final String ASSET_CONNECTION = "Asset_connection";
      public static final String ASSET_CPU = "Asset_cpu";
      public static final String ASSET_RAM = "Asset_ram";
      public static final String ASSET_STORAGECTRL = "Asset_storagectrl";
      public static final String ASSET_HDD1 = "Asset_hdd1";
      public static final String ASSET_HDD2 = "Asset_hdd2";
      public static final String ASSET_HDD3 = "Asset_hdd3";
      public static final String ASSET_HDD4 = "Asset_hdd4";
      public static final String ASSET_HDD5 = "Asset_hdd5";
      public static final String ASSET_HDD6 = "Asset_hdd6";
      public static final String ASSET_NUMPOWERSUPPLIES = "Asset_numpowersupplies";
      public static final String ASSET_INPUTPOWER = "Asset_inputpower";
      public static final String ASSET_ADDITIONALHARDWARE = "Asset_additionalhardware";
      public static final String ASSET_ADMIN = "Asset_admin";
      public static final String ASSET_SNMPCOMMUNITY = "Asset_snmpcommunity";
      public static final String ASSET_RACKUNITHEIGHT = "Asset_rackunitheight";
      public static final String ASSET_COUNTRY = "Asset_country";

      public static final String METADATA_SERVICE_ID = "MetaData_requisition:serviceId";
      public static final String METADATA_SERVICE_NAME = "MetaData_requisition:serviceName";
      public static final String METADATA_CUSTOMER_ID = "MetaData_requisition:customerId";
      public static final String METADATA_CUSTOMER_NAME = "MetaData_requisition:customerName";

      // note additional fields but preserve the order of original files
      public static final List<String> OPENNMS_REQUISITION_HEADERS = Arrays.asList(NODE_LABEL, ID_, MINION_LOCATION, PARENT_FOREIGN_ID,
               PARENT_FOREIGN_SOURCE, IP_MANAGEMENT, MGMTTYPE_, SVC_FORCED, CAT_, ASSET_CATEGORY, ASSET_REGION, ASSET_SERIALNUMBER,
               ASSET_ASSETNUMBER, ASSET_LATITUDE, ASSET_LONGITUDE, ASSET_THRESHOLDCATEGORY, ASSET_NOTIFYCATEGORY, ASSET_POLLERCATEGORY, ASSET_DISPLAYCATEGORY,
               ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE,
               ASSET_CIRCUITID, ASSET_DESCRIPTION,

               // additional metadata
               METADATA_SERVICE_ID, METADATA_SERVICE_NAME, METADATA_CUSTOMER_ID, METADATA_CUSTOMER_NAME,

               ASSET_COMMENT,

               // location values
               ASSET_BUILDING, ASSET_FLOOR, ASSET_ROOM, ASSET_RACK, ASSET_RACKUNITHEIGHT, ASSET_SLOT, ASSET_PORT, ASSET_REGION, ASSET_DIVISION, ASSET_DEPARTMENT,
               ASSET_ADDRESS1, ASSET_ADDRESS2, ASSET_CITY, ASSET_STATE, ASSET_ZIP, ASSET_COUNTRY,

               // equipment description
               ASSET_MODELNUMBER,
               ASSET_MANUFACTURER, ASSET_VENDOR, ASSET_VENDORPHONE, ASSET_VENDORFAX,

               // lease contacts
               ASSET_USERLASTMODIFIED, ASSET_LASTMODIFIEDDATE, ASSET_DATEINSTALLED, ASSET_LEASE, ASSET_LEASEEXPIRES, ASSET_SUPPORTPHONE, ASSET_MAINTCONTRACT, ASSET_MAINTCONTRACTEXPIRES,

               // misccredentials
               ASSET_USERNAME, ASSET_PASSWORD, ASSET_ENABLE, ASSET_AUTOENABLE, ASSET_CONNECTION,

               // misc data fields
               ASSET_OPERATINGSYSTEM, ASSET_CPU, ASSET_RAM, ASSET_STORAGECTRL, ASSET_HDD1, ASSET_HDD2, ASSET_HDD3, ASSET_HDD4, ASSET_HDD5, ASSET_HDD6,
               ASSET_NUMPOWERSUPPLIES, ASSET_INPUTPOWER, ASSET_ADDITIONALHARDWARE, ASSET_ADMIN, ASSET_SNMPCOMMUNITY

      );

      // this is same order as in csv header line
      public static final List<String> ORIGINAL_OPENNMS_REQUISITION_HEADERS = Arrays.asList(NODE_LABEL, ID_, MINION_LOCATION, PARENT_FOREIGN_ID,
               PARENT_FOREIGN_SOURCE, IP_MANAGEMENT, MGMTTYPE_, SVC_FORCED, CAT_, ASSET_CATEGORY, ASSET_REGION, ASSET_SERIALNUMBER,
               ASSET_ASSETNUMBER, ASSET_LATITUDE, ASSET_LONGITUDE, ASSET_THRESHOLDCATEGORY, ASSET_NOTIFYCATEGORY, ASSET_POLLERCATEGORY, ASSET_DISPLAYCATEGORY,
               ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE,
               ASSET_CIRCUITID, ASSET_DESCRIPTION,
               METADATA_SERVICE_ID, METADATA_SERVICE_NAME, METADATA_CUSTOMER_ID, METADATA_CUSTOMER_NAME);

      public static final String DEFAULT_MINION_LOCATION = "Default"; // used when OpenNMS core is the poller.

      // These service names should match the service definitions in poller-configuration.xml
      public static final String SERVICE_PASSIVE_SECONDARY_SPLITTER = "passive-secondary-node";
      public static final String SERVICE_PASSIVE_PRIMARY_SPLITTER = "passive-primary-node";
      public static final String SERVICE_PASSIVE_NODE_UP_SERVICE = "passive-node-up-service";

   }

   //  TODO REMOVE
   // class Temp{
   //      
   //      String ontLabelName = null ; //ontName;
   //      String ontContainerName = null ; //ontBuildingName;
   //      Double ontContainerLatitude = null ; //ontLatitude;
   //      Double ontContainerLongitude = null ; //ontLongitude;
   //      String secondarySplitterName = null ; //poleSplitterName;
   //      String secondarySplitterContainerName = null ; //poleName;
   //      Double secondarySplitterContainerLatitude = null ; //poleLatitude;
   //      Double secondarySplitterContainerLongitude = null ; //poleLongitude;
   //      String primarySplitterName = null ; //cabinetSplitterName;
   //      String primarySplitterContainerName = null ; //cabinetName;
   //      Double primarySplitterContainerLatitude = null ; //cabinetLatitude;
   //      Double primarySplitterContainerLongitude = null ; //cabinetLongitude;
   //      String oltLabelName = null ; //lteName;
   //      String lteFexName = null ; //parentFexName;
   //      Double lteFexLatitude = null ; //fexLatitude;
   //      Double lteFexLongitude = null ; //fexLongitude;
   //      
   //      public void addXXXLineToOpenNMSRequisition(
   //               String ontName, String ontBuildingName, Double ontLatitude, Double ontLongitude,
   //               String poleSplitterName, String poleName, Double poleLatitude, Double poleLongitude,
   //               String cabinetSplitterName, String cabinetName, Double cabinetLatitude, Double cabinetLongitude,
   //               String lteName, String parentFexName, Double fexLatitude, Double fexLongitude) {
   //
   //         ontLabelName = ontName;
   //         ontContainerName = ontBuildingName;
   //         ontContainerLatitude = ontLatitude;
   //         ontContainerLongitude = ontLongitude;
   //         secondarySplitterName = poleSplitterName;
   //         secondarySplitterContainerName = poleName;
   //         secondarySplitterContainerLatitude = poleLatitude;
   //         secondarySplitterContainerLongitude = poleLongitude;
   //         primarySplitterName = cabinetSplitterName;
   //         primarySplitterContainerName = cabinetName;
   //         primarySplitterContainerLatitude = cabinetLatitude;
   //         primarySplitterContainerLongitude = cabinetLongitude;
   //         lteLabelName = lteName;
   //         lteFexName = parentFexName;
   //         lteFexLatitude = fexLatitude;
   //         lteFexLongitude = fexLongitude;
   //         
   //
   //      }
   //
   //      @Override
   //      public String toString() {
   //         return "Temp [ontLabelName=" + ontLabelName + ", ontContainerName=" + ontContainerName + ", ontContainerLatitude=" + ontContainerLatitude + ", ontContainerLongitude=" + ontContainerLongitude
   //                  + ", secondarySplitterName=" + secondarySplitterName + ", secondarySplitterContainerName=" + secondarySplitterContainerName + ", secondarySplitterContainerLatitude="
   //                  + secondarySplitterContainerLatitude + ", secondarySplitterContainerLongitude=" + secondarySplitterContainerLongitude + ", primarySplitterName=" + primarySplitterName
   //                  + ", primarySplitterContainerName=" + primarySplitterContainerName + ", primarySplitterContainerLatitude=" + primarySplitterContainerLatitude + ", primarySplitterContainerLongitude="
   //                  + primarySplitterContainerLongitude + ", lteLabelName=" + lteLabelName + ", lteFexName=" + lteFexName + ", lteFexLatitude=" + lteFexLatitude + ", lteFexLongitude=" + lteFexLongitude
   //                  + "]";
   //      }
   //      
   //      
   //   }

}
