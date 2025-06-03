/**
 * Exports inventory of nodes and interface IP addresses as OpenNMS PRIS CSV format.
 * (see https://docs.opennms.com/pris/latest/ )
 * Entimoss Ltd - version 0.7
 * Parameters:
 *    useNodeLabelAsForeignId
 *    If blank or false, report uses the kuwaiba object id of the device as the node foreignId in the requisition (default)
 *    If true the report uses the generated object label as node foreignId in the requisition.
 *
 *    useAbsoluteNames
 *    If blank or false, the report uses parent location and rack to generate each node name.
 *    if true it uses only the name of the node given in the model
 *
 *    useAllPortAddresses
 *    If blank or false, the report only uses ports designated as isManagement. (default)
 *    If true it uses all port addresses assigned to a device and designates the interface snmp-primary P (primary snmp) if isManagment is true
 *    or N (Not managed) if isManagment is false
 *
 *    defaultAssetCategory
 *    AssetCategory is populated from device EquipmentModel displayName
 *    if the displayName is not set then the AssetCategory is set to the defaultAssetCategory or blank if the defaultAssetCategory is not set
 *    (this can be used in grafana to determine which display template to use)
 *
 *    defaultAssetDisplayCategory
 *    AssetDisplayCategory is populated from the customer name associated with a service attached to a device or a parent rack in the model.
 *    if AssetDisplayCategory is not populated from the model, a default value can be used.
 *    AssetDisplayCategory is set to the defaultAssetDisplayCategory or blank if the defaultAssetDisplauCategory is not set
 *    (this can be used in OpenNMS to determine which users can view an object)
 *
 *    subnetNetSubstitutionFilter
 *    Substitutes the network portion of the inputIpv4Address for the network portion of the substitute address
 *    if the address being filtered is within the within subnet range.
 *    If null or empty, then the address is passed through unchanged.
 *    For example:
 *                                        <within subnet>=<substitute subnet>
 *       String subnetNetSubstitutionStr = "172.16.0.0/22=192.168.105.0/24"
 *       if the input inputIpv4AddressStr = "172.16.105.20"
 *       the substitute is  substituteAddressStr= "192.168.105.20
 * 
 *    rangeParentValue
 *    rangeParentValue is used to find the parent visible object of the devices to include in the device list.
 *    If a device has this parent somewhere in their parent object tree, the device will be a candidate to be included in the requisition for OpenNMS.
 *    The rangeParentValue can be the name property of the object or the kuwaiba objectID of the object.
 *    If the rangeParentValue is not set or is empty, all devices will be included in the tree.
 *    If the rangeParentValue is not found, an exception will be thrown and the report will not complete.
 * 
 * Applies to: TBD
 *
 * Notes - todo
 * LOG.warn should be LOG.debug if debugging is enabled
 * 
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

//uncomment in groovy script
OpenNMSExport07 opennmsExport = new OpenNMSExport07(bem, aem, parameters);
return opennmsExport.returnReport();

public class OpenNMSExport07 {
   static Logger LOG =  LoggerFactory.getLogger("OpenNMSInventoryExport");  // remove static in groovy
   
   BusinessEntityManager    bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   Map<String, String> parameters = new HashMap<>();
   
   IPLocationDAO ipLocationDAO = null;

   String title = "OpenNMSInventoryExport";
   String version = "0.7";
   String author = "Craig Gallen";
   
   public OpenNMSExport07() {
   };

   public OpenNMSExport07(BusinessEntityManager bem, ApplicationEntityManager aem, Map<String, String> parameters) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.parameters = parameters;

      // first we get all ip addresses, folders and subnets names from ipam
      ipLocationDAO = new IPLocationDAO(bem);
      try {
         ipLocationDAO.init();
      } catch (Exception ex) {
         throw new RuntimeException("problem initialising ipLocationDao ",ex);
      }
   }

   // main report function
   InventoryReport returnReport() {
      LOG.info("Start of "+title+" Version "+version+" Author "+author);

      LOG.info("opennms export report parameters :");
      for(Entry<String, String> entry : parameters.entrySet()){
         LOG.info("   key: "+entry.getKey()+" value: "+entry.getValue());
      }
      
      /*
       * useNodeLabelAsForeignId
       * If blank or false, report uses the kuwaiba object id of the device as the node foreignId in the requisition (default)
       * If true the report uses the generated object label as node foreignId in the requisition.
       */
      Boolean useNodeLabelAsForeignId = Boolean.valueOf(parameters.getOrDefault("useNodeLabelAsForeignId", "false"));
      
      /*
       * useAbsoluteNames
       * If blank or false, the report uses parent location and rack to generate each node name. 
       * if true it uses only the name of the node given in the model
       */
      Boolean useAbsoluteNames = Boolean.valueOf(parameters.getOrDefault("useAbsoluteNames", "false"));
      
      /*
       * useAllPortAddresses
       * If blank or false, the report only uses ports designated as isManagement. (default)
       * If true it uses all port addresses assigned to a device and designates the interface snmp-primary P (primary snmp) if isManagment is true
       * or N (Not managed) if isManagment is false
       */
      Boolean useAllPortAddresses = Boolean.valueOf(parameters.getOrDefault("useAllPortAddresses", "false"));
      
      /*
       * defaultAssetCategory
       * AssetCategory is populated from device EquipmentModel displayName
       * if the displayName is not set then the AssetCategory is set to the defaultAssetCategory or blank if the defaultAssetCategory is not set
       * (this can be used in grafana to determine which display template to use)
       */
      String defaultAssetCategory= parameters.getOrDefault("defaultAssetCategory", "");

      /*
       * defaultAssetDisplayCategory
       * AssetDisplayCategory is currently not populated from the model
       * AssetDisplayCategory is set to the defaultAssetDisplayCategory or blank if the defaultAssetDisplauCategory is not set
       * (this can be used in OpenNMS to determine which users can view an object)
       */
      String defaultAssetDisplayCategory= parameters.getOrDefault("defaultAssetDisplayCategory", "");
      
      /*
       * subnetNetSubstitutionFilterStr
       * substitutes the network portion of the inputIpv4Address for the network portion of the substitute address
       * For example:
       *                                   <within subnet>=<substitute subnet>
       *  String subnetNetSubstitutionStr = "172.16.0.0/22=192.168.105.0/24";
       *  String inputIpv4AddressStr = "172.16.105.20";
       *  String substituteAddressStr= "192.168.105.20
       */ 
      String subnetNetSubstitutionFilter= parameters.getOrDefault("subnetNetSubstitutionFilter", "");

      /*
       * rangeParentValue
       * The rangeParentValue can be the name property of the object or the kuwaiba objectID of the object.
       * If the rangeParentValue is not set, all devices will be included in the tree.
       * If the rangeParentValue is not found, an exception will be thrown and the report will not complete
       * Finds the parent visable object of the devices to include in the device list. 
       * If a device has this parent somewhere in their parent object tree, the device will be a candidate to be included in the requisition for OpenNMS.
       */
      String rangeParentValue = parameters.getOrDefault("rangeParentValue", "");

      StringBuffer textBuffer = new StringBuffer();

      // create CSV headerline
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
      ArrayList<HashMap<String, String>> csvLineData = generateCsvLineData(bem, aem, useAbsoluteNames, useAllPortAddresses, useNodeLabelAsForeignId, defaultAssetCategory,
               defaultAssetDisplayCategory, subnetNetSubstitutionFilter, rangeParentValue);

      for (HashMap<String, String> singleCsvlineData : csvLineData) {

         // create and populate empty CSV line
         List<String> requisitionLine = new ArrayList<>(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size());
         for (int i = 0; i < OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.size(); i++)
            requisitionLine.add("");

         // populate csv values in line if they exist
         for (String key : OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS) {
            if (singleCsvlineData.containsKey(key)) {
               requisitionLine.set(OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.indexOf(key), singleCsvlineData.get(key));
            }
         }
         // write out each csv line to text buffer with commas
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

      LOG.info("End of "+title);
 
      return report;

   }

   public ArrayList<HashMap<String, String>> generateCsvLineData(BusinessEntityManager bem, ApplicationEntityManager aem,
            Boolean useAbsoluteNames, Boolean useAllPortAddresses, Boolean useNodeLabelAsForeignId, String defaultAssetCategory, String defaultAssetDisplayCategory,
            String subnetNetSubstitutionFilter, String rangeParentValue) {
      
      ArrayList<HashMap<String, String>> csvLineData = new ArrayList<HashMap<String, String>>();
      List<BusinessObject> devices;

      try {

         // tries to find parent object of all devices to include in range
         // parent must be a viewable object which can have a generic communications element as a child 
         // range parent value can be an absolute object id or an object name
         BusinessObjectLight rangeParent = null;
         String rangeParentClassName = null;
         String rangeParentId = null;
         
         String searchErrorMsg=null;
         if (rangeParentValue != null && !rangeParentValue.isEmpty()) {
            try {
               // see if there is an object with the same name
               List<BusinessObjectLight> rangeParents = bem.getObjectsWithFilterLight(Constants.CLASS_VIEWABLEOBJECT, Constants.PROPERTY_NAME, rangeParentValue);
               if (!rangeParents.isEmpty())
                  rangeParent = rangeParents.get(0);
            } catch (Exception ex) {
               searchErrorMsg=ex.getMessage();
            }
            if (rangeParent == null)
               // else see if there is an object with the object id = viewable object
               try {
                  // see if there is an object with the same id
                  List<BusinessObjectLight> rangeParents = bem.getObjectsWithFilterLight(Constants.CLASS_VIEWABLEOBJECT, Constants.PROPERTY_UUID, rangeParentValue);
                  if (!rangeParents.isEmpty())
                     rangeParent = rangeParents.get(0);
               } catch (Exception ex) {
                   searchErrorMsg=ex.getMessage();
               }
            if(rangeParent ==null) {
               throw new IllegalArgumentException("cannot find parent wiewable object with rangeParentValue="+rangeParentValue+ " search error:"+searchErrorMsg );
            }
            rangeParentClassName = rangeParent.getClassName();
            rangeParentId = rangeParent.getId();
            LOG.warn("finding devices with parent object rangeParentClassName="+rangeParentClassName + " rangeParentId="+rangeParentId +" "+rangeParent);
         }

         // Next we get all active network devices
         devices = bem.getObjectsOfClass(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, -1);

         for (BusinessObject device : devices) {

            String name = device.getName().strip().replace(" ", "_");
            String deviceId = device.getId();

            String latitude = "";
            String longitude = "";
            String locationName="";
            String rackName="";
            String deviceEquipmentDisplayName="";
            String customerName = "NOT_ASSIGNED";
            String customerId="";
            String serviceName = "NOT_ASSIGNED";
            String serviceId="";

            try {
               
               LOG.warn("************ processing device with attributes :"+device.getAttributes());

               // if rangeParent is set do not proceed if device is not a child of rangeParent

               // TODO this is correct method but doesn't work because transaction is not closed in BusinessEntityManagerImpl.isParent (no txSuccess())
               //if (rangeParentId != null) {
               //   if (! bem.isParent(rangeParentClassName, rangeParentId, device.getClassName(), device.getId())) {
               //      break;
               //   }
               //}
               // TODO work around
               if (rangeParentId != null) {
                  List<BusinessObjectLight> parents = bem.getParents(device.getClassName(), device.getId());
                  
                  LOG.warn("parents of device name: "+device.getClassName()+" Id: "+ device.getId()+" : "+ parents);
                  
                  boolean isParent=false;
                  for(BusinessObjectLight parent : parents) {
                     if (parent.getId().equals(rangeParentId)) {
                        isParent=true;
                        break;
                     }
                  }
                  // if parent doesn't match process next device
                  if(! isParent) {
                     continue;
                  }
               }

               
               String equipmentModelId = (String) device.getAttributes().get(Constants.ATTRIBUTE_MODEL);
               if(equipmentModelId!=null) {
                   BusinessObject equipmentModel = aem.getListTypeItem(Constants.CLASS_EQUIPMENTMODEL, equipmentModelId);
                   deviceEquipmentDisplayName = (String) equipmentModel.getAttributes().get(Constants.PROPERTY_DISPLAY_NAME);
               }
               
               // get the first parent location of each device for latitude/longitude
               BusinessObject location = bem.getFirstParentOfClass(device.getClassName(), device.getId(), "GenericLocation");
               if (location!=null && location.getName()!=null) { 
                  locationName = location.getName().strip().replace(" ", "_");
                  latitude = bem.getAttributeValueAsString(location.getClassName(), location.getId(), "latitude");
                  longitude = bem.getAttributeValueAsString(location.getClassName(), location.getId(), "longitude");
               }
               
               // get any service / customer associated with the device
               List<BusinessObjectLight> associatedServices = bem.getSpecialAttribute(device.getClassName(), device.getId(), "uses");
               if (associatedServices != null) {
                  // only take first associated service even if there are more
                  for (BusinessObjectLight associatedSvc : associatedServices) {
                     serviceName = associatedSvc.getName();
                     serviceId= associatedSvc.getId();
                     BusinessObject customer = bem.getFirstParentOfClass(associatedSvc.getClassName(), associatedSvc.getId(), "GenericCustomer");
                     if (customer != null) {
                        customerName = customer.getName();
                        customerId=customer.getId();
                        break;
                     }
                  }
               }

               // get the first rack containing each device for rackName
               BusinessObject rack = bem.getFirstParentOfClass(device.getClassName(), device.getId(), "Rack");
               if(rack!=null && rack.getName()!=null) {
                  rackName = rack.getName().strip().replace(" ", "_");

                  // if there is no service with customer name associated with the device then try to use the service associated with the parent rack
                  if ("NOT_ASSIGNED".equals(serviceName)) {
                     // get any service / customer associated with the parent rack
                     associatedServices = bem.getSpecialAttribute(rack.getClassName(), rack.getId(), "uses");
                     if (associatedServices != null) {
                        // only take first associated service even if there are more
                        for (BusinessObjectLight associatedSvc : associatedServices) {
                           serviceName = associatedSvc.getName();
                           serviceId= associatedSvc.getId();
                           BusinessObject customer = bem.getFirstParentOfClass(associatedSvc.getClassName(), associatedSvc.getId(), "GenericCustomer");
                           if (customer != null) {
                              customerName = customer.getName();
                              customerId=customer.getId();
                              break;
                           }
                        }
                        LOG.warn("assocaitedService assigned from rack "+rack.getName()+" serviceName:"+serviceName+ " serviceId:"+serviceId+ " customerName: "+customerName+ 
                                 " customerId "+customerId);
                     }
                  }
               }
               LOG.warn("assocaitedService serviceName:"+serviceName+ " serviceId:"+serviceId+ " customerName: "+customerName+ " customerId "+customerId);
               
            } catch (Exception ex) {
               ex.printStackTrace();
            }

            // then we get comms ports (interfaces) on each device
            List<BusinessObjectLight> commPorts = bem.getChildrenOfClassLightRecursive(device.getId(), device.getClassName(), "GenericCommunicationsPort", null, -1, -1);

            for (BusinessObjectLight aPort : commPorts) {

               // TODO not used
               String portStatus = bem.getAttributeValueAsString(aPort.getClassName(), aPort.getId(), "state");
               
               String isManagementStr = bem.getAttributeValueAsString(aPort.getClassName(), aPort.getId(), "isManagement");
               boolean isManagement =  Boolean.valueOf(isManagementStr);

               // We check if there's an IP address associated to the port.
               List<BusinessObjectLight> ipAddressesInPort = bem.getSpecialAttribute(aPort.getClassName(), aPort.getId(), "ipamHasIpAddress");

               Iterator<BusinessObjectLight> ipAddressesInPortIterator = ipAddressesInPort.iterator();
               while (ipAddressesInPortIterator.hasNext()) {
                  BusinessObjectLight ipAddress = ipAddressesInPortIterator.next();

                  // need to know the subnet of the ip address to get the location
                  List<BusinessObjectLight> ipaddressfound = bem.getObjectsByNameAndClassName(new ArrayList<>(Arrays.asList(ipAddress.getName())), -1, -1, Constants.CLASS_IP_ADDRESS);
                  LOG.warn("IPADDRESS NAME " + ipAddress.getName() + " ipaddressfound " + ipaddressfound);

                  HashMap<String, String> line = new HashMap<String, String>();
                  
                  // use node name derived from containment hierarchy OR use the given node name
                  String nodename = locationName+"_"+rackName+"_"+name;
                  if(useAbsoluteNames){
                     nodename=name;
                  } 
                  line.put(OnmsRequisitionConstants.NODE_LABEL, nodename);
                  
                  // sets the foreignId 
                  if (useNodeLabelAsForeignId) {
                     line.put(OnmsRequisitionConstants.ID_, nodename);
                  } else {
                     line.put(OnmsRequisitionConstants.ID_, deviceId);
                  }
                  
                  // sets asset category which determines which panel is displayed in grafana
                  if(deviceEquipmentDisplayName==null || deviceEquipmentDisplayName.isEmpty()) {
                     line.put(OnmsRequisitionConstants.ASSET_CATEGORY, defaultAssetCategory);
                  } else {
                     line.put(OnmsRequisitionConstants.ASSET_CATEGORY, deviceEquipmentDisplayName);
                  }
                  
                  // sets display category which indicates customer
                  String cName= "NOT_ASSIGNED".equals(customerName) ? defaultAssetDisplayCategory : customerName ;
                  cName= cName.replace(" ","_");
                  line.put(OnmsRequisitionConstants.ASSET_DISPLAYCATEGORY, cName  );
                  
                  line.put(OnmsRequisitionConstants.METADATA_CUSTOMER_ID, customerId.replace(" ","_"));
                  line.put(OnmsRequisitionConstants.METADATA_CUSTOMER_NAME, customerName.replace(" ","_"));
                  line.put(OnmsRequisitionConstants.METADATA_SERVICE_ID, serviceId.replace(" ","_"));
                  line.put(OnmsRequisitionConstants.METADATA_SERVICE_NAME, serviceName.replace(" ","_"));

                  // sets the management address of the device
                  // this can be a derived address to emulate multiple address spaces
                  String ipManagement = ipAddress.getName();
                  if (subnetNetSubstitutionFilter != null && ! subnetNetSubstitutionFilter.isEmpty()) {
                     ipManagement = IpV4Cidr.subnetIpv4Substitution(subnetNetSubstitutionFilter, ipAddress.getName());
                  }
                  line.put(OnmsRequisitionConstants.IP_MANAGEMENT, ipManagement);

                  // if port set as isManagement then set as Primary (P) snmp interface else (N) - not management
                  line.put(OnmsRequisitionConstants.MGMTTYPE_, (String) (isManagement ? "P" : "N"));

                  if (latitude != null && !latitude.isEmpty()) {
                     line.put(OnmsRequisitionConstants.ASSET_LATITUDE, latitude);
                  }
                  if (longitude != null && !longitude.isEmpty()) {
                     line.put(OnmsRequisitionConstants.ASSET_LONGITUDE, longitude);
                  }

                  // set the location of the minion monitoring this interface based on the 'folder' containing this address

                  String location = ipLocationDAO.getLocationForIpAddress(ipAddress.getName());

                  if (location != null) {
                     line.put(OnmsRequisitionConstants.MINION_LOCATION, location);
                  } else {
                     line.put(OnmsRequisitionConstants.MINION_LOCATION, OnmsRequisitionConstants.DEFAULT_MINION_LOCATION);
                  }
                  
                  // only create a line if useAllPortAddresses is true or if isManagement is true for the port
                  if (useAllPortAddresses) {
                      csvLineData.add(line);
                  } else if (isManagement) {
                     csvLineData.add(line);
                  }
               }
            }

         }
      } catch (MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException e) {
         throw new RuntimeException(e);
      }

      return csvLineData;
   }

   /**
    * class which looks up which minion location ip addresses are in are in based on the parent folder name
    */
   public class IPLocationDAO {
      Logger LOG =  LoggerFactory.getLogger("OpenNMSInventoryExport"); // needed for groovy

      BusinessEntityManager bem;

      HashMap<String, String> addresslookup = new HashMap<String, String>();

      IPLocationDAO(BusinessEntityManager bem) {
         this.bem = bem;
      }

      public void init() throws InvalidArgumentException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {

         // first we get all ip addresses, folders and subnets names from ipam

         // find ipv4 root pools - currently only one root but could be more
         List<InventoryObjectPool> ipv4RootPools;

         ipv4RootPools = bem.getRootPools(Constants.CLASS_SUBNET_IPV4, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);

         HashMap<String, ArrayList<String>> folderAddresses = new HashMap<String, ArrayList<String>>();

         poolLookup(ipv4RootPools, bem, Constants.CLASS_SUBNET_IPV4, folderAddresses);
         printFolderAddresses(folderAddresses);

      for (String folderName : folderAddresses.keySet()) {
         ArrayList<String> addresses = folderAddresses.get(folderName);
         for (String address : addresses) {
               addresslookup.put(address, folderName);
         }
      }
         LOG.warn("************************* addresslookup size " + addresslookup.size() + " " + addresslookup);

   }

   void poolLookup(List<InventoryObjectPool> topFolderPoolList, BusinessEntityManager bem, String ipType, HashMap<String, ArrayList<String>> folderAddresses)
            throws InvalidArgumentException, ApplicationObjectNotFoundException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {

      for (InventoryObjectPool topFolderPool : topFolderPoolList) {
         LOG.warn("topFolderPool " + topFolderPool.getName() + "  " + topFolderPool.getId());

         // Look up subnets
         List<BusinessObjectLight> subnetsInfolder = bem.getPoolItemsByClassName(topFolderPool.getId(), ipType, 0, 50);

         ArrayList<String> addresses = new ArrayList<String>();
         folderAddresses.put(topFolderPool.getName().strip().replace(" ", "_"), addresses);

         subnetLookup(subnetsInfolder, bem, ipType, addresses);

         // Look up Individual ip addresses in folder
         List<BusinessObjectLight> ipaddressesInFolder = bem.getPoolItemsByClassName(topFolderPool.getId(), Constants.CLASS_IP_ADDRESS, 0, 50);
         LOG.warn("individual ipaddressesInFolder " + ipaddressesInFolder);

         for (BusinessObjectLight ip : ipaddressesInFolder) {
            addresses.add(ip.getName());
         }

         List<InventoryObjectPool> foldersInPool = bem.getPoolsInPool(topFolderPool.getId(), Constants.CLASS_GENERICADDRESS);

         // recurse through sub folders
         poolLookup(foldersInPool, bem, ipType, folderAddresses);

      }

   }

   void subnetLookup(List<BusinessObjectLight> subnetsList, BusinessEntityManager bem, String ipType, ArrayList<String> addresses) throws ApplicationObjectNotFoundException,
            InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {

      LOG.warn("subnetLookup subnetsList " + subnetsList);

      for (BusinessObjectLight subnet : subnetsList) {

         List<BusinessObjectLight> children = bem.getObjectSpecialChildrenWithFilters(ipType, subnet.getId(), new ArrayList<>(Arrays.asList(ipType)), 0, 50);
         List<BusinessObjectLight> subnets = new ArrayList<>();
         for (BusinessObjectLight child : children) {
            if (child.getClassName().equals(Constants.CLASS_SUBNET_IPV4) ||
                     child.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
               subnets.add(child);
         }

         // recursively look up subnets
         subnetLookup(subnets, bem, ipType, addresses);

         //addresses in subnets

            List<BusinessObjectLight> usedIpsInSubnet = bem.getObjectSpecialChildrenWithFilters(ipType, subnet.getId(),
                     new ArrayList<>(Arrays.asList(Constants.CLASS_IP_ADDRESS)), 0, 50);
         for (BusinessObjectLight ip : usedIpsInSubnet) {
            addresses.add(ip.getName());
         }

         LOG.warn("ip addresses in subnet " + subnet.getName() + " " + usedIpsInSubnet);
      }

   }

      public void printFolderAddresses(HashMap<String, ArrayList<String>> folderAddresses) {

         for (String folderName : folderAddresses.keySet()) {
            ArrayList<String> addresses = folderAddresses.get(folderName);
            for (String address : addresses) {
               LOG.warn("Folder: '" + folderName + "' Address: '" + address + "'");
            }
         }
      }

   /**
       * returns the containing folder name as the location if the address is in a folder or null if address is not in a folder
       * @param ipAddressString
       * @return location name which contains this address
       */
      public String getLocationForIpAddress(String ipAddressString) {
         return addresslookup.get(ipAddressString);
      }

   }

   /**
    * Class which sets requisition values at top of csv file
    */
   // remove public static class in groovy
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

      public static final String METADATA_SERVICE_ID = "MetaData_requisition:serviceId";
      public static final String METADATA_SERVICE_NAME = "MetaData_requisition:serviceName";
      public static final String METADATA_CUSTOMER_ID = "MetaData_requisition:customerId";
      public static final String METADATA_CUSTOMER_NAME = "MetaData_requisition:customerName";

      // this is same order as in csv header line
      public static final List<String> OPENNMS_REQUISITION_HEADERS = Arrays.asList(NODE_LABEL, ID_, MINION_LOCATION, PARENT_FOREIGN_ID, 
               PARENT_FOREIGN_SOURCE, IP_MANAGEMENT, MGMTTYPE_, SVC_FORCED, CAT_, ASSET_CATEGORY, ASSET_REGION, ASSET_SERIALNUMBER, 
               ASSET_ASSETNUMBER, ASSET_LATITUDE, ASSET_LONGITUDE, ASSET_THRESHOLDCATEGORY,
               ASSET_NOTIFYCATEGORY, ASSET_POLLERCATEGORY, ASSET_DISPLAYCATEGORY, ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE, ASSET_CIRCUITID,
               ASSET_DESCRIPTION, METADATA_SERVICE_ID, METADATA_SERVICE_NAME,METADATA_CUSTOMER_ID,METADATA_CUSTOMER_NAME);

      public static final String DEFAULT_MINION_LOCATION = "Default"; // used when OpenNMS core is the poller.

   }
   
   /**
    * Class to decode IP V4 Address with or without a cidr address prefix
    */
   // remove static class in groovy
   public static class IpV4Cidr {
      static Logger LOG =  LoggerFactory.getLogger("OpenNMSInventoryExport"); // needed for groovy


      private String ipv4WithCidrString;
      private InetAddress netMask;
      private byte[] netMaskBytes;
      private String netMaskString;
      private byte[] netMaskComplimentBytes;
      private InetAddress ipAddress;
      private String ipAddressString;

      private InetAddress networkAddress;
      private byte[] networkAddressBytes;
      private String networkAddressString;

      private int cidrPrefix;

      public IpV4Cidr(String ipv4WithCidrString) {
         this.ipv4WithCidrString = ipv4WithCidrString;

         try {
            String[] parts = splitIPv4WithCidr(ipv4WithCidrString);
            ipAddressString = parts[0];
            if (parts.length < 2) {
               cidrPrefix = 0;
            } else {
               cidrPrefix = Integer.parseInt(parts[1]);
            }
         } catch (Exception ex) {
            throw new IllegalArgumentException("invalid ip v4 with cidr prefix: " + ipv4WithCidrString, ex);
         }

         int mask = 0xffffffff << (32 - cidrPrefix);

         int value = mask;
         // not in groovy netMaskBytes = new byte[] { (byte) (value >>> 24), (byte) (value >> 16 & 0xff), (byte) (value >> 8 & 0xff), (byte) (value & 0xff) };
         netMaskBytes = new byte[4] ; 
         netMaskBytes[0] =(byte) (value >>> 24);
         netMaskBytes[1] =(byte) (value >> 16 & 0xff);
         netMaskBytes[2] =(byte) (value >> 8 & 0xff);
         netMaskBytes[3] =(byte) (value & 0xff);
         try {
            netMask = InetAddress.getByAddress(netMaskBytes);
         } catch (Exception ex) {
            throw new IllegalArgumentException("invalid ip v4 cidr prefix: " + cidrPrefix, ex);
         }

         netMaskString = netMask.getHostAddress();

         try {
            ipAddress = InetAddress.getByName(ipAddressString);

            byte[] ipAddressBytes = ipAddress.getAddress();

            networkAddressBytes = andByteArrays(ipAddressBytes, netMaskBytes);
            networkAddress = InetAddress.getByAddress(networkAddressBytes);
            networkAddressString = networkAddress.getHostAddress();

            netMaskComplimentBytes = complimentByteArray(netMaskBytes);

         } catch (Exception ex) {
            throw new IllegalArgumentException("invalid ipAddressString: " + ipAddressString, ex);
         }

      }

      /**
       * check if sub network represented by this object contains the testAddress
       * @param testAddress
       * @return
       */
      public boolean networkContainsAddress(InetAddress testAddress) {
         boolean contains = true;

         try {
            byte[] testAddressBytes = testAddress.getAddress();
            //LOG.warn("xxx testAddressBytes:        " + bytesToHex(testAddressBytes) + "  " + bytesToBinary(testAddressBytes));
            //LOG.warn("xxx netMaskBytes:            " + bytesToHex(netMaskBytes) + "  " + bytesToBinary(netMaskBytes));

            byte[] testAddressNetworkBytes = andByteArrays(testAddressBytes, netMaskBytes);

            //LOG.warn("xxx testAddressNetworkBytes: " + bytesToHex(testAddressNetworkBytes) + "  " + bytesToBinary(testAddressNetworkBytes));
            //LOG.warn("xxx networkAddressBytes:     " + bytesToHex(networkAddressBytes) + "  " + bytesToBinary(networkAddressBytes));

            byte[] xor = xorByteArrays(networkAddressBytes, testAddressNetworkBytes);

            //LOG.warn("xxx xor: " + bytesToHex(xor) + "  " + bytesToBinary(xor));

            for (int x = 0; x < xor.length; x++) {
               if (xor[x] != 0) {
                  contains = false;
                  break;
               }
            }

         } catch (Exception ex) {
            throw new IllegalArgumentException("problem comparing inetAddress: " + ipAddressString, ex);
         }

         return contains;
      }
      
      

      /**
       * check if sub network represented by this object contains the testAddress in string form
       * @param testAddressStr
       * @return true if network contains ip address
       */
      public boolean networkContainsAddress(String testAddressStr) {
         if (testAddressStr.contains("/"))
            throw new IllegalArgumentException("test address cannot have cidr notation: " + testAddressStr);
         IpV4Cidr testAddress = new IpV4Cidr(testAddressStr);
         return networkContainsAddress(testAddress.ipAddress);

      }

      /**
       * splits ipv4 address into address and prefix and checks address with a regix
       * @param ipv4WithCidrString e.g. 192.168.1.1/24 with prefix or 192.168.1.1 without prefix
       * @return String[] parts. parts[0] = ipv4 address parts[1] = prefix
       */
      public static String[] splitIPv4WithCidr(String ipv4WithCidrString) {

         int cidrPrefix;

         String[] parts = ipv4WithCidrString.split("/");
         String ipAddressString = parts[0];

         // TODO change for groovy
         // use '^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$' because groovy cant parse $ in  "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$" 
         String regex = '^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$'; //change " to 'for groovy

         Pattern pattern = Pattern.compile(regex);
         Matcher matcher = pattern.matcher(ipAddressString);
         if (!matcher.matches()) {
            throw new IllegalArgumentException("invalid ip v4 address: " + ipAddressString);
         }

         try {
            if (parts.length > 2)
               throw new IllegalArgumentException();
            if (parts.length < 2) {
               cidrPrefix = 0;
            } else {
               cidrPrefix = Integer.parseInt(parts[1]);
               if (cidrPrefix < 0 || cidrPrefix > 32)
                  throw new IllegalArgumentException();
            }
         } catch (Exception ex) {
            throw new IllegalArgumentException("invalid ip v4 with cidr prefix: " + ipv4WithCidrString, ex);
         }

         return parts;
      }

      public static byte[] complimentByteArray(byte[] bytes) {
         byte[] compliment = new byte[bytes.length];
         for (int x = 0; x < bytes.length; x++) {
            // int bits = (bytes[x] & 0xFF);
            int bits = Byte.toUnsignedInt(bytes[x]);
            int bitsCompliment = ~bits;
            byte byteCompliment = (byte) bitsCompliment;
            compliment[x] = byteCompliment;
         }
         return compliment;
      }

      public static byte[] andByteArrays(byte[] bytesA, byte[] bytesB) {
         if (bytesA.length != bytesB.length)
            throw new IllegalArgumentException("byte arrays not same length. bytesA " + bytesA.length + " bytesB " + bytesB.length);

         byte[] anded = new byte[bytesA.length];
         for (int x = 0; x < bytesA.length; x++) {
            int bitsA = Byte.toUnsignedInt(bytesA[x]);
            int bitsB = Byte.toUnsignedInt(bytesB[x]);
            int bitsAnd = bitsA & bitsB;
            byte byteAnd = (byte) bitsAnd;
            anded[x] = byteAnd;
         }
         return anded;
      }

      public static byte[] xorByteArrays(byte[] bytesA, byte[] bytesB) {
         if (bytesA.length != bytesB.length)
            throw new IllegalArgumentException("byte arrays not same length. bytesA " + bytesA.length + " bytesB " + bytesB.length);

         byte[] xored = new byte[bytesA.length];
         for (int x = 0; x < bytesA.length; x++) {
            int bitsA = Byte.toUnsignedInt(bytesA[x]);
            int bitsB = Byte.toUnsignedInt(bytesB[x]);
            int bitsXor = bitsA ^ bitsB;
            byte byteXor = (byte) bitsXor;
            xored[x] = byteXor;
         }
         return xored;
      }
      
      public static byte[] orByteArrays(byte[] bytesA, byte[] bytesB) {
         if (bytesA.length != bytesB.length)
            throw new IllegalArgumentException("byte arrays not same length. bytesA " + bytesA.length + " bytesB " + bytesB.length);

         byte[] ored = new byte[bytesA.length];
         for (int x = 0; x < bytesA.length; x++) {
            int bitsA = Byte.toUnsignedInt(bytesA[x]);
            int bitsB = Byte.toUnsignedInt(bytesB[x]);
            int bitsOr = bitsA | bitsB;
            byte byteXor = (byte) bitsOr;
            ored[x] = byteXor;
         }
         return ored;
      }

      /**
       * substitutes the network portion of the inputIpv4Address for the netowrk portion of the substitute address
       * For example:
       *                                 <inside subnet>=<substitute subnet>
       *  String subnetNetSubstitutionStr = "172.16.0.0/22=192.168.0.0/24";
       *  String inputIpv4AddressStr = "172.16.105.20";
       *  String substituteAddressStr= "192.168.105.20
       *  
       * @param subnetNetSubstitutionFilterStr
       * @param inputIpv4AddressStr
       * @return substituteAddressStr
       */
      public static String subnetIpv4Substitution(String subnetNetSubstitutionFilterStr, String inputIpv4AddressStr) {
         
         
         String substituteAddressStr = "";

         IpV4Cidr ipV4Address = null;
         IpV4Cidr insideSubnet = null;
         IpV4Cidr substituteSubnet = null;
         
         if(subnetNetSubstitutionFilterStr==null || subnetNetSubstitutionFilterStr.isEmpty()) {
            LOG.warn("no subnetNetSubstitutionFilter provided. Passing address unchanged");
            return inputIpv4AddressStr;
         }

         try {

            String[] parts = subnetNetSubstitutionFilterStr.split("=");
            if (parts.length != 2) {
               throw new IllegalArgumentException("no '=' seperating parts in subnetNetSubstitution: " + subnetNetSubstitutionFilterStr);
            }

            insideSubnet = new IpV4Cidr(parts[0]);
            substituteSubnet = new IpV4Cidr(parts[1]);
            ipV4Address = new IpV4Cidr(inputIpv4AddressStr);

            LOG.warn("\n ipV4Address = " + ipV4Address+"\n insideSubnet = " + insideSubnet+"\n substituteSubnet = " + substituteSubnet);

            if (insideSubnet.networkContainsAddress(ipV4Address.getIpAddress())) {
               
               byte[] substituteNetmaskBytes = substituteSubnet.getNetMask().getAddress();
               LOG.warn("\n substituteNetmaskBytes           = " + bytesToBinary( substituteNetmaskBytes));
               
               byte[] complimentSubstituteNetmaskBytes = complimentByteArray(substituteNetmaskBytes);
               LOG.warn("\n complimentSubstituteNetmaskBytes = " + bytesToBinary( complimentSubstituteNetmaskBytes));
               
               byte[] substituteNetworkAddressBytes = substituteSubnet.getNetworkAddress().getAddress();
               LOG.warn("\n substituteNetworkAddressBytes    = " + bytesToBinary( substituteNetworkAddressBytes));
               
               byte[] ipV4AddressBytes = ipV4Address.getIpAddress().getAddress();
               LOG.warn("\n ipV4AddressBytes                 = " + bytesToBinary(ipV4AddressBytes));

               byte[] andAddressBytes = andByteArrays(ipV4AddressBytes, complimentSubstituteNetmaskBytes);
               LOG.warn("\n andAddressBytes                  = " + bytesToBinary(andAddressBytes));
               
               byte[] substitueAddressBytes = orByteArrays(andAddressBytes, substituteNetworkAddressBytes);
               
               InetAddress substitueAddress = InetAddress.getByAddress(substitueAddressBytes);
               
               substituteAddressStr = substitueAddress.getHostAddress();
               
               LOG.warn("\n substitueAddressBytes            = " + bytesToBinary(substitueAddressBytes)+ " substituteAddress: "+substituteAddressStr);

               //LOG.warn("subnet contains address using substitute address string" + substituteAddressStr);
            } else {
               substituteAddressStr = inputIpv4AddressStr;
               //LOG.warn("subnet does not contain address using supplied addresss string : "+substituteAddressStr);

            }

         } catch (Exception ex) {
            throw new IllegalArgumentException("incorrectly formatted subnetNetSubstitution: " + subnetNetSubstitutionFilterStr, ex);
         }

         return substituteAddressStr;
      }

      public static String bytesToHex(byte[] bytes) {
         StringBuffer sb = new StringBuffer();
         for (byte b : bytes) {
            String st = String.format("%02X", b);
            sb.append(st);
         }
         return sb.toString();
      }

      public static String bytesToBinary(byte[] bytes) {
         StringBuffer sb = new StringBuffer();
         for (byte b : bytes) {
            String st = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            sb.append(st);
         }
         return sb.toString();
      }

      public String getIpv4WithCidrString() {
         return ipv4WithCidrString;
      }

      public InetAddress getNetMask() {
         return netMask;
      }

      public String getNetMaskString() {
         return netMaskString;
      }

      public InetAddress getIpAddress() {
         return ipAddress;
      }

      public String getIpAddressString() {
         return ipAddressString;
      }

      public int getCidrPrefix() {
         return cidrPrefix;
      }

      public InetAddress getNetworkAddress() {
         return networkAddress;
      }

      public String getNetworkAddressString() {
         return networkAddressString;
      }

      // TODO note in groovy do NOT start new line in string aggregation with +
      @Override
      public String toString() {
         return "IpV4Cidr [ipv4WithCidrString=" + ipv4WithCidrString + ", netMask=" + netMask + ", netMaskString=" + netMaskString +
                  ", ipAddress=" + ipAddress + ", ipAddressString=" + ipAddressString + ", networkAddress=" + networkAddress +
                  ", networkAddressString=" + networkAddressString + ", cidrPrefix=" + cidrPrefix + "]";
      }

   }
   
}
