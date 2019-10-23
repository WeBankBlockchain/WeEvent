#!/bin/bash

JAVA_HOME=

if [[ -z ${JAVA_HOME} ]];then
   echo "JAVA_HOME is null, please set it first"
   exit 1
fi

${JAVA_HOME}/bin/java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.path=./lib,../lib -Dloader.main=com.webank.weevent.processor.db.InitialDb org.springframework.boot.loader.PropertiesLauncher
if [[ $? -ne 0 ]];then
    echo "init processor db failed, please check the configuration of mysql"
    exit 1
else
   echo "init processor db success"
fi
