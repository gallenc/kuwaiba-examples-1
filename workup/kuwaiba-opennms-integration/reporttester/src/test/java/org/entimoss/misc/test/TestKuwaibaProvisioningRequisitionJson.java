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
import org.junit.Test;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestKuwaibaProvisioningRequisitionJson {

   @Test
   public void test() throws StreamWriteException, DatabindException, IOException {

      String ontLabelName = "ONT_200001919492";
      String ontContainerName = "UPRN_200001919492"; // BUILDING

      Double ontContainerLatitude = 50.924450;
      Double ontContainerLongitude = -1.372045;
      String ontIpAddress = "";
      String ontComment = "";
      String ontSerialNumber = "";
      String ontAssetNumber = "";

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

      String oltLabelName = "";
      String oltFexName = "";
      Double oltFexLatitude = 0.0;
      Double oltFexLongitude = 0.0;
      String oltIpAddress = "";
      String oltComment = "";
      String oltSerialNumber = "";
      String oltAssetNumber = "";

      KuwaibaProvisioningRequisition pr;

      KuwaibaGponProvisoner kuwaibaGponProvisoner = new KuwaibaGponProvisoner();

      kuwaibaGponProvisoner.addLineToKuwaibaRequisition(ontLabelName, ontContainerName, ontContainerLatitude, ontContainerLongitude, ontIpAddress,
               ontComment, ontSerialNumber, ontAssetNumber,

               oncLabelName,

               secondarySplitterName, secondarySplitterContainerName, secondarySplitterContainerLatitude, secondarySplitterContainerLongitude,
               secondarySplitterComment, secondarySplitterSerialNumber, secondarySplitterAssetNumber,

               primarySplitterName, primarySplitterContainerName, primarySplitterContainerLatitude, primarySplitterContainerLongitude,
               primarySplitterComment, primarySplitterSerialNumber, primarySplitterAssetNumber,

               oltLabelName, oltFexName, oltFexLatitude, oltFexLongitude, oltIpAddress, oltComment, oltSerialNumber, oltAssetNumber);
      
      kuwaibaGponProvisoner.addTemplatesToProvisioningRequisition();

      pr = kuwaibaGponProvisoner.finalise();

      //      ProvisioningRecord pr = new ProvisioningRecord();
      //      KuwaibaClass kuwaibaClass = new KuwaibaClass();
      //      kuwaibaClass.setClassName("");
      //      HashMap<String, String> attributes = new HashMap<String,String>();
      //      attributes.put("attriba", "attribvalue");
      //      kuwaibaClass.setAttributes(attributes );
      //      kuwaibaClass.setName(null);
      //      kuwaibaClass.setParentName(null);
      //      kuwaibaClass.setTemplateName(null);

      ObjectMapper om = new ObjectMapper();

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

      GponConstants gponConstants = new GponConstants();

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

         ontContainer.setClassName(gponConstants.ONT_CONTAINER_CLASS_NAME);
         ontContainer.setTemplateName(gponConstants.ONT_CONTAINER_TEMPLATE_NAME); // house
         ontContainer.setParentName(gponConstants.PARENT_LOCATION_VALUE); // bitterne park
         ontContainer.setParentClassName(gponConstants.PARENT_LOCATION_CLASS_NAME);
         ontContainer.setName(ontContainerName);

         HashMap<String, String> ontContainerAttributes = new HashMap<String, String>();
         ontContainerAttributes.put("latitude", String.format("%.8f", ontContainerLatitude));
         ontContainerAttributes.put("longitude", String.format("%.8f", ontContainerLongitude));
         ontContainer.getAttributes().putAll(ontContainerAttributes);

         // ont
         KuwaibaClass ont = new KuwaibaClass();
         pr.getKuwaibaClassList().add(ont);

         ont.setClassName(gponConstants.ONT_CLASS_NAME);
         ont.setTemplateName(gponConstants.ONT_TEMPLATE_NAME); // house
         ont.setParentName(ontContainerName);
         ont.setParentClassName(gponConstants.ONT_CONTAINER_CLASS_NAME);
         ont.setName(ontLabelName);
         HashMap<String, String> ontAttributes = new HashMap<String, String>();

         ontAttributes.put("serialNumber", ontSerialNumber);
         ontContainer.getAttributes().putAll(ontAttributes);

         // onc
         KuwaibaClass onc = new KuwaibaClass();
         pr.getKuwaibaClassList().add(onc);

         onc.setClassName(gponConstants.ONC_CLASS_NAME);
         onc.setTemplateName(gponConstants.ONC_TEMPLATE_NAME); // house
         onc.setParentName(ontContainerName);
         onc.setParentClassName(gponConstants.ONT_CONTAINER_CLASS_NAME);
         onc.setName(oncLabelName); // TODO

         // pole secondary splitter container
         KuwaibaClass secondarySplitterContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(secondarySplitterContainer);

         secondarySplitterContainer.setClassName(gponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
         secondarySplitterContainer.setTemplateName(gponConstants.SECONDARY_SPLITTER_CONTAINER_TEMPLATE_NAME); // house
         secondarySplitterContainer.setParentName(gponConstants.PARENT_LOCATION_VALUE); // bitterne park
         secondarySplitterContainer.setParentClassName(gponConstants.PARENT_LOCATION_CLASS_NAME);
         secondarySplitterContainer.setName(secondarySplitterContainerName);

         HashMap<String, String> secondarySplitterContainerAttributes = new HashMap<String, String>();
         secondarySplitterContainerAttributes.put("latitude", String.format("%.8f", secondarySplitterContainerLatitude));
         secondarySplitterContainerAttributes.put("longitude", String.format("%.8f", secondarySplitterContainerLongitude));
         secondarySplitterContainer.getAttributes().putAll(secondarySplitterContainerAttributes);

         // secondarySplitter 
         KuwaibaClass secondarySplitter = new KuwaibaClass();
         pr.getKuwaibaClassList().add(secondarySplitter);

         secondarySplitter.setClassName(gponConstants.SECONDARY_SPLITTER_CLASS_NAME);
         secondarySplitter.setTemplateName(gponConstants.SECONDARY_SPLITTER_TEMPLATE_NAME); // house
         secondarySplitter.setParentName(secondarySplitterContainerName);
         secondarySplitter.setParentClassName(gponConstants.SECONDARY_SPLITTER_CONTAINER_CLASS_NAME);
         secondarySplitter.setName(secondarySplitterName);

         // cab primarySplitter container
         KuwaibaClass primarySplitterContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(primarySplitterContainer);

         primarySplitterContainer.setClassName(gponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
         primarySplitterContainer.setTemplateName(gponConstants.PRIMARY_SPLITTER_CONTAINER_TEMPLATE_NAME); // house
         primarySplitterContainer.setParentName(gponConstants.PARENT_LOCATION_VALUE); // bitterne park
         primarySplitterContainer.setParentClassName(gponConstants.PARENT_LOCATION_CLASS_NAME);
         primarySplitterContainer.setName(primarySplitterContainerName);

         HashMap<String, String> primarySplitterContainerAttributes = new HashMap<String, String>();
         primarySplitterContainerAttributes.put("latitude", String.format("%.8f", primarySplitterContainerLatitude));
         primarySplitterContainerAttributes.put("longitude", String.format("%.8f", primarySplitterContainerLongitude));
         primarySplitterContainer.getAttributes().putAll(primarySplitterContainerAttributes);

         // primarySplitter 
         KuwaibaClass primarySplitter = new KuwaibaClass();
         pr.getKuwaibaClassList().add(primarySplitter);

         primarySplitter.setClassName(gponConstants.PRIMARY_SPLITTER_CLASS_NAME);
         primarySplitter.setTemplateName(gponConstants.PRIMARY_SPLITTER_TEMPLATE_NAME); // house
         primarySplitter.setParentName(primarySplitterContainerName);
         primarySplitter.setParentClassName(gponConstants.PRIMARY_SPLITTER_CONTAINER_CLASS_NAME);
         primarySplitter.setName(primarySplitterName);

         // olt 
         KuwaibaClass olt = new KuwaibaClass();
         pr.getKuwaibaClassList().add(olt);

         olt.setClassName(gponConstants.OLT_CLASS_NAME);
         olt.setTemplateName(gponConstants.OLT_TEMPLATE_NAME); // fex

         olt.setParentName(gponConstants.OLT_CONTAINER_NAME);
         olt.setParentClassName(gponConstants.OLT_CONTAINER_CLASS_NAME);
         olt.setName(oltLabelName);
         HashMap<String, String> oltAttributes = new HashMap<String, String>();

         oltAttributes.put("serialNumber", oltSerialNumber);
         olt.getAttributes().putAll(oltAttributes);

      }
      
      public void addStaticObjectsToProvisioningRequisition() {
         
      }

      public void addTemplatesToProvisioningRequisition() {

         List<KuwaibaTemplateDefinition> kuwaibaTemplateDefinitionList = new ArrayList<KuwaibaTemplateDefinition>();

         // test creating templates from functions
         // block to isolate local variables
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("TestFiberSplitterTemplate_1");
            definition1.setTemplateElementName("splitter");
            definition1.setClassName("FiberSplitter");
            definition1.setSpecial(false);
            definition1.setTemplateFunction("FiberSplitterFunction");

            HashMap<String, String> attributes = new HashMap<String, String>();
            attributes.put("numberOfPorts", "4");
            definition1.setTemplateFunctionAttributes(attributes);

            kuwaibaTemplateDefinitionList.add(definition1);
         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // block to isolate local variables            
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("TestOpticalSpliceBoxTemplate_1");
            definition1.setTemplateElementName("splice");
            definition1.setClassName("SpliceBox");
            definition1.setSpecial(false);
            definition1.setTemplateFunction("OpticalSpliceBoxFunction");

            HashMap<String, String> attributes1 = new HashMap<String, String>();
            attributes1.put("numberOfPorts", "6");
            definition1.setTemplateFunctionAttributes(attributes1);

            kuwaibaTemplateDefinitionList.add(definition1);
         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // block to isolate local variables            
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("ColoredFiberWireContainerTemplate_1");
            definition1.setTemplateElementName("wireContainer");
            definition1.setClassName("WireContainer");
            definition1.setSpecial(false);
            definition1.setTemplateFunction("ColoredFiberWireContainerFunction");

            HashMap<String, String> attributes1 = new HashMap<String, String>();
            attributes1.put("numberOfCables", "4");
            attributes1.put("numberOfFibers", "4");
            definition1.setTemplateFunctionAttributes(attributes1);

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // block to isolate local variables  
         // creating template from definition
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("manualHouseTemplateDefinition1");
            definition1.setTemplateElementName("house1");
            definition1.setClassName("House");
            definition1.setSpecial(false);

            // ONT
            KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
            childDefinition1.setTemplateElementName("test-nokia-ont");
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
            childDefinition2.setTemplateElementName("test-csp");
            childDefinition2.setClassName("SpliceBox");
            childDefinition2.setSpecial(false);
            definition1.getChildKuwaibaTemplateDefinitions().add(childDefinition2);

            KuwaibaTemplateDefinition childDefinition2_1 = new KuwaibaTemplateDefinition();
            childDefinition2_1.setTemplateElementName("IN-01");
            childDefinition2_1.setClassName("OpticalPort");
            childDefinition2_1.setSpecial(false);
            childDefinition2.getChildKuwaibaTemplateDefinitions().add(childDefinition2_1);

            KuwaibaTemplateDefinition childDefinition2_2 = new KuwaibaTemplateDefinition();
            childDefinition2_2.setTemplateElementName("OUT-01");
            childDefinition2_2.setClassName("OpticalPort");
            childDefinition2_2.setSpecial(false);
            childDefinition2.getChildKuwaibaTemplateDefinitions().add(childDefinition2_2);

            KuwaibaTemplateDefinition childDefinition2_3 = new KuwaibaTemplateDefinition();
            childDefinition2_3.setTemplateElementName("IN-02");
            childDefinition2_3.setClassName("OpticalPort");
            childDefinition2_3.setSpecial(false);
            childDefinition2.getChildKuwaibaTemplateDefinitions().add(childDefinition2_3);

            KuwaibaTemplateDefinition childDefinition2_4 = new KuwaibaTemplateDefinition();
            childDefinition2_4.setTemplateElementName("OUT-02");
            childDefinition2_4.setClassName("OpticalPort");
            childDefinition2_4.setSpecial(false);
            childDefinition2.getChildKuwaibaTemplateDefinitions().add(childDefinition2_4);

            kuwaibaTemplateDefinitionList.add(definition1);

         } catch (Exception e) {
            throw new IllegalArgumentException("problem creating definition");
         }

         // block to isolate local variables  
         // creating template from function definitions
         try {
            KuwaibaTemplateDefinition definition1 = new KuwaibaTemplateDefinition();
            definition1.setTemplateName("manualHouseTemplateDefinition2");
            definition1.setTemplateElementName("house1");
            definition1.setClassName("House");
            definition1.setSpecial(false);

            // ONT
            KuwaibaTemplateDefinition childDefinition1 = new KuwaibaTemplateDefinition();
            childDefinition1.setTemplateElementName("test-nokia-ont");
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
            childDefinition2.setTemplateElementName("test-csp");
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
         
         pr.setKuwaibaTemplateList(kuwaibaTemplateDefinitionList);
         
      }

   }

   public static class GponConstants {

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

      public static final String OLT_CONTAINER_TEMPLATE_NAME = "FEX_10";
      public static final String OLT_CONTAINER_CLASS_NAME = "Rack"; // facility
      public static final String OLT_CONTAINER_NAME = "SOTN001"; //    public static final String parentFexName = "SOTN001";

      public static final String OLT_TEMPLATE_NAME = "OLT_NOKIA_01";
      public static final String OLT_CLASS_NAME = "OpticalLineTerminal";

   }

}
