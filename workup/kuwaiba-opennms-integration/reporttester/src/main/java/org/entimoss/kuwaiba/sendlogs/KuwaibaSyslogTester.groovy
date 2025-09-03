/**
 * Exports inventory of nodes and interface IP addresses as OpenNMS PRIS CSV format.
 * (see https://docs.opennms.com/pris/latest/ )
 * Entimoss Ltd - version 0.8 (Apache Licensed)
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
 *    generatePassivePon
 *    If true, the exporter searches for OpticalLineTerminals and searches for trees of provisioned PON devices describing circuits from OLTs to ONT's
 *    Each device in the tree is provisioned with an upstream device so that alarm downstream and upstream correlation can be performed.  
 *    
 *    defaultParentForeignSource
 *    Sets the default foreign source for the upstream definitions
 *    
 *    csvOutputFile
 *    Sets the name and location (within the container) of the csv output file.
 *    This is useful where the report is too long for vaadin to export as a rawReport
 *    If not set, defaults to "/exported-reports/"+defaultParentForeignSource+".csv"
 * 
 * Applies to: All classes as a generic report
 * 
 * Notes - todo
 * LOG.warn should be LOG.debug if debugging is enabled
 * 
 */

package org.entimoss.kuwaiba.sendlogs; // package omitted from groovy

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.InventoryObjectPool;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.PersistenceService.EXECUTION_STATE;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.modules.optional.reports.defaults.RawReport;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

// TODO uncomment in groovy script
//KuwaibaSyslogTester opennmsExport = new KuwaibaSyslogTester(bem, aem,  mem,  parameters, connectionHandler);
//return opennmsExport.returnReport();

public class KuwaibaSyslogTester {
   static Logger LOG = LoggerFactory.getLogger("OpenNMSInventoryExport"); // remove static in groovy

   BusinessEntityManager bem = null; // injected in groovy
   ApplicationEntityManager aem = null; // injected in groovy
   MetadataEntityManager mem = null; // injected in groovy
   Map<String, String> parameters = new HashMap<>(); // injected in groovy
   GraphDatabaseService connectionHandler = null; //injected in groovy

   IPLocationDAO ipLocationDAO = null;

   String title = "KuwaibaSyslogTester";
   String version = "0.1";
   String author = "Craig Gallen";

   public KuwaibaSyslogTester() {
   };

   public KuwaibaSyslogTester(BusinessEntityManager bem, ApplicationEntityManager aem, MetadataEntityManager mem, Map<String, String> parameters, GraphDatabaseService connectionHandler) {
      super();
      this.bem = bem;
      this.aem = aem;
      this.mem = mem;
      this.parameters = (parameters == null) ? new HashMap<String, String>() : parameters;
      this.connectionHandler = connectionHandler;

      LOG.info("****************************************************************");
      LOG.info("Start of " + title + " Version " + version + " Author " + author);

      LOG.info("opennms export report parameters :");
      for (Entry<String, String> entry : parameters.entrySet()) {
         LOG.info("   key: " + entry.getKey() + " value: " + entry.getValue());
      }
      LOG.info("****************************************************************");

      LOG.info("initialise to get all ip addresses, folders and subnets names from ipam");
      ipLocationDAO = new IPLocationDAO(bem);
      try {
         ipLocationDAO.init();
      } catch (Exception ex) {
         throw new RuntimeException("problem initialising ipLocationDao ", ex);
      }

   }

   // main report function
   InventoryReport returnReport() {
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
      String defaultAssetCategory = parameters.getOrDefault("defaultAssetCategory", "");

      /*
       * defaultAssetDisplayCategory
       * AssetDisplayCategory is currently not populated from the model
       * AssetDisplayCategory is set to the defaultAssetDisplayCategory or blank if the defaultAssetDisplauCategory is not set
       * (this can be used in OpenNMS to determine which users can view an object)
       */
      String defaultAssetDisplayCategory = parameters.getOrDefault("defaultAssetDisplayCategory", "");

      /*
       * subnetNetSubstitutionFilterStr
       * substitutes the network portion of the inputIpv4Address for the network portion of the substitute address
       * For example:
       *                                   <within subnet>=<substitute subnet>
       *  String subnetNetSubstitutionStr = "172.16.0.0/22=192.168.105.0/24";
       *  String inputIpv4AddressStr = "172.16.105.20";
       *  String substituteAddressStr= "192.168.105.20
       */
      String subnetNetSubstitutionFilter = parameters.getOrDefault("subnetNetSubstitutionFilter", "");

      /*
       * rangeParentValue
       * The rangeParentValue can be the name property of the object or the kuwaiba objectID of the object.
       * If the rangeParentValue is not set, all devices will be included in the tree.
       * If the rangeParentValue is not found, an exception will be thrown and the report will not complete
       * Finds the parent visable object of the devices to include in the device list. 
       * If a device has this parent somewhere in their parent object tree, the device will be a candidate to be included in the requisition for OpenNMS.
       */
      String rangeParentValue = parameters.getOrDefault("rangeParentValue", "");

      /*
       * generatePassivePon
       * If true, the exporter searches for OpticalLineTerminals and searches for trees of provisioned PON devices describing circuits from OLTs to ONT's
       * Each device in the tree is provisioned with an upstream device so that alarm downstream and upstream correlation can be performed.        
       */
      String generatePassivePonStr = parameters.getOrDefault("generatePassivePon", "true");
      boolean generatePassivePon = Boolean.parseBoolean(generatePassivePonStr);

      /*
       * defaultParentForeignSource
       * Sets the default foreign source for the upstream object definitions (parent-foreign-source="?" parent-foreign-id="?")
       */
      String defaultParentForeignSource = parameters.getOrDefault("defaultParentForeignSource", "kuwaibaForeignSource");

      /*
       * csvOutputFile
       * Sets the name and location (within the container) of the csv output file.
       * This is useful where the report is too long for vaadin to export as a rawReport
       * If not set, defaults to "/exported-reports/"+defaultParentForeignSource+".csv"
       */
      String csvOutputFileString = parameters.getOrDefault("csvOutputFile", "/exported-reports/" + defaultParentForeignSource + ".csv");
      File csvOutputFile = new File(csvOutputFileString);

      String csvOutputStr = "";

      // return a RawReport containing csv
      InventoryReport report = new RawReport(title, author, version, csvOutputStr);

      LOG.info("****************************************************************");
      LOG.info("End of " + title);
      LOG.info("****************************************************************");

      return report;

   }

