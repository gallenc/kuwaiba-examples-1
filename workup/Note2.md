# notes 2

bulk upload tutorial

https://kuwaiba.org/docs/tutorials/operation/bulk-upload/index.html

task manager https://kuwaiba.org/docs/manuals/user/administration/taskman/

bulk upload script https://sourceforge.net/p/kuwaiba/code/HEAD/tree/server/trunk/scripts/tasks/TM_generic_bulk_import_example.groovy
basic hardware import https://sourceforge.net/p/kuwaiba/code/HEAD/tree/server/trunk/scripts/tasks/TM_basic_hardware_import_u2000.groovy

container lab examples https://github.com/h4ndzdatm0ld/containerlabs/tree/main


https://blog.sflow.com/2023/08/containerlab-dashboard.html

https://github.com/sflow-rt/containerlab



## not possible to access connectionBuilder service from task script

physical connections built from NewPhysicalConnectionWizard  using PhysicalConnectionService

```
NewPhysicalConnectionWizard wizard = new NewPhysicalConnectionWizard((BusinessObjectLight) nodeSideA.getIdentifier(),
                (BusinessObjectLight) nodeSideB.getIdentifier(), bem, aem, mem, physicalConnectionsService, resourceFactory, ts, log);
```

want to use org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionService but not accessible from script

task executor is in  org.neotropic.kuwaiba.core.persistence.reference.neo4j.ApplicationEntityManagerImpl implements ApplicationEntityManager

public TaskResult executeTask(long taskId)  injects services but not the physical connection service.

tried but didnt work using fields in script to try and access the service from spring within the groovy file. Application context is not available.
```
   public static void getField() {
      // this is a work around to access the connection manager service within the script
      // see https://stackoverflow.com/questions/822648/accessing-private-instance-variables-of-parent-from-child-class
      // see https://jenkov.com/tutorials/java-reflection/private-fields-and-methods.html
      // extends org.neotropic.kuwaiba.web.Application

      //import org.neotropic.kuwaiba.northbound.ws.KuwaibaSoapWebServiceImpl;
      //import org.neotropic.kuwaiba.northbound.ws.KuwaibaSoapWebService;
      //physicalConnectionsService

      try {


         // static class
         org.neotropic.kuwaiba.web.Application.Bootstrap bootstrap = new org.neotropic.kuwaiba.web.Application.Bootstrap();

         Field privatedbPathField = org.neotropic.kuwaiba.web.Application.Bootstrap.class.getDeclaredField("dbPath");
         privatedbPathField.setAccessible(true);

         String fieldValue = (String) privatedbPathField.get(bootstrap);

         System.out.println("dbPath fieldValue = " + fieldValue);

      } catch (Exception ex) {
         LOG.error("problem accesing field", ex);
      }
      


   }
```