#!/bin/bash
current_path=$(pwd)

# check processor
function check_processor(){
    echo "check processor service "

    if [[ ! -e ${current_path}/conf/application-prod.properties ]];then
        echo "${current_path}/conf/application-prod.properties not exist"
        exit 1
    fi

    port=$(grep "port" ${current_path}/conf/application-prod.properties| head -1 | awk -F'=' '{print $NF}' | sed s/[[:space:]]//g)
    if [[ $? -ne 0 ]];then
        echo "get processor port fail"
        exit 1
    fi

    curl -s  -d 'payload={\"a\":\"1\"}&condition=a<10' http://127.0.0.1:${port}/processor/checkWhereCondition | grep "errorCode" >>/dev/null
     if [[ $? -eq 0 ]];then
        echo "processor service is ok"
    else
        echo "processor service is error"
    fi
 }

check_processor

