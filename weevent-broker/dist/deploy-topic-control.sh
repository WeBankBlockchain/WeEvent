#!/bin/bash

JAVA_HOME=

if [[ -z ${JAVA_HOME} ]];then
   echo "JAVA_HOME is empty, please set it first"
   exit 1
fi

#check java jdk, not support openjdk 1.8 in CentOS
function check_java_jdk(){
    ${JAVA_HOME}/bin/java -version >>/dev/null 2>&1
    if [[ $? -ne 0 ]];then
        echo "not installed JDK"
        exit 1
    fi
    java_version=$(${JAVA_HOME}/bin/java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }' | awk -F[.] '{print $1$2}')
    system_version=$(grep "=" /etc/os-release | head -1 | awk -F'[= "]' '{print $3}')
    openjdk=$(${JAVA_HOME}/bin/java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $1 }')
    if [[ ${java_version} -le 18 && "${system_version}" == "CentOS" && "${openjdk}" == "openjdk" ]];then
        echo "in CentOS, OpenJDK's version must be 1.9 or greater"
        exit 1
    fi
}
check_java_jdk

${JAVA_HOME}/bin/java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.path=./lib -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher
if [[ $? -ne 0 ]];then
    echo "deploy topic control failed."
    exit $?
fi
