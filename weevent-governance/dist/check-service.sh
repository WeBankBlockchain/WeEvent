#!/bin/bash
current_path=$(pwd)

# check governance
function check_governance(){
    echo "check governance service "

    if [[ ! -e ${current_path}/conf/application-prod.properties ]];then
        echo "${current_path}/conf/application-prod.properties not exist"
        exit 1        
    fi
    
    port=$(grep "port" ${current_path}/conf/application-prod.properties| head -1 | awk -F'=' '{print $NF}' | sed s/[[:space:]]//g)
    if [[ $? -ne 0 ]];then
        echo "get governance port fail"
        exit 1
    fi

 curl -s  http://127.0.0.1:${port}/weevent-governance/user/login -X POST -d "username=admin&password=AC0E7D037817094E9E0B4441F9BAE3209D67B02FA484917065F71B16109A1A78" | grep 'Authorization' >>/dev/null
     if [[ $? -eq 0 ]];then
        echo "governance service is ok"
    else
        echo "governance service is error"
    fi
 }

check_governance
