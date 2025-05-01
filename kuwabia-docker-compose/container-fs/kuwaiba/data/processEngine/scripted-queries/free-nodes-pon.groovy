/**
 * Scripted query find physical nodes with free ports, inside of a radius in meters 
 * with center in the given latitude and longitude.
 *
 * Pool Name: procruntime.service-feasibility
 * Name: free-nodes-pon
 * Parameters: 
 *  latitude:Double, 
 *  longitude:Double, 
 *  radius:Double (meters)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
import org.neotropic.kuwaiba.core.apis.persistence.application.ScriptedQueryResult
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject

def latitude = scriptParameters.get("latitude")
def longitude = scriptParameters.get("longitude")
def radius = scriptParameters.get("radius")

def query = """
  MATCH (:classes{name:'GenericPhysicalNode'})<-[:EXTENDS*]-(class:classes)<-[:INSTANCE_OF]-(physicalNode:inventoryObjects)<-[:CHILD_OF*]-(physicalPort:inventoryObjects)-[:INSTANCE_OF]->(:classes)-[:EXTENDS*]->(:classes{name: 'GenericPhysicalPort'})
  WHERE NOT (physicalPort)-[:RELATED_TO_SPECIAL{name: 'endpointA'}]-(:inventoryObjects)
  AND NOT (physicalPort)-[:RELATED_TO_SPECIAL{name: 'endpointB'}]-(:inventoryObjects)
  RETURN DISTINCT physicalNode AS physicalNode, class.name AS class
"""

def queryResult = connectionHandler.execute(query)
def physicalNodes = new ArrayList()
while (queryResult.hasNext()) {
  def next = queryResult.next()
  def physicalNode = next.get("physicalNode")
  def businessObject = new BusinessObject(next.get("class"), physicalNode.getProperty("_uuid"), physicalNode.getProperty("name"))
  businessObject.setAttributes(['longitude': physicalNode.getProperty("longitude", null), 'latitude': physicalNode.getProperty("latitude", null)])
  physicalNodes.add(businessObject)
}
def parameterViewNodes = new ArrayList()

physicalNodes.each {
  if (it.getAttributes()['longitude'] != null && it.getAttributes()['latitude'] != null) {
    def viewNode = ['_uuid': it.getId(), 'class': it.getClassName(), 'name': it.getName(), 'longitude': it.getAttributes()['longitude'], 'latitude': it.getAttributes()['latitude']]
    parameterViewNodes.add(viewNode)
  }
}

query = """
  RETURN [
      viewNode IN \$viewNodes
      WHERE distance(point({longitude: viewNode.longitude, latitude: viewNode.latitude}), point({longitude: \$longitude, latitude: \$latitude})) <= \$radius
      | {
        class: viewNode.class,
        _uuid: viewNode._uuid,
        name: viewNode.name,
        distance: distance(point({longitude: viewNode.longitude, latitude: viewNode.latitude}), point({longitude: \$longitude, latitude: \$latitude})),
        latitude: viewNode.latitude,
        longitude: viewNode.longitude
        }
    ] AS result
"""
parameters = ['viewNodes': parameterViewNodes, 'longitude': longitude, 'latitude': latitude, 'radius': radius]
queryResult = connectionHandler.execute(query, parameters).columnAs("result")

def result = new ScriptedQueryResult(4)

while (queryResult.hasNext()) {
  def next = queryResult.next()
  next.each {
    result.addRow([ new BusinessObject(it['class'], it['_uuid'], it['name']), it['distance'], it['latitude'], it['longitude'] ])
  }
}
result
