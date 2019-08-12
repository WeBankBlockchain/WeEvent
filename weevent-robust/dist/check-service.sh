#!/bin/bash
current_path=`pwd`

# check robust
function check_robust(){
    echo "check robust service "
    
    if [[ ! -e ${current_path}/conf/application-prod.properties ]];then
        echo "${current_path}/conf/application-prod.properties not exist"
        exit 1        
    fi
    
    url=`cat ${current_path}/conf/application-prod.properties | grep "weevent.broker.url" | awk -F'=' '{print $2}' | sed s/[[:space:]]//g`
    if [[ $? -ne 0 ]];then
        echo "get robust 'weevent.broker.url' fail"
        exit $?
    fi
}

check_robust
