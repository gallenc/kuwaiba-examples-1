/**
 * Exports inventory as OpenNMS PRIS CSV format.
 * Entimoss Ltd - version 0.1
 * Parameters: None
 * Applies to: TBD
 */

//package org.entimoss.kuwaiba; // package omitted from groovy
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.modules.optional.reports.defaults.RawReport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

// public class OpenNMSExport2 { // class omitted from groovy

//InventoryReport returnReport() { // function omitted from groovy

// main report function

String title = "OpenNMSExport";
String version = "0.1";
String author = "Craig Gallen";

// create CSV headerline
StringBuffer textBuffer = new StringBuffer();

Iterator<String> columnIterator = OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.iterator();
while (columnIterator.hasNext()) {
   String columnName = columnIterator.next();
   textBuffer.append(columnName);
   if (columnIterator.hasNext()) {
      textBuffer.append(",");
   }
}

textBuffer.append("\n");

// now populate data lines
// BusinessEntityManager bem = null; //remove

ArrayList<HashMap<String,String>> lineData = generateLineData(bem);

for(HashMap<String, String> singlelineData: lineData) {
   // create and populate empty CSV line
   List<String> requisitionLine = new ArrayList<>(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size());
   for (int i = 0; i < OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size(); i++)
      requisitionLine.add("");
   // populate values if they exist
   for(String key : OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS) {
      if (singlelineData.containsKey(key)) {
         requisitionLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(key), singlelineData.get(key));
      }
   }
   // write out csv line with commas
   Iterator<String> requisitionLineIterator = requisitionLine.iterator();
   while (requisitionLineIterator.hasNext()) {
      String columnValue = requisitionLineIterator.next();
      textBuffer.append(columnValue);
      if (requisitionLineIterator.hasNext()) {
         textBuffer.append(",");
      }
   }
   textBuffer.append("\n");
}

// return a RawReport containing csv
InventoryReport report = new RawReport(title, author, version, textBuffer.toString());
 
return report;
 
// end of main report function

// }  // function omitted from groovy

