#!/bin/sh

# -------------------------------------------------------------
# startup script for kuwaiba which copies data in from overlay
# -------------------------------------------------------------

set -e # exit script on error
set -x # print out lines as executed

echo "external start script for kuwaiba"

# Error codes
E_ILLEGAL_ARGS=126
E_INIT_CONFIG=127

export KUWAIBA_TMP="/data-tmp"
export KUWAIBA_DATA_ZIP="/data-zip"
export KUWAIBA_OVERLAY="/data-overlay"
export KUWAIBA_DATA="/data"

applyOverlayConfig() {

  # check if database installed in volume. If installed do not unzip and replace database 
  if [ ! -f "${KUWAIBA_DATA}/configured" ]; then
    if [ -f "${KUWAIBA_DATA_ZIP}/data.zip" ]; then
       echo "Reinstalling database and data in volume. Unzipping from ${KUWAIBA_DATA_ZIP}/data.zip to ${KUWAIBA_DATA}"
    
       rm -r ${KUWAIBA_TMP}/*
       echo "moving old data ${KUWAIBA_DATA} to temporary ${KUWAIBA_TMP}/data_$(date +%Y%m%d_%H%M%S)"
       cp -r "${KUWAIBA_DATA}" "${KUWAIBA_TMP}/data_$(date +%Y%m%d_%H%M%S)"
       
       unzip ${KUWAIBA_DATA_ZIP}/data.zip -d ${KUWAIBA_TMP}

       rm -r "${KUWAIBA_DATA}"
       cp -r "${KUWAIBA_TMP}" "${KUWAIBA_DATA}"
       chown -r kuwaiba:kuwaiba ${KUWAIBA_DATA} 
    else
       echo "No data zip found at ${KUWAIBA_DATA_ZIP}/data.zip Using default configuration from docker image"
    fi
    touch "${KUWAIBA_DATA}/configured"
  else
    echo "Database already configured in volume. Using existing database"
  fi

  # Overlay relative to the root of the install dir
  if [ -d "${KUWAIBA_OVERLAY}" ] && [ -n "$(ls -A ${KUWAIBA_OVERLAY})" ]; then
    echo "Apply custom configuration from ${KUWAIBA_OVERLAY}."
    # Use rsync so that we can overlay files into directories that are symlinked
    rsync -K -rl --out-format="%n %C" ${KUWAIBA_OVERLAY}/* ${KUWAIBA_DATA}/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${KUWAIBA_OVERLAY}. Using existing configuration in volume."
  fi
}

applyOverlayConfig

java -jar /opt/programs/kuwaiba_server_2.1.1-stable.jar 2>&1 | tee /data/logs/kuwaiba_$(date +%Y%m%d_%H%M%S).log


