#!/bin/sh

set -ex

# JAVA_HOME must be set
if [ -z "$JAVA_HOME" ]; then
  echo JAVA_HOME not set, exiting...
fi

# MAVEN_HOME must be set
if [ -z "$MAVEN_HOME" ]; then
  echo MAVEN_HOME not set, exiting...
fi

export PATH="${PATH}":${MAVEN_HOME}/bin

REFRESH_BUILD=${REFRESH_BUILD:-false}
MINECRAFT_VERSION=${MINECRAFT_VERSION:-26.2}
PAPER_BUILD=${PAPER_BUILD:-123}
PAPER_HASH=7b7b3b43c009103e1971a0576c26f655a7dd9b56a0a2a4438e352c03a7fecd08
export MINECRAFT_VERSION="${MINECRAFT_VERSION}"

./build_plugin.sh

mkdir -p server
mkdir -p server/plugins
PLUGIN_VERSION=$(mvn help:evaluate -Dexpression=UHCPlugin.version -q -DforceStdout)
find ./server/plugins/ -name 'EasyUHC-*.jar' -delete
find ./server/plugins/ -name 'SpigotUHC-*.jar' -delete
cp build/EasyUHC-"${PLUGIN_VERSION}".jar server/plugins/EasyUHC-"${PLUGIN_VERSION}".jar

if [ "${REFRESH_BUILD}" = "true" ]; then
  cd server
  curl -o paper-latest.jar "https://fill-data.papermc.io/v1/objects/${PAPER_HASH}/paper-${MINECRAFT_VERSION}-${PAPER_BUILD}.jar"
  cd ..
fi

