#!/bin/sh

set -ex

# if you are not seeing this version of mvn installed, set REFRESH_BUILD env var to true
if [ -z "$MAVEN_HOME" ]; then
  export MAVEN_HOME=./server/apache-maven-3.9.6
  export PATH="${PATH}":${MAVEN_HOME}/bin
fi

# if you are seeing mvn not found, set this
if [ "$(uname)" != "Darwin" ]; then
  export JAVA_HOME=/c/Program\ Files/Java/jdk-25
fi

REFRESH_BUILD=${REFRESH_BUILD:-false}
MINECRAFT_VERSION=${MINECRAFT_VERSION:-26.1.2}
PAPER_BUILD=${PAPER_BUILD:-66}
PAPER_HASH=2c2af90d6ef0e823c272e7059873e3b7a24e07674e56e3b8d6c63ebff03cf827
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

