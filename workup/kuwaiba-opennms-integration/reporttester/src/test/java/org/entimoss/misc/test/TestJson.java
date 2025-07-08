package org.entimoss.misc.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.entimoss.kuwaiba.provisioning.KuwaibaClass;
import org.entimoss.kuwaiba.provisioning.KuwaibaProvisioningRequisition;
import org.junit.Test;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJson {

   @Test
   public void test() throws StreamWriteException, DatabindException, IOException {

      
      String ontLabelName = "ONT_200001919492";
      String ontContainerName = "UPRN_200001919492";  // BUILDING

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

         ontContainer.setClassName(gponConstants.getOntContainerClassName());
         ontContainer.setTemplateName(gponConstants.getOntContainerTemplateName()); // house
         ontContainer.setParentName(gponConstants.getParentLocationValue()); // bitterne park
         ontContainer.setParentClassName(gponConstants.getParentLocationClassName());
         ontContainer.setName(ontContainerName);

         HashMap<String, String> ontContainerAttributes = new HashMap<String, String>();
         ontContainerAttributes.put("latitude", String.format("%.8f", ontContainerLatitude));
         ontContainerAttributes.put("longitude", String.format("%.8f", ontContainerLongitude));
         ontContainer.getAttributes().putAll(ontContainerAttributes);

         // ont
         KuwaibaClass ont = new KuwaibaClass();
         pr.getKuwaibaClassList().add(ont);

         ont.setClassName(gponConstants.getOntClassName());
         ont.setTemplateName(gponConstants.getOntTemplateName()); // house
         ont.setParentName(ontContainerName);
         ont.setParentClassName(gponConstants.getOntContainerClassName());
         ont.setName(ontLabelName);
         HashMap<String, String> ontAttributes = new HashMap<String, String>();

         ontAttributes.put("serialNumber", ontSerialNumber);
         ontContainer.getAttributes().putAll(ontAttributes);

         // onc
         KuwaibaClass onc = new KuwaibaClass();
         pr.getKuwaibaClassList().add(onc);

         onc.setClassName(gponConstants.getOncClassName());
         onc.setTemplateName(gponConstants.getOncTemplateName()); // house
         onc.setParentName(ontContainerName);
         onc.setParentClassName(gponConstants.getOntContainerClassName());
         onc.setName(oncLabelName); // TODO

         // pole secondary splitter container
         KuwaibaClass secondarySplitterContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(secondarySplitterContainer);

         secondarySplitterContainer.setClassName(gponConstants.getSecondarySplitterContainerClassName());
         secondarySplitterContainer.setTemplateName(gponConstants.getSecondarySplitterContainerTemplateName()); // house
         secondarySplitterContainer.setParentName(gponConstants.getParentLocationValue()); // bitterne park
         secondarySplitterContainer.setParentClassName(gponConstants.getParentLocationClassName());
         secondarySplitterContainer.setName(secondarySplitterContainerName);

         HashMap<String, String> secondarySplitterContainerAttributes = new HashMap<String, String>();
         secondarySplitterContainerAttributes.put("latitude", String.format("%.8f", secondarySplitterContainerLatitude));
         secondarySplitterContainerAttributes.put("longitude", String.format("%.8f", secondarySplitterContainerLongitude));
         secondarySplitterContainer.getAttributes().putAll(secondarySplitterContainerAttributes);

         // secondarySplitter 
         KuwaibaClass secondarySplitter = new KuwaibaClass();
         pr.getKuwaibaClassList().add(secondarySplitter);

         secondarySplitter.setClassName(gponConstants.getSecondarySplitterClassName());
         secondarySplitter.setTemplateName(gponConstants.getSecondarySplitterTemplateName()); // house
         secondarySplitter.setParentName(secondarySplitterContainerName);
         secondarySplitter.setParentClassName(gponConstants.getSecondarySplitterContainerClassName());
         secondarySplitter.setName(secondarySplitterName);

         // cab primarySplitter container
         KuwaibaClass primarySplitterContainer = new KuwaibaClass();
         pr.getKuwaibaClassList().add(primarySplitterContainer);

         primarySplitterContainer.setClassName(gponConstants.getPrimarySplitterContainerClassName());
         primarySplitterContainer.setTemplateName(gponConstants.getPrimarySplitterContainerTemplateName()); // house
         primarySplitterContainer.setParentName(gponConstants.getParentLocationValue()); // bitterne park
         primarySplitterContainer.setParentClassName(gponConstants.getParentLocationClassName());
         primarySplitterContainer.setName(primarySplitterContainerName);

         HashMap<String, String> primarySplitterContainerAttributes = new HashMap<String, String>();
         primarySplitterContainerAttributes.put("latitude", String.format("%.8f", primarySplitterContainerLatitude));
         primarySplitterContainerAttributes.put("longitude", String.format("%.8f", primarySplitterContainerLongitude));
         primarySplitterContainer.getAttributes().putAll(primarySplitterContainerAttributes);

         // primarySplitter 
         KuwaibaClass primarySplitter = new KuwaibaClass();
         pr.getKuwaibaClassList().add(primarySplitter);

         primarySplitter.setClassName(gponConstants.getPrimarySplitterClassName());
         primarySplitter.setTemplateName(gponConstants.getPrimarySplitterTemplateName()); // house
         primarySplitter.setParentName(primarySplitterContainerName);
         primarySplitter.setParentClassName(gponConstants.getPrimarySplitterContainerClassName());
         primarySplitter.setName(primarySplitterName);

         // olt 
         KuwaibaClass olt = new KuwaibaClass();
         pr.getKuwaibaClassList().add(olt);

         olt.setClassName(gponConstants.getOltClassName());
         olt.setTemplateName(gponConstants.getOltTemplateName()); // fex

         olt.setParentName(gponConstants.getOltContainerName());
         olt.setParentClassName(gponConstants.getOltContainerClassName());
         olt.setName(oltLabelName);
         HashMap<String, String> oltAttributes = new HashMap<String, String>();

         oltAttributes.put("serialNumber", oltSerialNumber);
         olt.getAttributes().putAll(oltAttributes);

      }

   }


   public static class GponConstants {

      private String parentLocationClassName = "Neighborhood";
      private String parentLocationValue = "BitternePk";

      private String ontContainerClassName = "House";
      private String ontContainerTemplateName = "House_01";

      private String ontClassName = "OpticalNetworkTerminal";
      private String ontTemplateName = "ONT_NOKIA_01";

      private String oncClassName = "SpliceBox";
      private String oncTemplateName = "CSP_BFU_1_2_01";

      private String secondarySplitterContainerClassName = "Pole";
      private String secondarySplitterContainerTemplateName = "POLE_2_16drop";

      private String secondarySplitterClassName = "FiberSplitter";
      private String secondarySplitterTemplateName = "SPL16";

      private String primarySplitterContainerTemplateName = "CAB_10SPL8";
      private String primarySplitterContainerClassName = "OutdoorsCabinet";

      private String primarySplitterClassName = "FiberSplitter";
      private String primarySplitterTemplateName = "SPL8";

      private String oltContainerTemplateName = "FEX_10";
      private String oltContainerClassName = "Rack"; // facility
      private String oltContainerName = "SOTN001"; //    private String parentFexName = "SOTN001";

      private String oltTemplateName = "OLT_NOKIA_01";
      private String oltClassName = "OpticalLineTerminal";

      public String getParentLocationClassName() {
         return parentLocationClassName;
      }

      public void setParentLocationClassName(String parentLocationClassName) {
         this.parentLocationClassName = parentLocationClassName;
      }

      public String getParentLocationValue() {
         return parentLocationValue;
      }

      public void setParentLocationValue(String parentLocationValue) {
         this.parentLocationValue = parentLocationValue;
      }

      public String getOntContainerClassName() {
         return ontContainerClassName;
      }

      public void setOntContainerClassName(String ontContainerClassName) {
         this.ontContainerClassName = ontContainerClassName;
      }

      public String getOntContainerTemplateName() {
         return ontContainerTemplateName;
      }

      public void setOntContainerTemplateName(String ontContainerTemplateName) {
         this.ontContainerTemplateName = ontContainerTemplateName;
      }

      public String getOntClassName() {
         return ontClassName;
      }

      public void setOntClassName(String ontClassName) {
         this.ontClassName = ontClassName;
      }

      public String getOntTemplateName() {
         return ontTemplateName;
      }

      public void setOntTemplateName(String ontTemplateName) {
         this.ontTemplateName = ontTemplateName;
      }

      public String getOncClassName() {
         return oncClassName;
      }

      public void setOncClassName(String oncClassName) {
         this.oncClassName = oncClassName;
      }

      public String getOncTemplateName() {
         return oncTemplateName;
      }

      public void setOncTemplateName(String oncTemplateName) {
         this.oncTemplateName = oncTemplateName;
      }

      public String getSecondarySplitterContainerClassName() {
         return secondarySplitterContainerClassName;
      }

      public void setSecondarySplitterContainerClassName(String secondarySplitterContainerClassName) {
         this.secondarySplitterContainerClassName = secondarySplitterContainerClassName;
      }

      public String getSecondarySplitterContainerTemplateName() {
         return secondarySplitterContainerTemplateName;
      }

      public void setSecondarySplitterContainerTemplateName(String secondarySplitterContainerTemplateName) {
         this.secondarySplitterContainerTemplateName = secondarySplitterContainerTemplateName;
      }

      public String getSecondarySplitterClassName() {
         return secondarySplitterClassName;
      }

      public void setSecondarySplitterClassName(String secondarySplitterClassName) {
         this.secondarySplitterClassName = secondarySplitterClassName;
      }

      public String getSecondarySplitterTemplateName() {
         return secondarySplitterTemplateName;
      }

      public void setSecondarySplitterTemplateName(String secondarySplitterTemplateName) {
         this.secondarySplitterTemplateName = secondarySplitterTemplateName;
      }

      public String getPrimarySplitterContainerTemplateName() {
         return primarySplitterContainerTemplateName;
      }

      public void setPrimarySplitterContainerTemplateName(String primarySplitterContainerTemplateName) {
         this.primarySplitterContainerTemplateName = primarySplitterContainerTemplateName;
      }

      public String getPrimarySplitterContainerClassName() {
         return primarySplitterContainerClassName;
      }

      public void setPrimarySplitterContainerClassName(String primarySplitterContainerClassName) {
         this.primarySplitterContainerClassName = primarySplitterContainerClassName;
      }

      public String getPrimarySplitterClassName() {
         return primarySplitterClassName;
      }

      public void setPrimarySplitterClassName(String primarySplitterClassName) {
         this.primarySplitterClassName = primarySplitterClassName;
      }

      public String getPrimarySplitterTemplateName() {
         return primarySplitterTemplateName;
      }

      public void setPrimarySplitterTemplateName(String primarySplitterTemplateName) {
         this.primarySplitterTemplateName = primarySplitterTemplateName;
      }

      public String getOltContainerTemplateName() {
         return oltContainerTemplateName;
      }

      public void setOltContainerTemplateName(String oltContainerTemplateName) {
         this.oltContainerTemplateName = oltContainerTemplateName;
      }

      public String getOltContainerClassName() {
         return oltContainerClassName;
      }

      public void setOltContainerClassName(String oltContainerClassName) {
         this.oltContainerClassName = oltContainerClassName;
      }

      public String getOltContainerName() {
         return oltContainerName;
      }

      public void setOltContainerName(String oltContainerName) {
         this.oltContainerName = oltContainerName;
      }

      public String getOltTemplateName() {
         return oltTemplateName;
      }

      public void setOltTemplateName(String oltTemplateName) {
         this.oltTemplateName = oltTemplateName;
      }

      public String getOltClassName() {
         return oltClassName;
      }

      public void setOltClassName(String oltClassName) {
         this.oltClassName = oltClassName;
      }

   }


}
