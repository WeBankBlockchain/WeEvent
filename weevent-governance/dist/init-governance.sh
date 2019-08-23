#!/bin/bash
JAVA_HOME=


if [ -z ${JAVA_HOME} ];then
   echo "JAVA_HOME is null, please set it first"
   exit 1
fi

${JAVA_HOME}/bin/java -Xbootclasspath/a:./conf -cp ./apps/*  -Dloader.main=com.webank.weevent.governance.initial.InitialDb org.springframework.boot.loader.PropertiesLauncher
if [[ $? -ne 0 ]];then
    echo "init governance db failed, please check the configuration of mysql"
    exit 1
else
   echo "init governance db success"
fi
