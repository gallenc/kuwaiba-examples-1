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
package org.entimoss.misc.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.junit.Test;

public class ExtractFromUPRNGis {

   public static final String COMMA_DELIMITER = ",";


  
   /**
    * reads ordinance survey csv data in format
    * UPRN  X_COORDINATE   Y_COORDINATE   LATITUDE    LONGITUDE
    * 1,     358260.99,      172796.83,      51.4526038,  -2.6020703
    * 
    * and extracts smaller file bounded by a square of coordinates
    * 
    * @param inputGisFile  Input gis csv file to read
    * @param outputGisFile Output gis csv file to write
    * @param minLatitude   most south
    * @param maxLatitude   most north
    * @param minLongitude  most west
    * @param maxLongitude  most east
    */
   public int extractGIS(File inputGisFile, File outputGisFile, 
            double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
      
      System.out.println("Extracting GIS with  minLatitude "+minLatitude+ " maxLatitude "+maxLatitude+
               " minLongitude "+ minLongitude+" maxLongitude "+maxLongitude);
      
      int outputLineCount = 0;
      // set up output file and directory
      outputGisFile.delete();

      File parent =  outputGisFile.getParentFile();
      if (!parent.exists())
         parent.mkdirs();

      try (BufferedReader inGisReader = new BufferedReader(new FileReader(inputGisFile));
               PrintWriter outGisReader = new PrintWriter(outputGisFile)) {

         //write header line
         String line = inGisReader.readLine();
         outGisReader.println(line);
         
         while ((line = inGisReader.readLine()) != null) {
            String[] values = line.split(COMMA_DELIMITER);
            if (values.length != 5) {
               System.out.println("incorrectly formatted line: " + line);
               continue;
            }

            try {
               Double latitude = Double.parseDouble(values[3]);
               Double longitude = Double.parseDouble(values[4]);
               //System.out.println("processing latitude "+latitude + " longitude "+longitude+" line: " + line);
               
               if (latitude > maxLatitude) {
                  //System.out.println("ignoring latitude ("+latitude + ") > maxLatitude ("+maxLatitude+") line: " + line);
                  continue;
               }
               if (latitude < minLatitude) {
                  //System.out.println("ignoring latitude ("+latitude + ") < minLatitude ("+minLatitude+") line: " + line);
                  continue;
               }


               if (longitude > maxLongitude ) {
                  //System.out.println("ignoring longitude ("+longitude + ") > maxLongitude("+maxLongitude+") line: " + line);
                  continue;
               }
               if (longitude < minLongitude) {
                 // System.out.println("ignoring longitude ("+longitude + ") < minLongitude("+minLongitude+") line: " + line);
                  continue;
               }
               
            } catch (Exception ex) {
               System.out.println("incorrectly formatted line: '" + line + "' exception: " + ex.getMessage());
               continue;
            }
            
            outGisReader.println(line);
            outputLineCount++;
         }
         
         outGisReader.close();
         
      } catch (Exception e) {
         throw new RuntimeException("problem exporting file: ", e);
      }
      
      return outputLineCount;

   }
   
   /**
    * test values in file
    * UPRN,X_COORDINATE,Y_COORDINATE,LATITUDE,LONGITUDE
    * 1,358260.99,172796.83,51.4526038,-2.6020703
    * 26,352967.00,181077.00,51.5266333,-2.6793612
    * 27,352967.00,181077.00,51.5266333,-2.6793612
    * 30,354800.00,180469.00,51.5213173,-2.6528615
    * 31,354796.00,180460.00,51.5212360,-2.6529180
    */

   @Test
   public void test1() {
      File inputGisFile  = new File("./src/test/resources/csvFiles/test1In.csv");
      File outputGisFile = new File("./target/outputCsv/test1Out.csv");
      
      System.out.println("test1: inputGisFile "+inputGisFile.getAbsolutePath());
      
      double minLatitude = Double.parseDouble("51"); //most south
      double maxLatitude = Double.parseDouble("52"); // most north
      double maxLongitude = Double.parseDouble("-2"); //most east
      double minLongitude = Double.parseDouble("-3"); //most west
      
      int count = extractGIS(inputGisFile, outputGisFile, minLatitude, maxLatitude, minLongitude, maxLongitude);
      System.out.println("test1: "+count+" lines written to  outputGisFile:"+outputGisFile.getAbsolutePath());
      assertEquals(4,count);
   }
   
   @Test
   public void test2() {
      File inputGisFile  = new File("./src/test/resources/csvFiles/test1In.csv");
      File outputGisFile = new File("./target/outputCsv/test1Out.csv");
      
      System.out.println("test2: inputGisFile "+inputGisFile.getAbsolutePath());
      
      double minLatitude = Double.parseDouble("51.5266333"); //most south
      double maxLatitude = Double.parseDouble("52"); // most north
      double maxLongitude = Double.parseDouble("-2.6793612"); //most east
      double minLongitude = Double.parseDouble("-3"); //most west
      
      int count = extractGIS(inputGisFile, outputGisFile, minLatitude, maxLatitude, minLongitude, maxLongitude);
      
      System.out.println("test2: "+count+" lines written to  outputGisFile:"+outputGisFile.getAbsolutePath());
      assertEquals(2,count);
   }
   
   @Test
   public void testRealFile() {
      File inputGisFile  = new File("./src/test/resources/osCsvFiles/osopenuprn_202506.csv");
      File outputGisFile = new File("./target/outputCsv/reducedOs.csv");
      
      System.out.println("testRealFile: inputGisFile "+inputGisFile.getAbsolutePath());
      
      // coordinates bitterne park
      // 50.934728, -1.380382
      // 50.920958, -1.368434
      double minLatitude = Double.parseDouble("50.920958"); // most south
      double maxLatitude = Double.parseDouble("50.934728"); // most north
      double maxLongitude = Double.parseDouble("-1.368434"); //most east
      double minLongitude = Double.parseDouble("-1.380382"); //most west
      
      // coordinates southampton
      // 50.958009, -1.437225 Chilworth Civil Parish
      // 50.892396, -1.391445 Ocean Village, Southampton
      
//      double minLatitude = Double.parseDouble("50.892396"); // most south
//      double maxLatitude = Double.parseDouble("50.958009"); // most north
//      double maxLongitude = Double.parseDouble("-1.391445"); //most east
//      double minLongitude = Double.parseDouble("-1.437225"); //most west
      
      int count = extractGIS(inputGisFile, outputGisFile, minLatitude, maxLatitude, minLongitude, maxLongitude);
      
      System.out.println("real Output: "+count+" lines written to  outputGisFile:"+outputGisFile.getAbsolutePath());
     
   }

}
