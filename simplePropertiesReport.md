# simple repot to print out java system properties

```
/*
Simple report to show system properties
*/
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import org.neotropic.kuwaiba.modules.optional.reports.defaults.RawReport;

import java.util.Map;
import java.util.Properties;

StringBuffer textBuffer = new StringBuffer();

textBuffer.append("System properties\n\n");

Properties props = System.getProperties();
for(String key: props.keySet()){
    textBuffer.append(key+" = "+props.get(key)+"\n");
}

textBuffer.append("\n\nEnvironment properties\n\n");

Map envprops = System.getenv();
for(String key: envprops.keySet()){
    textBuffer.append(key+" = "+envprops.get(key)+"\n");
}

// Using simple html and not using HTML constrcts from kuwaiba
String html="<!DOCTYPE html>"+
"<html>\n"+
"<body>\n"+
"<textarea rows=\"100\" cols=\"80\">"+textBuffer.toString()+"</textarea>\n"+
"\n"+
"</body>\n"+
"</html>\n";

InventoryReport report = new RawReport("properties report", "Craig Gallen", "0.1", html);
return report;
```