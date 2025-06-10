# OpenNMS Radio Integration

[Main Menu](../README.md) | [Radio Management Case Study](./README.md) | [OpenNMS Radio Integration](../docs/opennmsRadioModel.md)

When collecting time series data from a device, OpenNMS first detects if the device supports SNMP and if yes, then detects the sysOid of the device and uses this to determine which particular measurements it should collect for that device.

Full details of the process for creating an OpenNMS device configuration for each device are not provided here. 

However the [radio-mibs](../radio-mibs) folder contains the documentation, manufacturers MIBs, workup and final configuration for each device.
