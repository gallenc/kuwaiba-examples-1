package org.entimoss.misc.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This test class enhances the raw UPRN data with steee and address info from the Nominatum api. 
 * It takes some time to run and only interacts with the api every 5 seconds
 */

public class ExtractOSMDataFromUprn {
   static Logger LOG = LoggerFactory.getLogger(ExtractOSMDataFromUprn.class); // remove static in groovy

   String jsonOutputFileStr = "./target/output-json/uprnBitternePk1nominatum.json";

   String csvOutputFileStr = "./target/output-json/uprnBitternePk1nominatum.csv";

   /*
    * latitude longitude UPRN 
    * note UPRN may have leading ' to avoid interpretation as number
    */
   String csvFileName = "./src/test/resources/modelimportCsv/uprnBitternePk1.csv";

   // csv columns
   public String SEPARATOR = ",";
   public int LATITUDE_COLUMN = 0;
   public int LONGITUDE_COLUMN = 1;
   public int UPRN_COLUMN = 2;

  // @Test
   public void test1() throws StreamReadException, DatabindException, IOException {

      LOG.info("**** start of test1");

      int uprnNo = 1234567890;
      Double latitude = Double.valueOf("50.9246111");
      Double longitude = Double.valueOf("-1.3719191");

      // https://nominatim.openstreetmap.org/reverse?format=geojson&lat=50.9246111&lon=-1.3719191
      String url = "https://nominatim.openstreetmap.org/reverse?format=geojson"
               + "&lat=" + latitude
               + "&lon=" + longitude;

      LOG.warn("reading from url=" + url);

      URL jsonResponse = new URL(url);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(jsonResponse);

      // create line to put in csv file
      StringBuffer newLine = new StringBuffer().append(uprnNo).append(",").append(latitude).append(",").append(longitude);

      String house_number = "";
      String road = "";
      String display_name = "";

      try {
         JsonNode address = jsonNode.get("features").get(0).get("properties").get("address");
         house_number = address.get("house_number").asText();
         road = address.get("road").asText();
         display_name = jsonNode.get("features").get(0).get("properties").get("display_name").asText().replace(",", " ");
      } catch (Exception ex) {
         LOG.warn("cant read address", ex);
      }

      newLine.append(",").append(road).append(",").append(house_number).append(",").append(display_name);
      LOG.warn("line to output: " + newLine.toString());

      // create line to put in json file

      ObjectNode jsonLine = mapper.createObjectNode();

      jsonLine.put("UPRN", uprnNo);

      jsonLine.put("latitude", latitude);

      jsonLine.put("longitude", longitude);

      jsonLine.set("nominatumGeojson", jsonNode);

      String geoJsonLine = mapper.writeValueAsString(jsonLine);

      LOG.warn("writing nominatumGeojson: " + geoJsonLine);

      String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonLine);
      LOG.info("nominatumGeojson indented:\n" + indented);

