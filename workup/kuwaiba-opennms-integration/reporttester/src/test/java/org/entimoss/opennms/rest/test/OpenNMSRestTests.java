/*
 *  Copyright 2025 Entimoss Ltd (craig.gallen@entimoss.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.entimoss.opennms.rest.test;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.Serializable;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
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

public class OpenNMSRestTests {
   
   //Note see rest queries https://github.com/OpenNMS/opennms/blob/develop/opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/README.adoc
   

   
   public static String OPENNMS_URL = "http://localhost:8980/opennms";
   public static String USERNAME = "admin";
   public static String PASSWORD = "admin";

   // https://localhost/opennms/api/v2/nodes?count=10&offset=10

   public String getNodes(String openNMSUrl, String username, String password, int count, int offset) {
      
      String requestUrl = openNMSUrl + "/api/v2/nodes?count="+count+"&offset="+offset;
      
      System.out.println("requuestUrl: "+requestUrl);

      String reportId = "";

      // create session
      
      HttpClient client = HttpClient.newBuilder()
               .authenticator(new Authenticator() {
                   @Override
                   protected PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(username, password.toCharArray());
                   }
               })
               .build();
      
      HttpRequest request = HttpRequest.newBuilder()
               .timeout(Duration.ofSeconds(10))
               .uri(URI.create(requestUrl))
               .header("Content-Type", "application/json")
               .GET()
               .build();

      try {
         HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
         
         //System.out.println(response.toString());

         //System.out.println(response.body());

         ObjectMapper mapper = new ObjectMapper();

         // read the json strings and convert it into JsonNode
         JsonNode jsonNode = mapper.readTree(response.body());
         
         String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
         System.out.println("json response: \n"+indented);
         
         
         // is this an array?
//         if (jsonNode.isArray()) {
//            // yes, loop the JsonNode and display one by one
//            for (JsonNode node : jsonNode) {
//               String name = node.get("name").asText();
//               if (reportName.equals(name)) {
//                  reportId = node.get("id").asText();
//                  break;
//               }
//            }
//         }

      } catch (Exception e) {
         e.printStackTrace();
      }
      return reportId;
   }
   
   // /api/v2/ipinterfaces?_s=node.label==onms-prd-01
   public String getIPInterfaces(String foreignSource, String foreignId, String openNMSUrl, String username, String password, int count, int offset) {
       
       String requestUrl = openNMSUrl + "/api/v2/ipinterfaces/?_s=node.foreignSource=="+foreignSource+";node.foreignId=="+foreignId; //+"&count="+count+"&offset="+offset;
       
       System.out.println("requuestUrl: "+requestUrl);

       String reportId = "";

       // create session
       
       HttpClient client = HttpClient.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .build();
       
       HttpRequest request = HttpRequest.newBuilder()
                .timeout(Duration.ofSeconds(10))
                .uri(URI.create(requestUrl))
                .header("Content-Type", "application/json")
                .GET()
                .build();

       try {
          HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
          
          //System.out.println(response.toString());

          //System.out.println(response.body());

          ObjectMapper mapper = new ObjectMapper();

          // read the json strings and convert it into JsonNode
          JsonNode jsonNode = mapper.readTree(response.body());
          
          String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
          System.out.println("json response: \n"+indented);
          
          
          // is this an array?
//          if (jsonNode.isArray()) {
//             // yes, loop the JsonNode and display one by one
//             for (JsonNode node : jsonNode) {
//                String name = node.get("name").asText();
//                if (reportName.equals(name)) {
//                   reportId = node.get("id").asText();
//                   break;
//                }
//             }
//          }

       } catch (Exception e) {
          e.printStackTrace();
       }
       return reportId;
    }
    
   
   // http://127.0.0.1:8980/opennms/api/v2/enlinkd/1
  public String getNodeLinks(String node_criteria, String openNMSUrl, String username, String password, int count, int offset) {
      
      String requestUrl = openNMSUrl + "/api/v2/elinkd/"+node_criteria+"?count="+count+"&offset="+offset;
      
      System.out.println("requuestUrl: "+requestUrl);

      String reportId = "";

      // create session
      
      HttpClient client = HttpClient.newBuilder()
               .authenticator(new Authenticator() {
                   @Override
                   protected PasswordAuthentication getPasswordAuthentication() {
                       return new PasswordAuthentication(username, password.toCharArray());
                   }
               })
               .build();
      
      HttpRequest request = HttpRequest.newBuilder()
               .timeout(Duration.ofSeconds(10))
               .uri(URI.create(requestUrl))
               .header("Content-Type", "application/json")
               .GET()
               .build();

      try {
         HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
         
         //System.out.println(response.toString());

         //System.out.println(response.body());

         ObjectMapper mapper = new ObjectMapper();

         // read the json strings and convert it into JsonNode
         JsonNode jsonNode = mapper.readTree(response.body());
         
         String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
         System.out.println("json response: \n"+indented);
         
         
         // is this an array?
//         if (jsonNode.isArray()) {
//            // yes, loop the JsonNode and display one by one
//            for (JsonNode node : jsonNode) {
//               String name = node.get("name").asText();
//               if (reportName.equals(name)) {
//                  reportId = node.get("id").asText();
//                  break;
//               }
//            }
//         }

      } catch (Exception e) {
         e.printStackTrace();
      }
      return reportId;
   }
  
  // /api/v2/snmpinterfaces
  // 
  // http://localhost:8980/opennms/api/v2/snmpinterfaces/?_s=node.id==38 
  public String getSnmpInterfaces(String nodeId, String openNMSUrl, String username, String password, int count, int offset) {
     
     String requestUrl = openNMSUrl + "/api/v2/snmpinterfaces/?_s=node.id=="+nodeId; //";node.foreignId=="+foreignId; //+"&count="+count+"&offset="+offset;
     
     System.out.println("requuestUrl: "+requestUrl);

     String reportId = "";

     // create session
     
     HttpClient client = HttpClient.newBuilder()
              .authenticator(new Authenticator() {
                  @Override
                  protected PasswordAuthentication getPasswordAuthentication() {
                      return new PasswordAuthentication(username, password.toCharArray());
                  }
              })
              .build();
     
     HttpRequest request = HttpRequest.newBuilder()
              .timeout(Duration.ofSeconds(10))
              .uri(URI.create(requestUrl))
              .header("Content-Type", "application/json")
              .GET()
              .build();

     try {
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        
        //System.out.println(response.toString());

        //System.out.println(response.body());

        ObjectMapper mapper = new ObjectMapper();

        // read the json strings and convert it into JsonNode
        JsonNode jsonNode = mapper.readTree(response.body());
        
        String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        System.out.println("json response: \n"+indented);
        
        
        // is this an array?
//        if (jsonNode.isArray()) {
//           // yes, loop the JsonNode and display one by one
//           for (JsonNode node : jsonNode) {
//              String name = node.get("name").asText();
//              if (reportName.equals(name)) {
//                 reportId = node.get("id").asText();
//                 break;
//              }
//           }
//        }

     } catch (Exception e) {
        e.printStackTrace();
     }
     return reportId;
  }
  


  @Test
  public void testgetNodes() {

     System.out.println("************ TestgetNodes *************");
     
     getNodes(OPENNMS_URL, USERNAME, PASSWORD, 2, 40);
    
  }
   
   @Test
   public void testgetIPInterfaces() {
      
      System.out.println("************ TestgetIPInterfaces *************");
      
      //getNodeLinks("1",OPENNMS_URL, USERNAME, PASSWORD, 10, 0);
      
      
      //String foreignSource="kuwaiba-UK";
      //String foreignId="c5f4e71c-9505-4fd4-90fe-4768d5c019a9";
      
      String foreignSource="selfmonitor";
      String foreignId="1";
      
      getIPInterfaces(foreignSource, foreignId, OPENNMS_URL, USERNAME, PASSWORD, 10, 0);
      
      //
   }
   
   @Test
   public void testgetSnmpInterfaces() {
      
      System.out.println("************ TestgetSnmpInterfaces *************");
      
      // foreignSource="selfmonitor";
      //String foreignId="1";
      String nodeId="38";
      getSnmpInterfaces(nodeId, OPENNMS_URL, USERNAME, PASSWORD, 10, 0);
      
      //
   }

}
