# Kuwaiba Simple Example

This is a basic stand alone docker compose project to allow you to experiment with Kuwaiba and store a backup of your
database externally within in your docker compose project.

This means that a complete working Kuwaiba network model and report scripts can be included in each project for demonstration purposes.
Different projects can maintain separate models which makes it easy to create multiple demonstrators which are easy to run for testing and demonstration purposes.

If no data is provided, the container uses the default data within the `/data` folder of the container and copies this into the mounted `data-kuwaiba` volume.

If data is provided, on first start, the default `/data` is deleted and the user provided data is copied into the `/data` folder

On first start any data in `container-fs/kuwaiba/data-overlay` is then copied and overlayed on the `/data` folder

In this model, the `container-fs/kuwaiba/data-zip/data.zip` folder is simply a copy of the /data folder within the container.

## Detailed description
This docker compose project contains a kuwaiba test project which uses the standard [kuwaiba 2.1.1 container](https://hub.docker.com/r/neotropic/kuwaiba).

The docker compose project extends the container so that it can automatically import an external model from host folders or a zip file. The project contains a `Dockerfile` which extends the standard Kuwaiba container to include some extra utilities and folders.

The structure of the project is as follows

```
container-fs
    kuwaiba
        data-overlay    # overlay data which is copied over the container /data on first start.
        data-zip
            .gitignore  # set to allow .zip files check into in git
            data.zip    # zip file containing a model which replaces the default /data on first start.
    Dockerfile          # builds on top of neotropic/kuwaiba:v2.1-nightly. Adds Utilities and empty folders.
    start.sh            # replacement start script which allows external data overlays.
    docker-compose.yaml # docker compose project file which controls the project
    
```

A new start.sh script is provided which docker compose uses to pre-load any user provided data before starting the container.
The `start.sh` script follows these steps to configure the container:

1. Build the extended container if it has not already been built in the  local docker repo.
2. Mount `container-fs/kuwaiba/data-overlay` into the container root `/data-overlay` and `container-fs/kuwaiba/data-zip` into the container root `/data-zip`
3. The kuwaiba `./data` folder is mounted in a docker volume which will persist between docker restarts provided it is not deleted. Docker automatically copies the container provided /data folder into the volume.
4. On first start,  `start.sh` checks if data has already been copied by looking for the file `/data/configured`
5. If the container has been configured, kuwaiba is started normally with the data already in the volume.
6. If the container has not been configured, the script checks for contents in `/data-zip/data.zip`. If there is no data.zip, the container starts using the default /data provided in the container.
6. If the data.zip file is present, the default container `/data` is deleted and replaced with the zip. 
7. On first start, any additional files in `/data-overlay` are copied and overlayed on the`/data` folder

## Running the project
To run the project, you should have Docker installed on your system.
([Docker Desktop](https://docs.docker.com/desktop/) on a PC).

Check out the project using git and cd to the `kuwaiba-simple` project.

Commands (in power shell or a terminal window) when docker is running:

```
cd kuwaiba-simple

# build the extended Kuwaiba container (only needed before first run - and may automatically happen anyway)
docker compose build

# start Kuwaiba as a service daemon
docker compose up -d

# to see logs
docker compose logs -f kuwaiba

# to shut down
docker compose down
```
After a short time, kuwaiba will be available at http://localhost:8080/kuwaiba

The new kawaiba model will be imported from `container-fs/kuwaiba/data-zip/data.zip` on the first run.
(If data.zip is not present, the default kuwiba model from the container will be used).

Any changes to this model will be persisted to the docker `kuwaiba-data` volume across restarts.

You can clear the model back to the original data.zip by running

```
docker compose down -v
```

## Creating a zip of your own model.

As you extend your model, you may wish to save it under version control in your project,

You can export/backup any changes to your own model as data.zip from a running container by zipping the `/data` folder in a running kuwaiba container.

The following commands will do this for a running Kuwaiba project without logging into the container.

```
# creates a zip of the data inside the container without the log files
docker compose exec kuwaiba sh -c "rm -rf /tmp/kuwaiba && mkdir /tmp/kuwaiba && zip -rv  --exclude='/data/logs*' /tmp/kuwaiba/data.zip /data/* "

# copy the zip file out of the container to overwrite the current data.zip
docker compose cp kuwaiba:/tmp/kuwaiba/data.zip ./container-fs/kuwaiba/data-zip
```

If you then remove the data from your volume, using 

```
docker compose down -v
```
on next start, the new zip will be used to populate the container.
