package org.entimoss.kuwaiba.rest.test;

import static org.junit.Assert.*;

import java.io.IOException;
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

import org.apache.groovy.util.Maps;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * this test uses the standard java HTTP client to make rest requests
 */
public class ReportRetreivalTest {

   public static String KUWAIBA_URL = "http://localhost:8080/kuwaiba";
   public static String USERNAME = "admin";
   public static String PASSWORD = "kuwaiba";
   public static String REPORTNAME = "OpenNMSInventoryExport";

   String token = null;

   public String getToken(String kuwaibaUrl, String username, String password) {

      // calls org.neotropic.kuwaiba.northbound.rest.aem.SessionRestController.createSession

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

   public String getReportId(String kuwaibaUrl, String token, String reportName) {
      // http://localhost:8080/kuwaiba/v2.1.1/reports/getInventoryLevelReports/true/13039085A6B8DA8670AF1C688F4FB62005A5
      // calls org.neotropic.kuwaiba.northbound.rest.bem.ReportRestController.getInventoryLevelReports
      
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

         // is this a array?
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

   public String executeReport(String kuwaibaUrl, String token, String reportId, Map<String, String> parameters) {
      // http://localhost:8080/kuwaiba/v2.1.1/reports/executeInventoryLevelReport/10367/13031CC2E9A82F9BB096CD167B3C8D43970E

      // calls org.neotropic.kuwaiba.northbound.rest.bem.ReportRestController.executeInventoryLevelReport
      
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

   @Test
   public void test() {
      // log in and get session token

      if (token == null) {
         token = getToken(KUWAIBA_URL, USERNAME, PASSWORD);
      }

      System.out.println("token: " + token);

      // get report id for reportname from list of reports
      
      String reportId = getReportId(KUWAIBA_URL, token, REPORTNAME);

      System.out.println("reportId: " + reportId);

      Map<String, String> parameters = Maps.of("firstkey","firstvalue","secondkey","secondvalue");
      // execute report by id and print out result
      String csv = executeReport(KUWAIBA_URL, token, reportId, parameters);

      System.out.println("csv:\n" + csv);

   }
   
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

}
