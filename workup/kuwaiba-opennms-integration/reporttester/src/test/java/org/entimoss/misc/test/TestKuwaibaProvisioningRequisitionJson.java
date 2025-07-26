package org.entimoss.misc.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.entimoss.kuwaiba.provisioning.KuwaibaClass;
import org.entimoss.kuwaiba.provisioning.KuwaibaProvisioningRequisition;
import org.entimoss.kuwaiba.provisioning.KuwaibaTemplateDefinition;
import org.entimoss.kuwaiba.provisioning.KuwaibaWireContainerConnection;
import org.junit.Test;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestKuwaibaProvisioningRequisitionJson {

   @Test
   public void test() throws StreamWriteException, DatabindException, IOException {

      String ontLabelName = "ONT_200001919492";
      String ontContainerName = "UPRN_200001919492"; // BUILDING

      Double ontContainerLatitude = 50.924450;
      Double ontContainerLongitude = -1.372045;
      String ontIpAddress = "254.0.0.1";
      String ontComment = "";
      String ontSerialNumber = "ALCLF2E910B1A04";

      String ontAssetNumber = "200001919492";

      String oncLabelName = "ONC_200001919492";

      String secondarySplitterName = "SO18BPK1_POLE_001_SPL16_001"; // SPLITTER
      String secondarySplitterContainerName = "SO18BPK1_POLE_001"; //NAME
      Double secondarySplitterContainerLatitude = 0.0;
      Double secondarySplitterContainerLongitude = 0.0;
      String secondarySplitterComment = "";
      String secondarySplitterSerialNumber = "";
      String secondarySplitterAssetNumber = "";

      String primarySplitterName = "SO18BPK1_CAB_001_SPL8_001";
      String primarySplitterContainerName = "SO18BPK1_CAB_001";
      Double primarySplitterContainerLatitude = 0.0;
      Double primarySplitterContainerLongitude = 0.0;
      String primarySplitterComment = "";
      String primarySplitterSerialNumber = "";
      String primarySplitterAssetNumber = "";

      String oltLabelName = "SOTN001_OLT_103";
      String oltRackName = "SOTNOO1_RACK001";
      Double oltFexLatitude = 0.0;
      Double oltFexLongitude = 0.0;
      String oltIpAddress = "254.0.0.1";
      String oltComment = "";
      String oltSerialNumber = "";
      String oltAssetNumber = "";

      KuwaibaProvisioningRequisition pr;

      KuwaibaGponProvisoner kuwaibaGponProvisoner = new KuwaibaGponProvisoner();

      kuwaibaGponProvisoner.addTemplatesToProvisioningRequisition();

      kuwaibaGponProvisoner.addStaticObjectsToProvisioningRequisition();

      kuwaibaGponProvisoner.addLineToKuwaibaRequisition(ontLabelName, ontContainerName, ontContainerLatitude, ontContainerLongitude, ontIpAddress,
               ontComment, ontSerialNumber, ontAssetNumber,

               oncLabelName,

               secondarySplitterName, secondarySplitterContainerName, secondarySplitterContainerLatitude, secondarySplitterContainerLongitude,
               secondarySplitterComment, secondarySplitterSerialNumber, secondarySplitterAssetNumber,

               primarySplitterName, primarySplitterContainerName, primarySplitterContainerLatitude, primarySplitterContainerLongitude,
               primarySplitterComment, primarySplitterSerialNumber, primarySplitterAssetNumber,

               oltLabelName, oltRackName, oltFexLatitude, oltFexLongitude, oltIpAddress, oltComment, oltSerialNumber, oltAssetNumber);

      pr = kuwaibaGponProvisoner.finalise();

      ObjectMapper om = new ObjectMapper();
      om.enable(SerializationFeature.INDENT_OUTPUT);

      File file = new File("./target/data-overlay/kuwaibaProvisioningRequisition.json");
      file.delete();

      File dir = file.getParentFile();
      System.out.println("output directory: " + dir.getAbsolutePath());
      dir.mkdirs();

      om.writeValue(file, pr);
      System.out.println("Filed saved to: " + dir.getAbsolutePath());

      // check you can read the file
      KuwaibaProvisioningRequisition pr2 = om.readValue(file, KuwaibaProvisioningRequisition.class);
      System.out.println("read file: " + pr2);

   }

   public class KuwaibaGponProvisoner {

      KuwaibaProvisioningRequisition pr = new KuwaibaProvisioningRequisition();

      public KuwaibaProvisioningRequisition finalise() {
         return pr;
      }

      public void addLineToKuwaibaRequisition(
               String ontLabelName, String ontContainerName, Double ontContainerLatitude, Double ontContainerLongitude, String ontIpAddress,
               String ontComment, String ontSerialNumber, String ontAssetNumber,

               String oncLabelName,

               String secondarySplitterName, String secondarySplitterContainerName, Double secondarySplitterContainerLatitude, Double secondarySplitterContainerLongitude,
               String secondarySplitterComment, String secondarySplitterSerialNumber, String secondarySplitterAssetNumber,

               String primarySplitterName, String primarySplitterContainerName, Double primarySplitterContainerLatitude, Double primarySplitterContainerLongitude,
               String primarySplitterComment, String primarySplitterSerialNumber, String primarySplitterAssetNumber,

               String oltLabelName, String oltFexName, Double oltFexLatitude, Double oltFexLongitude, String oltIpAddress, String oltComment, String oltSerialNumber, String oltAssetNumber) {

         // House ont container
         KuwaibaClass ontContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(ontContainer);

         ontContainer.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
         ontContainer.setTemplateName(GponConstants.ONT_CONTAINER_TEMPLATE_NAME); // house
         ontContainer.setParentName(GponConstants.PARENT_LOCATION_VALUE); // bitterne park
         ontContainer.setParentClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);
         ontContainer.setName(ontContainerName);

         HashMap<String, String> ontContainerAttributes = new HashMap<String, String>();
         ontContainerAttributes.put("latitude", String.format("%.8f", ontContainerLatitude));
         ontContainerAttributes.put("longitude", String.format("%.8f", ontContainerLongitude));
         ontContainer.getAttributes().putAll(ontContainerAttributes);

         // ont
         KuwaibaClass ont = new KuwaibaClass();
         pr.getKuwaibaClassList().add(ont);

         ont.setClassName(GponConstants.ONT_CLASS_NAME);
         ont.setTemplateName(GponConstants.ONT_TEMPLATE_NAME); // house
         ont.setParentName(ontContainerName);
         ont.setParentClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
         ont.setName(ontLabelName);
         HashMap<String, String> ontAttributes = new HashMap<String, String>();

         ontAttributes.put("serialNumber", ontSerialNumber);
         ontContainer.getAttributes().putAll(ontAttributes);

         // onc
         KuwaibaClass onc = new KuwaibaClass();
         pr.getKuwaibaClassList().add(onc);

         onc.setClassName(GponConstants.ONC_CLASS_NAME);
         onc.setTemplateName(GponConstants.ONC_TEMPLATE_NAME); // house
         onc.setParentName(ontContainerName);
         onc.setParentClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
         onc.setName(oncLabelName); // TODO

         // pole secondary splitter container
         KuwaibaClass secondarySplitterContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(secondarySplitterContainer);

         secondarySplitterContainer.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
         secondarySplitterContainer.setTemplateName(GponConstants.SECONDARY_SPLITTER_CONTAINER_TEMPLATE_NAME); // house
         secondarySplitterContainer.setParentName(GponConstants.PARENT_LOCATION_VALUE); // bitterne park
         secondarySplitterContainer.setParentClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);
         secondarySplitterContainer.setName(secondarySplitterContainerName);

         HashMap<String, String> secondarySplitterContainerAttributes = new HashMap<String, String>();
         secondarySplitterContainerAttributes.put("latitude", String.format("%.8f", secondarySplitterContainerLatitude));
         secondarySplitterContainerAttributes.put("longitude", String.format("%.8f", secondarySplitterContainerLongitude));
         secondarySplitterContainer.getAttributes().putAll(secondarySplitterContainerAttributes);

         // secondarySplitter 
         KuwaibaClass secondarySplitter = new KuwaibaClass();
         pr.getKuwaibaClassList().add(secondarySplitter);

         secondarySplitter.setClassName(GponConstants.SECONDARY_SPLITTER_CLASS_NAME);
         secondarySplitter.setTemplateName(GponConstants.SECONDARY_SPLITTER_TEMPLATE_NAME); // house
         secondarySplitter.setParentName(secondarySplitterContainerName);
         secondarySplitter.setParentClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
         secondarySplitter.setName(secondarySplitterName);

         // cab primarySplitter container
         KuwaibaClass primarySplitterContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(primarySplitterContainer);

         primarySplitterContainer.setClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
         primarySplitterContainer.setTemplateName(GponConstants.PRIMARY_SPLITTER_CONTAINER_TEMPLATE_NAME); // house
         primarySplitterContainer.setParentName(GponConstants.PARENT_LOCATION_VALUE); // bitterne park
         primarySplitterContainer.setParentClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);
         primarySplitterContainer.setName(primarySplitterContainerName);

         HashMap<String, String> primarySplitterContainerAttributes = new HashMap<String, String>();
         primarySplitterContainerAttributes.put("latitude", String.format("%.8f", primarySplitterContainerLatitude));
         primarySplitterContainerAttributes.put("longitude", String.format("%.8f", primarySplitterContainerLongitude));
         primarySplitterContainer.getAttributes().putAll(primarySplitterContainerAttributes);

         // primarySplitter 
         KuwaibaClass primarySplitter = new KuwaibaClass();
         pr.getKuwaibaClassList().add(primarySplitter);

         primarySplitter.setClassName(GponConstants.PRIMARY_SPLITTER_CLASS_NAME);
         primarySplitter.setTemplateName(GponConstants.PRIMARY_SPLITTER_TEMPLATE_NAME); // house
         primarySplitter.setParentName(primarySplitterContainerName);
         primarySplitter.setParentClassName(GponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
         primarySplitter.setName(primarySplitterName);

         // olt 
         KuwaibaClass olt = new KuwaibaClass();
         pr.getKuwaibaClassList().add(olt);

         olt.setClassName(GponConstants.OLT_CLASS_NAME);
         olt.setTemplateName(GponConstants.OLT_TEMPLATE_NAME); // fex

         olt.setParentName(GponConstants.OLT_CONTAINER_NAME);
         olt.setParentClassName(GponConstants.OLT_CONTAINER_CLASS_NAME);
         olt.setName(oltLabelName);
         HashMap<String, String> oltAttributes = new HashMap<String, String>();

         oltAttributes.put("serialNumber", oltSerialNumber);
         olt.getAttributes().putAll(oltAttributes);

      }

      public void addStaticObjectsToProvisioningRequisition() {

         // create southampton if doesn't exist
         // block to isolate repeat variables
         try {
            KuwaibaClass kuwaibaClass1 = new KuwaibaClass();
            pr.getKuwaibaClassList().add(kuwaibaClass1);

            kuwaibaClass1.setClassName("City");
            kuwaibaClass1.setName(GponConstants.PARENT_CITY); // southampton
            kuwaibaClass1.setParentName(GponConstants.PARENT_STATE); //hampshire
            kuwaibaClass1.setParentClassName("State");

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
            kuwaibaClass1.setParentName(GponConstants.PARENT_CITY); // bitterne park
            kuwaibaClass1.setParentClassName("City");

         } catch (Exception e) {
            e.printStackTrace();
         }

         // FEX Facility  if doesn't exist
         // block to isolate repeat variables
         try {
            KuwaibaClass kuwaibaClass1 = new KuwaibaClass();
            pr.getKuwaibaClassList().add(kuwaibaClass1);

            kuwaibaClass1.setClassName("Facility");
            kuwaibaClass1.setName(GponConstants.PARENT_FACILITY); // fex
            kuwaibaClass1.setParentName(GponConstants.PARENT_CITY);
            kuwaibaClass1.setParentClassName("City");

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

            kuwaibaClass1.setClassName(GponConstants.OLT_CONTAINER_CLASS_NAME);
            kuwaibaClass1.setTemplateName(GponConstants.OLT_CONTAINER_TEMPLATE_NAME); // FEX_RACK_001
            kuwaibaClass1.setName(GponConstants.OLT_CONTAINER_NAME);
            kuwaibaClass1.setParentName(GponConstants.PARENT_FACILITY);
            kuwaibaClass1.setParentClassName("Facility");

         } catch (Exception e) {
            e.printStackTrace();
         }

         // TODO REMOVE TEST OBJECTS - as will be created from data
         // House ont container
         // block to isolate repeat variables
         try {
            KuwaibaClass ontContainer = new KuwaibaClass();
            pr.getKuwaibaClassList().add(ontContainer);

            ontContainer.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
            ontContainer.setTemplateName(GponConstants.ONT_CONTAINER_TEMPLATE_NAME); // House_01
            ontContainer.setParentName(GponConstants.PARENT_LOCATION_VALUE); // bitterne park
            ontContainer.setParentClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);
            ontContainer.setName("UPRN_200001919492");

            HashMap<String, String> ontContainerAttributes = new HashMap<String, String>();
            // BURNETT CLOSE
            ontContainerAttributes.put("latitude", String.format("%.8f", -1.371881206));
            ontContainerAttributes.put("longitude", String.format("%.8f", 50.92471325));
            ontContainer.getAttributes().putAll(ontContainerAttributes);

         } catch (Exception e) {
            e.printStackTrace();
         }

         // pole POLE_2_16drop
         // block to isolate repeat variables
         try {
            // pole secondary splitter container
            KuwaibaClass secondarySplitterContainer = new KuwaibaClass();
            pr.getKuwaibaClassList().add(secondarySplitterContainer);

            secondarySplitterContainer.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
            secondarySplitterContainer.setTemplateName(GponConstants.SECONDARY_SPLITTER_CONTAINER_TEMPLATE_NAME);
            secondarySplitterContainer.setParentName(GponConstants.PARENT_LOCATION_VALUE); // bitterne park
            secondarySplitterContainer.setParentClassName(GponConstants.PARENT_LOCATION_CLASS_NAME);
            secondarySplitterContainer.setName("SO18BPK1_POLE_001");

            HashMap<String, String> secondarySplitterContainerAttributes = new HashMap<String, String>();
            // pole 50.92451031284738, -1.371651746700767
            secondarySplitterContainerAttributes.put("latitude", String.format("%.8f", -1.371651746700767));
            secondarySplitterContainerAttributes.put("longitude", String.format("%.8f", 50.92451031284738));
            secondarySplitterContainer.getAttributes().putAll(secondarySplitterContainerAttributes);

         } catch (Exception e) {
            e.printStackTrace();
         }

         // Add Static connections 
         
         // pole to house
         // block to isolate repeat variables
         try {
            KuwaibaWireContainerConnection connection1 = new KuwaibaWireContainerConnection();
            pr.getKuwaibaWireContainerConnectionList().add(connection1);

            // pole
            KuwaibaClass aEnd = new KuwaibaClass();
            aEnd.setName("SO18BPK1_POLE_001");
            aEnd.setClassName(GponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
            connection1.setaEnd(aEnd);

            //house
            KuwaibaClass bEnd = new KuwaibaClass();
            bEnd.setName("UPRN_200001919492");
            bEnd.setClassName(GponConstants.ONT_CONTAINER_CLASS_NAME);
            connection1.setbEnd(bEnd);

            KuwaibaClass connectionClass = new KuwaibaClass();
            connectionClass.setName("BFU_1_2_SO18BPK1_POLE_001_UPRN_200001919492");
            connectionClass.setClassName("WireContainer");
            connectionClass.setTemplateName("BFU_1_2");
            connection1.setConnectionClass(connectionClass);

         } catch (Exception e) {
            e.printStackTrace();
         }
         
      }

      public void addTemplatesToProvisioningRequisition() {

         List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList = new ArrayList<KuwaibaTemplateDefinition>();

         // ONT_CONTAINER_CLASS_NAME = "House";
         // ONT_CONTAINER_TEMPLATE_NAME = "House_01";
         //         
         // ONT_CLASS_NAME = "OpticalNetworkTerminal";
         // ONT_TEMPLATE_NAME = "ONT_NOKIA_01";
         // ONC_CLASS_NAME = "SpliceBox";
         // ONC_TEMPLATE_NAME = "CSP_BFU_1_2_01";

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
               childDefinition1.setTemplateElementName("SPL16_" + String.format("%02d", splitterNo));
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
               childDefinition1.setTemplateElementName("SPL8_" + String.format("%02d", splitterNo));
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

            // 10 OLT in rack
            for (int oltNo = 1; oltNo <= 10; oltNo++) {
               KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
               childDefinition1.setTemplateElementName("OLT_NOKIA_01_" + String.format("%02d", oltNo));
               childDefinition1.setClassName("OpticalLineTerminal");
               childDefinition1.setSpecial(false);

               for (int card = 1; card <= 2; card++) {
                  KuwaibaTemplateDefinition childDefinition1_1 = new KuwaibaTemplateDefinition();
                  childDefinition1_1.setTemplateElementName("card-" + String.format("%02d", card));
                  childDefinition1_1.setClassName("OLTBoard");
                  childDefinition1_1.setSpecial(false);
                  childDefinition1.getChildKuwaibaTemplateDefinitions().add(childDefinition1_1);

                  for (int opticalPort = 1; opticalPort <= 16; opticalPort++) {
                     KuwaibaTemplateDefinition childDefinition1_2 = new KuwaibaTemplateDefinition();
                     childDefinition1_2.setTemplateElementName("IN-" + String.format("%02d", opticalPort));
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

      public static final String ONT_CONTAINER_CLASS_NAME = "House";
      public static final String ONT_CONTAINER_TEMPLATE_NAME = "House_01";

      public static final String ONT_CLASS_NAME = "OpticalNetworkTerminal";
      public static final String ONT_TEMPLATE_NAME = "ONT_NOKIA_01";

      public static final String ONC_CLASS_NAME = "SpliceBox";
      public static final String ONC_TEMPLATE_NAME = "CSP_BFU_1_2_01";

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
      public static final String OLT_CONTAINER_NAME = "SOTNOO1_RACK001";

      public static final String OLT_TEMPLATE_NAME = "OLT_NOKIA_01";
      public static final String OLT_CLASS_NAME = "OpticalLineTerminal";

   }

}
