#!/bin/bash
JAVA_HOME=

if [[ -z ${JAVA_HOME} ]];then
   echo "JAVA_HOME is empty, please set it first"
   exit 1
fi

java_cmd_deploy="${JAVA_HOME}/bin/java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.path=./lib,../lib -Dloader.main=com.webank.weevent.broker.fabric.util.FabricDeployContractUtil org.springframework.boot.loader.PropertiesLauncher"
java_cmd_invoke="${JAVA_HOME}/bin/java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.path=./lib,../lib -Dloader.main=com.webank.weevent.broker.fabric.util.FabricUpdateContractUtil org.springframework.boot.loader.PropertiesLauncher"
cmd=$1

if [[ ${cmd} = "deploy" ]];then
    ${java_cmd_deploy}
    if [[ $? -ne 0 ]];then
        echo "deploy topic control failed."
        exit 1
    fi
fi

if [[ ${cmd} = "add" || ${cmd} = "update" ]];then
    ${java_cmd_invoke} ${cmd}
    if [[ $? -ne 0 ]];then
        echo "${cmd} topic control failed."
        exit 1
    fi
fi