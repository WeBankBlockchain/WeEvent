#!/bin/bash
echo "source ~/.bashrc to confirm java jdk environment"
source ~/.bashrc >/dev/null 2>&1

#check java jdk, not support openjdk 1.8 in CentOS
function check_java_jdk(){
    java_version=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }' | awk -F[.] '{print $1$2}'`
    system_version=`cat /etc/os-release | awk -F'[= "]' '{print $3}' | head -1`
    openjdk=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $1 }'`
    if [[ ${java_version} -le 18 && "${system_version}" == "CentOS" && "${openjdk}" == "openjdk" ]];then
        echo "in CentOS, OpenJDK's version must be 1.9 or greater"
        exit -1
    fi
}
check_java_jdk

java_cmd="java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher"
if [[ $# -gt 0 ]];then
    groupId=$1
    if [[ ${groupId} -lt 1 ]];then
        echo "groupId must be >= 1"
        exit 1
    fi
    ${java_cmd} ./address.txt ${groupId}
else
    ${java_cmd} ./address.txt
fi
