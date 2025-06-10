# Case Study - Broadcast Radio Management using OpenNMS and Kuwaiba

[Main Menu](../README.md) | [Radio Management Case Study](./README.md)

## Introduction

This project demonstrates a proof of concept of Broadcast Radio Management using Kuwaiba and OpenNMS.

This project has been created partly as a result of a number of student investigations at Solent University UK with additional work by Dr Craig Gallen Invocom Ltd and Alan Beech of Comtronnix Ltd. Additional support was provided by Charles Edward Bedon Cortazar
CTO of Neotropic SAS, Ryan Jeffery of Passionate About OSS and Ronny Trommer of OpenNMS Group.

## Overview of Broadcast Radio Management Requirements

The Office of Communications (OFCOM) has licensed a network of small community radio stations to operate across the UK as illustrated in map below.

![alt text](./images/communityRadioUKOfcom-small.png "Figure communityRadioUKOfcom-small.png")

[source OFCOM](https://www.ofcom.org.uk/siteassets/resources/documents/manage-your-licence/community-radio/community-map.pdf?v=331663)

Many of these small stations are operated independently by small local organisation but some larger companies operate collections of stations.
Community local radio typically operates on a very tight budget and in many cases the stations have expertise in studio equipment but not in radio frequency transmitters. 
This leads them to out-source the construction and maintenance of the radio broadcast transmitters to small independent engineering companies who specialise in provisioning low cost broadcast infrastructure provided by OEM broadcast equipment manufacturers.

The stations cannot afford to have any breaks in transmission as this can significantly affect market share, audience perception and advertising revenues. 
For this reason, most of the transmission chain is often duplicated so that in the event of a single equipment failure, the station can remain on air.

However it is also very important to be able to detect and react to faults before a problem escalates to a station outage.
Often service monitoring is done through fairly manual mechanisms including the stations listening to their own output, listeners reporting problems or simple emails being sent out by the faulty transmission equipment.
Clearly this is a bit disjointed and not entirely satisfactory. 
A cost effective monitoring system which can detect and escalate faults would be very desirable.

In this project we demonstrate how comprehensive monitoring solution could be constructed using OpenNMS and Grafana to monitor a variety of transmission stations and provide independent alerts and dashboards to each of the local radio contractors.

In addition we demonstrate how Kuwaiba could be used to store a network inventory which documents all of the equipment on multiple sites and the topology of relationships between this equipment and the transmission services.

We also show how the Kuwaiba model can be used to populate OpenNMS and provide an end to end design and provisioning solution for broadcast networks.

## A Typical Broadcast Site

The schematic below shows the design of a typical FM and DAB transmitter for a small radio station. 
The specific station and site have been anonymised.
The manufacturers of each broadcast function may change from station to station but the structure is fairly typical.

The power is distributed from the top of the rack using a managed distribution unit (iMDU).
This monitors power to all of the devices and allows remote turning on and off of each box. 

The equipment on each site is connected to a separate private network which is behind a NAT firewall and can only be accessed through a VPN.
The overall network has overlapping private address spaces for each station. 

The station is connected to the studio through an optical network backed up by a 5G connection. 
Two Draytek routers are designated as Primary and Secondary connections and present a single virtual address to the subnet which is the same no matter which primary or the secondary router is currently being used.

The Audio program input equipment controls what happens if the studio input fails.
The silence switcher can choose between re-broadcasting a DAB or FM signal received from another station off air or if this fails, playing out a pre-recorded MP3 program or announcement.

An off the shelf FM transmitter broadcasts the audio signal along side an injected RDS signal.

The DAB Transmitter chain is more complicated because it uses a set of open source software defined radio components to create the DAB multiplex before amplification through an RF amplifier.

Most of the equipment supports SNMP although the broadcast MIBs are proprietary to each manufacturer and SNMP Traps are not always supported. 

Some of the equipment also supports SYSLOG events although these have proven not to provide very useful messages.

The DAB transmitter chain uses a set of separate components which do not provide a unified management interface but do provide programming API's which could be used for monitoring. 

![alt text](./images/radiosite1.png "Figure radiosite1.png")

Images of this equipment in a rack along side their kuwaiba rack positions are shown below. 

| Equipment Rack             |  Kuwaiba Representation   |
:---------------------------:|:-------------------------:|
| ![alt text](./images/RackImage-anonymised-small.png "Figure RackImage-anonymised-small.png")  |  ![alt text](./images/Rack1-small.png "Figure Rack1.png") |

## Solution Requirements

The following are a minimal set of requirements to be investigated in the overall solution.

1. Useful service affecting alarms should be presented for any site having a problem. These should identify the services affected and urgency of action required.
2. Useful time series performance data should be stored from each site showing the ongoing performance and availability of the equipment.
3. An overview of the whole broadcast network should be shown for the radio network operating company.
4. A customer specific view should be available for each customer showing only their services and equipment.
5. Where possible the proprietary SNMP MIBs from each manufacturer should be supported.
6. Alternative management protocols such as SYSLOGs or proprietary API's should be considered if there is no more standard solution.
7. The management system should be able to monitor the broadcast equipment without permanent VPN's being in place between the NOC and each station.
8. The management solution should be deployable to the Cloud with remote access to each broadcast station.
8. The design and layout of the site should be stored in a suitable inventory model / CMDB solution
9. the overall IP address space should be managed by a central IPAM solution.
10. The inventory system should be able to provision the management system automatically with IP addresses, Equipment Naming and Service relationships as new equipment is added.
11. The inventory system should be able to track spares and quickly provision new sites with flow through provisioning to he management system.
12. The overall solution should allow for secure sign-on and TLS/SSH security by design.
13. Once installed, any remote equipment should be securely configurable and upgrade-able without human visits to site.

## Solution Overview

Not all of the above requirements have been fully addressed in the present proof of concept but a large proportion of them have been achieved with a recommended route to final delivery. 

The following sections describe how the requirements have been addressed in each component of the solution.

[Kuwaiba Radio Model](./kuwaibaRadioModel.md) Discussion of the Kuwaiba data model for this project

[Kuwaiba OpenNMS Integration](./opennmsKuwaibaIntegration.md) Discussion of the initial integration between OpenNMS and Kuwaiba.

[OpenNMS Radio Integration](./opennmsRadioModel.md) Discussion of how the radio equipment can be monitored by OpenNMS

[Grafana Radio Visualisation](./grafanaRadioModel.md) Discussion of how the network and service state can be visualised on Grafana


