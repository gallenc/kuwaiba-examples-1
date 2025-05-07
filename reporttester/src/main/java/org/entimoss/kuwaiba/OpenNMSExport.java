/**
 * Exports inventory as OpenNMS PRIS CSV format.
 * Entimoss Ltd - version 0.1
 * Parameters: None
 * Applies to: TBD
 */
package org.entimoss.kuwaiba;

//import org.neotropic.kuwaiba.modules.optional.reports.defaults.RawReport;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class OpenNMSExport {
   
  void returnReport(){
   
   String title = "OpenNMSExport";
   String version = "0.1";
   String author = "Craig Gallen";

   // main report functions
   StringBuffer  textBuffer = new StringBuffer();

   // create and populate header line
  // for (String columnName : OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS){
  //     text = text+columnName+",";
  // }
   
   Iterator<String> columnIterator = OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.iterator();
   while (columnIterator.hasNext()) {
      String columnName = columnIterator.next();
      textBuffer.append(columnName);
      if(columnIterator.hasNext()) textBuffer.append(",");
   }

   textBuffer.append("\n");


    
//   RawReport report = new RawReport(title, author, version, text);
    
//   return report;
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
            MGMTTYPE_, SVC_FORCED, CAT_, ASSET_REGION, ASSET_SERIALNUMBER, ASSET_ASSETNUMBER, ASSET_LATITUDE, ASSET_LONGITUDE, ASSET_THRESHOLDCATEGORY,
            ASSET_NOTIFYCATEGORY, ASSET_POLLERCATEGORY, ASSET_DISPLAYCATEGORY, ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE, ASSET_CIRCUITID,
            ASSET_DESCRIPTION);
   
   public static final String DEFAULT_MINION_LOCATION="Default"; // used when OpenNMS core is the poller.
   
   // These service names should match the service definitions in poller-configuration.xml
   public static final String SERVICE_PASSIVE_SECONDARY_NODE = "passive-secondary-node";
   public static final String SERVICE_PASSIVE_PRIMARY_NODE = "passive-primary-node";
   public static final String SERVICE_PASSIVE_NODE_UP_SERVICE = "passive-node-up-service";
   

}