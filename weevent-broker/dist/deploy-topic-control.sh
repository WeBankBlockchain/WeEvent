#!/bin/bash

JAVA_HOME=

if [[ -z ${JAVA_HOME} ]];then
   echo "JAVA_HOME is empty, please set it first"
   exit 1
fi

${JAVA_HOME}/bin/java -classpath "./conf:./lib/*:../lib/*" com.webank.weevent.core.fisco.util.Web3sdkUtils
if [[ $? -ne 0 ]];then
    echo "deploy topic control failed."
    exit 1
fi
