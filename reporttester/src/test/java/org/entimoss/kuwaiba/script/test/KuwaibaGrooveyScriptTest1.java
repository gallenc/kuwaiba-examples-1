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

public class KuwaibaGrooveyScriptTest1 extends  GrooveyScriptTestBase {

   @Test
   public void test1() throws IOException, InvalidArgumentException {
      System.out.println("running test1");
      
      String testFile="./GrooveyScripts/OpenNMSReport.groovy";
      
      System.out.println("loading test script from: "+testFile);
      String reportgroovyscript = getResourceFileAsString(testFile);
      
      List<StringPair> parameters = Arrays.asList(); 
      BusinessEntityManager bem = null; 
      MetadataEntityManager mem = null; 
      ApplicationEntityManager aem = null; 
      
      byte[] bytes = executeReport(reportgroovyscript ,  parameters, bem,  mem,  aem);
      
      String str = new String(bytes, StandardCharsets.UTF_8);
      
      System.out.println("Result of script execution\n"+str);
      
      System.out.println("end of test1");
      
   }
   

}
  