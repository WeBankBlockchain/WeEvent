#!/bin/bash
groupId=$1
java_cmd="java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher"
if [[ $# -gt 0 ]];then
    if [[ $groupId -lt 1 ]];then
        echo "groupId is error must  start at 1"
        exit -1
    fi
    echo "fisco-bcos 2.0x verison"
    ${java_cmd} ./address.txt $groupId
else
    echo "fisco-bcos 1.3x verison"
    ${java_cmd} ./address.txt
fi