      LOG.info("**** end of test1");

   }
   
   @Test
   public void test2(){
      LOG.info("**** start of test2");
      
      Integer lineLimit = null; // process all lines - slowly
      
      extractOSMData(lineLimit);
      
      LOG.info("**** end of test2");
      
   }

   public void extractOSMData(Integer lineLimit) {

      int lineCount = 0;
      int errorLineCount = 0;

      BufferedReader bufferedReader = null;
      PrintWriter jsonWriter = null;
      PrintWriter csvWriter = null;

      try {
         
         bufferedReader = new BufferedReader(new FileReader(csvFileName));
         
         File jsonOutputFile = new File(jsonOutputFileStr);
         jsonOutputFile.delete();
         
         jsonOutputFile.getParentFile().mkdirs();
         
         File csvOutputFile = new File(csvOutputFileStr);
         jsonOutputFile.delete();
         
         
         csvOutputFile.getParentFile().mkdirs();

         jsonWriter = new PrintWriter(jsonOutputFile);
         
         csvWriter = new PrintWriter(csvOutputFile);
         // write header line
         csvWriter.println("Asset_latitude,Asset_longitude,UPRN,road,houseNumber,fullAddress");

         String line;

         bufferedReader.readLine(); // skip header line

         // read input uprn csv file
         while ((line = bufferedReader.readLine()) != null) {

            // note you want to maintain the line count even if you cant read a line
            lineCount++;
            
            if(lineLimit !=null && lineCount > lineLimit) {
               LOG.warn("finishing because line limit reached lineCount="+lineCount);
               break;
            }

            List<String> csvColumns = Arrays.asList(line.split(SEPARATOR));
            if (csvColumns.size() != 3) { // All columns are mandatory, even if they're just empty
               String errormsg = String.format("Line %s does not have 3 columns as expected but %s", line, csvColumns.size());
               LOG.warn(errormsg);
            } else {

               try {

                  LOG.warn("processing line lineCount=" + lineCount + " (errorLineCount="+errorLineCount
                           + "), line=" + line);

                  // remove leading quote on uprn '
                  String uprn = csvColumns.get(UPRN_COLUMN).replaceFirst("'", "");

                  long uprnNo = Long.parseUnsignedLong(uprn);

                  Double latitude = Double.valueOf(csvColumns.get(LATITUDE_COLUMN));
                  Double longitude = Double.valueOf(csvColumns.get(LONGITUDE_COLUMN));

                  // create line to put in csv file
                  StringBuffer newLine = new StringBuffer().append(latitude).append(",").append(longitude).append(",").append(uprnNo);

                  String house_number = "";
                  String road = "";
                  String display_name = "";

                  // retry 3 times if server doesnt respond - 500 error
                  ObjectMapper mapper = new ObjectMapper();
                  JsonNode jsonNode = null;

                  // https://nominatim.openstreetmap.org/reverse?format=geojson&lat=50.9246111&lon=-1.3719191
                  String url = "https://nominatim.openstreetmap.org/reverse?format=geojson"
                           + "&lat=" + latitude
                           + "&lon=" + longitude;

                  LOG.warn("reading from url=" + url);

                  for (int tries = 1; tries < 4; tries++) {
                     try {
                        // rate limit speed of url rate
                        TimeUnit.SECONDS.sleep(5);
                        
                        URL jsonResponse = new URL(url);
                        
                        jsonNode = mapper.readTree(jsonResponse);
                        
                     } catch (Exception ex) {
                        LOG.error("try "+tries+ "failed reading url"+url, ex);
                     }
                     if (jsonNode != null) {
                        break;
                     }
                  }
                  
                  // try to append values to csv file
                  if (jsonNode!=null) {

                     try {
                        JsonNode address = jsonNode.get("features").get(0).get("properties").get("address");
                        house_number = (address.get("house_number")!=null) ? address.get("house_number").asText() : null ;
                        road = (address.get("road")!=null) ? address.get("road").asText() : null ;
                     } catch (Exception ex) {
                        LOG.warn("cant read address", ex);
                     }
                     
                     try {
                        display_name = jsonNode.get("features").get(0).get("properties").get("display_name").asText().replace(",", " ");
                     } catch (Exception ex) {
                        LOG.warn("cant read display_name", ex);
                     }

                  }

                  newLine.append(",").append(road).append(",").append(house_number).append(",").append(display_name);
                  LOG.warn("Writing line to file: " + newLine.toString());
                  csvWriter.println(newLine.toString());

                  // create line to put in json file
                  ObjectNode jsonLine = mapper.createObjectNode();

                  jsonLine.put("UPRN", uprnNo);

                  jsonLine.put("latitude", latitude);

                  jsonLine.put("longitude", longitude);

                  jsonLine.set("nominatumGeojson", jsonNode);

                  String geoJsonLine = mapper.writeValueAsString(jsonLine);

                  LOG.warn("writing nominatumGeojson: " + geoJsonLine);
                  jsonWriter.println(geoJsonLine);



               } catch (Exception ie) {
                  errorLineCount++;
                  String errormsg = String.format("Error processing line %s: %s", line, ie.getMessage());
                  LOG.warn(errormsg);
               }
            }
         }
      } catch (Exception ie) {
         // TODO Auto-generated catch block
         ie.printStackTrace();
      } finally {
         try {
            bufferedReader.close();
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         try {
            jsonWriter.close();
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         try {
            csvWriter.close();
         } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      
      LOG.warn("finished processing files lineCount=" + lineCount + " (errorLineCount="+errorLineCount+ ")");

   }

}
