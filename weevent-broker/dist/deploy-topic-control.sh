#!/bin/bash
java_cmd="java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher"
if [[ $# -gt 0 ]];then
    groupId=$1
    if [[ $groupId -lt 1 ]];then
        echo "groupId is error must  start at 1"
        exit -1
    fi
    ${java_cmd} ./address.txt $groupId
else
    ${java_cmd} ./address.txt
fi
