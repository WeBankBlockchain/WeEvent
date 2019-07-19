#!/bin/bash

java -Xbootclasspath/a:./conf -cp ./apps/*  -Dloader.main=com.webank.weevent.governance.initial.InitialDb org.springframework.boot.loader.PropertiesLauncher
if [[ $? -ne 0 ]];then
    echo "init governance db failed, please check the configuration of mysql"
    return 1
else
   echo "init governance db success"
fi
