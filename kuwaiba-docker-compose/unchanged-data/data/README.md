# overlay configuration from kuwiba 2.1

to extract full overlay config by logging into container

```
docker compose exec kuwaiba sh
rm -rf /tmp/kuwaiba
mkdir /tmp/kuwaiba
zip -rv /tmp/kuwaiba/data.zip /data/*  -x /data/logs**

exit


docker compose cp kuwaiba:/tmp/kuwaiba/data.zip ./container-fs/kuwaiba/data-zip

```

or backup as single line without logging in to the container

```
docker compose exec kuwaiba sh -c "rm -rf /tmp/kuwaiba && mkdir /tmp/kuwaiba && zip -rv  --exclude='/data/logs*' /tmp/kuwaiba/data.zip /data/* "

docker compose cp kuwaiba:/tmp/kuwaiba/data.zip ./container-fs/kuwaiba/data-zip
```