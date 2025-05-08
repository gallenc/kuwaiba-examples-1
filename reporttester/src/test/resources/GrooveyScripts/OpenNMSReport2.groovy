/**
 * Exports inventory as OpenNMS PRIS CSV format.
 * Entimoss Ltd - version 0.1
 * Parameters: None
 * Applies to: TBD
 */
 
import org.neotropic.kuwaiba.modules.optional.reports.defaults.RawReport;
import java.util.Arrays;
import java.util.List;

class Main {
   static void main(String[] args) {
      
  }
}

// main report function

String title = "OpenNMSExport";
String version = "0.1";
String author = "Craig Gallen";

// create CSV headerline
StringBuffer  textBuffer = new StringBuffer();
Iterator<String> columnIterator = OnmsRequisitionConstants.OPENNMS_REQUISITION_HEADERS.iterator();
while (columnIterator.hasNext()) {
      String columnName = columnIterator.next();
      textBuffer.append(columnName);
      if(columnIterator.hasNext()) textBuffer.append(",");
}

textBuffer.append("\n");

// now populate data lines


// return a RawReport containing csv
report = new RawReport(title, author, version, textBuffer.toString());
 
return report;
 
 


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
   
   //MetaData_requisition:primarysnmproutername 
   //public static final String METADATA_REQUISITION = "MetaData_requisition:primarysnmproutername";
      

   // this is same order as in csv header line
   public static final List<String> OPENNMS_REQUISITION_HEADERS = Arrays.asList(NODE_LABEL, ID_, MINION_LOCATION, PARENT_FOREIGN_ID, PARENT_FOREIGN_SOURCE, IP_MANAGEMENT,
            MGMTTYPE_, SVC_FORCED, CAT_, ASSET_CATEGORY ,ASSET_REGION, ASSET_SERIALNUMBER, ASSET_ASSETNUMBER, ASSET_LATITUDE, ASSET_LONGITUDE, ASSET_THRESHOLDCATEGORY,
            ASSET_NOTIFYCATEGORY, ASSET_POLLERCATEGORY, ASSET_DISPLAYCATEGORY, ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE, ASSET_CIRCUITID,
            ASSET_DESCRIPTION);
   
   public static final String DEFAULT_MINION_LOCATION="Default"; // used when OpenNMS core is the poller.
   

}