// runs import from kuwiba rest api

import org.opennms.pris.model.*;
import org.opennms.pris.api.Source;
import org.opennms.pris.config.InstanceApacheConfiguration;
import org.opennms.opennms.pris.plugins.xls.source.XlsSource;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.Serializable;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


Requisition requisition = new Requisition();
requisition.setForeignSource(instance);

logger.info("importing from kuwaiba")

// setting up internal location to place the received csv file
String outputCsvRequisitonFileLocation = config.getString("outputCsvRequisitonFileLocation");

// setting up basic kuwaiba credentials
String kuwaibaUrl = config.getString("kuwaiba.URL");
String kuwaibaUsername = config.getString("kuwaiba.Username");
String kuwaibaPassword = config.getString("kuwaiba.Password");

logger.info("kuwaiba.URL="+kuwaibaUrl);
logger.info("kuwaiba.Username="+kuwaibaUsername);

HashMap<String,String> parameters = new HashMap<String,String>();
for (String key : config.getKeys() ){
   if (key.contains("parameters.")) {
      String parameterKey = key.substring(key.lastIndexOf("parameters.")).replace("parameters.","");
      parameters.put(parameterKey,config.getString(key));
   }
}

logger.info("parameters:" + parameters.toString());

// this code exercises the kuwaiba report to get the csv file
if (token == null) {
   token = getToken(kuwaibaUrl, kuwaibaUsername, kuwaibaPassword);
}

logger.info("token: " + token);

// get report id for reportname from list of reports

String reportId = getReportId(KUWAIBA_URL, token, REPORTNAME);

logger.info("reportId: " + reportId);

// execute report by id and print out result
String csv = executeReport(kuwaibaUrl, token, reportId, parameters);
logger.info("csv:\n" + csv);

String requisitionInstance=config.getString("requisitionInstance");
Path basePath = Paths.get(outputCsvRequisitonFileLocation+"/"+requisitionInstance);

// write csv file to tmp directory
File csvFile = new File(basePath.toFile(), "/"+requisitionInstance+".csv");
exportFile(csvFile, csv);

// write pris properties file
File propertiesFile = new File(basePath.toFile(), "requisition.properties");
logger.info("writing pris properties file " + propertiesFile.getAbsolutePath() + " for requisition " + requisitionInstance);
exportPrisPropertiesFile(propertiesFile, requisitionInstance);

// this code calls csv mapper to return requisition with foreign source requisitionInstance for generated csv file
logger.info("convert csv file to requisition");
InstanceApacheConfiguration xlsConfig= new InstanceApacheConfiguration(basePath,requisitionInstance);

for(String key: xlsConfig.getKeys()) {
  logger.info("configurationKey: "+key + " configurationvalue: "+xlsConfig.getString(key));
}

Source xlsSource = new XlsSource(xlsConfig);

// need to explicitly set the csv file
xlsSource.setXlsFile(new File(outputCsvRequisitonFileLocation+"/"+requisitionInstance+"/"+requisitionInstance+".csv"));

requisition = (Requisition) xlsSource.dump();

return requisition;

/**
 * writes a given string contents to a new file
 */
public void exportFile(File file, String contents) {

   PrintWriter printWriter = null;
   try {
      file.delete();

      File parent = file.getParentFile();
      if (!parent.exists())
         parent.mkdirs();

      printWriter = new PrintWriter(new FileWriter(file));

      printWriter.println(contents);

   } catch (Exception ex) {
      ex.printStackTrace();
   } finally {
      if (printWriter != null)
         printWriter.close();
   }

}

/**
 * Writes a properties file to load the csv requisition
 */
public void exportPrisPropertiesFile(File propertiesFile, String requisitionFileName) {
   // note in groovy + must be at end of line
   String propertiesTemplate = ""+ 
            "# This example imports devices from a csv file\n" + 
            "# Path to the csv file is relative to\n" + 
            "# requisition.properties\n" + 
            "source = xls\n" + 
            "source.file = ./" + requisitionFileName + "\n" + 
            "\n" + 
            "# default no-operation mapper\n" + 
            " mapper = echo\n";

   exportFile(propertiesFile, propertiesTemplate);

}

/**
 * gets token from kuwaiba
 * e.g. http://localhost:8080/kuwaiba/v2.1.1/session-manager/createSession/admin/kuwaiba/2
 * calls org.neotropic.kuwaiba.northbound.rest.aem.SessionRestController.createSession
 * @param kuwaibaUrl base url e.g http://localhost:8080/kuwaiba
 * @param username
 * @param password
 * @return string token for use in other calls
 */
