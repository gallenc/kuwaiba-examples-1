# Kuwaiba Gpon Demonstration and Test project

[Main Menu](../README.md) | [Kuwaiba Gpon Demonstration](./README.md)

## Introduction

(Note: This project is still work in progress and subject to change).

This project contains a demonstration of modelling a network using Kuwaiba and exporting a network configuration from Kuwaiba to OpenNMS. 

* [Kuwaiba](https://www.kuwaiba.org/) is an open source Service Provider Inventory solution
* [OpenNMS](https://github.com/OpenNMS/opennms) is an open source enterprise grade network management platform.

## Brief Description
This docker compose project contains a Kuwaiba test project which uses the standard [kuwaiba 2.1.1 container](https://hub.docker.com/r/neotropic/kuwaiba).

The project demonstrates modelling a small GPON network.
It also contains a report for exporting a configuration in a CSV format which can be directly imported to OpenNMS
using the [provisioning integration server PRIS](https://docs.opennms.com/pris/2.1.0/index.html).

The docker compose project extends the Kuwaiba container so that it can automatically import an external model from a data.zip file contained in this project.
This means that a full working Kuwaiba network model and report scripts can be included in the project for demonstration purposes.
Different projects can maintain separate models which makes it easy to create multiple demonstrators which are easy to run for testing and demonstration purposes.

The project also contains an OpenNNS installation 

Nginx is used to provide a front end and home page for the simulation including buttons to control the import of models from OpenNMS.

## Running only Nginx, Kuwaiba and opennms-pris

The full demo uses a lot of memory so there is an option to only run the Kuwaiba and opennms-pris containers.
With no profile set only Kuwaiba and kuwaiba-pris containers will start.

To run the project, wou should have Docker installed on your system.
([Docker Desktop](https://docs.docker.com/desktop/) on a PC).

Check out the project using git and cd to the `kuwaiba-docker-compose-radio` project.

Commands (in power shell or terminal window) when docker is running:

```
cd kuwaiba-docker-compose-radio

# build the extended kuwaiba container (only needed before first run - and may automaticlaly happen anyway)
docker compose build

# start kuwaiba as a service
docker compose up -d

# to see kuwaiba logs
docker compose logs -f kuwaiba

# to shut down
docker compose down
```
After a short time Kuwaiba will be available through the Nginx proxy at 

[https://localhost/kuwaiba](https://localhost/kuwaiba)

or directly at

[http://localhost:8080/kuwaiba](http://localhost:8080/kuwaiba)

The new Kuwaiba model will be imported from `container-fs/kuwaiba/data-zip/data.zip` on the first run.
(If data.zip is not present, the default kuwiba model from the container will be used).

Any changes to this model will be persisted to the docker `kuwaiba-data` volume across restarts.

You can clear the model back to the original data.zip by running

```
docker compose down -v
```
## default credentials

The following default credentials are used in this demo. 
(They may be changed in a publicly hosted version)

| Component         |URL                         | Username | Password |
| :---------------- | :------------------------- |:-------- | :------- |
| OpenNMS           | https://localhost/opennms  | admin     | admin   |
| Grafana           | https://localhost/grafana  | admin     | mypass  |
| Kuwaiba           | https://localhost/kuwaiba  | admin     | kuwaiba |

## Running the complete simulation

The docker compose script has a profile to start OpenNMS and the simulated devices as well as Kuwaiba and PRIS.

```
docker compose  --profile opennms up -d
```
With the [opennms] profile set all the containers will start (kuwaiba, opennms-pris horizon (OpenNMS core), grafana, 4 minions and the simulated radio network).

![alt text](./images/docker-compose-start1.png "Figure docker-compose-start1.png")

Note the simulation will take a few minutes to start first time.

To follow progress use

```
docker compose  --profile opennms ps
```

When all of the containers are running and/or show `healthy`, the simulator is running.
Wait until all the minions become `healthy`

![alt text](./images/docker-compose-healthy1.png "Figure docker-compose-healthy1.png")

You can access the containers usign the nginx hosted front page at

[https://localhost/index.html](https://localhost/index.html)

![alt text](./images/nginx-index-page.png "Figure nginx-index-page.png")

This page gives links to all of the components behind the proxy and also buttons which make rest calls to load and synchronise requisitions into OpenNMS.

To ensure complete shutdown use

```
docker compose  --profile opennms down
```

You can look at OpenNMS logs using

```
docker compose --profile opennms logs -f opennms 
```

To clear the simulation including OpenNMS and Kuwaiba back to the initial state use

```
docker compose  --profile opennms down -v
```

## Creating a zip of your own Kuwaiba model.

You can export/backup any changes to your own model as data.zip from a running container by zipping the /data folder in a running Kuwaiba container.
The following commands will do this for a running project without logging into the container.

```
# creates a zip of the data inside the container
docker compose exec kuwaiba sh -c "rm -rf /tmp/kuwaiba && mkdir /tmp/kuwaiba && zip -rv  --exclude='/data/logs*' /tmp/kuwaiba/data.zip /data/* "

# copy the zip file out of the container to overwrite the current data.zip
docker compose cp kuwaiba:/tmp/kuwaiba/data.zip ./container-fs/kuwaiba/data-zip
```

