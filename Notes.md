# Notes

docker

When you first start the container from an image data from the image will be copied to the volume that you mounted to that specific path, but only if that volume is empty.


## building kuwaiba 

code - subversion https://sourceforge.net/projects/kuwaiba/


This is an old build instruction 
https://www.kuwaiba.org/docs/dev/build/

Note -need to do this with maven 3.8.5
https://stackoverflow.com/questions/67001968/how-to-disable-maven-blocking-external-http-repositories

user management
https://kuwaiba.org/docs/manuals/user/
https://www.kuwaiba.org/docs/manuals/admin/



javadoc (no frames)
https://kuwaiba.org/docs/dev/javadoc/current/index.html


logging in

http://localhost:8080/kuwaiba/home  - UI 


wsdl web service

http://localhost:8081/kuwaiba/KuwaibaService

http://localhost:8081/kuwaiba/KuwaibaService?wsdl

see org.neotropic.kuwaiba.web.Application.java -- Endpoint started on different port 8081
```
Endpoint.publish(String.format("http://0.0.0.0:%s/kuwaiba/KuwaibaService", wsPort), ws);
```

org.neotropic.kuwaiba.northbound.ws.KuwaibaSoapWebService      definition interface 
org.neotropic.kuwaiba.northbound.ws.KuwaibaSoapWebServiceImpl  Implementation class


rest web service
http://localhost:8080/kuwaiba/v2.1.1/core/bem/getObject/{className}/{objectId}/{sessionId}  response unauthorised

uses <groupId>org.springdoc</groupId><artifactId>springdoc-openapi-ui</artifactId><version>1.6.13</version>

http://localhost:8080/kuwaiba/v3/api-docs   shows api

http://localhost:8080/kuwaiba/v3/api-docs.yaml shows api with yaml

http://localhost:8080/kuwaiba/swagger-ui.html  doesn't work but redirects to http://localhost:8080/kuwaiba/swagger-ui/index.html

videos https://www.youtube.com/channel/UCbZ59WvSyzR-__9adEPIWSQ   Kuwaiba Open Network Inventory

note we can use soapUI import soap using http://localhost:8080/kuwaiba/v3/api-docs.yaml
 