public String getToken(String kuwaibaUrl, String username, String password) {

   String requestUrl = kuwaibaUrl + "/v2.1.1/session-manager/createSession/" + username + "/" + password + "/2";

   String token = null;

   // create session
   HttpClient client = HttpClient.newHttpClient();
   HttpRequest request = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(10))
            .uri(URI.create(requestUrl))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.noBody())
            .build();

   try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println(response.toString());

      System.out.println(response.body());

      ObjectMapper mapper = new ObjectMapper();

      // read the json strings and convert it into JsonNode
      JsonNode jsonNode = mapper.readTree(response.body());
      token = jsonNode.get("token").asText();

      System.out.println("token: " + token);

   } catch  (Exception e) {
      token = null;
      e.printStackTrace();
   }
   
   return token;
}

/**
 * retrieves the report id for a report with a given name from the list of reports in kuwaiba
 * e.g http://localhost:8080/kuwaiba/v2.1.1/reports/getInventoryLevelReports/true/13039085A6B8DA8670AF1C688F4FB62005A5
   calls org.neotropic.kuwaiba.northbound.rest.bem.ReportRestController.getInventoryLevelReports
 * @param kuwaibaUrl base url e.g http://localhost:8080/kuwaiba
 * @param token the token previously retrieved
 * @param reportName  the name of the report to retrieve from report list
 * @return the report id or empty string if not found
 */
public String getReportId(String kuwaibaUrl, String token, String reportName) {
   
   if (token == null) {
      token = getToken(KUWAIBA_URL, USERNAME, PASSWORD);
   }

   String requestUrl = kuwaibaUrl + "/v2.1.1/reports/getInventoryLevelReports/true/" + token;

   String reportId = "";

   // create session
   HttpClient client = HttpClient.newHttpClient();
   HttpRequest request = HttpRequest.newBuilder()
            .timeout(Duration.ofSeconds(10))
            .uri(URI.create(requestUrl))
            .header("Content-Type", "application/json")
            .GET()
            .build();

   try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println(response.toString());

      System.out.println(response.body());

      ObjectMapper mapper = new ObjectMapper();

      // read the json strings and convert it into JsonNode
      JsonNode jsonNode = mapper.readTree(response.body());

      // is this an array?
      if (jsonNode.isArray()) {
         // yes, loop the JsonNode and display one by one
         for (JsonNode node : jsonNode) {
            String name = node.get("name").asText();
            if (reportName.equals(name)) {
               reportId = node.get("id").asText();
               break;
            }
         }
      }

   } catch (Exception e) {
      token = null;
      e.printStackTrace();
   }
   return reportId;
}

/**
 * executes a report in kuwaiba and returns the result
 * e.g. http://localhost:8080/kuwaiba/v2.1.1/reports/executeInventoryLevelReport/10367/13031CC2E9A82F9BB096CD167B3C8D43970E
 * calls org.neotropic.kuwaiba.northbound.rest.bem.ReportRestController.executeInventoryLevelReport
 *
 * @param kuwaibaUrl base url e.g http://localhost:8080/kuwaiba
 * @param token
 * @param reportId
 * @param parameters
 * @return the report output as a String
 */
public String executeReport(String kuwaibaUrl, String token, String reportId, Map<String, String> parameters) {

   if (token == null) {
      token = getToken(KUWAIBA_URL, USERNAME, PASSWORD);
   }

   String requestUrl = kuwaibaUrl + "/v2.1.1/reports/executeInventoryLevelReport/" + reportId + "/" + token;

   String csvResponse = "";

   String jsonString = "";
   try {
      
      // encode parameters using StringPair so that Deserialises correctly in kuwaiba
      if (parameters != null && !parameters.isEmpty()) {
         
         List<StringPair> splist = new ArrayList<StringPair>();
         
         for (Entry<String, String> entry : parameters.entrySet()) {
            splist.add(new StringPair(entry.getKey(),entry.getValue()));
         }

         ObjectMapper objectMapper = new ObjectMapper();
         jsonString = objectMapper.writeValueAsString(splist);
         System.out.println("body: " + jsonString);

      }
      // execute report
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
               .timeout(Duration.ofSeconds(10))
               .uri(URI.create(requestUrl))
               // .header("Content-Type", "application/x-www-form-urlencoded")
               .header("Content-Type", "application/json")
               .header("Accept", "application/json")
               .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
               .build();

      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      System.out.println(response.toString());

      csvResponse = response.body();

   } catch  (Exception e) {
      token = null;
      e.printStackTrace();
   }

   return csvResponse;
}

/**
 * Used to map string pairs to the kuwaiba parameters 
 */
class StringPair implements Serializable {
   
         private String key;
   
         private String value;
   
         public StringPair() {}
   
         public StringPair(String key, String value) {
             this.key = key;
             this.value = value;
         }
   
         public String getKey() {
             return key;
         }
   
         public void setKey(String key) {
             this.key = key;
         }
   
         public String getValue() {
             return value;
         }
   
         public void setValue(String value) {
             this.value = value;
         }
      }
