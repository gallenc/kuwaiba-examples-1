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

package org.entimoss.kuwaiba.script.test;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GrooveyScriptTestBase {

   @Test
   public void baseTest() throws IOException, InvalidArgumentException {
      System.out.println("Running baseTest");
      
      String testFile="./GrooveyScripts/BaseTest.groovy";
      
      System.out.println("loading test script from: "+testFile);
      
      String reportgroovyscript = getResourceFileAsString(testFile);
      
      List<StringPair> parameters = Arrays.asList(); 
      BusinessEntityManager bem = null; 
      MetadataEntityManager mem = null; 
      ApplicationEntityManager aem = null; 
      
      byte[] bytes = executeReport(reportgroovyscript ,  parameters, bem,  mem,  aem);
      
      String str = new String(bytes, StandardCharsets.UTF_8);
      
      System.out.println("Result of script execution\n"+str);
      
      System.out.println("End of baseTest");
   }

   // adapted from org.neotropic.kuwaiba.core.persistence.reference.neo4j.BusinessEntityManagerImpl
   public byte[] executeReport(String reportgroovyscript , List<StringPair> parameters, 
            BusinessEntityManager bem, MetadataEntityManager mem, ApplicationEntityManager aem) throws InvalidArgumentException {
      
      try {
          
          HashMap<String, String> scriptParameters = new HashMap<>();
          for(StringPair parameter : parameters)
              scriptParameters.put(parameter.getKey(), parameter.getValue());
          
          Binding environmentParameters = new Binding();
          environmentParameters.setVariable("parameters", scriptParameters); 
          environmentParameters.setVariable("mem", mem); 
          environmentParameters.setVariable("aem", aem); 
          environmentParameters.setVariable("bem", bem); 
          
          try {
              GroovyShell shell = new GroovyShell(GrooveyScriptTestBase.class.getClassLoader(), environmentParameters);
              
              System.out.println("Parsing test script");
              shell.parse(reportgroovyscript);
              
              System.out.println("Script parsed successfully. Evaluating test script");
              Object theResult = shell.evaluate(reportgroovyscript);
              
              if (theResult == null)
                  throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
              else {
                  if (theResult instanceof InventoryReport) {
                      return ((InventoryReport)theResult).asByteArray();
                  } else throw new InvalidArgumentException("The script does not return an InventoryReport instance. Please check the return value.");
              }
          } catch(Exception ex) {
              throw new Exception("exception", ex);
          } 
          
      } catch(Exception ex) {
         ex.printStackTrace();
         return ("<html><head><title>Error</title></head><body><center>" + ex.getMessage() + "</center></body></html>").getBytes(StandardCharsets.UTF_8);
      }
  }

   /**
    * Reads given resource file as a string.
    *
    * @param fileName path to the resource file
    * @return the file's contents
    * @throws IOException if read fails for any reason
    */
   static String getResourceFileAsString(String fileName) throws IOException {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      try (InputStream is = classLoader.getResourceAsStream(fileName)) {
         if (is == null)
            return null;
         try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                  BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
         }
      }
   }

}
