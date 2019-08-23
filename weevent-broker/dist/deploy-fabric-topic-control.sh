#!/bin/bash
echo "source ~/.bashrc to confirm java jdk environment"
source ~/.bashrc >/dev/null 2>&1

#check java jdk, not support openjdk 1.8 in CentOS
function check_java_jdk(){
    java -version >>/dev/null 2>&1
    if [[ $? -ne 0 ]];then
        echo "not installed JDK"
        exit 1
    fi
    java_version=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }' | awk -F[.] '{print $1$2}'`
    system_version=`cat /etc/os-release | awk -F'[= "]' '{print $3}' | head -1`
    openjdk=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $1 }'`
    if [[ ${java_version} -le 18 && "${system_version}" == "CentOS" && "${openjdk}" == "openjdk" ]];then
        echo "in CentOS, OpenJDK's version must be 1.9 or greater"
        exit 1
    fi
}
check_java_jdk

java_cmd_deploy="java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.main=com.webank.weevent.broker.fabric.util.FabricDeployContractUtil org.springframework.boot.loader.PropertiesLauncher"
java_cmd_invoke="java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.main=com.webank.weevent.broker.fabric.util.FabricUpdateContractUtil org.springframework.boot.loader.PropertiesLauncher"
cmd=$1

if [[ ${cmd} = "deploy" ]];then
    ${java_cmd_deploy}
fi

if [[ ${cmd} = "add" || ${cmd} = "update" ]];then
    ${java_cmd_invoke} ${cmd}
fi