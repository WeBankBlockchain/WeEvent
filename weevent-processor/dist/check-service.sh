#!/bin/bash
current_path=$(pwd)

# check broker
function check_processor(){
    echo "check processor service "

    if [[ ! -e ${current_path}/conf/application-prod.properties ]];then
        echo "${current_path}/conf/application-prod.properties not exist"
        exit 1
    fi

    port=$(grep "server.port" ${current_path}/conf/application-prod.properties| awk -F'=' '{print $2}' | sed s/[[:space:]]//g)
    if [[ $? -ne 0 ]];then
        echo "get broker port fail"
        exit 1
    fi

    curl -s "http://127.0.0.1:$port/weevent/processor/getCEPRuleListByPage?currPage=1&pageSize=10" | grep 302000 >>/dev/null
    if [[ $? -eq 0 ]];then
         echo "processor service is ok"
    else
        echo "processor service is error"
    fi
}

check_processor