public ArrayList<HashMap<String,String>>  generateLineData(BusinessEntityManager bem) {
   
      ArrayList<HashMap<String, String>> lineData = new ArrayList<HashMap<String, String>>();
      List<BusinessObject> devices;

      try {
         
         // first we get all ip addresses, folders and subnet names from ipam

         // find ipv4 root pools - currently only one root but could be more
         List<InventoryObjectPool> ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
         HashMap<String,ArrayList<String>> folderAddresses = new HashMap<String,ArrayList<String>>();
         
         HashMap<String,String> addresslookup = new HashMap<String, String>();
         
         
         poolLookup(ipv4RootPools, bem, Constants.CLASS_SUBNET_IPV4, folderAddresses);
         printFolderAddresses(folderAddresses);
         
         for (String folderName : folderAddresses.keySet()) {
            ArrayList<String> addresses = folderAddresses.get(folderName);
            for (String address : addresses) {
               addresslookup.put(address, folderName);
            }
         }
         System.out.println("************************* addresslookup" + addresslookup.size()+ " "+ addresslookup);



         // Next we get all active network devices
         devices = bem.getObjectsOfClass("GenericCommunicationsElement", -1);

         for (BusinessObject device : devices) {

            String name = device.getName();

            // then we get coms posts on devices
            List<BusinessObjectLight> commPorts = bem.getChildrenOfClassLightRecursive(device.getId(), device.getClassName(), "GenericCommunicationsPort", null, -1, -1);

            for (BusinessObjectLight aPort : commPorts) {

               String portStatus = bem.getAttributeValueAsString(aPort.getClassName(), aPort.getId(), "state");

               // We check if there's an IP address associated to the interface.
               List<BusinessObjectLight> ipAddressesInPort = bem.getSpecialAttribute(aPort.getClassName(), aPort.getId(), "ipamHasIpAddress");

               Iterator<BusinessObjectLight> ipAddressesInPortIterator = ipAddressesInPort.iterator();
               while (ipAddressesInPortIterator.hasNext()) {
                  BusinessObjectLight ipAddress = ipAddressesInPortIterator.next();

                  // need to know subnet of ip address to get location
                  // (taken from IpanService existsInInventory()

                  List<BusinessObjectLight> ipaddressfound = bem.getObjectsByNameAndClassName(new ArrayList<>(Arrays.asList(ipAddress.getName())), -1, -1, Constants.CLASS_IP_ADDRESS);
                  System.out.println("IPADDRESS NAME " + ipAddress.getName() + " ipaddressfound " + ipaddressfound);

                  // need to know if port is management port to set N/P

                  HashMap<String, String> line = new HashMap();
                  line.put(OnmsRequisitionConstants.NODE_LABEL, name);
                  line.put(OnmsRequisitionConstants.ID_, name);
                  line.put(OnmsRequisitionConstants.IP_MANAGEMENT, ipAddress.getName());
                  
                  if (addresslookup.containsKey(ipAddress.getName())) {
                     line.put(OnmsRequisitionConstants.MINION_LOCATION,addresslookup.get(ipAddress.getName()));
                  } else {
                     line.put(OnmsRequisitionConstants.MINION_LOCATION, OnmsRequisitionConstants.DEFAULT_MINION_LOCATION);
                  }
                  lineData.add(line);
               }
            }

         }
      } catch (MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException e) {
         throw new RuntimeException(e);
      }

      return lineData;
   }

  void printFolderAddresses(HashMap<String,ArrayList<String>> folderAddresses){
      for (String folderName : folderAddresses.keySet()) {
         ArrayList<String> addresses = folderAddresses.get(folderName);
         for (String address : addresses) {
            System.out.println("Folder: '"+folderName+"' Address: '"+address+"'");
         }
      }
   }

   void poolLookup(List<InventoryObjectPool> topFolderPoolList, BusinessEntityManager bem, String ipType, HashMap<String,ArrayList<String>> folderAddresses )
            throws InvalidArgumentException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {

      for (InventoryObjectPool topFolderPool : topFolderPoolList) {
         System.out.println("topFolderPool " + topFolderPool.getName() + "  " + topFolderPool.getId());

         // Look up subnets
         List<BusinessObjectLight> subnetsInfolder = bem.getPoolItemsByClassName(topFolderPool.getId(), ipType, 0, 50);
         
         ArrayList<String> addresses = new ArrayList<String>();
        folderAddresses.put(topFolderPool.getName().strip().replaceAll(" ", "_"), addresses);
         
         subnetLookup(subnetsInfolder, bem, ipType, addresses );

         // Look up Individual ip addresses in folder
         List<BusinessObjectLight> ipaddressesInFolder = bem.getPoolItemsByClassName(topFolderPool.getId(), Constants.CLASS_IP_ADDRESS, 0, 50);
         System.out.println("individual ipaddressesInFolder " + ipaddressesInFolder);
         
         for(BusinessObjectLight ip : ipaddressesInFolder) {
            addresses.add(ip.getName());
         }

         List<InventoryObjectPool> foldersInPool = bem.getPoolsInPool(topFolderPool.getId(), Constants.CLASS_GENERICADDRESS);

         // recurse through sub folders
         poolLookup(foldersInPool, bem, ipType, folderAddresses);

      }

   }

   void subnetLookup(List<BusinessObjectLight> subnetsList, BusinessEntityManager bem, String ipType, ArrayList<String> addresses ) throws ApplicationObjectNotFoundException,
            InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {

      System.out.println("subnetLookup subnetsList " + subnetsList);

      for (BusinessObjectLight subnet : subnetsList) {

         List<BusinessObjectLight> children = bem.getObjectSpecialChildrenWithFilters(ipType, subnet.getId(), new ArrayList<>(Arrays.asList(ipType)), 0, 50);
         List<BusinessObjectLight> subnets = new ArrayList<>();
         for (BusinessObjectLight child : children) {
            if (child.getClassName().equals(Constants.CLASS_SUBNET_IPV4) ||
                     child.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
               subnets.add(child);
         }
         
         // recursively look up subnets
         subnetLookup(subnets,  bem, ipType, addresses);
         
         //addresses in subnets

         List<BusinessObjectLight> usedIpsInSubnet = bem.getObjectSpecialChildrenWithFilters(ipType, subnet.getId(), new ArrayList<>(Arrays.asList(Constants.CLASS_IP_ADDRESS)), 0, 50);
         for(BusinessObjectLight ip : usedIpsInSubnet) {
            addresses.add(ip.getName());
         }

         
         System.out.println("ip addresses in subnet " + subnet.getName() + " " + usedIpsInSubnet);
      }

   }

   class OnmsRequisitionConstants {

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
   
   // this is same order as in csv header line
   public static final List<String> OPENNMS_REQUISITION_HEADERS = Arrays.asList(NODE_LABEL, ID_, MINION_LOCATION, PARENT_FOREIGN_ID, PARENT_FOREIGN_SOURCE, IP_MANAGEMENT,
            MGMTTYPE_, SVC_FORCED, CAT_, ASSET_CATEGORY ,ASSET_REGION, ASSET_SERIALNUMBER, ASSET_ASSETNUMBER, ASSET_LATITUDE, ASSET_LONGITUDE, ASSET_THRESHOLDCATEGORY,
            ASSET_NOTIFYCATEGORY, ASSET_POLLERCATEGORY, ASSET_DISPLAYCATEGORY, ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE, ASSET_CIRCUITID,
            ASSET_DESCRIPTION);
   
   public static final String DEFAULT_MINION_LOCATION="Default"; // used when OpenNMS core is the poller.
   

}