#!/bin/sh

# startup script for kuwaiba which copies data in from overlay

echo "external start script for kuwaiba"

# Error codes
E_ILLEGAL_ARGS=126
E_INIT_CONFIG=127

KUWAIBA_OVERLAY="/data-overlay"
KUWAIBA_DATA="/data"

applyOverlayConfig() {
  # Overlay relative to the root of the install dir
  if [ -d "${KUWAIBA_OVERLAY}" ] && [ -n "$(ls -A ${KUWAIBA_OVERLAY})" ]; then
    echo "Apply custom configuration from ${KUWAIBA_OVERLAY}."
    # Use rsync so that we can overlay files into directories that are symlinked
    rsync -K -rl --out-format="%n %C" ${KUWAIBA_OVERLAY}/* ${KUWAIBA_DATA}/. || exit ${E_INIT_CONFIG}
  else
    echo "No custom config found in ${KUWAIBA_OVERLAY}. Use default configuration."
  fi
}

applyOverlayConfig

java -jar /opt/programs/kuwaiba_server_2.1.1-stable.jar 2>&1 | tee /data/logs/kuwaiba_$(date +%Y%m%d_%H%M%S).log


