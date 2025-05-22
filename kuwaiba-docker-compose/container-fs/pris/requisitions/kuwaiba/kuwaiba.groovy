// runs import from kuwiba rest api

import org.opennms.pris.model.*;
import org.opennms.pris.api.Source;
import org.opennms.pris.config.InstanceApacheConfiguration;
import org.opennms.opennms.pris.plugins.xls.source.XlsSource;
import java.nio.file.Path;
import java.nio.file.Paths;


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

// this code calls csv mapper to return requisition with foreign source requisitionInstance for generated csv file

String requisitionInstance=config.getString("requisitionInstance");
Path basePath = Paths.get(outputCsvRequisitonFileLocation+"/"+requisitionInstance);

// write pris properties file
File propertiesFile = new File(basePath.toFile(), "requisition.properties");
logger.info("writing pris properties file " + propertiesFile.getAbsolutePath() + " for requisition " + requisitionInstance);
try {
exportPrisPropertiesFile(propertiesFile, requisitionInstance);
}catch (Exception e) {
   logger.info("problem ");
   e.printStackTrace();
}

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