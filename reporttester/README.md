# Local KUWAIBA Report Tester

This is a simple maven project to allow testing of Kuwaiba scripts.

The tester imports the kuwaiba persistence api and reports api jars and runs the script with these injected.
2.1.1 versions of these jars are included. 
If kuwaiba posted jars to maven, then we could download automatically.
 
The test 
You will need to create mock objects which return the expected response for searches to test a full script

In order to edit groovy scripts., you can import the Groovy Eclipse Plugin to eclipse https://github.com/groovy/groovy-eclipse
