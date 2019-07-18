#!/bin/bash

#check javajdk
source ~/.bashrc
function check_javajdk(){
    java_version=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }' | awk -F[.] '{print $1$2}'`
    system_version=`cat /etc/os-release | awk -F'[= "]' '{print $3}' | head -1`
    if [[ ${java_version} -le 18 && ${system_version} -eq "CentOS" ]];then
        echo "if the system is CentOS java verison must greater than 1.9"
        exit -1
    fi
}
check_javajdk

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
