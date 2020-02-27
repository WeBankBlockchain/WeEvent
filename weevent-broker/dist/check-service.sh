#!/bin/bash
current_path=$(pwd)

# check broker
function check_broker(){
    echo "check broker service "
    
    if [[ ! -e ${current_path}/conf/application-prod.properties ]];then
        echo "${current_path}/conf/application-prod.properties not exist"
        exit 1        
    fi
    
    port=$(grep "server.port" ${current_path}/conf/application-prod.properties| head -1 | awk -F'=' '{print $NF}' | sed s/[[:space:]]//g)
    if [[ $? -ne 0 ]];then
        echo "get broker port fail"
        exit 1
    fi
    
    list_group_response=$(curl -s "http://127.0.0.1:${port}/weevent-broker/admin/listGroup")
    if [[ ${list_group_response} == {*success*} ]];then
        echo "broker service is ok"
    else
        echo "broker service is error"
    fi
}

check_broker
