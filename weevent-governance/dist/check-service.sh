#!/bin/bash
current_path=`pwd`

# check governance
function check_governance(){
    echo "check governance service "

    if [[ ! -e ${current_path}/conf/application-prod.yml ]];then
        echo "${current_path}/conf/application-prod.yml not exist"
        exit 1        
    fi
    
    port=`cat ${current_path}/conf/application-prod.yml | grep "port" | head -n1 | awk -F':' '{print $NF}' | sed s/[[:space:]]//g`
    if [[ $? -ne 0 ]];then
        echo "get governance port fail"
        exit 1
    fi

    curl -s "http://127.0.0.1:${port}/weevent-governance/topic/getTopics?pageIndex=0&pageSize=10" | grep 302000 >>/dev/null
    if [[ $? -eq 0 ]];then
        echo "governance service is ok"
    else
        echo "governance service is error"
    fi
 }

check_governance
