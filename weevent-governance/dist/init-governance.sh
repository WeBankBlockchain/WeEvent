#!/bin/bash
#java -Xbootclasspath/a:./conf -cp ./apps/*  -Dloader.main=com.webank.weevent.governance.initial.InitialDb org.springframework.boot.loader.PropertiesLauncher

java -Xbootclasspath/a:./conf -cp ./apps/*  -Dloader.main=com.webank.weevent.governance.initial.InitialDb org.springframework.boot.loader.PropertiesLauncher

if [ $? -ne 0 ];then
    echo "init governance db failed"
    echo "please check cofiguration of mysql"
    return 1
else
   echo "init governance db success"
fi
