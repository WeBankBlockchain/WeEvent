#!/bin/bash
current_path=`pwd`

# check robust
function check_robust(){
    echo "check robust service "

    if [[ ! -e ${current_path}/conf/application-prod.properties ]];then
        echo "${current_path}/conf/application-prod.properties not exist"
        exit 1
    fi

    port=`cat ${current_path}/conf/application-prod.properties | grep "weevent.broker.url" | head -n1 | awk -F':' '{print $NF}' | sed s/[[:space:]]//g`
    if [[ $? -ne 0 ]];then
        echo "get robust weevent.broker.url fail"
        exit 1
    fi
 }

check_robust
