# Kuwaiba Demonstration and Test project

Note: This project is work in progress and subject to change.

This project contains a demonstration of modelling a network using Kuwaiba and exporting a network configuration from Kuwaiba to OpenNMS. 
* [Kuwaiba](https://www.kuwaiba.org/) is an open source Service Provider Inventory solution
* [OpenNMS](https://github.com/OpenNMS/opennms) is an open source enterprise grade network manamgent platform.

# Brief Description
This docker compose project contains a kuwaiba test project which uses the standard [kuwaiba 2.1.1 container](https://hub.docker.com/r/neotropic/kuwaiba).

The project demonstrates modelling a small broadcast radio network.
It also contains a report for exporting a configuration in a CSV format which can be directly imported to OpenNMS
using the [provisioning integration server PRIS](https://docs.opennms.com/pris/2.1.0/index.html).

The docker compose project extends the container so that it can automatically import an external model from a data.zip file contained in this project.
This means that a full working kuwaiba network model and report scripts can be included in the project for demonstration purposes.
Different projects can maintain seperate models which makes it easy to create multiple demonstrators which are easy to run for testing and demonstration purposes.

# Running the project
To run the project, wou should have Docker installed on your system.
([Docker Desktop](https://docs.docker.com/desktop/) on a PC).

Check out the project using git and cd to the `kuwaiba-docker-compose` project.

Commands (in power shell or terminal window) when docker is running:

```
cd kuwaiba-docker-compose

# build the extended kuwaiba container (only needed before first run - and may automaticlaly happen anyway)
docker compose build

# start kuwaiba as a service
docker compose up -d

# to see logs
docker compose logs -f kuwaiba

# to shut down
docker compose down
```
After a short time, kuwaiba will be available at http://localhost:8080/kuwaiba

The new kawaiba model will be imported from `container-fs/kuwaiba/data-zip/data.zip` on the first run.
(If data.zip is not present, the default kuwiba model from the container will be used).

Any changes to this model will be persisted to the docker kuwaiba-data volume across restarts.


You can clear the model back to the original data.zip by running

```
docker compose down -v
```

# Creating a zip of your own model.

You can export/backup any changes to your own model as data.zip from a running container by zipping the /data folder in a running kuwaiba container.
The following commands will do this for a running project without logging into the container.

```
# creates a zip of the data inside the container
docker compose exec kuwaiba sh -c "rm -rf /tmp/kuwaiba && mkdir /tmp/kuwaiba && zip -rv  --exclude='/data/logs*' /tmp/kuwaiba/data.zip /data/* "

# copy the zip file out of the container to overwrite the current data.zip
docker compose cp kuwaiba:/tmp/kuwaiba/data.zip ./container-fs/kuwaiba/data-zip
```

# Model Contents
The Radio Network is modelled under country `United Kingdom`.

See the `OpenNMSInventoryExport` report under the `Reports` tab in `Inventory report`s. 

Running this report exports the model in CSV format for import to OpenNMS Pris.

# Report Tester
Report Tester is a maven project to help build and test groovy scripts against the kuwaiba api in an ide like eclipse.

# Pris

Provisioning integration server now uses the kuwaiba rest api to get the pris CSV report from kuwaiba.

Make a call to should show requisition file for all devices with IP address [http://localhost:8020/requisitions/kuwaiba-all](http://localhost:8020/requisitions/kuwaiba-all)


Make a call to should show requisition file for only devices in Hampshire with an IP address[http://localhost:8020/requisitions/kuwaiba-hampshire](http://localhost:8020/requisitions/kuwaiba-hampshire)

A test requisition which uses a local CSV file is provided using [http://localhost:8020/requisitions/testrequisition](http://localhost:8020/requisitions/testrequisition)