   public ArrayList<HashMap<String, String>> generateCsvLineData(BusinessEntityManager bem, ApplicationEntityManager aem,
            Boolean useAbsoluteNames, Boolean useAllPortAddresses, Boolean useNodeLabelAsForeignId, String defaultAssetCategory, String defaultAssetDisplayCategory,
            String subnetNetSubstitutionFilter, String rangeParentValue, boolean generatePassivePon, String defaultParentForeignSource) {

      // data for each line in csv export
      ArrayList<HashMap<String, String>> csvLineData = new ArrayList<HashMap<String, String>>();

      // devices to be processed into csv export (maintains order of entry but only one of each device)
      LinkedHashSet<BusinessObject> devices = new LinkedHashSet<BusinessObject>();

      //                                 (className             downstream            upstream
      //   Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>>
      Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamUpsteamMappings = new LinkedHashMap<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>>();

      // simple hashmap to search upstream businessObjects
      HashMap<BusinessObject, BusinessObjectLight> simpleUpstreamMapping = new HashMap<BusinessObject, BusinessObjectLight>();

      try {

         // tries to find parent object of all devices to include in range
         // parent must be a viewable object which can have a generic communications element as a child 
         // range parent value can be an absolute object id or an object name
         BusinessObjectLight rangeParent = null;
         String rangeParentClassName = null;
         String rangeParentId = null;

         String searchErrorMsg = null;
         if (rangeParentValue != null && !rangeParentValue.isEmpty()) {
            try {
               // see if there is an object with the same name
               List<BusinessObjectLight> rangeParents = bem.getObjectsWithFilterLight(Constants.CLASS_VIEWABLEOBJECT, Constants.PROPERTY_NAME, rangeParentValue);
               if (!rangeParents.isEmpty())
                  rangeParent = rangeParents.get(0);
            } catch (Exception ex) {
               searchErrorMsg = ex.getMessage();
            }
            if (rangeParent == null)
               // else see if there is an object with the object id = viewable object
               try {
                  // see if there is an object with the same id
                  List<BusinessObjectLight> rangeParents = bem.getObjectsWithFilterLight(Constants.CLASS_VIEWABLEOBJECT, Constants.PROPERTY_UUID, rangeParentValue);
                  if (!rangeParents.isEmpty())
                     rangeParent = rangeParents.get(0);
               } catch (Exception ex) {
                  searchErrorMsg = ex.getMessage();
               }
            if (rangeParent == null) {
               throw new IllegalArgumentException("cannot find parent wiewable object with rangeParentValue=" + rangeParentValue + " search error:" + searchErrorMsg);
            }
            rangeParentClassName = rangeParent.getClassName();
            rangeParentId = rangeParent.getId();
            LOG.warn("finding devices with parent object rangeParentClassName=" + rangeParentClassName + " rangeParentId=" + rangeParentId + " " + rangeParent);
         }

         // if generating pon populate downstreamUpsteamMappings with OLTs in range
         if (generatePassivePon) {
            LOG.info("************************************************");
            LOG.info("GENERATING PASSIVE PON DATA FOR OLTS " + title);
            LOG.info("************************************************");

            List<String> searchClassNames = Arrays.asList("FiberSplitter", "OpticalNetworkTerminal", "OpticalLineTerminal");
            String terminatingClassName = "OpticalNetworkTerminal";

            try {

               LinkedHashSet<BusinessObjectLight> oltSet = new LinkedHashSet<BusinessObjectLight>();

               List<BusinessObject> oltdevices = bem.getObjectsOfClass("OpticalLineTerminal", -1);
               LOG.info("all olt devices:" + oltdevices);

               for (BusinessObject oltDevice : oltdevices) {

                  // only consider OLTs in range if rangeParent set
                  // TODO work around
                  if (rangeParentId != null) {
                     boolean oltInrange = false;
                     List<BusinessObjectLight> oltParents = bem.getParents(oltDevice.getClassName(), oltDevice.getId());
                     Iterator<BusinessObjectLight> oltParentsIterator = oltParents.iterator();
                     while (oltParentsIterator.hasNext() && !oltInrange) {
                        BusinessObjectLight parent = oltParentsIterator.next();
                        if (parent.getId().equals(rangeParentId)) {
                           oltInrange = true;
                        }
                     }
                     if (!oltInrange) {
                        LOG.info("olt not in range, Ignoring : " + businessObjectToString(oltDevice));
                        continue;
                     }
                  }

                  // only add OLTs with ip address if useAllPortAddresses true
                  // only add OLTs with ip Address and isManagement
                  boolean addOlt = false;

                  List<BusinessObjectLight> commPorts = bem.getChildrenOfClassLightRecursive(oltDevice.getId(), oltDevice.getClassName(), "GenericCommunicationsPort", null, -1, -1);
                  LOG.info("coms ports on device : " + businessObjectToString(oltDevice) + " ports: " + commPorts);

                  Iterator<BusinessObjectLight> commportsIterator = commPorts.iterator();

                  while (commportsIterator.hasNext() && addOlt != true) {
                     BusinessObjectLight port = commportsIterator.next();

                     // We check if there's an IP address associated to the port.
                     List<BusinessObjectLight> ipAddressesInPort = bem.getSpecialAttribute(port.getClassName(), port.getId(), "ipamHasIpAddress");

                     if (ipAddressesInPort.isEmpty()) {
                        LOG.info("no ip address on port : " + businessObjectToString(port));
                     } else {

                        if (useAllPortAddresses) {
                           addOlt = true;
                           LOG.info("useAllPorts: ip address on port : " + businessObjectToString(port) + " ipAddressesInPort: " + ipAddressesInPort);
                        } else {
                           String isManagementStr = bem.getAttributeValueAsString(port.getClassName(), port.getId(), "isManagement");
                           boolean isManagement = Boolean.valueOf(isManagementStr);
                           if (isManagement) {
                              addOlt = true;
                              LOG.info("ip address on isManagement port : " + businessObjectToString(port) + " ipAddressesInPort: " + ipAddressesInPort);
                           }
                        }

                     }
                  }

                  if (addOlt) {
                     LOG.info("will generate for OLT :" + businessObjectToString(oltDevice));
                     oltSet.add(oltDevice);
                  }

               }

               LOG.info("finding and adding downstream for olt devices:" + oltSet);

               downstreamUpsteamMappings = gettingDownstreamObjectsForOLTs(oltSet, searchClassNames, terminatingClassName);

               printChildParentMap(downstreamUpsteamMappings);


            } catch (Exception ex) {
               LOG.info("problem getting OLTS ", ex);
            }
            LOG.info("************************************************");
            LOG.info("END OF GENERATING PASSIVE PON DATA FOR OLTS " + title);
            LOG.info("************************************************");
         }

         // Next we add all remaining active network devices but don't replace ones already created
         List<BusinessObject> deviceList = bem.getObjectsOfClass(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, -1);
         for (BusinessObject device : deviceList) {
            if (!devices.contains(device)) {
               devices.add(device);
            }
         }

         LOG.info("************************************************");
         LOG.info("GENERATING FAULT LOGS FOR DEVICES " + title);
         LOG.info("************************************************");
         
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      return csvLineData;
   }

   /**
    * returns the data model classPath for the given class name
    * the class path begins with root of hierarchy and ends with given class name
    * @param className  name of the class as defined in kuwaiba data model manager
    * @return String hierarchy of class separated by /
    */
   public String getDataModelClassPath(String className) {
      StringBuffer sb = new StringBuffer();
      try {
         ArrayList<ClassMetadataLight> classMetadataList = new ArrayList<ClassMetadataLight>(mem.getSuperClassesLight(className, true));
         // reverse order of list
         Collections.reverse(classMetadataList);

         Iterator<ClassMetadataLight> classMetadataListIterator = classMetadataList.iterator();
         while (classMetadataListIterator.hasNext()) {
            ClassMetadataLight classMetadata = classMetadataListIterator.next();
            sb.append(classMetadata.getName());
            if (classMetadataListIterator.hasNext())
               sb.append("/");
         }
         return sb.toString();
      } catch (MetadataObjectNotFoundException e) {
         throw new IllegalArgumentException("cant find className=" + className, e);
      }
   }

   /**
    * Writes the contents of a given string to the output file
    * @param outputFile File object to write the string to. Ant pre existing file will be deleted. A new file object will be created each time this method is called
    * @param stringdata the string to write to the file
    */
   public void writeStringToFile(File outputFile, String stringdata) {
      PrintWriter printWriter = null;
      try {
         outputFile.delete();

         File parent = outputFile.getParentFile();
         if (!parent.exists())
            parent.mkdirs();
         printWriter = new PrintWriter(new FileWriter(outputFile));
         printWriter.print(stringdata);

      } catch (Exception ex) {
         LOG.error("problem printing data to outputfile: " + outputFile.getAbsolutePath(), ex);
      } finally {
         if (printWriter != null)
            printWriter.close();
      }
   }

   // overloaded toString methods for BusinessObjects
   String businessObjectToString(BusinessObject bo) {
      return (bo == null) ? "BusinessObject[ null ]"
               : "BusinessObject[ getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" +
                        bo.getClassDisplayName() + ", getId()=" + bo.getId() + " getAttributes()=" + bo.getAttributes() + "]";
   }

   String businessObjectToString(BusinessObjectLight bo) {
      return (bo == null) ? "BusinessObjectLight[ null ]"
               : "BusinessObjectLight[ getName()=" + bo.getName() + ", getClassName()=" + bo.getClassName() + ", getClassDisplayName()=" +
                        bo.getClassDisplayName() + ", getId()=" + bo.getId() + "]";
   }

   /**
    * class which looks up which minion location ip addresses are in are in based on the parent folder name
    */
   public class IPLocationDAO {
      Logger LOG = LoggerFactory.getLogger("OpenNMSInventoryExport"); // needed for groovy

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

   /*
    * METHODS FOR TRAVERSING PATHS AND FINDING PASSIVE SPLITTERS
    */

   /**
    * Search each device in the oltSet for PON optical ports. 
    * PON ports are identified as not isManagement and name starts with "PON"
    * @param oltSet set of OLT devices
    * @param searchClassNames set of passive nad active devices ot include in downstream list
    * @param terminatingClassName  should be an ONT identifier i.e. bottom of the tree with an ONT attached
    * @return
    */
   public Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> gettingDownstreamObjectsForOLTs(LinkedHashSet<BusinessObjectLight> oltSet, List<String> searchClassNames, String terminatingClassName) {

      // structure to hold mapping of objects to upstream parents
      //   className              downstream,          upstream (each downstream can have only one upstream)
      Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamUpsteamMappings = new LinkedHashMap<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>>();
      for (String name : searchClassNames) {
         downstreamUpsteamMappings.put(name, new LinkedHashMap<BusinessObjectLight, BusinessObjectLight>());
      }

      // add olts to mapping without upstream
      for (BusinessObjectLight olt : oltSet) {
         downstreamUpsteamMappings.get("OpticalLineTerminal").put(olt, null);
      }

      // now add downstream of OLTs
      for (BusinessObjectLight olt : oltSet) {
         LOG.info("getting downstream objects for " + businessObjectToString(olt));
         try {
            String parentOid = olt.getId();
            String parentClass = olt.getClassName();
            // getChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, HashMap <String, String> attributesToFilters, int page, int limit) 
            List<BusinessObjectLight> oltPorts = bem.getChildrenOfClassLightRecursive(parentOid, parentClass, "OpticalPort", null, -1, -1);

            for (BusinessObjectLight port : oltPorts) {

               // if isManagement attribute set this is NOT a PON port.
               String isManagementStr = bem.getAttributeValueAsString(port.getClassName(), port.getId(), "isManagement");
               boolean isManagement = Boolean.valueOf(isManagementStr);

               LOG.info("processing oltName: " + olt.getName() + " port: " + businessObjectToString(port) + " isManagement property:" + isManagement);
               if (!isManagement) {
                  Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamMapping = gettingDownstreamObjectsForPort(port.getClassName(), port.getId(), searchClassNames, terminatingClassName);
                  LOG.info("adding: " + olt.getName() + " downstream nodes: " + downstreamMapping.size());

                  addDownstreamMapping(downstreamUpsteamMappings, downstreamMapping);
               }
            }
         } catch (Exception ex) {
            throw new IllegalArgumentException("problem mapping ports for olt: " + businessObjectToString(olt), ex);
         }
      }

      return downstreamUpsteamMappings;
   }

   /**
    * Adds the contents of the addtionalMapping to the currentMapping. If contents of oth maps are identical for a given key, no change is made. 
    * If the additional mapping tries to redfine an upstream mapping in the currentMapping an exception is thrown
    * @param currentMapping
    * @param additionalMapping
    * @return currentMapping with additional objects
    * @Throws IllegalArgumentException if additionalMapping tries to redefine an upstream mpping in the current mapping (shouldn't happen)
    */
   public Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> addDownstreamMapping(Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> currentMapping,
            Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> additionalMapping) {

      // check maps have same structure
      for (String key : currentMapping.keySet()) {
         if (!additionalMapping.containsKey(key))
            throw new IllegalArgumentException("mapping keys are different additionalMapping does not contain currentMapping key: " + key);
      }
      for (String key : additionalMapping.keySet()) {
         if (!currentMapping.containsKey(key))
            throw new IllegalArgumentException("mapping keys are different currentMapping does not contain additionalMapping key: " + key);
      }

      for (String key : currentMapping.keySet()) {
         LinkedHashMap<BusinessObjectLight, BusinessObjectLight> currentObjectMapping = currentMapping.get(key);
         LinkedHashMap<BusinessObjectLight, BusinessObjectLight> additionalObjectMapping = additionalMapping.get(key);

         for (BusinessObjectLight additionalBusinessObjectKey : additionalObjectMapping.keySet()) {
            if (!currentObjectMapping.containsKey(additionalBusinessObjectKey)) {
               currentObjectMapping.put(additionalBusinessObjectKey, additionalObjectMapping.get(additionalBusinessObjectKey));
            } else {
               if (additionalObjectMapping.get(additionalBusinessObjectKey) != currentObjectMapping.get(additionalBusinessObjectKey)) {
                  throw new IllegalArgumentException("addDownstreamMapping is trying to redefine parent of " +
                           businessObjectToString(additionalBusinessObjectKey) + " from " + businessObjectToString(currentObjectMapping.get(additionalBusinessObjectKey)) +
                           " to " + additionalObjectMapping.get(additionalBusinessObjectKey));
               }
            }

         }
      }

      return currentMapping;
   }

   /**
    * gets devices in paths for a upstream parent port defined by String objectClass, String objectId
    * @param portObjectClass
    * @param portObjectId
    * @param searchClassNames  The device types to include in search
    * @param terminatingClassName The device type to define bottom of tree (ends search)
    * @return
    */
   public Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> gettingDownstreamObjectsForPort(String portObjectClass, String portObjectId, List<String> searchClassNames, String terminatingClassName) {

      Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamUpsteamMapping = null;

      // create the connection manager
      PhysicalConnectionsServiceProxy physicalConnectionService = new PhysicalConnectionsServiceProxy(aem, bem, mem, connectionHandler);
      try {

         HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalTreeResult = physicalConnectionService.getPhysicalTree(portObjectClass, portObjectId);

         LOG.info("************ print physical tree results for port objectClass" + portObjectClass + " objectId" + portObjectId);
         printPhysicalTreeResult(physicalTreeResult, searchClassNames);
         LOG.info("************ END OF print physical tree results");

         LOG.info("************ starting downstream mapping searchClassNames: " + searchClassNames + " terminatingClassName=" + terminatingClassName);
         downstreamUpsteamMapping = traverseTree(physicalTreeResult, searchClassNames, terminatingClassName);
         LOG.info("************ END OF downstream mapping");

         LOG.info("************ Print down stream up stream mapping");
         printDownstreamUpsteamMapping(downstreamUpsteamMapping);
         LOG.info("************ END OF Print down stream up stream mapping");

      } catch (Exception ex) {
         LOG.error("problem getting tree for olt ", ex);
      }

      return downstreamUpsteamMapping;
   }

   /**
    * Search a tree of links between optical ports to find upstream devices which contain the ports.
    * 
    * Each port has a containing device which it belongs to. 
    * Only the containing devices which have class names corresponding to searchClassNames are considered
    * @param physicalTreeResult  
   
    * @param searchClassNames list of classnames to include in the search
    * 
    *                                 (className             downstream            upstream
    * @return Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>>
    * returns a map of device maps with keys matching the classNames in parameter searchClassNames
    * each device map contains an entry for a business object with a reference to its upstream object
    */
   public Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> traverseTree(HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalTreeResult,
            List<String> searchClassNames,
            String terminatingClassName) {

      // create data structures

      // structure to hold mapping of objects to upstream parents
      //   className              downstream,          upstream (each downstream can have only one upstream)
      Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamUpsteamMappings = new HashMap<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>>();
      for (String name : searchClassNames) {
         downstreamUpsteamMappings.put(name, new LinkedHashMap<BusinessObjectLight, BusinessObjectLight>());
      }

      // structure to hold mapping of port to parent device
      LinkedHashMap<BusinessObjectLight, BusinessObjectLight> connectionPortToParentDeviceMap = new LinkedHashMap<BusinessObjectLight, BusinessObjectLight>();

      try {
         // create portToMap mapping for all ports in mapping
         for (BusinessObjectLight connectnPort : physicalTreeResult.keySet()) {

            //                                                   getParentsUntilFirstOfClass(String objectClass,          String oid,            String... objectToMatchClassNames)
            List<BusinessObjectLight> containingDeviceList = bem.getParentsUntilFirstOfClass(connectnPort.getClassName(), connectnPort.getId(), (String[]) searchClassNames.toArray());

            for (BusinessObjectLight containingDevice : containingDeviceList) {
               if (searchClassNames.contains(containingDevice.getClassName())) {
                  if (!connectionPortToParentDeviceMap.containsKey(connectnPort)) {
                     connectionPortToParentDeviceMap.put(connectnPort, containingDevice);
                  } else {
                     if (!connectionPortToParentDeviceMap.get(connectnPort).equals(containingDevice)) {
                        // should not happen
                        throw new IllegalArgumentException("Trying to redefine parent of " + businessObjectToString(connectnPort)
                                 + " from " + businessObjectToString(connectionPortToParentDeviceMap.get(connectnPort)) + " to " + businessObjectToString(containingDevice));
                     }
                  }
                  break;
               }
            }
         }

         LOG.info("************ Print connectionPortToParentDeviceMap");
         for (BusinessObjectLight bo : connectionPortToParentDeviceMap.keySet()) {
            LOG.info("connectionPort: " + businessObjectToString(bo));
            LOG.info("        device: " + businessObjectToString(connectionPortToParentDeviceMap.get(bo)));
         }
         LOG.info("************ END OF Print connectionPortToParentDeviceMap");

         // traverse tree to find actual links

         BusinessObjectLight upstreamFoundClass = null;

         for (BusinessObjectLight connectionPort : physicalTreeResult.keySet()) {

            if (!"OpticalPort".equals(connectionPort.getClassName())) {
               LOG.info("************ ignoring downstream mapping for " + businessObjectToString(connectionPort));
               continue;
            }

            LOG.info("************ starting downstream mapping for OpticalPort " + businessObjectToString(connectionPort));

            boolean treeContainsTerminatingObject = traverse(connectionPort,
                     upstreamFoundClass,
                     searchClassNames,
                     terminatingClassName,
                     downstreamUpsteamMappings,
                     connectionPortToParentDeviceMap,
                     physicalTreeResult, 0);

            LOG.info("************ finished downstream mapping for connection port " + businessObjectToString(connectionPort));
         }

      } catch (Exception ex) {
         LOG.error("problem getting tree for olt ", ex);
      }

      return downstreamUpsteamMappings;
   }

   /**
    * 
    * @param connectionPort
    * @param upstreamFoundhClass
    * @param searchClassNames
    * @param downstreamUpsteamMappings
    * @param portToParentMap
    * @param physicalTreeResult
    * @param traverseDeapth
    * @return boolean treeContainsTerminatingObject  true if there is an ONT attached to this device
    */
   private boolean traverse(BusinessObjectLight connectionPort,
            BusinessObjectLight upstreamFoundClass,
            List<String> searchClassNames,
            String terminatingClassName,
            Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamUpsteamMappings,
            HashMap<BusinessObjectLight, BusinessObjectLight> connectionPortToParentDeviceMap,
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalTreeResult,
            Integer traverseDeapth) {

      int MAX_TRAVERSE_DEAPTH = 20;
      traverseDeapth++;
      if (traverseDeapth > MAX_TRAVERSE_DEAPTH)
         throw new IllegalArgumentException("Number of links too large to traverse. Traverse deapth " + traverseDeapth + " > " + MAX_TRAVERSE_DEAPTH);

      StringBuffer sb = new StringBuffer();
      for (int i = 0; i <= traverseDeapth; i++)
         sb.append(" ");

      BusinessObjectLight parentDeviceOfCurrentPort = connectionPortToParentDeviceMap.get(connectionPort);

      LOG.info(sb.toString() + "traversing path depth=" + traverseDeapth + " for connectionPort: " +
               businessObjectToString(connectionPort) + " parent: " + businessObjectToString(parentDeviceOfCurrentPort));

      boolean treeContainsTerminatingObject = false;

      // dont continue below terminating class type even if sublist exists
      if (parentDeviceOfCurrentPort != null && terminatingClassName.equals(parentDeviceOfCurrentPort.getClassName())) {
         treeContainsTerminatingObject = true;
         LOG.info(sb.toString() + "found terminating class: " + businessObjectToString(parentDeviceOfCurrentPort) + " for port " + businessObjectToString(connectionPort));

      } else {

         List<BusinessObjectLight> downstreamPorts = physicalTreeResult.get(connectionPort);

         // traverse to bottom of path 
         if (!downstreamPorts.isEmpty()) {

            // traverse downstream ports
            for (BusinessObjectLight downStreamPort : downstreamPorts) {

               BusinessObjectLight newUpstreamFoundClass = upstreamFoundClass;

               if (parentDeviceOfCurrentPort != null && searchClassNames.contains(parentDeviceOfCurrentPort.getClassName())) {
                  newUpstreamFoundClass = parentDeviceOfCurrentPort;
               }

               Boolean terminatingObjectInTree = traverse(downStreamPort,
                        newUpstreamFoundClass,
                        searchClassNames,
                        terminatingClassName,
                        downstreamUpsteamMappings,
                        connectionPortToParentDeviceMap,
                        physicalTreeResult,
                        traverseDeapth);

               if (terminatingObjectInTree) {
                  treeContainsTerminatingObject = true;
               }

            }
         }

      }

      if (treeContainsTerminatingObject) {

         // ignore device types not wanted in tree
         if (parentDeviceOfCurrentPort != null && searchClassNames.contains(parentDeviceOfCurrentPort.getClassName())) {

            LinkedHashMap<BusinessObjectLight, BusinessObjectLight> childParentMapping = downstreamUpsteamMappings.get(parentDeviceOfCurrentPort.getClassName());

            // add upstream device to current device
            // if upstream device is same as this device then we are inside splitter or a splice so do not assign
            if (upstreamFoundClass != null && upstreamFoundClass != parentDeviceOfCurrentPort) {
               if (!childParentMapping.keySet().contains(parentDeviceOfCurrentPort)) {
                  childParentMapping.put(parentDeviceOfCurrentPort, upstreamFoundClass);
                  LOG.info(sb.toString() + "assigning upstream device for " + businessObjectToString(parentDeviceOfCurrentPort) +
                           " to " + businessObjectToString(upstreamFoundClass));
               } else {
                  // check you aren't reassigning upstream device (should not happen)
                  if (!childParentMapping.get(parentDeviceOfCurrentPort).equals(upstreamFoundClass)) {
                     throw new IllegalArgumentException("should not be reassigning upstream device for " + businessObjectToString(parentDeviceOfCurrentPort) +
                              " from " + businessObjectToString(childParentMapping.get(parentDeviceOfCurrentPort)) +
                              " to " + businessObjectToString(upstreamFoundClass));
                  }
                  LOG.info(sb.toString() + " already assigned upstream device for " + businessObjectToString(parentDeviceOfCurrentPort) +
                           " to " + businessObjectToString(upstreamFoundClass));
               }
            }

         }

      }

      return treeContainsTerminatingObject;

   }

   /**
    * utility method for printing out debugging info
    * @param childParentMaps
    */
   public void printChildParentMap(Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> childParentMaps) {

      LOG.info("*************** printChildParentMap");

      for (String className : childParentMaps.keySet()) {
         LinkedHashMap<BusinessObjectLight, BusinessObjectLight> childParentMap = childParentMaps.get(className);
         LOG.info(" child parent map for className: " + className + " size=" + childParentMap.size());

         for (BusinessObjectLight child : childParentMap.keySet()) {
            BusinessObjectLight parent = childParentMap.get(child);
            LOG.info("    parent:" + businessObjectToString(parent));
            LOG.info("       child: " + businessObjectToString(child));
         }
      }
      LOG.info("*************** END printChildParentMap");
   }

   /**
    * utility method for printing out debugging info
    * @param childParentMaps
    */
   public void printDownstreamUpsteamMapping(Map<String, LinkedHashMap<BusinessObjectLight, BusinessObjectLight>> downstreamUpsteamMapping) {
      LOG.info("*************** printDownstreamUpsteamMapping");

      for (String classType : downstreamUpsteamMapping.keySet()) {
         LOG.info(" down stream upstream mapping for : " + classType + " size=" + downstreamUpsteamMapping.get(classType).size());
         for (BusinessObjectLight downstream : downstreamUpsteamMapping.get(classType).keySet()) {
            BusinessObjectLight upstream = downstreamUpsteamMapping.get(classType).get(downstream);
            LOG.info("    upstream     : " + businessObjectToString(upstream));
            LOG.info("       downstream: " + businessObjectToString(downstream));
         }

      }
      LOG.info("*************** END printDownstreamUpsteamMapping");
   }

   /**
    * used for debugging physicalTreeResult
    * @param physicalTreeResult
    */
   public void printPhysicalTreeResult(HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalTreeResult, List<String> searchClassNames) {
      LOG.info("*************** physicalTreeResult");

      try {
         for (BusinessObjectLight connection : physicalTreeResult.keySet()) {
            LOG.info("     connection: " + businessObjectToString(connection));
            List<BusinessObjectLight> connectionParents = bem.getParentsUntilFirstOfClass(connection.getClassName(), connection.getId(), (String[]) searchClassNames.toArray());
            for (BusinessObjectLight parent : connectionParents) {
               if (searchClassNames.contains(parent.getClassName())) {
                  LOG.info("     connection parent=" + businessObjectToString(parent));
                  break;
               }
            }

            LOG.info("           downstream size: " + physicalTreeResult.get(connection).size());
            for (BusinessObjectLight downstream : physicalTreeResult.get(connection)) {
               LOG.info("           downstream:" + businessObjectToString(downstream));
               List<BusinessObjectLight> downstreamParents = bem.getParentsUntilFirstOfClass(downstream.getClassName(), downstream.getId(), (String[]) searchClassNames.toArray());
               for (BusinessObjectLight downstreamParent : downstreamParents) {
                  if (searchClassNames.contains(downstreamParent.getClassName())) {
                     LOG.info("                         downstream parent=" + businessObjectToString(downstreamParent));
                     break;
                  }
               }
            }

         }

      } catch (Exception ex) {
         LOG.error("problem printing PhysicalTreeResult ", ex);
      }

      LOG.info("*************** END OF physicalTreeResult");
   }

   /**
   * PhysicalConnectionsServiceProxy replicates the function of the internal PhysicalConnectionsService 
   * org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService
   * 
   * This is a clone of methods in the internal PhysicalConnectionsService 
   * because the service is not accessible from a script
   * TODO - allow service access from script in kuwaiba
   */
   public static class PhysicalConnectionsServiceProxy {

      private ApplicationEntityManager aem;

      private BusinessEntityManager bem;

      private MetadataEntityManager mem;

      private GraphDatabaseService connectionHandler;

      public PhysicalConnectionsServiceProxy(ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, GraphDatabaseService connectionHandler) {
         super();
         this.aem = aem;
         this.bem = bem;
         this.mem = mem;
         this.connectionHandler = connectionHandler;
      }

      // taken from ogmService ObjectGraphMappingService - ONLY USED MINIMAL METHOD to get a BusinessObjectLight with no validators
      public BusinessObjectLight createObjectLightFromNode(Node instance) {
         Node classNode = instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode();
         String className = (String) classNode.getProperty(Constants.PROPERTY_NAME);

         //First, we create the naked business object, without validators
         BusinessObjectLight res = new BusinessObjectLight(className, (String) instance.getProperty(Constants.PROPERTY_UUID),
                  (String) instance.getProperty(Constants.PROPERTY_NAME), (String) classNode.getProperty(Constants.PROPERTY_DISPLAY_NAME, null));
         return res;
      }

      /**
       * Gets A tree representation of all physical paths as a hash map.
       * @param objectClass The source port class name.
       * @param objectId The source port id.
       * @return A tree representation of all physical paths as a hash map.
       * @throws BusinessObjectNotFoundException If any of the objects involved in the path cannot be found
       * @throws MetadataObjectNotFoundException If any of the object classes involved in the path cannot be found
       * @throws ApplicationObjectNotFoundException If any of the objects involved in the path has a malformed list type attribute
       * @throws InvalidArgumentException If any of the objects involved in the path has an invalid objectId or className
       */
      public HashMap<BusinessObjectLight, List<BusinessObjectLight>> getPhysicalTree(String objectClass, String objectId)
               throws IllegalStateException, BusinessObjectNotFoundException, MetadataObjectNotFoundException,
               ApplicationObjectNotFoundException, InvalidArgumentException {

         //          if (persistenceService.getState() == EXECUTION_STATE.STOPPED)
         //              throw new IllegalStateException(ts.getTranslatedString("module.general.messages.cant-reach-backend"));

         HashMap<BusinessObjectLight, List<BusinessObjectLight>> tree = new LinkedHashMap();
         // If the port is a logical port (virtual port, Pseudowire or service instance, we look for the first physical parent port)
         //try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
         Transaction tx = null;
         try {
            tx = connectionHandler.beginTx();
            //The first part of the query will return many paths, that we build as a tree
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(String.format("MATCH paths = (o)-[r:%s*]-(c) ", RelTypes.RELATED_TO_SPECIAL));
            queryBuilder.append(String.format("WHERE o._uuid = '%s' AND all(rel in r where rel.name IN "
                     + "['mirror','mirrorMultiple'] or rel.name = 'endpointA' or rel.name = 'endpointB') ", objectId));
            queryBuilder.append("WITH nodes(paths) as path ");
            queryBuilder.append("RETURN path ORDER BY length(path) DESC");

            //Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString());
            Result result = connectionHandler.execute(queryBuilder.toString());
            Iterator<List<Node>> column = result.columnAs("path"); //NOI18N

            for (List<Node> listOfNodes : Iterators.asIterable(column)) {
               for (int i = 0; i < listOfNodes.size(); i++) {
                  //BusinessObjectLight object = ogmService.createObjectLightFromNode(listOfNodes.get(i)); // CHANGED
                  BusinessObjectLight object = createObjectLightFromNode(listOfNodes.get(i));

                  if (!tree.containsKey(object))
                     tree.put(object, new ArrayList());

                  if (i < listOfNodes.size() - 1) {
                     //BusinessObjectLight nextObject = ogmService.createObjectLightFromNode(listOfNodes.get(i + 1)); // CHANGED
                     BusinessObjectLight nextObject = createObjectLightFromNode(listOfNodes.get(i + 1));

                     if (!tree.get(object).contains(nextObject))
                        tree.get(object).add(nextObject);
                  }
               }
            }
            tx.success();
         } catch (Exception ex) {
            throw new IllegalArgumentException("problem finding node tree", ex);
         }

         return tree;
      }

   }


   /**
    * Class to decode IP V4 Address with or without a cidr address prefix
    */
   // remove static class in groovy
   public static class IpV4Cidr {
      static Logger LOG = LoggerFactory.getLogger("OpenNMSInventoryExport"); // needed for groovy

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
         netMaskBytes = new byte[4];
         netMaskBytes[0] = (byte) (value >>> 24);
         netMaskBytes[1] = (byte) (value >> 16 & 0xff);
         netMaskBytes[2] = (byte) (value >> 8 & 0xff);
         netMaskBytes[3] = (byte) (value & 0xff);
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

         if (subnetNetSubstitutionFilterStr == null || subnetNetSubstitutionFilterStr.isEmpty()) {
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

            LOG.warn("\n ipV4Address = " + ipV4Address + "\n insideSubnet = " + insideSubnet + "\n substituteSubnet = " + substituteSubnet);

            if (insideSubnet.networkContainsAddress(ipV4Address.getIpAddress())) {

               byte[] substituteNetmaskBytes = substituteSubnet.getNetMask().getAddress();
               LOG.warn("\n substituteNetmaskBytes           = " + bytesToBinary(substituteNetmaskBytes));

               byte[] complimentSubstituteNetmaskBytes = complimentByteArray(substituteNetmaskBytes);
               LOG.warn("\n complimentSubstituteNetmaskBytes = " + bytesToBinary(complimentSubstituteNetmaskBytes));

               byte[] substituteNetworkAddressBytes = substituteSubnet.getNetworkAddress().getAddress();
               LOG.warn("\n substituteNetworkAddressBytes    = " + bytesToBinary(substituteNetworkAddressBytes));

               byte[] ipV4AddressBytes = ipV4Address.getIpAddress().getAddress();
               LOG.warn("\n ipV4AddressBytes                 = " + bytesToBinary(ipV4AddressBytes));

               byte[] andAddressBytes = andByteArrays(ipV4AddressBytes, complimentSubstituteNetmaskBytes);
               LOG.warn("\n andAddressBytes                  = " + bytesToBinary(andAddressBytes));

               byte[] substitueAddressBytes = orByteArrays(andAddressBytes, substituteNetworkAddressBytes);

               InetAddress substitueAddress = InetAddress.getByAddress(substitueAddressBytes);

               substituteAddressStr = substitueAddress.getHostAddress();

               LOG.warn("\n substitueAddressBytes            = " + bytesToBinary(substitueAddressBytes) + " substituteAddress: " + substituteAddressStr);

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
