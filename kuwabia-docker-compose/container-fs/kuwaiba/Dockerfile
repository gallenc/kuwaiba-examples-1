FROM neotropic/kuwaiba:v2.1-nightly

USER root

#  install unzip rsync telnet nano (busybox-extras) and some other basic tools
RUN apk add --no-cache busybox-extras nano zip unzip rsync

RUN mkdir -p /data-tmp  && chown kuwaiba:kuwaiba /data-tmp 
RUN mkdir -p /data-zip  && chown kuwaiba:kuwaiba /data-zip
RUN mkdir -p /data-overlay  && chown kuwaiba:kuwaiba /data-overlay
RUN mkdir -p /data-old  && chown kuwaiba:kuwaiba /data-old

USER kuwaiba

