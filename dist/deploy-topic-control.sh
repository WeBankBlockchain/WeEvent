#!/bin/bash
java -Xbootclasspath/a:./conf -cp ./apps/* -Dloader.main=com.webank.weevent.broker.fisco.util.Web3sdkUtils org.springframework.boot.loader.PropertiesLauncher ./address.txt
